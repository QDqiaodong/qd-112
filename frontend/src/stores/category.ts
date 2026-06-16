import { defineStore } from 'pinia'
import { ref } from 'vue'
import type { Category, MaintenanceItem, CategoryDeletionCheck } from '@/types'
import * as categoryApi from '@/api/category'
import { ElMessage } from 'element-plus'

export const useCategoryStore = defineStore('category', () => {
  const categoryTree = ref<Category[]>([])
  const maintenanceItems = ref<MaintenanceItem[]>([])
  const loading = ref(false)
  const deletionCheck = ref<CategoryDeletionCheck | null>(null)

  async function fetchCategoryTree() {
    loading.value = true
    try {
      const res = await categoryApi.getCategoryTree()
      categoryTree.value = res.data
    } finally {
      loading.value = false
    }
  }

  async function fetchMaintenanceItems(categoryId?: number) {
    const res = await categoryApi.getMaintenanceItems(categoryId)
    maintenanceItems.value = res.data
  }

  async function checkDeletion(id: number) {
    const res = await categoryApi.checkCategoryDeletion(id)
    deletionCheck.value = res.data
    return res.data
  }

  async function deleteCategory(id: number) {
    const res = await categoryApi.deleteCategory(id)
    if (res.code === 200) {
      ElMessage.success('删除成功')
      await fetchCategoryTree()
      return true
    } else {
      ElMessage.error(res.message || '删除失败')
      return false
    }
  }

  return {
    categoryTree,
    maintenanceItems,
    loading,
    deletionCheck,
    fetchCategoryTree,
    fetchMaintenanceItems,
    checkDeletion,
    deleteCategory
  }
})
