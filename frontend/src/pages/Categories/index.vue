<template>
  <div class="p-6">
    <h2 class="text-2xl font-bold text-gray-800 mb-6">分类管理</h2>

    <div class="flex gap-6">
      <div class="w-72 bg-white rounded-lg p-5 shadow-sm shrink-0">
        <h3 class="text-lg font-semibold text-gray-700 mb-4">分类树</h3>
        <el-tree
          :data="categoryStore.categoryTree"
          :props="{ children: 'children', label: 'name' }"
          node-key="id"
          highlight-current
          default-expand-all
          @node-click="handleNodeClick"
        />
      </div>

      <div class="flex-1">
        <div v-if="selectedCategory" class="bg-white rounded-lg p-5 shadow-sm mb-6">
          <h3 class="text-lg font-semibold text-gray-700 mb-4">分类详情</h3>
          <div class="space-y-2 text-sm">
            <p><span class="text-gray-500">分类名称：</span>{{ selectedCategory.name }}</p>
            <p><span class="text-gray-500">编码：</span>{{ selectedCategory.code }}</p>
            <p><span class="text-gray-500">层级：</span>{{ selectedCategory.level }}</p>
            <p><span class="text-gray-500">排序：</span>{{ selectedCategory.sortOrder }}</p>
            <p><span class="text-gray-500">描述：</span>{{ selectedCategory.description || '-' }}</p>
          </div>
        </div>

        <div v-if="selectedCategory" class="bg-white rounded-lg p-5 shadow-sm">
          <h3 class="text-lg font-semibold text-gray-700 mb-4">维护项目</h3>
          <el-table :data="categoryStore.maintenanceItems" stripe>
            <el-table-column prop="name" label="项目名称" />
            <el-table-column prop="code" label="编码" width="100" />
            <el-table-column prop="defaultCycleDays" label="周期(天)" width="100" />
            <el-table-column prop="description" label="内容" />
          </el-table>
          <div v-if="!categoryStore.maintenanceItems.length" class="text-center py-8 text-gray-400">
            暂无维护项目
          </div>
        </div>

        <div v-else class="bg-white rounded-lg p-20 shadow-sm text-center text-gray-400">
          请在左侧选择分类查看详情
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useCategoryStore } from '@/stores/category'
import type { Category } from '@/types'

const categoryStore = useCategoryStore()
const selectedCategory = ref<Category | null>(null)

onMounted(() => categoryStore.fetchCategoryTree())

function handleNodeClick(node: Category) {
  selectedCategory.value = node
  categoryStore.fetchMaintenanceItems(node.id)
}
</script>
