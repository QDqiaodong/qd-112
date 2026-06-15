import request from './request'
import type { Tool, PageResult, ApiResponse, MaintenanceTrack } from '@/types'

export function getTools(params?: Record<string, any>): Promise<ApiResponse<PageResult<Tool>>> {
  return request.get('/tools', { params })
}

export function getTool(id: number): Promise<ApiResponse<Tool>> {
  return request.get(`/tools/${id}`)
}

export function createTool(data: Partial<Tool>): Promise<ApiResponse<Tool>> {
  return request.post('/tools', data)
}

export function updateTool(id: number, data: Partial<Tool> & { operator?: string; statusReason?: string }): Promise<ApiResponse<Tool>> {
  return request.put(`/tools/${id}`, data)
}

export function deleteTool(id: number): Promise<ApiResponse<null>> {
  return request.delete(`/tools/${id}`)
}

export function getDueMaintenanceTools(): Promise<ApiResponse<Tool[]>> {
  return request.get('/tools/due-maintenance')
}

export function getMaintenanceByMonth(year: number, month: number): Promise<ApiResponse<Tool[]>> {
  return request.get('/tools/maintenance-by-month', { params: { year, month } })
}

export function getMaintenanceTrack(toolId: number): Promise<ApiResponse<MaintenanceTrack[]>> {
  return request.get(`/tools/${toolId}/maintenance-track`)
}
