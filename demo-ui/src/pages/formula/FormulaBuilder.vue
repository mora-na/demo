<template>
  <div class="formula-builder" tabindex="0" @keydown="handleGlobalKeydown">
    <FormulaToolbar
        :can-redo="store.redoStack.length > 0"
        :can-undo="store.undoStack.length > 0"
        :has-errors="store.hasErrors"
        :has-tokens="store.tokens.length > 0"
        :is-compiled="compiler.isCompileSuccess.value"
        @clear="handleClear"
        @export="handleExport"
        @preview="previewVisible = true"
        @redo="store.redo()"
        @undo="store.undo()"
        @save-sub-expr="handleSaveSubExpr"
    />

    <div class="formula-builder__body">
      <PalettePanel/>

      <div class="formula-builder__workspace">
        <FormulaCanvas/>

        <FormulaPreview
            :compiled-expression="compiler.compiledExpression.value || compiler.rawExpression.value"
            :display-expression="compiler.displayExpression.value || compiler.rawDisplayExpression.value"
            :is-complete="compiler.isCompileSuccess.value"
            :parse-errors="compiler.parseErrors.value"
        />
      </div>
    </div>

    <FormulaStatusBar
        :error-count="errorCount"
        :token-count="store.tokens.length"
        :warning-count="warningCount"
    />

    <SaveSubExprDialog
        v-model="saveDialogVisible"
        :selected-tokens="selectedTokensForSubExpr"
        @saved="onSubExprSaved"
    />

    <el-dialog v-model="previewVisible" title="公式预览" width="640px">
      <div class="preview-dialog-content">
        <div class="preview-section">
          <h4>中文可读表达式</h4>
          <div class="preview-box">
            {{ compiler.displayExpression.value || "(空)" }}
          </div>
        </div>
        <div class="preview-section">
          <h4>后端标准表达式</h4>
          <div class="preview-box preview-box--code">
            <code>{{ compiler.compiledExpression.value || "(空)" }}</code>
          </div>
        </div>
        <div v-if="compiler.parseErrors.value.length > 0" class="preview-section">
          <h4>编译警告</h4>
          <div class="preview-errors">
            <div v-for="(err, i) in compiler.parseErrors.value" :key="i" class="error-item">
              <AlertCircle :size="14"/>
              {{ err }}
            </div>
          </div>
        </div>
      </div>
      <template #footer>
        <el-button @click="previewVisible = false">关闭</el-button>
        <el-button :disabled="!compiler.isCompileSuccess.value" type="primary" @click="copyCompiledExpression">
          <Copy :size="14"/>
          复制表达式
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script lang="ts" setup>
import {computed, onMounted, ref, watch} from "vue";
import {AlertCircle, Copy} from "lucide-vue-next";
import {ElMessage, ElMessageBox} from "element-plus";
import {useFormulaStore} from "../../stores/formulaStore";
import {useFormulaCompiler} from "../../composables/formula/useFormulaCompiler";
import FormulaToolbar from "../../components/formula/FormulaToolbar.vue";
import PalettePanel from "../../components/formula/PalettePanel.vue";
import FormulaCanvas from "../../components/formula/FormulaCanvas.vue";
import FormulaPreview from "../../components/formula/FormulaPreview.vue";
import FormulaStatusBar from "../../components/formula/FormulaStatusBar.vue";
import SaveSubExprDialog from "../../components/formula/SaveSubExprDialog.vue";
import type {FormulaToken} from "../../types/formula";

const store = useFormulaStore();
const compiler = useFormulaCompiler();

const saveDialogVisible = ref(false);
const previewVisible = ref(false);
const selectedTokensForSubExpr = ref<FormulaToken[]>([]);

const errorCount = computed(() => store.tokens.filter((token) => token.validationState === "error").length);
const warningCount = computed(() => store.tokens.filter((token) => token.validationState === "warning").length);

function handleGlobalKeydown(e: KeyboardEvent) {
  if ((e.ctrlKey || e.metaKey) && e.key === "z" && !e.shiftKey) {
    e.preventDefault();
    store.undo();
  }
  if (
      ((e.ctrlKey || e.metaKey) && e.key === "z" && e.shiftKey) ||
      ((e.ctrlKey || e.metaKey) && e.key === "y")
  ) {
    e.preventDefault();
    store.redo();
  }
}

async function handleClear() {
  if (!store.tokens.length) return;
  try {
    await ElMessageBox.confirm("确定清空画布上的所有公式元素？", "清空确认", {type: "warning"});
    store.clearCanvas();
  } catch {
    // ignore
  }
}

function handleSaveSubExpr() {
  if (!store.tokens.length) {
    ElMessage.warning("画布为空，无法保存子表达式");
    return;
  }
  selectedTokensForSubExpr.value = [...store.tokens];
  saveDialogVisible.value = true;
}

function onSubExprSaved() {
  ElMessage.success("子表达式已保存到左侧面板");
}

function handleExport() {
  const result = compiler.compile();
  if (result.success) {
    ElMessage.success("编译成功");
  } else {
    ElMessage.error(`编译失败: ${result.errors.join("; ")}`);
  }
}

async function copyCompiledExpression() {
  try {
    await navigator.clipboard.writeText(compiler.compiledExpression.value);
    ElMessage.success("已复制到剪贴板");
  } catch {
    ElMessage.error("复制失败");
  }
}

onMounted(() => {
  store.predefinedVariables = [
    {name: "revenue", label: "营业收入", description: "当期营业收入"},
    {name: "cost", label: "成本", description: "当期成本"},
    {name: "profit", label: "利润", description: "当期利润"},
    {name: "quantity", label: "数量", description: "销售数量"},
    {name: "price", label: "单价", description: "商品单价"}
  ];

  const savedSubExprs = localStorage.getItem("formula_sub_expressions");
  if (savedSubExprs) {
    try {
      store.subExpressions = JSON.parse(savedSubExprs);
    } catch {
      // ignore
    }
  }

  const savedCustomValues = localStorage.getItem("formula_custom_values");
  if (savedCustomValues) {
    try {
      store.customValues = JSON.parse(savedCustomValues);
    } catch {
      // ignore
    }
  }
});

watch(
    () => store.subExpressions,
    (val) => {
      localStorage.setItem("formula_sub_expressions", JSON.stringify(val));
    },
    {deep: true}
);

watch(
    () => store.customValues,
    (val) => {
      localStorage.setItem("formula_custom_values", JSON.stringify(val));
    },
    {deep: true}
);
</script>

<style lang="scss" scoped>
.formula-builder {
  --formula-chip-height: 32px;
  display: flex;
  flex-direction: column;
  min-height: 720px;
  height: 100%;
  background: #f5f7fa;
  outline: none;

  &:focus {
    outline: none;
  }

  &__body {
    display: flex;
    flex: 1;
    flex-direction: column;
    overflow: hidden;
  }

  &__workspace {
    flex: 1;
    display: flex;
    flex-direction: column;
    min-height: 0;
    padding: 20px;
    gap: 16px;
    overflow-y: auto;
  }
}

.preview-dialog-content {
  .preview-section {
    margin-bottom: 20px;

    h4 {
      margin: 0 0 8px 0;
      font-size: 14px;
      color: #606266;
    }
  }

  .preview-box {
    padding: 12px 16px;
    background: #f5f7fa;
    border-radius: 8px;
    font-size: 14px;
    color: #303133;
    line-height: 1.6;
    word-break: break-all;

    &--code {
      code {
        font-family: "Courier New", "JetBrains Mono", monospace;
        font-size: 13px;
      }
    }
  }

  .preview-errors {
    .error-item {
      display: flex;
      align-items: center;
      gap: 6px;
      padding: 6px 0;
      font-size: 13px;
      color: #f56c6c;
    }
  }
}
</style>
