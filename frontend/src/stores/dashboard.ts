import { defineStore } from 'pinia'
import { ref } from 'vue'
import type { StatsOverview } from '@/types'
import * as dashboardApi from '@/api/dashboard'

export const useDashboardStore = defineStore('dashboard', () => {
  const overview = ref<StatsOverview | null>(null)
  const loading = ref(false)

  async function fetchOverview() {
    loading.value = true
    try {
      const res = await dashboardApi.getOverview()
      overview.value = res.data
    } finally {
      loading.value = false
    }
  }

  return { overview, loading, fetchOverview }
})
