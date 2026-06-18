<template>
  <div class="p-6">
    <h2 class="text-2xl font-bold text-gray-800 mb-6">分类管理</h2>

    <div class="flex gap-6">
      <div class="w-80 bg-white rounded-lg p-5 shadow-sm shrink-0">
        <h3 class="text-lg font-semibold text-gray-700 mb-4">分类树</h3>
        <el-tree
          :data="categoryStore.categoryTreeWithStats"
          :props="{ children: 'children', label: 'name' }"
          node-key="id"
          highlight-current
          default-expand-all
          @node-click="handleNodeClick"
        >
          <template #default="{ data }">
            <div class="flex items-center gap-2 w-full">
              <span class="truncate">{{ data.name }}</span>
              <el-tag size="small" type="info" effect="plain" class="!text-xs !px-1.5 !py-0 shrink-0">
                {{ data.toolCount }}件
              </el-tag>
              <el-tag v-if="data.defaultCycleDays" size="small" type="warning" effect="plain" class="!text-xs !px-1.5 !py-0 shrink-0">
                {{ data.defaultCycleDays }}天
              </el-tag>
            </div>
          </template>
        </el-tree>
      </div>

      <div class="flex-1">
        <div v-if="selectedCategory" class="bg-white rounded-lg p-5 shadow-sm mb-6">
          <div class="flex items-center justify-between mb-4">
            <h3 class="text-lg font-semibold text-gray-700">分类详情</h3>
            <el-button type="danger" :icon="Delete" @click="handleDeleteClick">删除分类</el-button>
          </div>
          <div class="space-y-2 text-sm">
            <p><span class="text-gray-500">分类名称：</span>{{ selectedCategory.name }}</p>
            <p><span class="text-gray-500">编码：</span>{{ selectedCategory.code }}</p>
            <p><span class="text-gray-500">层级：</span>{{ selectedCategory.level }}</p>
            <p><span class="text-gray-500">排序：</span>{{ selectedCategory.sortOrder }}</p>
            <p><span class="text-gray-500">描述：</span>{{ selectedCategory.description || '-' }}</p>
            <div class="flex items-center gap-4 pt-2 border-t border-gray-100 mt-2">
              <div class="flex items-center gap-1">
                <el-tag size="small" type="info" effect="plain">{{ selectedCategory.toolCount }} 件工具</el-tag>
              </div>
              <div class="flex items-center gap-1">
                <el-tag v-if="selectedCategory.defaultCycleDays" size="small" type="warning" effect="plain">
                  默认保养周期: {{ selectedCategory.defaultCycleDays }} 天
                </el-tag>
                <el-tag v-else size="small" effect="plain">未配置保养周期</el-tag>
              </div>
              <div class="flex items-center gap-1">
                <el-tag size="small" effect="plain">
                  保养项: {{ selectedCategory.maintenanceItemCount }}/{{ selectedCategory.maintenanceItemTotal }}
                </el-tag>
              </div>
            </div>
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

    <el-dialog
      v-model="deleteDialogVisible"
      title="删除确认"
      width="500px"
      @close="handleDialogClose"
    >
      <div v-if="deletionCheckResult">
        <div v-if="!deletionCheckResult.canDelete" class="space-y-4">
          <el-alert
            :title="deletionCheckResult.message"
            type="warning"
            show-icon
          />
          <div class="bg-gray-50 rounded-lg p-4 space-y-2">
            <div class="flex justify-between">
              <span class="text-gray-600">子分类数量：</span>
              <span class="font-medium text-orange-600">{{ deletionCheckResult.subCategoryCount }} 个</span>
            </div>
            <div class="flex justify-between">
              <span class="text-gray-600">关联工具数量：</span>
              <span class="font-medium text-orange-600">{{ deletionCheckResult.toolCount }} 个</span>
            </div>
            <div class="flex justify-between">
              <span class="text-gray-600">保养记录数量：</span>
              <span class="font-medium text-orange-600">{{ deletionCheckResult.maintenanceRecordCount }} 条</span>
            </div>
          </div>
          <p class="text-sm text-gray-500 mt-2">
            请先解除上述关联后再尝试删除该分类。</p>
        </div>
        <div v-else class="space-y-4">
          <el-alert
            title="该分类可以安全删除"
            type="success"
            show-icon
          />
          <p class="text-gray-600">确定要删除分类 <strong class="text-red-600">{{ selectedCategory?.name }}</strong> 吗？</p>
          <p class="text-sm text-gray-500">此操作不可恢复，请谨慎操作。</p>
        </div>
      </div>
      <template #footer>
        <el-button @click="deleteDialogVisible = false">取消</el-button>
        <el-button
          v-if="deletionCheckResult?.canDelete"
          type="danger"
          :loading="deleting"
          @click="confirmDelete"
        >
          确认删除
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useCategoryStore } from '@/stores/category'
import { Delete } from '@element-plus/icons-vue'
import { ElMessageBox } from 'element-plus'
import type { CategoryTreeNode, CategoryDeletionCheck } from '@/types'

const categoryStore = useCategoryStore()
const selectedCategory = ref<CategoryTreeNode | null>(null)
const deleteDialogVisible = ref(false)
const deletionCheckResult = ref<CategoryDeletionCheck | null>(null)
const deleting = ref(false)

onMounted(() => categoryStore.fetchCategoryTreeWithStats())

function handleNodeClick(node: CategoryTreeNode) {
  selectedCategory.value = node
  categoryStore.fetchMaintenanceItems(node.id)
}

async function handleDeleteClick() {
  if (!selectedCategory.value) return
  deletionCheckResult.value = null
  deleteDialogVisible.value = true
  try {
    deletionCheckResult.value = await categoryStore.checkDeletion(selectedCategory.value.id)
  } catch (error) {
    console.error('检查删除状态失败', error)
  }
}

function handleDialogClose() {
  deletionCheckResult.value = null
}

async function confirmDelete() {
  if (!selectedCategory.value || !deletionCheckResult.value?.canDelete) return

  try {
    await ElMessageBox.confirm(
      `确定要删除分类「${selectedCategory.value.name}」吗？此操作不可恢复。`,
      '删除确认',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning',
      }
    )
  } catch {
    return
  }

  deleting.value = true
  try {
    const success = await categoryStore.deleteCategory(selectedCategory.value.id)
    if (success) {
      deleteDialogVisible.value = false
      selectedCategory.value = null
    }
  } finally {
    deleting.value = false
  }
}
</script>
