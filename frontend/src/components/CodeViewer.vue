<template>
  <div class="code-viewer" ref="containerRef">
    <div v-if="loading" class="code-viewer__loading">
      <el-icon class="is-loading" :size="32"><Loading /></el-icon>
      <span>加载编辑器中...</span>
    </div>
    <div v-show="!loading" ref="editorRef" class="code-viewer__editor"></div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted, watch, shallowRef, computed } from 'vue'
import { Loading } from '@element-plus/icons-vue'
import { useThemeStore } from '@/stores/theme'
import type * as Monaco from 'monaco-editor'

// Props
interface Props {
  code: string
  language?: string
  readonly?: boolean
  minimap?: boolean
  lineNumbers?: 'on' | 'off' | 'relative'
  wordWrap?: 'on' | 'off' | 'wordWrapColumn' | 'bounded'
  fontSize?: number
}

const props = withDefaults(defineProps<Props>(), {
  language: 'plaintext',
  readonly: true,
  minimap: false,
  lineNumbers: 'on',
  wordWrap: 'off',
  fontSize: 14
})

// Emits
const emit = defineEmits<{
  change: [value: string]
  ready: []
}>()

// Refs
const editorRef = ref<HTMLElement | null>(null)
const loading = ref(true)

// Monaco instance (shallow ref to avoid reactivity issues)
const editor = shallowRef<Monaco.editor.IStandaloneCodeEditor | null>(null)
const monaco = shallowRef<typeof Monaco | null>(null)

// Theme store
const themeStore = useThemeStore()

// Compute Monaco theme based on app theme
const monacoTheme = computed(() => {
  // tech-blue uses dark theme, others use light
  return themeStore.isTechBlue ? 'vs-dark' : 'vs'
})

// Initialize Monaco editor
const initEditor = async () => {
  if (!editorRef.value) return

  try {
    // Dynamic import for code splitting
    const monacoModule = await import('monaco-editor')
    monaco.value = monacoModule

    // Create editor instance
    editor.value = monacoModule.editor.create(editorRef.value, {
      value: props.code,
      language: props.language,
      theme: monacoTheme.value,
      readOnly: props.readonly,
      minimap: { enabled: props.minimap },
      lineNumbers: props.lineNumbers,
      wordWrap: props.wordWrap,
      fontSize: props.fontSize,
      automaticLayout: true,
      scrollBeyondLastLine: false,
      renderLineHighlight: 'line',
      folding: true,
      glyphMargin: false,
      contextmenu: true,
      quickSuggestions: false,
      snippetSuggestions: 'none',
      formatOnPaste: true,
      formatOnType: true
    })

    // Listen for content changes
    editor.value.onDidChangeModelContent(() => {
      const value = editor.value?.getValue() || ''
      emit('change', value)
    })

    loading.value = false
    emit('ready')
  } catch (error) {
    console.error('Failed to initialize Monaco editor:', error)
    loading.value = false
  }
}

// Update editor options
const updateEditorOptions = () => {
  if (!editor.value) return

  editor.value.updateOptions({
    theme: monacoTheme.value,
    readOnly: props.readonly,
    minimap: { enabled: props.minimap },
    lineNumbers: props.lineNumbers,
    wordWrap: props.wordWrap,
    fontSize: props.fontSize
  })
}

// Update code content
const updateCode = () => {
  if (!editor.value || !monaco.value) return

  const model = editor.value.getModel()
  if (model) {
    const currentValue = model.getValue()
    if (currentValue !== props.code) {
      model.setValue(props.code)
    }
  }
}

// Update language
const updateLanguage = () => {
  if (!editor.value || !monaco.value) return

  const model = editor.value.getModel()
  if (model) {
    monaco.value.editor.setModelLanguage(model, props.language)
  }
}

// Watch for theme changes
watch(() => themeStore.currentTheme, () => {
  if (editor.value) {
    editor.value.updateOptions({ theme: monacoTheme.value })
  }
})

// Watch for code changes
watch(() => props.code, updateCode)

// Watch for language changes
watch(() => props.language, updateLanguage)

// Watch for option changes
watch(
  () => [props.readonly, props.minimap, props.lineNumbers, props.wordWrap, props.fontSize],
  updateEditorOptions
)

// Lifecycle
onMounted(() => {
  initEditor()
})

onUnmounted(() => {
  if (editor.value) {
    editor.value.dispose()
    editor.value = null
  }
})

// Expose methods
defineExpose({
  /**
   * Get current code value
   */
  getValue: () => editor.value?.getValue() || '',

  /**
   * Set code value
   */
  setValue: (value: string) => {
    if (editor.value) {
      editor.value.setValue(value)
    }
  },

  /**
   * Focus the editor
   */
  focus: () => {
    editor.value?.focus()
  },

  /**
   * Format document
   */
  format: () => {
    editor.value?.getAction('editor.action.formatDocument')?.run()
  },

  /**
   * Get Monaco editor instance
   */
  getEditor: () => editor.value
})
</script>

<style scoped>
.code-viewer {
  width: 100%;
  height: 100%;
  min-height: 200px;
  position: relative;
  background: var(--bg-secondary);
  border: 1px solid var(--border-default);
  border-radius: var(--radius-md);
  overflow: hidden;
}

.code-viewer__loading {
  position: absolute;
  inset: 0;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: var(--spacing-sm);
  background: var(--bg-secondary);
  color: var(--text-muted);
  font-size: var(--text-sm);
}

.code-viewer__editor {
  width: 100%;
  height: 100%;
}

/* Override Monaco editor styles for theme integration */
:deep(.monaco-editor) {
  border-radius: var(--radius-md);
}

:deep(.monaco-editor .margin) {
  background: transparent !important;
}

:deep(.monaco-editor .monaco-editor-background) {
  background: var(--bg-secondary) !important;
}
</style>
