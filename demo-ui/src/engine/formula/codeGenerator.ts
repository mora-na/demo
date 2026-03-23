import type {ASTNode, BinaryExprNode, ConditionalExprNode, LiteralNode, UnaryExprNode} from "./ast";

export class CodeGenerator {
    generateExpression(ast: ASTNode): string {
        return this.visitNode(ast);
    }

    generateDisplay(ast: ASTNode): string {
        return this.visitNodeDisplay(ast);
    }

    private visitNode(node: ASTNode): string {
        switch (node.kind) {
            case "Literal":
                return this.genLiteral(node);
            case "Variable":
                return node.name;
            case "BinaryExpr":
                return this.genBinary(node);
            case "UnaryExpr":
                return this.genUnary(node);
            case "ConditionalExpr":
                return this.genConditional(node);
            case "GroupExpr":
                return `(${this.visitNode(node.expression)})`;
            case "SubExprRef":
                return `(${this.visitNode(node.expanded)})`;
            case "Placeholder":
                return "__PLACEHOLDER__";
            default:
                return "";
        }
    }

    private genLiteral(node: LiteralNode): string {
        const val = node.value.trim();
        if (/^-?\d+(\.\d+)?$/.test(val)) {
            return val;
        }
        if (val === "true" || val === "false") {
            return val;
        }
        if (/^[a-zA-Z_\u4e00-\u9fa5][\w\u4e00-\u9fa5]*$/.test(val)) {
            return val;
        }
        return `"${val.replace(/"/g, "\\\"")}"`;
    }

    private genBinary(node: BinaryExprNode): string {
        const leftStr = this.maybeParenthesize(node.left, node, "left");
        const rightStr = this.maybeParenthesize(node.right, node, "right");
        return `${leftStr}${node.operator}${rightStr}`;
    }

    private genUnary(node: UnaryExprNode): string {
        const operand = this.visitNode(node.operand);
        if (node.operand.kind === "BinaryExpr" || node.operand.kind === "ConditionalExpr") {
            return `${node.operator}(${operand})`;
        }
        return `${node.operator}${operand}`;
    }

    private genConditional(node: ConditionalExprNode): string {
        const condition = this.visitNode(node.condition);
        const consequent = this.visitNode(node.consequent);
        const alternate = this.visitNode(node.alternate);
        const condStr = node.condition.kind === "ConditionalExpr" ? `(${condition})` : condition;
        return `${condStr}?${consequent}:${alternate}`;
    }

    private maybeParenthesize(child: ASTNode, parent: BinaryExprNode, side: "left" | "right"): string {
        const childStr = this.visitNode(child);
        if (child.kind !== "BinaryExpr") {
            return childStr;
        }

        const childPriority = this.getOperatorPriority(child.operator);
        const parentPriority = this.getOperatorPriority(parent.operator);

        if (childPriority < parentPriority) {
            return `(${childStr})`;
        }

        if (childPriority === parentPriority && side === "right") {
            const nonAssociative = ["-", "/", "%"];
            if (nonAssociative.includes(parent.operator)) {
                return `(${childStr})`;
            }
        }

        return childStr;
    }

    private getOperatorPriority(op: string): number {
        const priorities: Record<string, number> = {
            "||": 1,
            "&&": 2,
            "==": 3,
            "!=": 3,
            ">": 3,
            "<": 3,
            ">=": 3,
            "<=": 3,
            "+": 4,
            "-": 4,
            "*": 5,
            "/": 5,
            "%": 5
        };
        return priorities[op] || 0;
    }

    private visitNodeDisplay(node: ASTNode): string {
        switch (node.kind) {
            case "Literal":
                return node.rawDisplay;
            case "Variable":
                return node.displayName;
            case "BinaryExpr":
                return `${this.visitNodeDisplay(node.left)} ${node.displayOp} ${this.visitNodeDisplay(node.right)}`;
            case "UnaryExpr":
                return `${node.displayOp}(${this.visitNodeDisplay(node.operand)})`;
            case "ConditionalExpr":
                return `如果(${this.visitNodeDisplay(node.condition)}) 那么 ${this.visitNodeDisplay(
                    node.consequent
                )} 否则 ${this.visitNodeDisplay(node.alternate)}`;
            case "GroupExpr":
                return `(${this.visitNodeDisplay(node.expression)})`;
            case "SubExprRef":
                return `[${node.subExprName}]`;
            case "Placeholder":
                return "___";
            default:
                return "";
        }
    }
}
