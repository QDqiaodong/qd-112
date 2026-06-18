import request from './request'
import type { CostSummary, ApiResponse } from '@/types'

export function getCostSummaryByCategory(): Promise<ApiResponse<CostSummary[]>> {
  return request.get('/cost-summary/by-category')
}

export function getCostSummaryByLocation(): Promise<ApiResponse<CostSummary[]>> {
  return request.get('/cost-summary/by-location')
}

export function getCostSummaryByMonth(): Promise<ApiResponse<CostSummary[]>> {
  return request.get('/cost-summary/by-month')
}
