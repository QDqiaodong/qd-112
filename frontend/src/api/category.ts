import request from './request'
import type { Category, CategoryTreeNode, MaintenanceItem, ApiResponse, CategoryDeletionCheck } from '@/types'

export function getCategoryTree(): Promise<ApiResponse<Category[]>> {
  return request.get('/categories/tree')
}

export function getCategoryTreeWithStats(): Promise<ApiResponse<CategoryTreeNode[]>> {
  return request.get('/categories/tree-with-stats')
}

export function getCategoryById(id: number): Promise<ApiResponse<Category>> {
  return request.get(`/categories/${id}`)
}

export function createCategory(data: Partial<Category>): Promise<ApiResponse<Category>> {
  return request.post('/categories', data)
}

export function updateCategory(id: number, data: Partial<Category>): Promise<ApiResponse<Category>> {
  return request.put(`/categories/${id}`, data)
}

export function updateCategorySort(id: number, sortOrder: number): Promise<ApiResponse<void>> {
  return request.patch(`/categories/${id}/sort`, null, { params: { sortOrder } })
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
