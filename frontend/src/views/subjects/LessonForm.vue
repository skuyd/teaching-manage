<template>
  <el-dialog
    :model-value="modelValue"
    :title="isEdit ? '编辑课程' : '新增课程'"
    width="700px"
    @close="handleClose"
  >
    <el-form
      ref="formRef"
      :model="form"
      :rules="rules"
      label-width="100px"
    >
      <el-form-item label="课程标题" prop="title">
        <el-input v-model="form.title" placeholder="请输入课程标题" />
      </el-form-item>

      <el-form-item label="课程内容" prop="content">
        <el-input
          v-model="form.content"
          type="textarea"
          :rows="5"
          placeholder="支持Markdown格式"
        />
      </el-form-item>

      <el-form-item label="上课时间" prop="lessonTime">
        <el-date-picker
          v-model="form.lessonTime"
          type="datetime"
          placeholder="选择上课时间"
          value-format="YYYY-MM-DDTHH:mm:ss"
          style="width: 100%"
        />
      </el-form-item>

      <el-form-item label="作业要求" prop="homeworkDesc">
        <el-input
          v-model="form.homeworkDesc"
          type="textarea"
          :rows="5"
          placeholder="支持Markdown格式"
        />
      </el-form-item>

      <el-form-item label="提交类型" prop="submitType">
        <el-radio-group v-model="form.submitType">
          <el-radio label="PERSONAL">个人提交</el-radio>
          <el-radio label="GROUP">小组提交</el-radio>
        </el-radio-group>
      </el-form-item>

      <el-form-item label="截止时间" prop="deadline">
        <el-date-picker
          v-model="form.deadline"
          type="datetime"
          placeholder="选择截止时间（可选）"
          value-format="YYYY-MM-DDTHH:mm:ss"
          style="width: 100%"
          clearable
        />
      </el-form-item>

      <el-form-item label="允许补交" prop="allowLate">
        <el-switch v-model="form.allowLate" />
      </el-form-item>
    </el-form>

    <template #footer>
      <el-button @click="handleClose">取消</el-button>
      <el-button type="primary" :loading="loading" @click="handleSubmit">
        确定
      </el-button>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { ref, reactive, computed, watch } from 'vue'
import { ElMessage } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import { createLesson, updateLesson, type LessonDTO, SubmitType } from '@/api/lesson'

const props = defineProps<{
  modelValue: boolean
  lesson: LessonDTO | null
  subjectId: number
}>()

const emit = defineEmits<{
  'update:modelValue': [value: boolean]
  success: []
}>()

const formRef = ref<FormInstance>()
const loading = ref(false)

const form = reactive({
  title: '',
  content: '',
  lessonTime: '',
  homeworkDesc: '',
  submitType: 'PERSONAL' as SubmitType,
  deadline: '',
  allowLate: false
})

const isEdit = computed(() => !!props.lesson?.id)

const rules: FormRules = {
  title: [
    { required: true, message: '请输入课程标题', trigger: 'blur' }
  ],
  lessonTime: [
    { required: true, message: '请选择上课时间', trigger: 'change' }
  ],
  submitType: [
    { required: true, message: '请选择提交类型', trigger: 'change' }
  ]
}

watch(() => props.lesson, (lesson) => {
  if (lesson) {
    form.title = lesson.title
    form.content = lesson.content || ''
    form.lessonTime = lesson.lessonTime
    form.homeworkDesc = lesson.homeworkDesc || ''
    form.submitType = lesson.submitType
    form.deadline = lesson.deadline || ''
    form.allowLate = lesson.allowLate
  } else {
    resetForm()
  }
}, { immediate: true })

const resetForm = () => {
  form.title = ''
  form.content = ''
  form.lessonTime = ''
  form.homeworkDesc = ''
  form.submitType = 'PERSONAL' as SubmitType
  form.deadline = ''
  form.allowLate = false
}

const handleClose = () => {
  emit('update:modelValue', false)
  resetForm()
}

const handleSubmit = async () => {
  if (!formRef.value) return

  await formRef.value.validate(async (valid) => {
    if (!valid) return

    loading.value = true
    try {
      const data = {
        subjectId: props.subjectId,
        title: form.title,
        content: form.content || undefined,
        lessonTime: form.lessonTime,
        homeworkDesc: form.homeworkDesc || undefined,
        submitType: form.submitType,
        deadline: form.deadline || undefined,
        allowLate: form.allowLate
      }

      if (isEdit.value && props.lesson) {
        await updateLesson(props.lesson.id, data)
        ElMessage.success('更新成功')
      } else {
        await createLesson(data)
        ElMessage.success('创建成功')
      }

      emit('success')
    } finally {
      loading.value = false
    }
  })
}
</script>
