import { defineStore } from 'pinia'
import { ref } from 'vue'
import type { Category, CategoryTreeNode, MaintenanceItem, CategoryDeletionCheck } from '@/types'
import * as categoryApi from '@/api/category'
import { ElMessage } from 'element-plus'

export const useCategoryStore = defineStore('category', () => {
  const categoryTree = ref<Category[]>([])
  const categoryTreeWithStats = ref<CategoryTreeNode[]>([])
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

  async function fetchCategoryTreeWithStats() {
    loading.value = true
    try {
      const res = await categoryApi.getCategoryTreeWithStats()
      categoryTreeWithStats.value = res.data
    } finally {
      loading.value = false
    }
  }

  async function createCategory(data: Partial<Category>) {
    const res = await categoryApi.createCategory(data)
    if (res.code === 200) {
      ElMessage.success('创建成功')
      await Promise.all([fetchCategoryTree(), fetchCategoryTreeWithStats()])
      return res.data
    } else {
      ElMessage.error(res.message || '创建失败')
      return null
    }
  }

  async function updateCategory(id: number, data: Partial<Category>) {
    const res = await categoryApi.updateCategory(id, data)
    if (res.code === 200) {
      ElMessage.success('更新成功')
      await Promise.all([fetchCategoryTree(), fetchCategoryTreeWithStats()])
      return res.data
    } else {
      ElMessage.error(res.message || '更新失败')
      return null
    }
  }

  async function updateSortOrder(id: number, sortOrder: number) {
    const res = await categoryApi.updateCategorySort(id, sortOrder)
    if (res.code === 200) {
      await fetchCategoryTreeWithStats()
      return true
    } else {
      ElMessage.error(res.message || '排序更新失败')
      return false
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
      await Promise.all([fetchCategoryTree(), fetchCategoryTreeWithStats()])
      return true
    } else {
      ElMessage.error(res.message || '删除失败')
      return false
    }
  }

  return {
    categoryTree,
    categoryTreeWithStats,
    maintenanceItems,
    loading,
    deletionCheck,
    fetchCategoryTree,
    fetchCategoryTreeWithStats,
    createCategory,
    updateCategory,
    updateSortOrder,
    fetchMaintenanceItems,
    checkDeletion,
    deleteCategory
  }
})
