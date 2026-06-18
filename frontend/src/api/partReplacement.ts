import request from './request'
import type { PartReplacement, PageResult, ApiResponse } from '@/types'

export function getPartReplacements(params?: Record<string, any>): Promise<ApiResponse<PageResult<PartReplacement>>> {
  return request.get('/part-replacements', { params })
}

export function getPartReplacementsByTool(toolId: number, params?: Record<string, any>): Promise<ApiResponse<PageResult<PartReplacement>>> {
  return request.get(`/part-replacements/tool/${toolId}`, { params })
}

export function createPartReplacement(data: Partial<PartReplacement>): Promise<ApiResponse<PartReplacement>> {
  return request.post('/part-replacements', data)
}

export function updatePartReplacement(id: number, data: Partial<PartReplacement>): Promise<ApiResponse<PartReplacement>> {
  return request.put(`/part-replacements/${id}`, data)
}

export function deletePartReplacement(id: number): Promise<ApiResponse<null>> {
  return request.delete(`/part-replacements/${id}`)
}
