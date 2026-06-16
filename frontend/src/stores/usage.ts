import { defineStore } from 'pinia'
import { ref } from 'vue'
import type { UsageRecord, ScenarioAnalysis } from '@/types'
import * as usageApi from '@/api/usage'

export const useUsageStore = defineStore('usage', () => {
  const usageRecords = ref<UsageRecord[]>([])
  const total = ref(0)
  const loading = ref(false)
  const scenarioAnalysis = ref<ScenarioAnalysis[]>([])
  const scenarioLoading = ref(false)

  async function fetchUsage(params?: Record<string, any>) {
    loading.value = true
    try {
      const res = await usageApi.getUsageRecords(params)
      usageRecords.value = res.data.list
      total.value = res.data.total
    } finally {
      loading.value = false
    }
  }

  async function createUsage(data: Partial<UsageRecord>) {
    const res = await usageApi.createUsage(data)
    return res.data
  }

  async function updateUsage(id: number, data: Partial<UsageRecord>) {
    const res = await usageApi.updateUsage(id, data)
    return res.data
  }

  async function deleteUsage(id: number) {
    await usageApi.deleteUsage(id)
  }

  async function fetchScenarioAnalysis(params?: Record<string, any>) {
    scenarioLoading.value = true
    try {
      const res = await usageApi.getScenarioAnalysis(params)
      scenarioAnalysis.value = res.data
    } finally {
      scenarioLoading.value = false
    }
  }

  return {
    usageRecords,
    total,
    loading,
    scenarioAnalysis,
    scenarioLoading,
    fetchUsage,
    createUsage,
    updateUsage,
    deleteUsage,
    fetchScenarioAnalysis
  }
})
