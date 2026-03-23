import {type FormulaToken, type SubExpression, type TemplateGroup, TokenType} from "../../types/formula";
import {TOKEN_META} from "../../config/tokenRegistry";

function createId() {
    return globalThis.crypto?.randomUUID?.() || `${Date.now()}-${Math.random().toString(16).slice(2)}`;
}

export function createToken(type: TokenType, overrides?: Partial<FormulaToken>): FormulaToken {
    const meta = TOKEN_META[type];
    return {
        id: createId(),
        type,
        category: meta.category,
        displayText: meta.displayText,
        outputValue: meta.outputValue,
        editable: meta.editable,
        validationState: "valid",
        nestingLevel: 0,
        createdAt: Date.now(),
        ...overrides
    };
}

export function createConstantToken(value: string): FormulaToken {
    return createToken(TokenType.CONSTANT, {
        displayText: value,
        outputValue: value,
        editable: true
    });
}

export function createVariableToken(name: string, outputName?: string): FormulaToken {
    return createToken(TokenType.VARIABLE, {
        displayText: name,
        outputValue: outputName || name,
        editable: true
    });
}

export function createIfThenElseTemplate(): { tokens: FormulaToken[]; group: TemplateGroup } {
    const groupId = createId();

    const ifToken = createToken(TokenType.IF, {templateGroupId: groupId});
    const condPlaceholder = createToken(TokenType.PLACEHOLDER, {
        templateGroupId: groupId,
        displayText: "条件"
    });
    const thenToken = createToken(TokenType.THEN, {templateGroupId: groupId});
    const truePlaceholder = createToken(TokenType.PLACEHOLDER, {
        templateGroupId: groupId,
        displayText: "真值"
    });
    const elseToken = createToken(TokenType.ELSE, {templateGroupId: groupId});
    const falsePlaceholder = createToken(TokenType.PLACEHOLDER, {
        templateGroupId: groupId,
        displayText: "假值"
    });

    const tokens = [ifToken, condPlaceholder, thenToken, truePlaceholder, elseToken, falsePlaceholder];

    const group: TemplateGroup = {
        id: groupId,
        templateType: "IF_THEN_ELSE",
        tokenIds: tokens.map((token) => token.id),
        slots: {
            condition: condPlaceholder.id,
            trueBranch: truePlaceholder.id,
            falseBranch: falsePlaceholder.id
        }
    };

    return {tokens, group};
}

export function cloneToken(token: FormulaToken): FormulaToken {
    return {
        ...JSON.parse(JSON.stringify(token)),
        id: createId(),
        createdAt: Date.now(),
        templateGroupId: undefined
    };
}

export function expandSubExpression(subExpr: SubExpression): FormulaToken[] {
    return subExpr.tokens.map((token) => cloneToken(token));
}
