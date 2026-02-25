<template>
  <div class="file-tree">
    <!-- Search bar -->
    <div v-if="searchable" class="file-tree__search">
      <el-input
        v-model="searchQuery"
        placeholder="搜索文件..."
        :prefix-icon="Search"
        clearable
        size="small"
      />
    </div>

    <!-- Tree content -->
    <div class="file-tree__content">
      <template v-if="filteredNodes.length > 0">
        <FileTreeNode
          v-for="node in filteredNodes"
          :key="node.path"
          :node="node"
          :selected-path="selectedPath"
          :expanded-paths="expandedPathsSet"
          :search-query="searchQuery"
          :depth="0"
          @select="handleSelect"
          @toggle="handleToggle"
        />
      </template>
      <div v-else class="file-tree__empty">
        <el-icon :size="32"><FolderDelete /></el-icon>
        <p>{{ searchQuery ? '未找到匹配的文件' : '暂无文件' }}</p>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, watch, onMounted, defineAsyncComponent } from 'vue'
import { Search, FolderDelete } from '@element-plus/icons-vue'
import type { FileTreeNode as FileTreeNodeType } from '@/api/submission'

// Recursive component
const FileTreeNode = defineAsyncComponent(() => import('./FileTreeNode.vue'))

// Props
interface Props {
  files: FileTreeNodeType[]
  selectedPath?: string
  searchable?: boolean
  persistKey?: string  // Key for sessionStorage persistence
}

const props = withDefaults(defineProps<Props>(), {
  selectedPath: '',
  searchable: false,
  persistKey: ''
})

// Emits
const emit = defineEmits<{
  select: [node: FileTreeNodeType]
}>()

// State
const searchQuery = ref('')
const expandedPathsSet = ref<Set<string>>(new Set())

// Load persisted state
onMounted(() => {
  if (props.persistKey) {
    const saved = sessionStorage.getItem(`file-tree-${props.persistKey}`)
    if (saved) {
      try {
        const paths = JSON.parse(saved) as string[]
        expandedPathsSet.value = new Set(paths)
      } catch (e) {
        console.error('Failed to load file tree state:', e)
      }
    }
  }
})

// Save state on change
watch(expandedPathsSet, (newPaths) => {
  if (props.persistKey) {
    sessionStorage.setItem(
      `file-tree-${props.persistKey}`,
      JSON.stringify([...newPaths])
    )
  }
}, { deep: true })

// Filter nodes based on search query
const filteredNodes = computed(() => {
  if (!searchQuery.value) {
    return props.files
  }
  return filterTree(props.files, searchQuery.value.toLowerCase())
})

// Recursive filter function
const filterTree = (nodes: FileTreeNodeType[], query: string): FileTreeNodeType[] => {
  const result: FileTreeNodeType[] = []

  for (const node of nodes) {
    const nameMatches = node.name.toLowerCase().includes(query)

    if (node.isDirectory && node.children) {
      const filteredChildren = filterTree(node.children, query)
      if (filteredChildren.length > 0 || nameMatches) {
        result.push({
          ...node,
          children: filteredChildren.length > 0 ? filteredChildren : node.children
        })
        // Auto-expand matching folders
        if (filteredChildren.length > 0) {
          expandedPathsSet.value.add(node.path)
        }
      }
    } else if (nameMatches) {
      result.push(node)
    }
  }

  return result
}

// Event handlers
const handleSelect = (node: FileTreeNodeType) => {
  emit('select', node)
}

const handleToggle = (path: string) => {
  if (expandedPathsSet.value.has(path)) {
    expandedPathsSet.value.delete(path)
  } else {
    expandedPathsSet.value.add(path)
  }
  // Trigger reactivity
  expandedPathsSet.value = new Set(expandedPathsSet.value)
}
</script>

<style scoped>
.file-tree {
  height: 100%;
  display: flex;
  flex-direction: column;
  background: var(--bg-secondary);
  border-radius: var(--radius-md);
  overflow: hidden;
}

.file-tree__search {
  padding: var(--spacing-sm);
  border-bottom: 1px solid var(--border-light);
}

.file-tree__search :deep(.el-input__wrapper) {
  background: var(--bg-primary);
  border-color: var(--border-default);
}

.file-tree__content {
  flex: 1;
  overflow-y: auto;
  padding: var(--spacing-xs);
}

.file-tree__empty {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: var(--spacing-2xl);
  color: var(--text-muted);
}

.file-tree__empty p {
  margin-top: var(--spacing-sm);
  font-size: var(--text-sm);
}
</style>
