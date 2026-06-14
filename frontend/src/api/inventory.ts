import request from './request'
import type { Inventory, InventoryItem, ApiResponse } from '@/types'

export function getInventories(params?: Record<string, any>): Promise<ApiResponse<Inventory[]>> {
  return request.get('/inventories', { params })
}

export function getInventory(id: number): Promise<ApiResponse<Inventory>> {
  return request.get(`/inventories/${id}`)
}

export function getInventoryItems(inventoryId: number): Promise<ApiResponse<InventoryItem[]>> {
  return request.get(`/inventories/${inventoryId}/items`)
}

export function createInventory(data?: Record<string, any>): Promise<ApiResponse<Inventory>> {
  return request.post('/inventories', data)
}

export function updateInventory(id: number, data: Partial<Inventory>): Promise<ApiResponse<Inventory>> {
  return request.put(`/inventories/${id}`, data)
}

export function updateInventoryItem(inventoryId: number, itemId: number, data: Partial<InventoryItem>): Promise<ApiResponse<InventoryItem>> {
  return request.put(`/inventories/${inventoryId}/items/${itemId}`, data)
}
