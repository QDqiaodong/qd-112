import request from './request'
import type { MaintenanceRecord, PageResult, ApiResponse } from '@/types'

export function getMaintenanceRecords(params?: Record<string, any>): Promise<ApiResponse<PageResult<MaintenanceRecord>>> {
  return request.get('/maintenance', { params })
}

export function getMaintenanceByTool(toolId: number, params?: Record<string, any>): Promise<ApiResponse<PageResult<MaintenanceRecord>>> {
  return request.get(`/tools/${toolId}/maintenance`, { params })
}

export function getDueMaintenance(): Promise<ApiResponse<MaintenanceRecord[]>> {
  return request.get('/maintenance/due')
}

export function createMaintenance(data: Partial<MaintenanceRecord>): Promise<ApiResponse<MaintenanceRecord>> {
  return request.post('/maintenance', data)
}

export function updateMaintenance(id: number, data: Partial<MaintenanceRecord>): Promise<ApiResponse<MaintenanceRecord>> {
  return request.put(`/maintenance/${id}`, data)
}

export function deleteMaintenance(id: number): Promise<ApiResponse<null>> {
  return request.delete(`/maintenance/${id}`)
}
