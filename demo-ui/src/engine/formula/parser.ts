import type {FormulaToken} from "../../types/formula";
import {TokenType} from "../../types/formula";
import type {ASTNode, ParserResult, PlaceholderNode} from "./ast";

export class FormulaParser {
    private tokens: FormulaToken[];
    private pos = 0;
    private errors: string[] = [];

    constructor(tokens: FormulaToken[]) {
        this.tokens = tokens;
    }

    parse(): ParserResult {
        this.pos = 0;
        this.errors = [];

        if (this.tokens.length === 0) {
            return {ast: null, errors: ["空表达式"]};
        }

        try {
            const ast = this.parseExpression();
            if (this.pos < this.tokens.length) {
                this.errors.push(`意外的 Token: "${this.peek()?.displayText}" 位置 ${this.pos}`);
            }
            return {ast, errors: this.errors};
        } catch (error: any) {
            this.errors.push(error?.message || "解析错误");
            return {ast: null, errors: this.errors};
        }
    }

    private peek(): FormulaToken | null {
        return this.pos < this.tokens.length ? this.tokens[this.pos] : null;
    }

    private advance(): FormulaToken {
        const token = this.tokens[this.pos];
        this.pos += 1;
        return token;
    }

    private match(...types: TokenType[]): boolean {
        const token = this.peek();
        return token !== null && types.includes(token.type);
    }

    private parseExpression(): ASTNode {
        return this.parseConditional();
    }

    private parseConditional(): ASTNode {
        if (this.match(TokenType.IF)) {
            this.advance();

            const condition = this.parseLogicalOr();

            if (!this.match(TokenType.THEN)) {
                this.errors.push("缺少「那么」");
                return this.makePlaceholder("missing-then");
            }
            this.advance();

            const consequent = this.parseExpression();

            if (!this.match(TokenType.ELSE)) {
                this.errors.push("缺少「否则」");
                return this.makePlaceholder("missing-else");
            }
            this.advance();

            const alternate = this.parseExpression();

            return {
                kind: "ConditionalExpr",
                condition,
                consequent,
                alternate
            };
        }

        return this.parseLogicalOr();
    }

    private parseLogicalOr(): ASTNode {
        let left = this.parseLogicalAnd();

        while (this.match(TokenType.OR)) {
            this.advance();
            const right = this.parseLogicalAnd();
            left = {
                kind: "BinaryExpr",
                operator: "||",
                displayOp: "或者",
                left,
                right
            };
        }

        return left;
    }

    private parseLogicalAnd(): ASTNode {
        let left = this.parseComparison();

        while (this.match(TokenType.AND)) {
            this.advance();
            const right = this.parseComparison();
            left = {
                kind: "BinaryExpr",
                operator: "&&",
                displayOp: "并且",
                left,
                right
            };
        }

        return left;
    }

    private parseComparison(): ASTNode {
        let left = this.parseAddition();
        const compOps = [
            TokenType.EQ,
            TokenType.NEQ,
            TokenType.GT,
            TokenType.LT,
            TokenType.GTE,
            TokenType.LTE
        ];

        while (this.match(...compOps)) {
            const opToken = this.advance();
            const right = this.parseAddition();
            left = {
                kind: "BinaryExpr",
                operator: opToken.outputValue,
                displayOp: opToken.displayText,
                left,
                right
            };
        }

        return left;
    }

    private parseAddition(): ASTNode {
        let left = this.parseMultiplication();

        while (this.match(TokenType.ADD, TokenType.SUB)) {
            const opToken = this.advance();
            const right = this.parseMultiplication();
            left = {
                kind: "BinaryExpr",
                operator: opToken.outputValue,
                displayOp: opToken.displayText,
                left,
                right
            };
        }

        return left;
    }

    private parseMultiplication(): ASTNode {
        let left = this.parseUnary();

        while (this.match(TokenType.MUL, TokenType.DIV, TokenType.MOD)) {
            const opToken = this.advance();
            const right = this.parseUnary();
            left = {
                kind: "BinaryExpr",
                operator: opToken.outputValue,
                displayOp: opToken.displayText,
                left,
                right
            };
        }

        return left;
    }

    private parseUnary(): ASTNode {
        if (this.match(TokenType.NOT)) {
            const opToken = this.advance();
            const operand = this.parseUnary();
            return {
                kind: "UnaryExpr",
                operator: "!",
                displayOp: "非",
                operand
            };
        }

        return this.parseAtom();
    }

    private parseAtom(): ASTNode {
        const token = this.peek();
        if (!token) {
            this.errors.push("表达式意外结束");
            return this.makePlaceholder("unexpected-end");
        }

        if (token.type === TokenType.CONSTANT) {
            this.advance();
            return {
                kind: "Literal",
                value: token.outputValue,
                rawDisplay: token.displayText,
                tokenId: token.id
            };
        }

        if (token.type === TokenType.VARIABLE) {
            this.advance();
            return {
                kind: "Variable",
                name: token.outputValue,
                displayName: token.displayText,
                tokenId: token.id
            };
        }

        if (token.type === TokenType.SUB_EXPR) {
            this.advance();
            if (token.subExprTokens && token.subExprTokens.length > 0) {
                const subParser = new FormulaParser(token.subExprTokens);
                const {ast, errors} = subParser.parse();
                this.errors.push(...errors.map((err) => `子表达式「${token.displayText}」: ${err}`));
                return {
                    kind: "SubExprRef",
                    subExprId: token.subExprId || "",
                    subExprName: token.displayText,
                    expanded: ast || this.makePlaceholder("sub-expr-error")
                };
            }
            return this.makePlaceholder("empty-sub-expr");
        }

        if (token.type === TokenType.LPAREN) {
            this.advance();
            const expr = this.parseExpression();
            if (!this.match(TokenType.RPAREN)) {
                this.errors.push("缺少右括号");
            } else {
                this.advance();
            }
            return {
                kind: "GroupExpr",
                expression: expr
            };
        }

        if (token.type === TokenType.PLACEHOLDER) {
            this.advance();
            return {
                kind: "Placeholder",
                label: token.displayText,
                tokenId: token.id
            };
        }

        this.errors.push(`意外的「${token.displayText}」`);
        this.advance();
        return this.makePlaceholder(`unexpected-${token.type}`);
    }

    private makePlaceholder(label: string): PlaceholderNode {
        return {kind: "Placeholder", label, tokenId: ""};
    }
}
