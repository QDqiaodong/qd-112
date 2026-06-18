import { defineStore } from 'pinia'
import { ref } from 'vue'
import type { PartReplacement } from '@/types'
import * as partReplacementApi from '@/api/partReplacement'

export const usePartReplacementStore = defineStore('partReplacement', () => {
  const records = ref<PartReplacement[]>([])
  const total = ref(0)
  const loading = ref(false)

  async function fetchRecords(params?: Record<string, any>) {
    loading.value = true
    try {
      const res = await partReplacementApi.getPartReplacements(params)
      records.value = res.data.list
      total.value = res.data.total
    } finally {
      loading.value = false
    }
  }

  async function createRecord(data: Partial<PartReplacement>) {
    const res = await partReplacementApi.createPartReplacement(data)
    return res.data
  }

  async function updateRecord(id: number, data: Partial<PartReplacement>) {
    const res = await partReplacementApi.updatePartReplacement(id, data)
    return res.data
  }

  async function deleteRecord(id: number) {
    await partReplacementApi.deletePartReplacement(id)
  }

  return {
    records,
    total,
    loading,
    fetchRecords,
    createRecord,
    updateRecord,
    deleteRecord
  }
})
