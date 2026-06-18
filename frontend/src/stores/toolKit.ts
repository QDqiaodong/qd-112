import { defineStore } from 'pinia'
import { ref } from 'vue'
import type { ToolKit, ToolKitWithItems, ToolKitDTO, ToolKitItemDTO } from '@/types'
import * as toolKitApi from '@/api/toolKit'

export const useToolKitStore = defineStore('toolKit', () => {
  const toolKits = ref<ToolKit[]>([])
  const currentKit = ref<ToolKitWithItems | null>(null)
  const loading = ref(false)

  async function fetchToolKits() {
    loading.value = true
    try {
      const res = await toolKitApi.getToolKits()
      toolKits.value = res.data
    } finally {
      loading.value = false
    }
  }

  async function fetchKitWithItems(id: number) {
    loading.value = true
    try {
      const res = await toolKitApi.getToolKitWithItems(id)
      currentKit.value = res.data
    } finally {
      loading.value = false
    }
  }

  async function createKit(data: ToolKitDTO) {
    const res = await toolKitApi.createToolKit(data)
    return res.data
  }

  async function updateKit(id: number, data: ToolKitDTO) {
    const res = await toolKitApi.updateToolKit(id, data)
    return res.data
  }

  async function deleteKit(id: number) {
    await toolKitApi.deleteToolKit(id)
  }

  async function addItem(kitId: number, data: ToolKitItemDTO) {
    await toolKitApi.addToolKitItem(kitId, data)
  }

  async function removeItem(kitId: number, toolId: number) {
    await toolKitApi.removeToolKitItem(kitId, toolId)
  }

  async function updateItem(kitId: number, data: ToolKitItemDTO) {
    await toolKitApi.updateToolKitItem(kitId, data)
  }

  return { toolKits, currentKit, loading, fetchToolKits, fetchKitWithItems, createKit, updateKit, deleteKit, addItem, removeItem, updateItem }
})
