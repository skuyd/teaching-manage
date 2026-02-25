<template>
  <div class="tree-node">
    <div
      class="node-content"
      :class="{
        'is-directory': node.isDirectory,
        'is-active': selectedPath === node.path
      }"
      :style="{ paddingLeft: `${depth * 16 + 8}px` }"
      @click="handleClick"
    >
      <!-- Expand/Collapse icon for directories -->
      <el-icon
        v-if="node.isDirectory"
        class="expand-icon"
        :class="{ 'is-expanded': isExpanded }"
      >
        <CaretRight />
      </el-icon>
      <span v-else class="expand-placeholder"></span>

      <!-- File/Folder icon -->
      <el-icon class="node-icon" :class="iconClass">
        <component :is="iconComponent" />
      </el-icon>

      <!-- File name with search highlight -->
      <span class="node-label" v-html="highlightedName"></span>
    </div>

    <!-- Children (recursive) -->
    <template v-if="node.isDirectory && node.children && isExpanded">
      <FileTreeNode
        v-for="child in node.children"
        :key="child.path"
        :node="child"
        :selected-path="selectedPath"
        :expanded-paths="expandedPaths"
        :search-query="searchQuery"
        :depth="depth + 1"
        @select="$emit('select', $event)"
        @toggle="$emit('toggle', $event)"
      />
    </template>
  </div>
</template>

<script setup lang="ts">
import { computed, defineAsyncComponent } from 'vue'
import {
  CaretRight,
  Folder,
  FolderOpened,
  Document,
  Picture,
  VideoPlay,
  Headset,
  Setting,
  DataLine
} from '@element-plus/icons-vue'
import type { FileTreeNode as FileTreeNodeType } from '@/api/submission'

// Recursive component reference
const FileTreeNode = defineAsyncComponent(() => import('./FileTreeNode.vue'))

// Props
interface Props {
  node: FileTreeNodeType
  selectedPath: string
  expandedPaths: Set<string>
  searchQuery: string
  depth: number
}

const props = defineProps<Props>()

// Computed
const isExpanded = computed(() => props.expandedPaths.has(props.node.path))

// Get file extension
const fileExtension = computed(() => {
  if (props.node.isDirectory) return ''
  const parts = props.node.name.split('.')
  return parts.length > 1 ? parts.pop()?.toLowerCase() || '' : ''
})

// Icon component based on file type
const iconComponent = computed(() => {
  if (props.node.isDirectory) {
    return isExpanded.value ? FolderOpened : Folder
  }

  const ext = fileExtension.value

  // Images
  if (['png', 'jpg', 'jpeg', 'gif', 'svg', 'webp', 'ico', 'bmp'].includes(ext)) {
    return Picture
  }

  // Videos
  if (['mp4', 'avi', 'mov', 'wmv', 'mkv', 'webm'].includes(ext)) {
    return VideoPlay
  }

  // Audio
  if (['mp3', 'wav', 'ogg', 'flac', 'aac'].includes(ext)) {
    return Headset
  }

  // Config files
  if (['json', 'yaml', 'yml', 'xml', 'toml', 'ini', 'env', 'config'].includes(ext)) {
    return Setting
  }

  // Data files
  if (['csv', 'sql', 'db', 'sqlite'].includes(ext)) {
    return DataLine
  }

  // Default document
  return Document
})

// Icon color class based on file type
const iconClass = computed(() => {
  if (props.node.isDirectory) {
    return 'icon-folder'
  }

  const ext = fileExtension.value

  // Code files
  if (['js', 'ts', 'jsx', 'tsx', 'mjs', 'cjs'].includes(ext)) {
    return 'icon-javascript'
  }
  if (['vue'].includes(ext)) {
    return 'icon-vue'
  }
  if (['java', 'class', 'jar'].includes(ext)) {
    return 'icon-java'
  }
  if (['py', 'pyw', 'pyc'].includes(ext)) {
    return 'icon-python'
  }
  if (['html', 'htm'].includes(ext)) {
    return 'icon-html'
  }
  if (['css', 'scss', 'sass', 'less'].includes(ext)) {
    return 'icon-css'
  }
  if (['json'].includes(ext)) {
    return 'icon-json'
  }
  if (['md', 'markdown'].includes(ext)) {
    return 'icon-markdown'
  }

  return 'icon-default'
})

// Highlight search matches in file name
const highlightedName = computed(() => {
  if (!props.searchQuery) {
    return props.node.name
  }

  const regex = new RegExp(`(${escapeRegExp(props.searchQuery)})`, 'gi')
  return props.node.name.replace(regex, '<mark class="highlight">$1</mark>')
})

// Escape special regex characters
const escapeRegExp = (str: string) => {
  return str.replace(/[.*+?^${}()|[\]\\]/g, '\\$&')
}

// Event handlers
const emit = defineEmits<{
  select: [node: FileTreeNodeType]
  toggle: [path: string]
}>()

const handleClick = () => {
  if (props.node.isDirectory) {
    emit('toggle', props.node.path)
  } else {
    emit('select', props.node)
  }
}
</script>

<style scoped>
.tree-node {
  user-select: none;
}

.node-content {
  display: flex;
  align-items: center;
  padding: var(--spacing-xs) var(--spacing-sm);
  cursor: pointer;
  border-radius: var(--radius-sm);
  transition: background var(--transition-fast);
}

.node-content:hover {
  background: var(--bg-hover);
}

.node-content.is-active {
  background: color-mix(in srgb, var(--color-primary) 15%, transparent);
  color: var(--color-primary);
}

.expand-icon {
  width: 16px;
  height: 16px;
  margin-right: var(--spacing-xs);
  color: var(--text-muted);
  transition: transform var(--transition-fast);
  flex-shrink: 0;
}

.expand-icon.is-expanded {
  transform: rotate(90deg);
}

.expand-placeholder {
  width: 16px;
  margin-right: var(--spacing-xs);
  flex-shrink: 0;
}

.node-icon {
  width: 16px;
  height: 16px;
  margin-right: var(--spacing-sm);
  flex-shrink: 0;
}

/* Icon colors */
.icon-folder {
  color: var(--color-warning);
}

.icon-javascript {
  color: #f7df1e;
}

.icon-vue {
  color: #42b883;
}

.icon-java {
  color: #e76f00;
}

.icon-python {
  color: #3776ab;
}

.icon-html {
  color: #e34c26;
}

.icon-css {
  color: #264de4;
}

.icon-json {
  color: #cbcb41;
}

.icon-markdown {
  color: #083fa1;
}

.icon-default {
  color: var(--text-secondary);
}

.node-label {
  font-size: var(--text-sm);
  color: var(--text-primary);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  flex: 1;
  min-width: 0;
}

.node-content.is-active .node-label {
  font-weight: var(--font-weight-medium);
}

/* Search highlight */
:deep(.highlight) {
  background: color-mix(in srgb, var(--color-warning) 40%, transparent);
  color: inherit;
  padding: 0 2px;
  border-radius: 2px;
}
</style>
