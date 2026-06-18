import request from './request'
import type { ToolKit, ToolKitWithItems, ToolKitDTO, ToolKitItemDTO, ApiResponse } from '@/types'

export function getToolKits(): Promise<ApiResponse<ToolKit[]>> {
  return request.get('/tool-kits')
}

export function getToolKit(id: number): Promise<ApiResponse<ToolKit>> {
  return request.get(`/tool-kits/${id}`)
}

export function getToolKitWithItems(id: number): Promise<ApiResponse<ToolKitWithItems>> {
  return request.get(`/tool-kits/${id}/with-items`)
}

export function createToolKit(data: ToolKitDTO): Promise<ApiResponse<ToolKit>> {
  return request.post('/tool-kits', data)
}

export function updateToolKit(id: number, data: ToolKitDTO): Promise<ApiResponse<ToolKit>> {
  return request.put(`/tool-kits/${id}`, data)
}

export function deleteToolKit(id: number): Promise<ApiResponse<null>> {
  return request.delete(`/tool-kits/${id}`)
}

export function addToolKitItem(kitId: number, data: ToolKitItemDTO): Promise<ApiResponse<null>> {
  return request.post(`/tool-kits/${kitId}/items`, data)
}

export function removeToolKitItem(kitId: number, toolId: number): Promise<ApiResponse<null>> {
  return request.delete(`/tool-kits/${kitId}/items/${toolId}`)
}

export function updateToolKitItem(kitId: number, data: ToolKitItemDTO): Promise<ApiResponse<null>> {
  return request.put(`/tool-kits/${kitId}/items`, data)
}
