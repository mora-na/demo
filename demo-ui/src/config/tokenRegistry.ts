import {type PaletteItem, TokenCategory, TokenType} from "../types/formula";

export const TOKEN_META: Record<TokenType, {
    category: TokenCategory;
    displayText: string;
    outputValue: string;
    editable: boolean;
    isTemplate?: boolean;
    templateType?: "IF_THEN_ELSE";
}> = {
    [TokenType.IF]: {
        category: TokenCategory.CONTROL_FLOW,
        displayText: "如果",
        outputValue: "",
        editable: false,
        isTemplate: true,
        templateType: "IF_THEN_ELSE"
    },
    [TokenType.THEN]: {category: TokenCategory.CONTROL_FLOW, displayText: "那么", outputValue: "?", editable: false},
    [TokenType.ELSE]: {category: TokenCategory.CONTROL_FLOW, displayText: "否则", outputValue: ":", editable: false},
    [TokenType.AND]: {category: TokenCategory.LOGIC_OP, displayText: "并且", outputValue: "&&", editable: false},
    [TokenType.OR]: {category: TokenCategory.LOGIC_OP, displayText: "或者", outputValue: "||", editable: false},
    [TokenType.NOT]: {category: TokenCategory.LOGIC_OP, displayText: "非", outputValue: "!", editable: false},
    [TokenType.EQ]: {category: TokenCategory.COMPARE_OP, displayText: "==", outputValue: "==", editable: false},
    [TokenType.NEQ]: {category: TokenCategory.COMPARE_OP, displayText: "!=", outputValue: "!=", editable: false},
    [TokenType.GT]: {category: TokenCategory.COMPARE_OP, displayText: ">", outputValue: ">", editable: false},
    [TokenType.LT]: {category: TokenCategory.COMPARE_OP, displayText: "<", outputValue: "<", editable: false},
    [TokenType.GTE]: {category: TokenCategory.COMPARE_OP, displayText: ">=", outputValue: ">=", editable: false},
    [TokenType.LTE]: {category: TokenCategory.COMPARE_OP, displayText: "<=", outputValue: "<=", editable: false},
    [TokenType.ADD]: {category: TokenCategory.ARITHMETIC_OP, displayText: "+", outputValue: "+", editable: false},
    [TokenType.SUB]: {category: TokenCategory.ARITHMETIC_OP, displayText: "-", outputValue: "-", editable: false},
    [TokenType.MUL]: {category: TokenCategory.ARITHMETIC_OP, displayText: "×", outputValue: "*", editable: false},
    [TokenType.DIV]: {category: TokenCategory.ARITHMETIC_OP, displayText: "÷", outputValue: "/", editable: false},
    [TokenType.MOD]: {category: TokenCategory.ARITHMETIC_OP, displayText: "%", outputValue: "%", editable: false},
    [TokenType.LPAREN]: {category: TokenCategory.GROUPING, displayText: "(", outputValue: "(", editable: false},
    [TokenType.RPAREN]: {category: TokenCategory.GROUPING, displayText: ")", outputValue: ")", editable: false},
    [TokenType.CONSTANT]: {category: TokenCategory.VALUE, displayText: "", outputValue: "", editable: true},
    [TokenType.VARIABLE]: {category: TokenCategory.VALUE, displayText: "", outputValue: "", editable: true},
    [TokenType.PLACEHOLDER]: {
        category: TokenCategory.PLACEHOLDER,
        displayText: "___",
        outputValue: "",
        editable: false
    },
    [TokenType.SUB_EXPR]: {category: TokenCategory.SUB_EXPR, displayText: "", outputValue: "", editable: false}
};

export const PALETTE_GROUPS: Array<{
    groupName: string;
    groupKey: string;
    icon: string;
    items: PaletteItem[];
}> = [
    {
        groupName: "控制流",
        groupKey: "control",
        icon: "GitBranch",
        items: [
            {
                label: "如果...那么...否则",
                tokenType: TokenType.IF,
                category: TokenCategory.CONTROL_FLOW,
                outputValue: "",
                isTemplate: true,
                templateType: "IF_THEN_ELSE"
            }
        ]
    },
    {
        groupName: "逻辑运算",
        groupKey: "logic",
        icon: "ToggleLeft",
        items: [
            {label: "并且", tokenType: TokenType.AND, category: TokenCategory.LOGIC_OP, outputValue: "&&"},
            {label: "或者", tokenType: TokenType.OR, category: TokenCategory.LOGIC_OP, outputValue: "||"},
            {label: "非", tokenType: TokenType.NOT, category: TokenCategory.LOGIC_OP, outputValue: "!"}
        ]
    },
    {
        groupName: "比较运算",
        groupKey: "compare",
        icon: "Equal",
        items: [
            {label: "等于 ==", tokenType: TokenType.EQ, category: TokenCategory.COMPARE_OP, outputValue: "=="},
            {label: "不等于 !=", tokenType: TokenType.NEQ, category: TokenCategory.COMPARE_OP, outputValue: "!="},
            {label: "大于 >", tokenType: TokenType.GT, category: TokenCategory.COMPARE_OP, outputValue: ">"},
            {label: "小于 <", tokenType: TokenType.LT, category: TokenCategory.COMPARE_OP, outputValue: "<"},
            {label: "大于等于 >=", tokenType: TokenType.GTE, category: TokenCategory.COMPARE_OP, outputValue: ">="},
            {label: "小于等于 <=", tokenType: TokenType.LTE, category: TokenCategory.COMPARE_OP, outputValue: "<="}
        ]
    },
    {
        groupName: "算术运算",
        groupKey: "arithmetic",
        icon: "Calculator",
        items: [
            {label: "加 +", tokenType: TokenType.ADD, category: TokenCategory.ARITHMETIC_OP, outputValue: "+"},
            {label: "减 -", tokenType: TokenType.SUB, category: TokenCategory.ARITHMETIC_OP, outputValue: "-"},
            {label: "乘 ×", tokenType: TokenType.MUL, category: TokenCategory.ARITHMETIC_OP, outputValue: "*"},
            {label: "除 ÷", tokenType: TokenType.DIV, category: TokenCategory.ARITHMETIC_OP, outputValue: "/"},
            {label: "取余 %", tokenType: TokenType.MOD, category: TokenCategory.ARITHMETIC_OP, outputValue: "%"}
        ]
    },
    {
        groupName: "分组",
        groupKey: "grouping",
        icon: "Parentheses",
        items: [
            {label: "左括号 (", tokenType: TokenType.LPAREN, category: TokenCategory.GROUPING, outputValue: "("},
            {label: "右括号 )", tokenType: TokenType.RPAREN, category: TokenCategory.GROUPING, outputValue: ")"}
        ]
    }
];
