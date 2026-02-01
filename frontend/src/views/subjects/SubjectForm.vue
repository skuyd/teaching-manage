<template>
  <el-dialog
    :model-value="modelValue"
    :title="isEdit ? '编辑学科' : '新增学科'"
    width="600px"
    @close="handleClose"
  >
    <el-form
      ref="formRef"
      :model="form"
      :rules="rules"
      label-width="100px"
    >
      <el-form-item label="学科名称" prop="name">
        <el-input v-model="form.name" placeholder="请输入学科名称" />
      </el-form-item>

      <el-form-item label="学科描述" prop="description">
        <el-input
          v-model="form.description"
          type="textarea"
          :rows="3"
          placeholder="请输入学科描述"
        />
      </el-form-item>

      <el-form-item label="是否分组" prop="isGrouped">
        <el-switch v-model="form.isGrouped" />
      </el-form-item>

      <template v-if="form.isGrouped">
        <el-form-item label="小组人数">
          <el-col :span="11">
            <el-form-item prop="minMembers">
              <el-input-number
                v-model="form.minMembers"
                :min="1"
                :max="form.maxMembers"
                placeholder="最小人数"
              />
            </el-form-item>
          </el-col>
          <el-col :span="2" class="text-center">~</el-col>
          <el-col :span="11">
            <el-form-item prop="maxMembers">
              <el-input-number
                v-model="form.maxMembers"
                :min="form.minMembers"
                placeholder="最大人数"
              />
            </el-form-item>
          </el-col>
        </el-form-item>
      </template>

      <el-form-item label="培训时间">
        <el-date-picker
          v-model="dateRange"
          type="daterange"
          start-placeholder="开始日期"
          end-placeholder="结束日期"
          format="YYYY-MM-DD"
          value-format="YYYY-MM-DD"
        />
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
import { createSubject, updateSubject, type SubjectDTO } from '@/api/subject'

const props = defineProps<{
  modelValue: boolean
  subject: SubjectDTO | null
}>()

const emit = defineEmits<{
  'update:modelValue': [value: boolean]
  success: []
}>()

const formRef = ref<FormInstance>()
const loading = ref(false)

const form = reactive({
  name: '',
  description: '',
  isGrouped: false,
  minMembers: 1,
  maxMembers: 1
})

const dateRange = ref<[string, string] | null>(null)

const isEdit = computed(() => !!props.subject?.id)

const rules: FormRules = {
  name: [
    { required: true, message: '请输入学科名称', trigger: 'blur' }
  ],
  minMembers: [
    { type: 'number', min: 1, message: '最小人数不能小于1', trigger: 'change' }
  ],
  maxMembers: [
    { type: 'number', min: 1, message: '最大人数不能小于1', trigger: 'change' }
  ]
}

watch(() => props.subject, (subject) => {
  if (subject) {
    form.name = subject.name
    form.description = subject.description
    form.isGrouped = subject.isGrouped
    form.minMembers = subject.minMembers
    form.maxMembers = subject.maxMembers
    if (subject.startDate && subject.endDate) {
      dateRange.value = [subject.startDate, subject.endDate]
    } else {
      dateRange.value = null
    }
  } else {
    resetForm()
  }
}, { immediate: true })

const resetForm = () => {
  form.name = ''
  form.description = ''
  form.isGrouped = false
  form.minMembers = 1
  form.maxMembers = 1
  dateRange.value = null
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
        name: form.name,
        description: form.description,
        isGrouped: form.isGrouped,
        minMembers: form.isGrouped ? form.minMembers : 1,
        maxMembers: form.isGrouped ? form.maxMembers : 1,
        startDate: dateRange.value?.[0],
        endDate: dateRange.value?.[1]
      }

      if (isEdit.value && props.subject) {
        await updateSubject(props.subject.id, data)
        ElMessage.success('更新成功')
      } else {
        await createSubject(data)
        ElMessage.success('创建成功')
      }

      emit('success')
    } finally {
      loading.value = false
    }
  })
}
</script>

<style scoped>
.text-center {
  text-align: center;
  line-height: 32px;
}
</style>
