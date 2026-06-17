import request from './request'
import type { Inventory, InventoryItem, DifferenceGroup, InventoryProgress, InventoryCompletionResult, ApiResponse, PageResult } from '@/types'

export function getInventories(params?: Record<string, any>): Promise<ApiResponse<PageResult<Inventory>>> {
  return request.get('/inventory', { params })
}

export function getInventory(id: number): Promise<ApiResponse<Inventory>> {
  return request.get(`/inventory/${id}`)
}

export function getInventoryItems(inventoryId: number): Promise<ApiResponse<InventoryItem[]>> {
  return request.get(`/inventory/${inventoryId}/items`)
}

export function getInventoryProgress(inventoryId: number): Promise<ApiResponse<InventoryProgress>> {
  return request.get(`/inventory/${inventoryId}/progress`)
}

export function createInventory(data?: Record<string, any>): Promise<ApiResponse<Inventory>> {
  return request.post('/inventory', data)
}

export function updateInventory(id: number, data: Partial<Inventory>): Promise<ApiResponse<Inventory>> {
  return request.put(`/inventory/${id}`, data)
}

export function updateInventoryItem(inventoryId: number, toolId: number, data: Partial<InventoryItem>): Promise<ApiResponse<InventoryItem>> {
  return request.post(`/inventory/${inventoryId}/item`, { toolId, ...data })
}

export function completeInventory(inventoryId: number): Promise<ApiResponse<InventoryCompletionResult>> {
  return request.post(`/inventory/${inventoryId}/complete`)
}

export function getInventoryDifferences(inventoryId: number, groupBy: string = 'category'): Promise<ApiResponse<DifferenceGroup[]>> {
  return request.get(`/inventory/${inventoryId}/differences`, { params: { groupBy } })
}
