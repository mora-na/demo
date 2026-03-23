import {computed, ref} from "vue";
import {defineStore} from "pinia";
import type {FormulaToken, SubExpression, TemplateGroup} from "../types/formula";
import {TokenType} from "../types/formula";
import {validateTokenSequence} from "../engine/formula/validator";
import {compileTokensToExpression} from "../engine/formula/compiler";
import {FormulaParser} from "../engine/formula/parser";
import {CodeGenerator} from "../engine/formula/codeGenerator";
import {cloneToken, createToken} from "../utils/formula/tokenFactory";

interface HistorySnapshot {
    tokens: FormulaToken[];
    templateGroups: Array<[string, TemplateGroup]>;
    timestamp: number;
}

function createId() {
    return globalThis.crypto?.randomUUID?.() || `${Date.now()}-${Math.random().toString(16).slice(2)}`;
}

export const useFormulaStore = defineStore("formula", () => {
    const tokens = ref<FormulaToken[]>([]);
    const templateGroups = ref<Map<string, TemplateGroup>>(new Map());
    const predefinedVariables = ref<Array<{ name: string; label: string; description?: string }>>([]);
    const customValues = ref<Array<{ id: string; type: "CONSTANT" | "VARIABLE"; name: string; value: string }>>([]);
    const subExpressions = ref<SubExpression[]>([]);
    const undoStack = ref<HistorySnapshot[]>([]);
    const redoStack = ref<HistorySnapshot[]>([]);
    const MAX_HISTORY = 50;
    const dragState = ref({
        isDragging: false,
        payload: null as any,
        dropTargetIndex: null as number | null,
        dropTargetTokenId: null as string | null
    });

    const visibleTokens = computed(() => tokens.value);
    const hasErrors = computed(() => tokens.value.some((token) => token.validationState === "error"));
    const displayExpression = computed(() =>
        tokens.value
            .filter((token) => token.type !== TokenType.PLACEHOLDER)
            .map((token) => token.displayText)
            .join(" ")
    );
    const compiledExpression = computed(() => compileTokensToExpression(tokens.value));

    function saveSnapshot() {
        const snapshot: HistorySnapshot = {
            tokens: JSON.parse(JSON.stringify(tokens.value)),
            templateGroups: JSON.parse(JSON.stringify(Array.from(templateGroups.value.entries()))),
            timestamp: Date.now()
        };
        undoStack.value.push(snapshot);
        if (undoStack.value.length > MAX_HISTORY) {
            undoStack.value.shift();
        }
        redoStack.value = [];
    }

    function undo() {
        if (undoStack.value.length === 0) return;
        const current: HistorySnapshot = {
            tokens: JSON.parse(JSON.stringify(tokens.value)),
            templateGroups: JSON.parse(JSON.stringify(Array.from(templateGroups.value.entries()))),
            timestamp: Date.now()
        };
        redoStack.value.push(current);
        const prev = undoStack.value.pop();
        if (!prev) return;
        tokens.value = prev.tokens;
        templateGroups.value = new Map(prev.templateGroups);
    }

    function redo() {
        if (redoStack.value.length === 0) return;
        const current: HistorySnapshot = {
            tokens: JSON.parse(JSON.stringify(tokens.value)),
            templateGroups: JSON.parse(JSON.stringify(Array.from(templateGroups.value.entries()))),
            timestamp: Date.now()
        };
        undoStack.value.push(current);
        const next = redoStack.value.pop();
        if (!next) return;
        tokens.value = next.tokens;
        templateGroups.value = new Map(next.templateGroups);
    }

    function insertToken(token: FormulaToken, index: number) {
        saveSnapshot();
        tokens.value.splice(index, 0, token);
        runValidation();
    }

    function insertTokens(newTokens: FormulaToken[], index: number) {
        saveSnapshot();
        tokens.value.splice(index, 0, ...newTokens);
        runValidation();
    }

    function removeToken(tokenId: string) {
        saveSnapshot();
        const token = tokens.value.find((t) => t.id === tokenId);
        if (!token) return;
        if (token.templateGroupId) {
            const group = templateGroups.value.get(token.templateGroupId);
            if (group) {
                tokens.value = tokens.value.filter((item) => item.templateGroupId !== token.templateGroupId);
                templateGroups.value.delete(token.templateGroupId);
            }
        } else {
            tokens.value = tokens.value.filter((item) => item.id !== tokenId);
        }
        runValidation();
    }

    function moveToken(fromIndex: number, toIndex: number) {
        saveSnapshot();
        const moving = tokens.value[fromIndex];
        if (moving?.templateGroupId) {
            handleTemplateMoveGroup(moving, fromIndex, toIndex);
        } else if (moving) {
            const [moved] = tokens.value.splice(fromIndex, 1);
            const adjustedIndex = toIndex > fromIndex ? toIndex - 1 : toIndex;
            tokens.value.splice(adjustedIndex, 0, moved);
        }
        runValidation();
    }

    function duplicateToken(tokenId: string) {
        saveSnapshot();
        const index = tokens.value.findIndex((token) => token.id === tokenId);
        if (index === -1) return;
        const original = tokens.value[index];
        if (original.templateGroupId) {
            duplicateTemplateGroup(original.templateGroupId);
        } else {
            const cloned = cloneToken(original);
            tokens.value.splice(index + 1, 0, cloned);
        }
        runValidation();
    }

    function updateTokenValue(tokenId: string, newDisplayText: string, newOutputValue: string) {
        const token = tokens.value.find((t) => t.id === tokenId);
        if (token && token.editable) {
            saveSnapshot();
            token.displayText = newDisplayText;
            token.outputValue = newOutputValue;
            runValidation();
        }
    }

    function clearCanvas() {
        saveSnapshot();
        tokens.value = [];
        templateGroups.value.clear();
    }

    function replacePlaceholder(placeholderId: string, newTokens: FormulaToken[]) {
        const index = tokens.value.findIndex((token) => token.id === placeholderId);
        if (index === -1) return;
        saveSnapshot();
        tokens.value.splice(index, 1, ...newTokens);
        runValidation();
    }

    function replacePlaceholderWithToken(placeholderId: string, tokenId: string) {
        const placeholderIndex = tokens.value.findIndex((token) => token.id === placeholderId);
        const tokenIndex = tokens.value.findIndex((token) => token.id === tokenId);
        if (placeholderIndex === -1 || tokenIndex === -1 || placeholderId === tokenId) return;
        saveSnapshot();
        const [movingToken] = tokens.value.splice(tokenIndex, 1);
        const adjustedIndex = tokenIndex < placeholderIndex ? placeholderIndex - 1 : placeholderIndex;
        tokens.value.splice(adjustedIndex, 1, movingToken);
        runValidation();
    }

    function runValidation() {
        validateTokenSequence(tokens.value);
    }

    function handleTemplateMoveGroup(moved: FormulaToken, fromIndex: number, toIndex: number) {
        const groupId = moved.templateGroupId;
        if (!groupId) return;
        const groupTokens = tokens.value.filter((token) => token.templateGroupId === groupId);
        if (!groupTokens.length) return;
        const firstIndex = tokens.value.findIndex((token) => token.id === groupTokens[0].id);
        if (firstIndex === -1) return;
        const removed = tokens.value.splice(firstIndex, groupTokens.length);
        let insertIndex = toIndex;
        if (toIndex > firstIndex) {
            insertIndex = Math.max(0, toIndex - removed.length);
        }
        tokens.value.splice(insertIndex, 0, ...removed);
    }

    function duplicateTemplateGroup(groupId: string) {
        const group = templateGroups.value.get(groupId);
        if (!group) return;
        const groupTokens = tokens.value.filter((token) => token.templateGroupId === groupId);
        if (!groupTokens.length) return;
        const firstIndex = tokens.value.findIndex((token) => token.id === groupTokens[0].id);
        const idMap = new Map<string, string>();
        const newGroupId = createId();
        const clonedTokens = groupTokens.map((token) => {
            const cloned = cloneToken(token);
            cloned.templateGroupId = newGroupId;
            idMap.set(token.id, cloned.id);
            return cloned;
        });
        const newGroup: TemplateGroup = {
            ...group,
            id: newGroupId,
            tokenIds: clonedTokens.map((token) => token.id),
            slots: {
                condition: idMap.get(group.slots.condition) || clonedTokens[1]?.id || "",
                trueBranch: idMap.get(group.slots.trueBranch) || clonedTokens[3]?.id || "",
                falseBranch: idMap.get(group.slots.falseBranch) || clonedTokens[5]?.id || ""
            }
        };
        templateGroups.value.set(newGroupId, newGroup);
        const insertIndex = firstIndex === -1 ? tokens.value.length : firstIndex + groupTokens.length;
        tokens.value.splice(insertIndex, 0, ...clonedTokens);
    }

    function addSubExpression(
        name: string,
        description: string,
        tokenSlice: FormulaToken[]
    ): { success: boolean; error?: string } {
        if (subExpressions.value.some((item) => item.name === name)) {
            return {success: false, error: "子表达式名称已存在"};
        }
        if (!name.trim()) {
            return {success: false, error: "名称不能为空"};
        }
        const validationErrors = validateTokenSequence([...tokenSlice]);
        const hasRealErrors = validationErrors.some((err) => err.severity === "error");
        if (hasRealErrors) {
            return {success: false, error: "所选表达式存在语法错误，请先修正"};
        }
        const hasPlaceholders = tokenSlice.some((token) => token.type === TokenType.PLACEHOLDER);
        if (hasPlaceholders) {
            return {success: false, error: "所选表达式包含未填写的占位符"};
        }

        const clonedTokens = tokenSlice.map((token) => ({
            ...JSON.parse(JSON.stringify(token)),
            id: createId(),
            templateGroupId: undefined,
            validationState: "valid" as const
        }));

        const parser = new FormulaParser(clonedTokens);
        const {ast} = parser.parse();
        const generator = new CodeGenerator();
        const compiledExpression = ast ? generator.generateExpression(ast) : "";
        const displayExpression = ast ? generator.generateDisplay(ast) : "";

        const subExpr: SubExpression = {
            id: createId(),
            name,
            description,
            tokens: clonedTokens,
            compiledExpression,
            displayExpression,
            createdAt: Date.now(),
            updatedAt: Date.now()
        };

        subExpressions.value.push(subExpr);
        return {success: true};
    }

    function removeSubExpression(id: string) {
        const index = subExpressions.value.findIndex((item) => item.id === id);
        if (index !== -1) {
            subExpressions.value.splice(index, 1);
        }
    }

    function updateSubExpression(id: string, updates: Partial<Pick<SubExpression, "name" | "description">>) {
        const subExpr = subExpressions.value.find((item) => item.id === id);
        if (subExpr) {
            Object.assign(subExpr, updates, {updatedAt: Date.now()});
        }
    }

    function insertSubExpression(subExprId: string, index: number, mode: "collapsed" | "expanded" = "collapsed") {
        const subExpr = subExpressions.value.find((item) => item.id === subExprId);
        if (!subExpr) return;
        if (mode === "collapsed") {
            const token = createToken(TokenType.SUB_EXPR, {
                displayText: subExpr.name,
                outputValue: subExpr.compiledExpression,
                subExprId: subExpr.id,
                subExprTokens: JSON.parse(JSON.stringify(subExpr.tokens))
            });
            insertToken(token, index);
        } else {
            const expandedTokens = subExpr.tokens.map((token) => cloneToken(token));
            insertTokens(expandedTokens, index);
        }
    }

    return {
        tokens,
        templateGroups,
        predefinedVariables,
        customValues,
        subExpressions,
        dragState,
        undoStack,
        redoStack,
        visibleTokens,
        hasErrors,
        displayExpression,
        compiledExpression,
        insertToken,
        insertTokens,
        removeToken,
        moveToken,
        duplicateToken,
        updateTokenValue,
        clearCanvas,
        replacePlaceholder,
        replacePlaceholderWithToken,
        runValidation,
        undo,
        redo,
        saveSnapshot,
        addSubExpression,
        removeSubExpression,
        updateSubExpression,
        insertSubExpression
    };
});
