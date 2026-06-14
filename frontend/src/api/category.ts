import request from './request'
import type { Category, MaintenanceItem, ApiResponse } from '@/types'

export function getCategoryTree(): Promise<ApiResponse<Category[]>> {
  return request.get('/categories/tree')
}

export function getMaintenanceItems(categoryId?: number): Promise<ApiResponse<MaintenanceItem[]>> {
  return request.get('/categories/maintenance-items', { params: { categoryId } })
}
