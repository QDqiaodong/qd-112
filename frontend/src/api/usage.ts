import request from './request'
import type { UsageRecord, PageResult, ApiResponse, ScenarioAnalysis } from '@/types'

export function getUsageRecords(params?: Record<string, any>): Promise<ApiResponse<PageResult<UsageRecord>>> {
  return request.get('/usage', { params })
}

export function getUsageByTool(toolId: number, params?: Record<string, any>): Promise<ApiResponse<PageResult<UsageRecord>>> {
  return request.get(`/tools/${toolId}/usage`, { params })
}

export function createUsage(data: Partial<UsageRecord>): Promise<ApiResponse<UsageRecord>> {
  return request.post('/usage', data)
}

export function updateUsage(id: number, data: Partial<UsageRecord>): Promise<ApiResponse<UsageRecord>> {
  return request.put(`/usage/${id}`, data)
}

export function deleteUsage(id: number): Promise<ApiResponse<null>> {
  return request.delete(`/usage/${id}`)
}

export function getScenarioAnalysis(params?: Record<string, any>): Promise<ApiResponse<ScenarioAnalysis[]>> {
  return request.get('/usage/scenario-analysis', { params })
}
