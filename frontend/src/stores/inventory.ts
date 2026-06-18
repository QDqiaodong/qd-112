import { defineStore } from 'pinia'
import { ref } from 'vue'
import type { Inventory, InventoryItem, DifferenceGroup, InventoryProgress, InventoryCompletionResult } from '@/types'
import * as inventoryApi from '@/api/inventory'

export const useInventoryStore = defineStore('inventory', () => {
  const inventories = ref<Inventory[]>([])
  const currentInventory = ref<Inventory | null>(null)
  const inventoryItems = ref<InventoryItem[]>([])
  const differenceGroups = ref<DifferenceGroup[]>([])
  const inventoryProgress = ref<InventoryProgress | null>(null)
  const loading = ref(false)

  async function fetchInventories(params?: Record<string, any>) {
    loading.value = true
    try {
      const res = await inventoryApi.getInventories(params)
      inventories.value = res.data.list || []
    } finally {
      loading.value = false
    }
  }

  async function fetchInventory(id: number) {
    loading.value = true
    try {
      const res = await inventoryApi.getInventory(id)
      currentInventory.value = res.data
    } finally {
      loading.value = false
    }
  }

  async function fetchInventoryItems(inventoryId: number) {
    loading.value = true
    try {
      const res = await inventoryApi.getInventoryItems(inventoryId)
      inventoryItems.value = res.data
    } finally {
      loading.value = false
    }
  }

  async function fetchInventoryProgress(inventoryId: number) {
    loading.value = true
    try {
      const res = await inventoryApi.getInventoryProgress(inventoryId)
      inventoryProgress.value = res.data
    } finally {
      loading.value = false
    }
  }

  async function fetchDifferenceGroups(inventoryId: number, groupBy: string = 'category') {
    loading.value = true
    try {
      const res = await inventoryApi.getInventoryDifferences(inventoryId, groupBy)
      differenceGroups.value = res.data
    } finally {
      loading.value = false
    }
  }

  async function createInventory(data?: Record<string, any>) {
    const payload = data || { operator: '系统管理员' }
    const res = await inventoryApi.createInventory(payload)
    return res.data
  }

  async function updateItem(inventoryId: number, toolId: number, data: Record<string, any>) {
    const res = await inventoryApi.updateInventoryItem(inventoryId, toolId, data)
    return res.data
  }

  async function completeInventory(inventoryId: number): Promise<InventoryCompletionResult> {
    const res = await inventoryApi.completeInventory(inventoryId)
    return res.data
  }

  return {
    inventories,
    currentInventory,
    inventoryItems,
    differenceGroups,
    inventoryProgress,
    loading,
    fetchInventories,
    fetchInventory,
    fetchInventoryItems,
    fetchInventoryProgress,
    fetchDifferenceGroups,
    createInventory,
    updateItem,
    completeInventory
  }
})
