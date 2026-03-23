import type {FormulaToken} from "../../types/formula";
import {TokenType} from "../../types/formula";

export enum TokenRole {
    VALUE = "VALUE",
    BINARY_OP = "BINARY_OP",
    UNARY_PREFIX = "UNARY_PREFIX",
    LPAREN = "LPAREN",
    RPAREN = "RPAREN",
    IF = "IF",
    THEN = "THEN",
    ELSE = "ELSE",
    PLACEHOLDER = "PLACEHOLDER"
}

export function getTokenRole(type: TokenType): TokenRole {
    switch (type) {
        case TokenType.CONSTANT:
        case TokenType.VARIABLE:
        case TokenType.SUB_EXPR:
            return TokenRole.VALUE;
        case TokenType.ADD:
        case TokenType.SUB:
        case TokenType.MUL:
        case TokenType.DIV:
        case TokenType.MOD:
        case TokenType.AND:
        case TokenType.OR:
        case TokenType.EQ:
        case TokenType.NEQ:
        case TokenType.GT:
        case TokenType.LT:
        case TokenType.GTE:
        case TokenType.LTE:
            return TokenRole.BINARY_OP;
        case TokenType.NOT:
            return TokenRole.UNARY_PREFIX;
        case TokenType.LPAREN:
            return TokenRole.LPAREN;
        case TokenType.RPAREN:
            return TokenRole.RPAREN;
        case TokenType.IF:
            return TokenRole.IF;
        case TokenType.THEN:
            return TokenRole.THEN;
        case TokenType.ELSE:
            return TokenRole.ELSE;
        case TokenType.PLACEHOLDER:
            return TokenRole.PLACEHOLDER;
        default:
            return TokenRole.VALUE;
    }
}

type AdjacencyKey = TokenRole | "START" | "END";

const adjacencyMatrix: Record<AdjacencyKey, Set<AdjacencyKey>> = {
    START: new Set([
        TokenRole.VALUE,
        TokenRole.UNARY_PREFIX,
        TokenRole.LPAREN,
        TokenRole.IF,
        TokenRole.PLACEHOLDER
    ]),
    [TokenRole.VALUE]: new Set([TokenRole.BINARY_OP, TokenRole.RPAREN, TokenRole.THEN, TokenRole.ELSE, "END"]),
    [TokenRole.BINARY_OP]: new Set([
        TokenRole.VALUE,
        TokenRole.UNARY_PREFIX,
        TokenRole.LPAREN,
        TokenRole.IF,
        TokenRole.PLACEHOLDER
    ]),
    [TokenRole.UNARY_PREFIX]: new Set([
        TokenRole.VALUE,
        TokenRole.LPAREN,
        TokenRole.UNARY_PREFIX,
        TokenRole.PLACEHOLDER
    ]),
    [TokenRole.LPAREN]: new Set([
        TokenRole.VALUE,
        TokenRole.UNARY_PREFIX,
        TokenRole.LPAREN,
        TokenRole.IF,
        TokenRole.PLACEHOLDER
    ]),
    [TokenRole.RPAREN]: new Set([TokenRole.BINARY_OP, TokenRole.RPAREN, TokenRole.THEN, TokenRole.ELSE, "END"]),
    [TokenRole.IF]: new Set([TokenRole.VALUE, TokenRole.UNARY_PREFIX, TokenRole.LPAREN, TokenRole.PLACEHOLDER]),
    [TokenRole.THEN]: new Set([TokenRole.VALUE, TokenRole.UNARY_PREFIX, TokenRole.LPAREN, TokenRole.IF, TokenRole.PLACEHOLDER]),
    [TokenRole.ELSE]: new Set([TokenRole.VALUE, TokenRole.UNARY_PREFIX, TokenRole.LPAREN, TokenRole.IF, TokenRole.PLACEHOLDER]),
    [TokenRole.PLACEHOLDER]: new Set([TokenRole.BINARY_OP, TokenRole.RPAREN, TokenRole.THEN, TokenRole.ELSE, "END"]),
    END: new Set([])
};

const VALID_END_ROLES = new Set<TokenRole>([TokenRole.VALUE, TokenRole.RPAREN, TokenRole.PLACEHOLDER]);

export interface ValidationError {
    tokenId: string;
    tokenIndex: number;
    message: string;
    severity: "error" | "warning";
    relatedTokenIds?: string[];
}

export function validateTokenSequence(tokens: FormulaToken[]): ValidationError[] {
    const errors: ValidationError[] = [];
    if (tokens.length === 0) {
        return errors;
    }

    errors.push(...validateAdjacency(tokens));
    errors.push(...validateBrackets(tokens));
    errors.push(...validateControlFlow(tokens));
    errors.push(...validatePlaceholders(tokens));
    errors.push(...validateEmptyValues(tokens));
    errors.push(...validateEmptyParens(tokens));
    errors.push(...validateConstantValues(tokens));

    applyValidationResults(tokens, errors);
    return errors;
}

function validateAdjacency(tokens: FormulaToken[]): ValidationError[] {
    const errors: ValidationError[] = [];

    for (let i = 0; i < tokens.length; i += 1) {
        const current = tokens[i];
        const currentRole = getTokenRole(current.type);
        const prevRole: AdjacencyKey = i === 0 ? "START" : getTokenRole(tokens[i - 1].type);

        if (!adjacencyMatrix[prevRole]?.has(currentRole)) {
            const prevToken = i > 0 ? tokens[i - 1] : null;
            const errMsg = generateAdjacencyErrorMessage(prevRole, currentRole, prevToken, current);
            errors.push({
                tokenId: current.id,
                tokenIndex: i,
                message: errMsg,
                severity: "error",
                relatedTokenIds: prevToken ? [prevToken.id, current.id] : [current.id]
            });
        }

        if (i === tokens.length - 1) {
            if (!VALID_END_ROLES.has(currentRole) && currentRole !== TokenRole.PLACEHOLDER) {
                errors.push({
                    tokenId: current.id,
                    tokenIndex: i,
                    message: `表达式不能以「${current.displayText}」结尾`,
                    severity: "error"
                });
            }
        }
    }

    return errors;
}

function generateAdjacencyErrorMessage(
    leftRole: AdjacencyKey,
    rightRole: AdjacencyKey,
    leftToken: FormulaToken | null,
    rightToken: FormulaToken
): string {
    if (leftRole === TokenRole.VALUE && rightRole === TokenRole.VALUE) {
        return `「${leftToken?.displayText}」和「${rightToken.displayText}」之间缺少运算符`;
    }
    if (leftRole === TokenRole.BINARY_OP && rightRole === TokenRole.BINARY_OP) {
        return `不能连续使用两个运算符「${leftToken?.displayText}」「${rightToken.displayText}」`;
    }
    if (leftRole === "START" && rightRole === TokenRole.BINARY_OP) {
        return `表达式不能以运算符「${rightToken.displayText}」开头`;
    }
    if (leftRole === TokenRole.BINARY_OP && rightRole === "END") {
        return `运算符「${leftToken?.displayText}」后面缺少操作数`;
    }
    return `「${leftToken?.displayText || "开头"}」后面不能直接跟「${rightToken.displayText}」`;
}

function validateBrackets(tokens: FormulaToken[]): ValidationError[] {
    const errors: ValidationError[] = [];
    const stack: Array<{ token: FormulaToken; index: number }> = [];

    for (let i = 0; i < tokens.length; i += 1) {
        const token = tokens[i];
        if (token.type === TokenType.LPAREN) {
            stack.push({token, index: i});
        } else if (token.type === TokenType.RPAREN) {
            if (stack.length === 0) {
                errors.push({
                    tokenId: token.id,
                    tokenIndex: i,
                    message: "多余的右括号，没有匹配的左括号",
                    severity: "error"
                });
            } else {
                stack.pop();
            }
        }
    }

    for (const {token, index} of stack) {
        errors.push({
            tokenId: token.id,
            tokenIndex: index,
            message: "左括号没有匹配的右括号",
            severity: "error"
        });
    }

    return errors;
}

function validateControlFlow(tokens: FormulaToken[]): ValidationError[] {
    const errors: ValidationError[] = [];
    const groupMap = new Map<string, FormulaToken[]>();
    for (const token of tokens) {
        if (token.templateGroupId) {
            if (!groupMap.has(token.templateGroupId)) {
                groupMap.set(token.templateGroupId, []);
            }
            groupMap.get(token.templateGroupId)?.push(token);
        }
    }

    for (const [groupId, groupTokens] of groupMap) {
        const hasIf = groupTokens.some((token) => token.type === TokenType.IF);
        const hasThen = groupTokens.some((token) => token.type === TokenType.THEN);
        const hasElse = groupTokens.some((token) => token.type === TokenType.ELSE);

        if (!hasIf || !hasThen || !hasElse) {
            const missing: string[] = [];
            if (!hasIf) missing.push("如果");
            if (!hasThen) missing.push("那么");
            if (!hasElse) missing.push("否则");
            for (const token of groupTokens) {
                if ([TokenType.IF, TokenType.THEN, TokenType.ELSE].includes(token.type)) {
                    errors.push({
                        tokenId: token.id,
                        tokenIndex: tokens.indexOf(token),
                        message: `条件表达式不完整，缺少：${missing.join("、")}`,
                        severity: "error"
                    });
                }
            }
        }

        const ifIndex = tokens.findIndex((token) => token.templateGroupId === groupId && token.type === TokenType.IF);
        const thenIndex = tokens.findIndex((token) => token.templateGroupId === groupId && token.type === TokenType.THEN);
        const elseIndex = tokens.findIndex((token) => token.templateGroupId === groupId && token.type === TokenType.ELSE);

        if (ifIndex !== -1 && thenIndex !== -1 && ifIndex > thenIndex) {
            errors.push({
                tokenId: groupTokens.find((token) => token.type === TokenType.IF)!.id,
                tokenIndex: ifIndex,
                message: "「如果」必须在「那么」之前",
                severity: "error"
            });
        }
        if (thenIndex !== -1 && elseIndex !== -1 && thenIndex > elseIndex) {
            errors.push({
                tokenId: groupTokens.find((token) => token.type === TokenType.THEN)!.id,
                tokenIndex: thenIndex,
                message: "「那么」必须在「否则」之前",
                severity: "error"
            });
        }
    }

    for (let i = 0; i < tokens.length; i += 1) {
        const token = tokens[i];
        if ((token.type === TokenType.THEN || token.type === TokenType.ELSE) && !token.templateGroupId) {
            errors.push({
                tokenId: token.id,
                tokenIndex: i,
                message: `孤立的「${token.displayText}」，缺少配对的「如果」`,
                severity: "error"
            });
        }
    }

    return errors;
}

function validatePlaceholders(tokens: FormulaToken[]): ValidationError[] {
    return tokens
        .filter((token) => token.type === TokenType.PLACEHOLDER)
        .map((token) => ({
            tokenId: token.id,
            tokenIndex: tokens.indexOf(token),
            message: `请填写${token.displayText || "此处"}`,
            severity: "warning" as const
        }));
}

function validateEmptyValues(tokens: FormulaToken[]): ValidationError[] {
    return tokens
        .filter((token) => token.editable && (!token.displayText || token.displayText.trim() === ""))
        .map((token) => ({
            tokenId: token.id,
            tokenIndex: tokens.indexOf(token),
            message: "值不能为空",
            severity: "error" as const
        }));
}

function validateEmptyParens(tokens: FormulaToken[]): ValidationError[] {
    const errors: ValidationError[] = [];
    for (let i = 0; i < tokens.length - 1; i += 1) {
        if (tokens[i].type === TokenType.LPAREN && tokens[i + 1].type === TokenType.RPAREN) {
            errors.push({
                tokenId: tokens[i].id,
                tokenIndex: i,
                message: "空括号，请在括号内添加内容",
                severity: "warning",
                relatedTokenIds: [tokens[i].id, tokens[i + 1].id]
            });
        }
    }
    return errors;
}

function validateConstantValues(tokens: FormulaToken[]): ValidationError[] {
    const errors: ValidationError[] = [];
    const forbiddenPatterns = [
        /[;{}]/,
        /\b(eval|exec|system)\b/i,
        /<script/i,
        /\\/
    ];

    for (let i = 0; i < tokens.length; i += 1) {
        const token = tokens[i];
        if (token.type !== TokenType.CONSTANT) {
            continue;
        }
        for (const pattern of forbiddenPatterns) {
            if (pattern.test(token.outputValue)) {
                errors.push({
                    tokenId: token.id,
                    tokenIndex: i,
                    message: "常量值包含非法字符",
                    severity: "error"
                });
                break;
            }
        }
        const val = token.outputValue.trim();
        if (/^\d/.test(val) && Number.isNaN(Number(val))) {
            errors.push({
                tokenId: token.id,
                tokenIndex: i,
                message: "数字格式不正确",
                severity: "error"
            });
        }
    }
    return errors;
}

function applyValidationResults(tokens: FormulaToken[], errors: ValidationError[]) {
    for (const token of tokens) {
        token.validationState = "valid";
        token.validationMessage = undefined;
    }

    for (const error of errors) {
        const relatedIds = error.relatedTokenIds || [error.tokenId];
        for (const id of relatedIds) {
            const token = tokens.find((item) => item.id === id);
            if (token) {
                if (error.severity === "error" || token.validationState !== "error") {
                    token.validationState = error.severity;
                    token.validationMessage = error.message;
                }
            }
        }
    }
}
