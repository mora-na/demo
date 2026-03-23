import type {FormulaToken} from "../../types/formula";

export type ASTNode =
    | LiteralNode
    | VariableNode
    | BinaryExprNode
    | UnaryExprNode
    | ConditionalExprNode
    | GroupExprNode
    | SubExprRefNode
    | PlaceholderNode;

export interface LiteralNode {
    kind: "Literal";
    value: string;
    rawDisplay: string;
    tokenId: string;
}

export interface VariableNode {
    kind: "Variable";
    name: string;
    displayName: string;
    tokenId: string;
}

export interface BinaryExprNode {
    kind: "BinaryExpr";
    operator: string;
    displayOp: string;
    left: ASTNode;
    right: ASTNode;
}

export interface UnaryExprNode {
    kind: "UnaryExpr";
    operator: string;
    displayOp: string;
    operand: ASTNode;
}

export interface ConditionalExprNode {
    kind: "ConditionalExpr";
    condition: ASTNode;
    consequent: ASTNode;
    alternate: ASTNode;
}

export interface GroupExprNode {
    kind: "GroupExpr";
    expression: ASTNode;
}

export interface SubExprRefNode {
    kind: "SubExprRef";
    subExprId: string;
    subExprName: string;
    expanded: ASTNode;
}

export interface PlaceholderNode {
    kind: "Placeholder";
    label: string;
    tokenId: string;
}

export interface ParserResult {
    ast: ASTNode | null;
    errors: string[];
}

export type TokenStream = FormulaToken[];
