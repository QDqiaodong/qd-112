import { defineStore } from 'pinia'
import { ref } from 'vue'
import type { CostSummary } from '@/types'
import * as costSummaryApi from '@/api/costSummary'

export const useCostSummaryStore = defineStore('costSummary', () => {
  const categorySummary = ref<CostSummary[]>([])
  const locationSummary = ref<CostSummary[]>([])
  const monthSummary = ref<CostSummary[]>([])
  const loading = ref(false)

  async function fetchByCategory() {
    loading.value = true
    try {
      const res = await costSummaryApi.getCostSummaryByCategory()
      categorySummary.value = res.data
    } finally {
      loading.value = false
    }
  }

  async function fetchByLocation() {
    loading.value = true
    try {
      const res = await costSummaryApi.getCostSummaryByLocation()
      locationSummary.value = res.data
    } finally {
      loading.value = false
    }
  }

  async function fetchByMonth() {
    loading.value = true
    try {
      const res = await costSummaryApi.getCostSummaryByMonth()
      monthSummary.value = res.data
    } finally {
      loading.value = false
    }
  }

  return { categorySummary, locationSummary, monthSummary, loading, fetchByCategory, fetchByLocation, fetchByMonth }
})
