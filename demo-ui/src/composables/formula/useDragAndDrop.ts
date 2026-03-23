import {computed} from "vue";
import {useFormulaStore} from "../../stores/formulaStore";
import type {DragPayload, FormulaToken, PaletteItem} from "../../types/formula";
import {TokenCategory, TokenType} from "../../types/formula";
import {createIfThenElseTemplate, createToken} from "../../utils/formula/tokenFactory";
import {rafThrottle} from "../../utils/formula/throttle";

export function useDragAndDrop() {
    const store = useFormulaStore();
    const isDragging = computed(() => store.dragState.isDragging);
    const dragPayload = computed(() => store.dragState.payload as DragPayload | null);
    const dropIndicatorIndex = computed(() => store.dragState.dropTargetIndex);
    const throttledDragOver = rafThrottle(updateDragOverState);

    function handlePaletteDragStart(event: DragEvent, paletteItem: PaletteItem) {
        if (!event.dataTransfer) return;

        const payload: DragPayload = {
            source: "palette",
            tokenPrototype: {
                type: paletteItem.tokenType,
                category: paletteItem.category,
                displayText: paletteItem.label,
                outputValue: paletteItem.outputValue,
                editable: paletteItem.editable || false,
                subExprId: paletteItem.subExprId,
                subExprTokens: paletteItem.subExprTokens
            },
            isTemplate: paletteItem.isTemplate || false,
            templateType: paletteItem.templateType
        };

        event.dataTransfer.setData("application/formula-token", JSON.stringify(payload));
        event.dataTransfer.effectAllowed = "copy";

        const ghost = createDragGhost(paletteItem.label);
        document.body.appendChild(ghost);
        event.dataTransfer.setDragImage(ghost, ghost.offsetWidth / 2, ghost.offsetHeight / 2);
        setTimeout(() => {
            document.body.removeChild(ghost);
        }, 0);

        store.dragState = {
            isDragging: true,
            payload,
            dropTargetIndex: null,
            dropTargetTokenId: null
        };
    }

    function handleCanvasDragStart(event: DragEvent, token: FormulaToken, _index: number) {
        if (!event.dataTransfer) return;

        const payload: DragPayload = {
            source: "canvas",
            tokenId: token.id
        };

        event.dataTransfer.setData("application/formula-token", JSON.stringify(payload));
        event.dataTransfer.effectAllowed = "move";

        const el = event.currentTarget as HTMLElement;
        setTimeout(() => el.classList.add("dragging"), 0);

        store.dragState = {
            isDragging: true,
            payload,
            dropTargetIndex: null,
            dropTargetTokenId: null
        };
    }

    function handleCanvasDragOver(event: DragEvent, containerEl: HTMLElement) {
        event.preventDefault();
        if (!event.dataTransfer) return;
        event.dataTransfer.dropEffect = dragPayload.value?.source === "palette" ? "copy" : "move";
        throttledDragOver({
            clientX: event.clientX,
            clientY: event.clientY,
            target: event.target as HTMLElement | null,
            containerEl
        });
    }

    function calculateInsertIndex(clientX: number, container: HTMLElement): number {
        const tokenElements = container.querySelectorAll<HTMLElement>("[data-token-index]");
        if (tokenElements.length === 0) return 0;

        let closestIndex = 0;
        let closestDistance = Infinity;

        for (let i = 0; i < tokenElements.length; i += 1) {
            const rect = tokenElements[i].getBoundingClientRect();
            const tokenCenterX = rect.left + rect.width / 2;
            if (clientX < tokenCenterX) {
                const distance = Math.abs(clientX - rect.left);
                if (distance < closestDistance) {
                    closestDistance = distance;
                    closestIndex = i;
                }
            } else {
                const distance = Math.abs(clientX - rect.right);
                if (distance < closestDistance) {
                    closestDistance = distance;
                    closestIndex = i + 1;
                }
            }
        }

        if (dragPayload.value?.source === "canvas" && dragPayload.value.tokenId) {
            const sourceIndex = store.tokens.findIndex((item) => item.id === dragPayload.value?.tokenId);
            if (sourceIndex !== -1 && closestIndex === sourceIndex) {
                return -1;
            }
        }

        return closestIndex;
    }

    function updateDragOverState(payload: {
        clientX: number;
        clientY: number;
        target: HTMLElement | null;
        containerEl: HTMLElement;
    }) {
        const insertIndex = calculateInsertIndex(payload.clientX, payload.containerEl);
        store.dragState.dropTargetIndex = insertIndex;
        const targetEl = payload.target?.closest?.("[data-token-id]") as HTMLElement | null;
        if (targetEl && payload.containerEl.contains(targetEl)) {
            const tokenId = targetEl.dataset.tokenId;
            const token = tokenId ? store.tokens.find((item) => item.id === tokenId) : null;
            store.dragState.dropTargetTokenId = token?.type === TokenType.PLACEHOLDER ? token.id : null;
        } else {
            store.dragState.dropTargetTokenId = null;
        }
    }

    function handleCanvasDrop(event: DragEvent) {
        event.preventDefault();
        const dataStr = event.dataTransfer?.getData("application/formula-token");
        if (!dataStr) return;

        const payload: DragPayload = JSON.parse(dataStr);
        const containerEl = event.currentTarget as HTMLElement | null;
        const placeholderTarget = resolvePlaceholderTarget(event, containerEl);

        if (placeholderTarget) {
            if (handleDropOnPlaceholder(payload, placeholderTarget, event)) {
                cleanupDragState();
                return;
            }
        }

        let insertIndex = store.dragState.dropTargetIndex ?? store.tokens.length;
        insertIndex = applyAutoPlaceholderCleanup(insertIndex);

        if (payload.source === "palette") {
            handleDropFromPalette(payload, insertIndex);
        } else if (payload.source === "canvas") {
            handleDropFromCanvas(payload, insertIndex);
        }

        cleanupDragState();
    }

    function handleDropFromPalette(payload: DragPayload, index: number) {
        if (payload.isTemplate && payload.templateType === "IF_THEN_ELSE") {
            const {tokens: newTokens, group} = createIfThenElseTemplate();
            store.templateGroups.set(group.id, group);
            store.insertTokens(newTokens, index);
            return;
        }
        if (payload.tokenPrototype) {
            const token = createToken(payload.tokenPrototype.type as TokenType, {
                displayText: payload.tokenPrototype.displayText,
                outputValue: payload.tokenPrototype.outputValue,
                editable: payload.tokenPrototype.editable,
                subExprId: payload.tokenPrototype.subExprId,
                subExprTokens: payload.tokenPrototype.subExprTokens
            });
            store.insertToken(token, index);
        }
    }

    function handleDropOnPlaceholder(
        payload: DragPayload,
        placeholderTarget: { token: FormulaToken; element: HTMLElement | null },
        event: DragEvent
    ): boolean {
        const placeholderId = placeholderTarget.token.id;
        const placeholderIndex = store.tokens.findIndex((token) => token.id === placeholderId);
        if (placeholderIndex === -1) return false;

        if (payload.isTemplate && payload.templateType === "IF_THEN_ELSE") {
            const {tokens: newTokens, group} = createIfThenElseTemplate();
            store.templateGroups.set(group.id, group);
            store.replacePlaceholder(placeholderId, newTokens);
            return true;
        }

        const payloadToken = resolvePayloadToken(payload);
        if (!payloadToken) return false;

        if (isValueLike(payloadToken.category, payloadToken.type)) {
            if (payload.source === "palette" && payload.tokenPrototype) {
                const token = createToken(payloadToken.type, {
                    displayText: payload.tokenPrototype.displayText,
                    outputValue: payload.tokenPrototype.outputValue,
                    editable: payload.tokenPrototype.editable,
                    subExprId: payload.tokenPrototype.subExprId,
                    subExprTokens: payload.tokenPrototype.subExprTokens
                });
                store.replacePlaceholder(placeholderId, [token]);
                return true;
            }
            if (payload.source === "canvas" && payload.tokenId) {
                store.replacePlaceholderWithToken(placeholderId, payload.tokenId);
                return true;
            }
        }

        const insertIndex = resolveInsertIndexAroundPlaceholder(event, placeholderIndex, placeholderTarget.element);
        if (payload.source === "palette") {
            if (payload.tokenPrototype) {
                const token = createToken(payloadToken.type, {
                    displayText: payload.tokenPrototype.displayText,
                    outputValue: payload.tokenPrototype.outputValue,
                    editable: payload.tokenPrototype.editable,
                    subExprId: payload.tokenPrototype.subExprId,
                    subExprTokens: payload.tokenPrototype.subExprTokens
                });
                store.insertToken(token, insertIndex);
                return true;
            }
        }
        if (payload.source === "canvas") {
            handleDropFromCanvas(payload, insertIndex);
            return true;
        }

        return false;
    }

    function handleDropFromCanvas(payload: DragPayload, targetIndex: number) {
        if (!payload.tokenId) return;
        const sourceIndex = store.tokens.findIndex((item) => item.id === payload.tokenId);
        if (sourceIndex === -1 || targetIndex === -1) return;
        if (sourceIndex === targetIndex) return;
        store.moveToken(sourceIndex, targetIndex);
    }

    function handleDragEnd() {
        cleanupDragState();
        document.querySelectorAll(".dragging").forEach((el) => el.classList.remove("dragging"));
    }

    function handleCanvasDragLeave(event: DragEvent) {
        const container = event.currentTarget as HTMLElement;
        const relatedTarget = event.relatedTarget as HTMLElement | null;
        if (!relatedTarget || !container.contains(relatedTarget)) {
            store.dragState.dropTargetIndex = null;
            store.dragState.dropTargetTokenId = null;
        }
    }

    function cleanupDragState() {
        store.dragState = {
            isDragging: false,
            payload: null,
            dropTargetIndex: null,
            dropTargetTokenId: null
        };
    }

    function resolvePlaceholderTarget(
        event: DragEvent,
        containerEl: HTMLElement | null
    ): { token: FormulaToken; element: HTMLElement | null } | null {
        const hitElement = document.elementFromPoint(event.clientX, event.clientY) as HTMLElement | null;
        const hitTokenEl = hitElement?.closest?.("[data-token-id]") as HTMLElement | null;
        if (hitTokenEl && containerEl?.contains(hitTokenEl)) {
            const tokenId = hitTokenEl.dataset.tokenId;
            const token = tokenId ? store.tokens.find((item) => item.id === tokenId) : null;
            if (token?.type === TokenType.PLACEHOLDER) {
                return {token, element: hitTokenEl};
            }
        }

        const targetEl = containerEl
            ? ((event.target as HTMLElement | null)?.closest?.("[data-token-id]") as HTMLElement | null)
            : null;
        if (targetEl && containerEl?.contains(targetEl)) {
            const tokenId = targetEl.dataset.tokenId;
            const token = tokenId ? store.tokens.find((item) => item.id === tokenId) : null;
            if (token?.type === TokenType.PLACEHOLDER) {
                return {token, element: targetEl};
            }
        }

        const fallbackId = store.dragState.dropTargetTokenId;
        if (fallbackId) {
            const token = store.tokens.find((item) => item.id === fallbackId);
            if (token?.type === TokenType.PLACEHOLDER) {
                const element = containerEl
                    ? (containerEl.querySelector(`[data-token-id="${fallbackId}"]`) as HTMLElement | null)
                    : null;
                return {token, element};
            }
        }
        return null;
    }

    function resolvePayloadToken(payload: DragPayload): {
        token?: FormulaToken;
        type: TokenType;
        category: TokenCategory;
    } | null {
        if (payload.source === "palette" && payload.tokenPrototype) {
            return {
                type: payload.tokenPrototype.type as TokenType,
                category: payload.tokenPrototype.category as TokenCategory
            };
        }
        if (payload.source === "canvas" && payload.tokenId) {
            const token = store.tokens.find((item) => item.id === payload.tokenId);
            if (!token) return null;
            return {token, type: token.type, category: token.category};
        }
        return null;
    }

    function isValueLike(category: TokenCategory, type: TokenType): boolean {
        if (category === TokenCategory.VALUE || category === TokenCategory.SUB_EXPR) return true;
        return type === TokenType.SUB_EXPR;
    }

    function resolveInsertIndexAroundPlaceholder(
        event: DragEvent,
        placeholderIndex: number,
        placeholderEl: HTMLElement | null
    ): number {
        if (!placeholderEl) return placeholderIndex;
        const rect = placeholderEl.getBoundingClientRect();
        const midpoint = rect.left + rect.width / 2;
        return event.clientX < midpoint ? placeholderIndex : placeholderIndex + 1;
    }

    function applyAutoPlaceholderCleanup(insertIndex: number): number {
        if (insertIndex == null || insertIndex < 0) return insertIndex;
        if (!store.tokens.length || store.templateGroups.size === 0) {
            return insertIndex;
        }

        const tokens = store.tokens;
        const indexById = new Map<string, number>();
        const groupPositions = new Map<string, { ifIndex?: number; thenIndex?: number; elseIndex?: number }>();
        for (let i = 0; i < tokens.length; i += 1) {
            const token = tokens[i];
            indexById.set(token.id, i);
            if (token.templateGroupId) {
                const pos = groupPositions.get(token.templateGroupId) || {};
                if (token.type === TokenType.IF) pos.ifIndex = i;
                if (token.type === TokenType.THEN) pos.thenIndex = i;
                if (token.type === TokenType.ELSE) pos.elseIndex = i;
                groupPositions.set(token.templateGroupId, pos);
            }
        }
        let bestCondition: { placeholderId: string; placeholderIndex: number; span: number } | null = null;
        let bestTrue: { placeholderId: string; placeholderIndex: number; span: number } | null = null;
        let bestFalse: { placeholderId: string; placeholderIndex: number; distance: number } | null = null;

        for (const group of store.templateGroups.values()) {
            const pos = groupPositions.get(group.id);
            const ifIndex = pos?.ifIndex ?? -1;
            const thenIndex = pos?.thenIndex ?? -1;
            const elseIndex = pos?.elseIndex ?? -1;
            if (ifIndex !== -1 && thenIndex !== -1 && insertIndex > ifIndex && insertIndex <= thenIndex) {
                const placeholderId = group.slots.condition;
                const placeholderIndex = indexById.get(placeholderId) ?? -1;
                const placeholderToken = tokens[placeholderIndex];
                if (placeholderIndex !== -1 && placeholderToken?.type === TokenType.PLACEHOLDER) {
                    const span = thenIndex - ifIndex;
                    if (!bestCondition || span < bestCondition.span) {
                        bestCondition = {placeholderId, placeholderIndex, span};
                    }
                }
                continue;
            }

            if (thenIndex === -1 || elseIndex === -1) continue;

            if (insertIndex > thenIndex && insertIndex <= elseIndex) {
                const placeholderId = group.slots.trueBranch;
                const placeholderIndex = indexById.get(placeholderId) ?? -1;
                const placeholderToken = tokens[placeholderIndex];
                if (placeholderIndex !== -1 && placeholderToken?.type === TokenType.PLACEHOLDER) {
                    const span = elseIndex - thenIndex;
                    if (!bestTrue || span < bestTrue.span) {
                        bestTrue = {placeholderId, placeholderIndex, span};
                    }
                }
            } else if (insertIndex > elseIndex) {
                const placeholderId = group.slots.falseBranch;
                const placeholderIndex = indexById.get(placeholderId) ?? -1;
                const placeholderToken = tokens[placeholderIndex];
                if (placeholderIndex !== -1 && placeholderToken?.type === TokenType.PLACEHOLDER) {
                    const distance = insertIndex - elseIndex;
                    if (!bestFalse || distance < bestFalse.distance) {
                        bestFalse = {placeholderId, placeholderIndex, distance};
                    }
                }
            }
        }

        const target = bestCondition || bestTrue || bestFalse;
        if (!target) return insertIndex;

        store.replacePlaceholder(target.placeholderId, []);
        return target.placeholderIndex < insertIndex ? insertIndex - 1 : insertIndex;
    }

    function createDragGhost(text: string): HTMLElement {
        const ghost = document.createElement("div");
        ghost.textContent = text;
        ghost.style.cssText = `
      position: absolute;
      top: -1000px;
      left: -1000px;
      padding: 6px 16px;
      background: #409eff;
      color: white;
      border-radius: 6px;
      font-size: 14px;
      font-weight: 500;
      box-shadow: 0 2px 12px rgba(0,0,0,0.15);
      white-space: nowrap;
    `;
        return ghost;
    }

    return {
        isDragging,
        dropIndicatorIndex,
        handlePaletteDragStart,
        handleCanvasDragStart,
        handleCanvasDragOver,
        handleCanvasDragLeave,
        handleCanvasDrop,
        handleDragEnd
    };
}
