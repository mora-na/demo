<template>
  <el-dialog
      v-model="visible"
      :close-on-click-modal="false"
      title="保存为子表达式"
      width="480px"
  >
    <el-form ref="formRef" :model="formData" :rules="formRules" label-position="top">
      <el-form-item label="名称" prop="name">
        <el-input
            v-model="formData.name"
            maxlength="30"
            placeholder="例如：利润率计算、成本公式"
            show-word-limit
        />
      </el-form-item>

      <el-form-item label="描述" prop="description">
        <el-input
            v-model="formData.description"
            :rows="3"
            maxlength="200"
            placeholder="可选，描述此子表达式的用途"
            show-word-limit
            type="textarea"
        />
      </el-form-item>

      <el-form-item label="表达式预览">
        <div class="sub-expr-preview">
          <div class="preview-tokens">
            <span
                v-for="token in selectedTokens"
                :key="token.id"
                :class="`preview-token-chip--${token.category.toLowerCase()}`"
                class="preview-token-chip"
            >
              {{ token.displayText }}
            </span>
          </div>
          <div v-if="compiledPreview" class="preview-compiled">
            <code>{{ compiledPreview }}</code>
          </div>
        </div>
      </el-form-item>
    </el-form>

    <template #footer>
      <el-button @click="visible = false">取消</el-button>
      <el-button :loading="saving" type="primary" @click="handleSave">
        保存
      </el-button>
    </template>
  </el-dialog>
</template>

<script lang="ts" setup>
import {computed, reactive, ref, watch} from "vue";
import type {FormInstance, FormRules} from "element-plus";
import {ElMessage} from "element-plus";
import {useFormulaStore} from "../../stores/formulaStore";
import {FormulaParser} from "../../engine/formula/parser";
import {CodeGenerator} from "../../engine/formula/codeGenerator";
import type {FormulaToken, SubExpression} from "../../types/formula";

const props = defineProps<{
  modelValue: boolean;
  selectedTokens: FormulaToken[];
}>();

const emit = defineEmits<{
  (e: "update:modelValue", val: boolean): void;
  (e: "saved", subExpr: SubExpression): void;
}>();

const store = useFormulaStore();
const formRef = ref<FormInstance>();
const saving = ref(false);

const visible = computed({
  get: () => props.modelValue,
  set: (val) => emit("update:modelValue", val)
});

const formData = reactive({
  name: "",
  description: ""
});

const formRules: FormRules = {
  name: [
    {required: true, message: "请输入名称", trigger: "blur"},
    {min: 1, max: 30, message: "名称长度 1-30 个字符", trigger: "blur"},
    {
      validator: (_rule, value, callback) => {
        if (store.subExpressions.some((item) => item.name === value)) {
          callback(new Error("名称已被使用"));
        } else {
          callback();
        }
      },
      trigger: "blur"
    }
  ]
};

const compiledPreview = computed(() => {
  if (!props.selectedTokens.length) return "";
  try {
    const parser = new FormulaParser(props.selectedTokens);
    const {ast} = parser.parse();
    if (!ast) return "";
    return new CodeGenerator().generateExpression(ast);
  } catch {
    return "// 无法编译";
  }
});

async function handleSave() {
  if (!formRef.value) return;
  await formRef.value.validate();

  saving.value = true;
  try {
    const result = store.addSubExpression(formData.name, formData.description, props.selectedTokens);
    if (result.success) {
      ElMessage.success("子表达式保存成功");
      const saved = store.subExpressions.find((item) => item.name === formData.name);
      if (saved) {
        emit("saved", saved);
      }
      visible.value = false;
      formData.name = "";
      formData.description = "";
    } else {
      ElMessage.error(result.error || "保存失败");
    }
  } finally {
    saving.value = false;
  }
}

watch(visible, (val) => {
  if (val) {
    formData.name = "";
    formData.description = "";
  }
});
</script>

<style lang="scss" scoped>
.sub-expr-preview {
  padding: 12px;
  background: #f5f7fa;
  border-radius: 8px;

  .preview-tokens {
    display: flex;
    flex-wrap: wrap;
    gap: 4px;
    margin-bottom: 8px;
  }

  .preview-token-chip {
    display: inline-block;
    padding: 2px 8px;
    border-radius: 4px;
    font-size: 12px;
    background: #e4e7ed;
    color: #606266;

    &--control_flow {
      background: #ecf5ff;
      color: #409eff;
    }

    &--logic_op {
      background: #f0f9eb;
      color: #67c23a;
    }

    &--compare_op {
      background: #fdf6ec;
      color: #e6a23c;
    }

    &--arithmetic_op {
      background: #f4f4f5;
      color: #606266;
    }

    &--value {
      background: #fef0f0;
      color: #f56c6c;
    }
  }

  .preview-compiled {
    padding-top: 8px;
    border-top: 1px solid #e4e7ed;

    code {
      font-family: "Courier New", monospace;
      font-size: 12px;
      color: #303133;
      word-break: break-all;
    }
  }
}
</style>
