import { defineStore } from 'pinia'
import { ref } from 'vue'
import type { LoanRecord } from '@/types'
import * as loanApi from '@/api/loan'

export const useLoanStore = defineStore('loan', () => {
  const loanRecords = ref<LoanRecord[]>([])
  const total = ref(0)
  const loading = ref(false)

  async function fetchLoanRecords(params?: Record<string, any>) {
    loading.value = true
    try {
      const res = await loanApi.getLoanRecords(params)
      loanRecords.value = res.data.list
      total.value = res.data.total
    } finally {
      loading.value = false
    }
  }

  async function createLoanRecord(data: Partial<LoanRecord>) {
    const res = await loanApi.createLoanRecord(data)
    return res.data
  }

  async function updateLoanRecord(id: number, data: Partial<LoanRecord>) {
    const res = await loanApi.updateLoanRecord(id, data)
    return res.data
  }

  async function returnLoanRecord(id: number, data: Partial<LoanRecord>) {
    const res = await loanApi.returnLoanRecord(id, data)
    return res.data
  }

  async function deleteLoanRecord(id: number) {
    await loanApi.deleteLoanRecord(id)
  }

  return {
    loanRecords,
    total,
    loading,
    fetchLoanRecords,
    createLoanRecord,
    updateLoanRecord,
    returnLoanRecord,
    deleteLoanRecord
  }
})
