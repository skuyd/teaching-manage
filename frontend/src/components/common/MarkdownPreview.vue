<template>
  <div class="markdown-preview" :class="containerClass">
    <div
      ref="previewRef"
      class="vditor-reset markdown-body"
      v-html="renderedContent"
    ></div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, watch, onMounted } from 'vue'
import Vditor from 'vditor'
import 'vditor/dist/index.css'

interface Props {
  content?: string
  theme?: 'light' | 'dark' | 'classic'
  codeTheme?: string
  maxHeight?: number | string
  emptyText?: string
}

const props = withDefaults(defineProps<Props>(), {
  content: '',
  theme: 'classic',
  codeTheme: 'github',
  maxHeight: undefined,
  emptyText: '暂无内容'
})

const renderedContent = ref('')

const containerClass = computed(() => ({
  'has-max-height': !!props.maxHeight
}))

const renderMarkdown = async (markdown: string) => {
  if (!markdown || markdown.trim() === '') {
    renderedContent.value = `<p class="empty-text">${props.emptyText}</p>`
    return
  }

  try {
    const html = await Vditor.md2html(markdown, {
      mode: 'light',
      hljs: {
        enable: true,
        style: props.codeTheme,
        lineNumber: true
      },
      markdown: {
        autoSpace: true,
        toc: false,
        sanitize: true
      },
      anchor: 0,
      lazyLoadImage: '/images/loading.svg'
    })
    renderedContent.value = html
  } catch (error) {
    console.error('Markdown render error:', error)
    renderedContent.value = `<pre>${escapeHtml(markdown)}</pre>`
  }
}

const escapeHtml = (str: string): string => {
  const htmlEntities: Record<string, string> = {
    '&': '&amp;',
    '<': '&lt;',
    '>': '&gt;',
    '"': '&quot;',
    "'": '&#39;'
  }
  return str.replace(/[&<>"']/g, char => htmlEntities[char])
}

watch(() => props.content, (newVal) => {
  renderMarkdown(newVal)
}, { immediate: true })

onMounted(() => {
  renderMarkdown(props.content)
})

defineExpose({
  refresh: () => renderMarkdown(props.content)
})
</script>

<style scoped>
.markdown-preview {
  width: 100%;
  padding: 16px;
  background: var(--bg-primary, #fff);
  border-radius: 4px;
  overflow: auto;
}

.markdown-preview.has-max-height {
  max-height: v-bind("typeof maxHeight === 'number' ? maxHeight + 'px' : maxHeight");
}

.markdown-body {
  color: var(--text-primary, #303133);
  font-size: 14px;
  line-height: 1.8;
}

:deep(.empty-text) {
  color: var(--text-placeholder, #a8abb2);
  text-align: center;
  padding: 24px 0;
}

:deep(h1),
:deep(h2),
:deep(h3),
:deep(h4),
:deep(h5),
:deep(h6) {
  margin-top: 24px;
  margin-bottom: 16px;
  font-weight: 600;
  line-height: 1.25;
  color: var(--text-primary, #303133);
}

:deep(h1) { font-size: 2em; border-bottom: 1px solid var(--border-default, #dcdfe6); padding-bottom: 0.3em; }
:deep(h2) { font-size: 1.5em; border-bottom: 1px solid var(--border-default, #dcdfe6); padding-bottom: 0.3em; }
:deep(h3) { font-size: 1.25em; }
:deep(h4) { font-size: 1em; }
:deep(h5) { font-size: 0.875em; }
:deep(h6) { font-size: 0.85em; color: var(--text-secondary, #606266); }

:deep(p) {
  margin-top: 0;
  margin-bottom: 16px;
}

:deep(ul),
:deep(ol) {
  padding-left: 2em;
  margin-bottom: 16px;
}

:deep(li) {
  margin-bottom: 4px;
}

:deep(li + li) {
  margin-top: 4px;
}

:deep(blockquote) {
  margin: 0 0 16px 0;
  padding: 0 1em;
  color: var(--text-secondary, #606266);
  border-left: 4px solid var(--border-default, #dcdfe6);
}

:deep(code) {
  padding: 0.2em 0.4em;
  margin: 0;
  font-size: 85%;
  background: var(--bg-secondary, #f5f7fa);
  border-radius: 3px;
  font-family: 'SFMono-Regular', Consolas, 'Liberation Mono', Menlo, monospace;
}

:deep(pre) {
  padding: 16px;
  overflow: auto;
  font-size: 85%;
  line-height: 1.45;
  background: var(--bg-secondary, #f5f7fa);
  border-radius: 4px;
  margin-bottom: 16px;
}

:deep(pre code) {
  padding: 0;
  margin: 0;
  font-size: 100%;
  background: transparent;
  border: 0;
}

:deep(table) {
  width: 100%;
  margin-bottom: 16px;
  border-collapse: collapse;
  border-spacing: 0;
}

:deep(table th),
:deep(table td) {
  padding: 8px 16px;
  border: 1px solid var(--border-default, #dcdfe6);
}

:deep(table th) {
  font-weight: 600;
  background: var(--bg-secondary, #f5f7fa);
}

:deep(table tr:nth-child(2n)) {
  background: var(--bg-secondary, #fafafa);
}

:deep(a) {
  color: var(--color-primary, #409eff);
  text-decoration: none;
}

:deep(a:hover) {
  text-decoration: underline;
}

:deep(img) {
  max-width: 100%;
  height: auto;
  border-radius: 4px;
}

:deep(hr) {
  height: 0.25em;
  padding: 0;
  margin: 24px 0;
  background: var(--border-default, #dcdfe6);
  border: 0;
}

:deep(.vditor-linenumber) {
  font-size: 12px;
}
</style>
