<template>
  <div class="file-tree">
    <div
      v-for="node in nodes"
      :key="node.path"
      class="tree-node"
    >
      <div
        class="node-content"
        :class="{ 'is-directory': node.isDirectory, 'is-active': selectedPath === node.path }"
        @click="handleNodeClick(node)"
      >
        <el-icon class="node-icon">
          <Folder v-if="node.isDirectory && !expandedPaths.has(node.path)" />
          <FolderOpened v-else-if="node.isDirectory && expandedPaths.has(node.path)" />
          <Document v-else />
        </el-icon>
        <span class="node-label">{{ node.name }}</span>
      </div>
      <div
        v-if="node.isDirectory && node.children && expandedPaths.has(node.path)"
        class="node-children"
      >
        <FileTree
          :nodes="node.children"
          :selected-path="selectedPath"
          :expanded-paths="expandedPaths"
          @select="handleSelect"
        />
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { Folder, FolderOpened, Document } from '@element-plus/icons-vue'
import type { FileTreeNode } from '@/api/submission'

interface Props {
  nodes: FileTreeNode[]
  selectedPath?: string
  expandedPaths?: Set<string>
}

interface Emits {
  (e: 'select', node: FileTreeNode): void
}

const props = withDefaults(defineProps<Props>(), {
  selectedPath: '',
  expandedPaths: () => new Set()
})

const emit = defineEmits<Emits>()

const handleNodeClick = (node: FileTreeNode) => {
  if (node.isDirectory) {
    if (props.expandedPaths.has(node.path)) {
      props.expandedPaths.delete(node.path)
    } else {
      props.expandedPaths.add(node.path)
    }
  } else {
    emit('select', node)
  }
}

const handleSelect = (node: FileTreeNode) => {
  emit('select', node)
}
</script>

<style scoped lang="scss">
.file-tree {
  .tree-node {
    .node-content {
      display: flex;
      align-items: center;
      padding: 4px 8px;
      cursor: pointer;
      border-radius: 4px;
      transition: background-color 0.2s;

      &:hover {
        background-color: var(--el-fill-color-light);
      }

      &.is-active {
        background-color: var(--el-color-primary-light-9);
        color: var(--el-color-primary);
      }

      .node-icon {
        margin-right: 6px;
        font-size: 16px;
      }

      .node-label {
        font-size: 14px;
        white-space: nowrap;
        overflow: hidden;
        text-overflow: ellipsis;
      }
    }

    .node-children {
      margin-left: 16px;
    }
  }
}
</style>
