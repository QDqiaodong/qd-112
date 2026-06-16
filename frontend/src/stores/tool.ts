import { defineStore } from 'pinia'
import { ref } from 'vue'
import type { Tool, ToolWithScore, ToolAvailabilityScore } from '@/types'
import * as toolApi from '@/api/tool'

export const useToolStore = defineStore('tool', () => {
  const tools = ref<Tool[]>([])
  const toolsWithScore = ref<ToolWithScore[]>([])
  const currentTool = ref<Tool | null>(null)
  const currentAvailabilityScore = ref<ToolAvailabilityScore | null>(null)
  const monthlyMaintenanceTools = ref<Tool[]>([])
  const total = ref(0)
  const loading = ref(false)

  async function fetchTools(params?: Record<string, any>) {
    loading.value = true
    try {
      const res = await toolApi.getTools(params)
      tools.value = res.data.list
      total.value = res.data.total
    } finally {
      loading.value = false
    }
  }

  async function fetchToolsWithScore(params?: Record<string, any>) {
    loading.value = true
    try {
      const res = await toolApi.getToolsWithScore(params)
      toolsWithScore.value = res.data.list
      total.value = res.data.total
    } finally {
      loading.value = false
    }
  }

  async function fetchTool(id: number) {
    loading.value = true
    try {
      const res = await toolApi.getTool(id)
      currentTool.value = res.data
    } finally {
      loading.value = false
    }
  }

  async function fetchToolAvailabilityScore(id: number) {
    const res = await toolApi.getToolAvailabilityScore(id)
    currentAvailabilityScore.value = res.data
    return res.data
  }

  async function createTool(data: Partial<Tool>) {
    const res = await toolApi.createTool(data)
    return res.data
  }

  async function updateTool(id: number, data: Partial<Tool> & { operator?: string; statusReason?: string }) {
    const res = await toolApi.updateTool(id, data)
    return res.data
  }

  async function deleteTool(id: number) {
    await toolApi.deleteTool(id)
  }

  async function fetchToolOptions() {
    const res = await toolApi.getTools({ page: 1, size: 9999 })
    return res.data.list
  }

  async function fetchMonthlyMaintenance(year: number, month: number) {
    loading.value = true
    try {
      const res = await toolApi.getMaintenanceByMonth(year, month)
      monthlyMaintenanceTools.value = res.data
    } finally {
      loading.value = false
    }
  }

  return {
    tools,
    toolsWithScore,
    currentTool,
    currentAvailabilityScore,
    monthlyMaintenanceTools,
    total,
    loading,
    fetchTools,
    fetchToolsWithScore,
    fetchTool,
    fetchToolAvailabilityScore,
    createTool,
    updateTool,
    deleteTool,
    fetchToolOptions,
    fetchMonthlyMaintenance
  }
})
