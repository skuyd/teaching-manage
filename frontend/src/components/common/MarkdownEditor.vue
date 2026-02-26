<template>
  <div class="markdown-editor" :class="{ 'is-disabled': disabled }">
    <div ref="editorRef" :id="editorId"></div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onBeforeUnmount, watch, nextTick } from 'vue'
import Vditor from 'vditor'
import 'vditor/dist/index.css'

interface Props {
  modelValue?: string
  placeholder?: string
  height?: number | string
  disabled?: boolean
  uploadUrl?: string
  minHeight?: number
  mode?: 'wysiwyg' | 'ir' | 'sv'
  minimal?: boolean
}

const props = withDefaults(defineProps<Props>(), {
  modelValue: '',
  placeholder: '请输入内容，支持 Markdown 格式...',
  height: 300,
  disabled: false,
  uploadUrl: '/api/files/image',
  minHeight: 200,
  mode: 'ir',
  minimal: false
})

// 完整工具栏
const fullToolbar = [
  'headings',
  'bold',
  'italic',
  'strike',
  '|',
  'list',
  'ordered-list',
  'check',
  '|',
  'quote',
  'code',
  'inline-code',
  '|',
  'link',
  'upload',
  'table',
  '|',
  'undo',
  'redo',
  '|',
  'preview',
  'fullscreen',
  {
    name: 'more',
    toolbar: ['export', 'outline', 'br', 'both', 'edit-mode']
  }
]

// 简化工具栏：仅代码/预览模式切换
const minimalToolbar = [
  'edit-mode',
  'preview',
  'fullscreen'
]

const emit = defineEmits<{
  (e: 'update:modelValue', value: string): void
  (e: 'change', value: string): void
  (e: 'focus'): void
  (e: 'blur'): void
}>()

const editorRef = ref<HTMLElement>()
const editorId = `vditor-${Date.now()}-${Math.random().toString(36).slice(2, 9)}`
let vditor: Vditor | null = null
let isSettingValue = false

const getToken = () => {
  return localStorage.getItem('token') || ''
}

const initEditor = () => {
  if (!editorRef.value) return

  vditor = new Vditor(editorId, {
    height: typeof props.height === 'number' ? props.height : parseInt(props.height),
    minHeight: props.minHeight,
    mode: props.mode,
    placeholder: props.placeholder,
    value: props.modelValue,
    theme: 'classic',
    icon: 'material',
    cache: {
      enable: false
    },
    toolbar: props.minimal ? minimalToolbar : fullToolbar,
    upload: {
      url: props.uploadUrl,
      fieldName: 'file',
      headers: {
        'Authorization': `Bearer ${getToken()}`
      },
      accept: 'image/*',
      max: 10 * 1024 * 1024,
      format: (_files: File[], responseText: string) => {
        try {
          const res = JSON.parse(responseText)
          if (res.code === 200 && res.data) {
            return JSON.stringify({
              msg: '',
              code: 0,
              data: {
                errFiles: [],
                succMap: {
                  [res.data.originalName || 'image']: res.data.url
                }
              }
            })
          }
          return JSON.stringify({
            msg: res.message || '上传失败',
            code: -1,
            data: { errFiles: [], succMap: {} }
          })
        } catch {
          return JSON.stringify({
            msg: '上传失败',
            code: -1,
            data: { errFiles: [], succMap: {} }
          })
        }
      }
    },
    preview: {
      hljs: {
        enable: true,
        style: 'github',
        lineNumber: true
      },
      markdown: {
        toc: true,
        autoSpace: true
      }
    },
    hint: {
      emoji: {
        '+1': '👍',
        '-1': '👎',
        'heart': '❤️',
        'star': '⭐',
        'fire': '🔥',
        'check': '✅',
        'x': '❌',
        'warning': '⚠️',
        'info': 'ℹ️',
        'question': '❓'
      }
    },
    after: () => {
      if (props.disabled && vditor) {
        vditor.disabled()
      }
    },
    input: (value: string) => {
      if (!isSettingValue) {
        emit('update:modelValue', value)
        emit('change', value)
      }
    },
    focus: () => {
      emit('focus')
    },
    blur: () => {
      emit('blur')
    }
  })
}

watch(() => props.modelValue, (newVal) => {
  if (vditor && newVal !== vditor.getValue()) {
    isSettingValue = true
    vditor.setValue(newVal)
    nextTick(() => {
      isSettingValue = false
    })
  }
})

watch(() => props.disabled, (newVal) => {
  if (vditor) {
    if (newVal) {
      vditor.disabled()
    } else {
      vditor.enable()
    }
  }
})

onMounted(() => {
  initEditor()
})

onBeforeUnmount(() => {
  if (vditor) {
    vditor.destroy()
    vditor = null
  }
})

defineExpose({
  getVditor: () => vditor,
  getValue: () => vditor?.getValue() || '',
  setValue: (value: string) => vditor?.setValue(value),
  focus: () => vditor?.focus(),
  blur: () => vditor?.blur(),
  disabled: () => vditor?.disabled(),
  enable: () => vditor?.enable()
})
</script>

<style scoped>
.markdown-editor {
  width: 100%;
}

.markdown-editor.is-disabled {
  opacity: 0.6;
  pointer-events: none;
}

:deep(.vditor) {
  border-radius: 4px;
  border-color: var(--el-border-color);
}

:deep(.vditor--fullscreen) {
  z-index: 9999;
}

:deep(.vditor-toolbar) {
  background: var(--bg-secondary, #f5f7fa);
  border-bottom-color: var(--el-border-color);
}

:deep(.vditor-content) {
  background: var(--bg-primary, #fff);
}

:deep(.vditor-ir pre.vditor-reset) {
  background: var(--bg-primary, #fff);
  color: var(--text-primary, #303133);
}

:deep(.vditor-preview) {
  background: var(--bg-primary, #fff);
}
</style>
