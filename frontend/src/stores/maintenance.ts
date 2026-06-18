import { defineStore } from 'pinia'
import { ref } from 'vue'
import type { MaintenanceRecord, Tool } from '@/types'
import * as maintenanceApi from '@/api/maintenance'

export const useMaintenanceStore = defineStore('maintenance', () => {
  const maintenanceRecords = ref<MaintenanceRecord[]>([])
  const dueMaintenance = ref<Tool[]>([])
  const total = ref(0)
  const loading = ref(false)

  async function fetchMaintenance(params?: Record<string, any>) {
    loading.value = true
    try {
      const res = await maintenanceApi.getMaintenanceRecords(params)
      maintenanceRecords.value = res.data.list
      total.value = res.data.total
    } finally {
      loading.value = false
    }
  }

  async function fetchDue() {
    const res = await maintenanceApi.getDueMaintenance()
    dueMaintenance.value = res.data
  }

  async function createMaintenance(data: Partial<MaintenanceRecord>) {
    const res = await maintenanceApi.createMaintenance(data)
    return res.data
  }

  async function updateMaintenance(id: number, data: Partial<MaintenanceRecord>) {
    const res = await maintenanceApi.updateMaintenance(id, data)
    return res.data
  }

  async function deleteMaintenance(id: number) {
    await maintenanceApi.deleteMaintenance(id)
  }

  return { maintenanceRecords, dueMaintenance, total, loading, fetchMaintenance, fetchDue, createMaintenance, updateMaintenance, deleteMaintenance }
})
