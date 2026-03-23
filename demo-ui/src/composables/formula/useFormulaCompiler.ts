import {computed, ref, watch} from "vue";
import {useFormulaStore} from "../../stores/formulaStore";
import {FormulaParser} from "../../engine/formula/parser";
import {CodeGenerator} from "../../engine/formula/codeGenerator";
import type {ASTNode} from "../../engine/formula/ast";
import {TokenType} from "../../types/formula";

export function useFormulaCompiler() {
    const store = useFormulaStore();
    const generator = new CodeGenerator();

    const parseErrors = ref<string[]>([]);
    const currentAST = ref<ASTNode | null>(null);

    const compiledExpression = computed(() => {
        if (!currentAST.value) return "";
        try {
            return generator.generateExpression(currentAST.value);
        } catch {
            return "// 编译错误";
        }
    });

    const displayExpression = computed(() => {
        if (!currentAST.value) return "";
        try {
            return generator.generateDisplay(currentAST.value);
        } catch {
            return "// 编译错误";
        }
    });

    const rawExpression = computed(() => {
        return store.tokens
            .filter((token) => token.type !== TokenType.PLACEHOLDER)
            .map((token) => token.outputValue)
            .join("");
    });

    const rawDisplayExpression = computed(() => {
        return store.tokens
            .filter((token) => token.type !== TokenType.PLACEHOLDER)
            .map((token) => token.displayText)
            .join(" ");
    });

    const isCompileSuccess = computed(() => {
        return parseErrors.value.length === 0 && currentAST.value !== null && !hasPlaceholderInAST(currentAST.value);
    });

    watch(
        () => store.tokens,
        (newTokens) => {
            if (!newTokens.length) {
                currentAST.value = null;
                parseErrors.value = [];
                return;
            }
            const parser = new FormulaParser(newTokens);
            const {ast, errors} = parser.parse();
            currentAST.value = ast;
            parseErrors.value = errors;
        },
        {deep: true, immediate: true}
    );

    function hasPlaceholderInAST(node: ASTNode): boolean {
        if (node.kind === "Placeholder") return true;
        if (node.kind === "BinaryExpr") {
            return hasPlaceholderInAST(node.left) || hasPlaceholderInAST(node.right);
        }
        if (node.kind === "UnaryExpr") return hasPlaceholderInAST(node.operand);
        if (node.kind === "ConditionalExpr") {
            return (
                hasPlaceholderInAST(node.condition) ||
                hasPlaceholderInAST(node.consequent) ||
                hasPlaceholderInAST(node.alternate)
            );
        }
        if (node.kind === "GroupExpr") return hasPlaceholderInAST(node.expression);
        if (node.kind === "SubExprRef") return hasPlaceholderInAST(node.expanded);
        return false;
    }

    function compile(): { success: boolean; expression: string; display: string; errors: string[] } {
        if (store.tokens.length === 0) {
            return {success: false, expression: "", display: "", errors: ["空表达式"]};
        }

        const parser = new FormulaParser(store.tokens);
        const {ast, errors} = parser.parse();

        if (!ast || errors.length > 0) {
            return {success: false, expression: "", display: "", errors};
        }

        if (hasPlaceholderInAST(ast)) {
            return {success: false, expression: "", display: "", errors: ["存在未填写的占位符"]};
        }

        return {
            success: true,
            expression: generator.generateExpression(ast),
            display: generator.generateDisplay(ast),
            errors: []
        };
    }

    return {
        compiledExpression,
        displayExpression,
        rawExpression,
        rawDisplayExpression,
        parseErrors,
        currentAST,
        isCompileSuccess,
        compile
    };
}
