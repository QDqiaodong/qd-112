import request from './request'
import type { LoanRecord, PageResult, ApiResponse } from '@/types'

export function getLoanRecords(params?: Record<string, any>): Promise<ApiResponse<PageResult<LoanRecord>>> {
  return request.get('/loan-records', { params })
}

export function getLoanRecord(id: number): Promise<ApiResponse<LoanRecord>> {
  return request.get(`/loan-records/${id}`)
}

export function getLoanRecordsByTool(toolId: number): Promise<ApiResponse<LoanRecord[]>> {
  return request.get(`/loan-records/tool/${toolId}`)
}

export function createLoanRecord(data: Partial<LoanRecord>): Promise<ApiResponse<LoanRecord>> {
  return request.post('/loan-records', data)
}

export function updateLoanRecord(id: number, data: Partial<LoanRecord>): Promise<ApiResponse<LoanRecord>> {
  return request.put(`/loan-records/${id}`, data)
}

export function returnLoanRecord(id: number, data: Partial<LoanRecord>): Promise<ApiResponse<LoanRecord>> {
  return request.post(`/loan-records/${id}/return`, data)
}

export function deleteLoanRecord(id: number): Promise<ApiResponse<null>> {
  return request.delete(`/loan-records/${id}`)
}
