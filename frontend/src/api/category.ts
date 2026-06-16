import request from './request'
import type { Category, MaintenanceItem, ApiResponse, CategoryDeletionCheck } from '@/types'

export function getCategoryTree(): Promise<ApiResponse<Category[]>> {
  return request.get('/categories/tree')
}

export function getMaintenanceItems(categoryId?: number): Promise<ApiResponse<MaintenanceItem[]>> {
  return request.get('/categories/maintenance-items', { params: { categoryId } })
}

export function checkCategoryDeletion(id: number): Promise<ApiResponse<CategoryDeletionCheck>> {
  return request.get(`/categories/${id}/deletion-check`)
}

export function deleteCategory(id: number): Promise<ApiResponse<void>> {
  return request.delete(`/categories/${id}`)
}
