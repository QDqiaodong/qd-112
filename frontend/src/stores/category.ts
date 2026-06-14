import { defineStore } from 'pinia'
import { ref } from 'vue'
import type { Category, MaintenanceItem } from '@/types'
import * as categoryApi from '@/api/category'

export const useCategoryStore = defineStore('category', () => {
  const categoryTree = ref<Category[]>([])
  const maintenanceItems = ref<MaintenanceItem[]>([])
  const loading = ref(false)

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

  return { categoryTree, maintenanceItems, loading, fetchCategoryTree, fetchMaintenanceItems }
})
