export enum TokenCategory {
    CONTROL_FLOW = "CONTROL_FLOW",
    LOGIC_OP = "LOGIC_OP",
    COMPARE_OP = "COMPARE_OP",
    ARITHMETIC_OP = "ARITHMETIC_OP",
    GROUPING = "GROUPING",
    VALUE = "VALUE",
    PLACEHOLDER = "PLACEHOLDER",
    SUB_EXPR = "SUB_EXPR"
}

export enum TokenType {
    IF = "IF",
    THEN = "THEN",
    ELSE = "ELSE",
    AND = "AND",
    OR = "OR",
    NOT = "NOT",
    EQ = "EQ",
    NEQ = "NEQ",
    GT = "GT",
    LT = "LT",
    GTE = "GTE",
    LTE = "LTE",
    ADD = "ADD",
    SUB = "SUB",
    MUL = "MUL",
    DIV = "DIV",
    MOD = "MOD",
    LPAREN = "LPAREN",
    RPAREN = "RPAREN",
    CONSTANT = "CONSTANT",
    VARIABLE = "VARIABLE",
    PLACEHOLDER = "PLACEHOLDER",
    SUB_EXPR = "SUB_EXPR"
}

export interface FormulaToken {
    id: string;
    type: TokenType;
    category: TokenCategory;
    displayText: string;
    outputValue: string;
    editable: boolean;
    templateGroupId?: string;
    validationState: "valid" | "error" | "warning";
    validationMessage?: string;
    subExprId?: string;
    subExprTokens?: FormulaToken[];
    nestingLevel: number;
    createdAt: number;
}

export interface TemplateGroup {
    id: string;
    templateType: "IF_THEN_ELSE";
    tokenIds: string[];
    slots: {
        condition: string;
        trueBranch: string;
        falseBranch: string;
    };
}

export interface SubExpression {
    id: string;
    name: string;
    description?: string;
    tokens: FormulaToken[];
    compiledExpression: string;
    displayExpression: string;
    createdAt: number;
    updatedAt: number;
}

export interface DragPayload {
    source: "palette" | "canvas";
    tokenPrototype?: Omit<FormulaToken, "id" | "createdAt" | "validationState" | "nestingLevel">;
    tokenId?: string;
    isTemplate?: boolean;
    templateType?: "IF_THEN_ELSE";
}

export interface PaletteItem {
    label: string;
    tokenType: TokenType;
    category: TokenCategory;
    outputValue: string;
    icon?: string;
    isTemplate?: boolean;
    templateType?: "IF_THEN_ELSE";
    editable?: boolean;
    subExprId?: string;
    subExprTokens?: FormulaToken[];
}
