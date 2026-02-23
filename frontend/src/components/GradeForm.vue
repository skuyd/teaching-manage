<template>
  <el-card class="grade-form-card">
    <template #header>
      <div class="card-header">
        <span>{{ isEdit ? '修改评分' : '评分' }}</span>
      </div>
    </template>

    <el-form :model="formData" :rules="rules" ref="formRef" label-width="80px">
      <!-- Grade Level Selection -->
      <el-form-item label="评分等级" prop="grade">
        <el-radio-group v-model="formData.grade" class="grade-radio-group">
          <el-radio-button :label="GradeLevel.A">
            <div class="grade-option">
              <span class="grade-label">A</span>
              <span class="grade-desc">优秀</span>
            </div>
          </el-radio-button>
          <el-radio-button :label="GradeLevel.B">
            <div class="grade-option">
              <span class="grade-label">B</span>
              <span class="grade-desc">良好</span>
            </div>
          </el-radio-button>
          <el-radio-button :label="GradeLevel.C">
            <div class="grade-option">
              <span class="grade-label">C</span>
              <span class="grade-desc">及格</span>
            </div>
          </el-radio-button>
          <el-radio-button :label="GradeLevel.D">
            <div class="grade-option">
              <span class="grade-label">D</span>
              <span class="grade-desc">不及格</span>
            </div>
          </el-radio-button>
        </el-radio-group>
      </el-form-item>

      <!-- Quick Comment Templates -->
      <el-form-item label="快捷评语">
        <el-space wrap>
          <el-tag
            v-for="template in quickComments"
            :key="template"
            class="comment-tag"
            @click="appendComment(template)"
            style="cursor: pointer"
          >
            {{ template }}
          </el-tag>
        </el-space>
      </el-form-item>

      <!-- Comment Input -->
      <el-form-item label="评语">
        <el-input
          v-model="formData.comment"
          type="textarea"
          :rows="6"
          placeholder="请输入评语（可选）"
          maxlength="2000"
          show-word-limit
        />
      </el-form-item>

      <!-- Action Buttons -->
      <el-form-item>
        <el-button type="primary" @click="handleSubmit" :loading="loading">
          {{ isEdit ? '更新' : '提交' }}
        </el-button>
        <el-button @click="handleCancel">取消</el-button>
      </el-form-item>
    </el-form>
  </el-card>
</template>

<script setup lang="ts">
import { ref, reactive, computed } from 'vue'
import { ElMessage, type FormInstance, type FormRules } from 'element-plus'
import { GradeLevel, type CreateGradeRequest, type UpdateGradeRequest } from '@/api/grade'

/**
 * Props
 */
interface Props {
  submissionId?: number
  existingGrade?: {
    id: number
    grade: GradeLevel
    comment: string | null
  }
}

const props = defineProps<Props>()

/**
 * Emits
 */
const emit = defineEmits<{
  (e: 'submit', data: CreateGradeRequest | UpdateGradeRequest): void
  (e: 'cancel'): void
}>()

/**
 * Refs
 */
const formRef = ref<FormInstance>()
const loading = ref(false)

/**
 * Computed
 */
const isEdit = computed(() => !!props.existingGrade)

/**
 * Form data
 */
const formData = reactive<{
  grade: GradeLevel | null
  comment: string
}>({
  grade: props.existingGrade?.grade || null,
  comment: props.existingGrade?.comment || ''
})

/**
 * Form validation rules
 */
const rules: FormRules = {
  grade: [
    { required: true, message: '请选择评分等级', trigger: 'change' }
  ]
}

/**
 * Quick comment templates
 */
const quickComments = [
  '代码结构清晰，逻辑严谨',
  '功能实现完整',
  '注释详细，易于理解',
  '代码规范，符合最佳实践',
  '有创新性的解决方案',
  '测试用例完善',
  '存在语法错误',
  '逻辑存在问题',
  '缺少必要的注释',
  '代码可读性待提高',
  '需要添加边界条件处理',
  '建议使用更高效的算法'
]

/**
 * Append quick comment to textarea
 */
const appendComment = (template: string) => {
  if (formData.comment) {
    formData.comment += '\n' + template
  } else {
    formData.comment = template
  }
}

/**
 * Handle form submit
 */
const handleSubmit = async () => {
  if (!formRef.value) return

  await formRef.value.validate((valid) => {
    if (valid) {
      if (isEdit.value) {
        // Update existing grade
        const updateData: UpdateGradeRequest = {
          grade: formData.grade!,
          comment: formData.comment || undefined
        }
        emit('submit', updateData)
      } else {
        // Create new grade
        if (!props.submissionId) {
          ElMessage.error('缺少提交ID')
          return
        }
        const createData: CreateGradeRequest = {
          submissionId: props.submissionId,
          grade: formData.grade!,
          comment: formData.comment || undefined
        }
        emit('submit', createData)
      }
    } else {
      ElMessage.warning('请填写必填项')
    }
  })
}

/**
 * Handle cancel
 */
const handleCancel = () => {
  emit('cancel')
}
</script>

<style scoped lang="scss">
.grade-form-card {
  max-width: 800px;
  margin: 20px auto;

  .card-header {
    font-size: 18px;
    font-weight: 600;
  }

  .grade-radio-group {
    width: 100%;

    :deep(.el-radio-button) {
      flex: 1;
      margin-right: 12px;

      &:last-child {
        margin-right: 0;
      }

      .el-radio-button__inner {
        width: 100%;
        padding: 12px 20px;
      }
    }

    .grade-option {
      display: flex;
      flex-direction: column;
      align-items: center;
      gap: 4px;

      .grade-label {
        font-size: 24px;
        font-weight: 700;
      }

      .grade-desc {
        font-size: 12px;
        color: var(--el-text-color-secondary);
      }
    }

    :deep(.el-radio-button__original-radio:checked + .el-radio-button__inner) {
      .grade-option {
        .grade-label {
          color: var(--el-color-white);
        }

        .grade-desc {
          color: var(--el-color-white);
          opacity: 0.8;
        }
      }
    }
  }

  .comment-tag {
    transition: all 0.3s;

    &:hover {
      transform: scale(1.05);
      box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
    }
  }
}

@media (max-width: 768px) {
  .grade-form-card {
    margin: 10px;

    .grade-radio-group {
      :deep(.el-radio-button) {
        margin-right: 8px;
        margin-bottom: 8px;
      }

      .grade-option {
        .grade-label {
          font-size: 20px;
        }
      }
    }
  }
}
</style>
