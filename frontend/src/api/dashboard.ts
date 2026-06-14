import request from './request'
import type { StatsOverview, ApiResponse } from '@/types'

export function getOverview(): Promise<ApiResponse<StatsOverview>> {
  return request.get('/stats/overview')
}
