import http, { type ApiResponse, type PageResult } from './http'

export interface MfgTask {
  id: number
  taskNo: string
  taskDate: string
  priority: string
  salesOrderNo?: string
  customerName?: string
  customerOrderNo?: string
  productId: number
  productName: string
  orderedQty: number
  planQty: number
  finishedQty: number
  scrapQty: number
  planStartDate?: string
  planFinishDate?: string
  actualFinishDate?: string
  materialStatus: string
  processStatus: string
  stockInStatus: string
  status: string
  marker?: string
  remark?: string
}

export interface MfgTaskQuery {
  pageNo: number
  pageSize: number
  keyword?: string
  status?: string
}

export interface MfgTaskPayload {
  taskNo?: string
  taskDate: string
  priority?: string
  salesOrderNo?: string
  customerId?: number
  customerName?: string
  customerOrderNo?: string
  productId: number
  productName: string
  orderedQty?: number
  planQty: number
  planStartDate?: string
  planFinishDate?: string
  marker?: string
  remark?: string
}

export interface MfgMaterialIssue {
  id: number
  issueNo: string
  taskId: number
  warehouseId: number
  issueUserName?: string
  issueTime: string
  status: string
  remark?: string
}

export interface MfgMaterialIssuePayload {
  taskId: number
  warehouseId: number
  issueUserName?: string
  remark?: string
  items: Array<{
    materialId: number
    materialName: string
    planQty?: number
    issueQty: number
    unitName?: string
    remark?: string
  }>
}

export async function fetchMfgTasks(params: MfgTaskQuery) {
  const response = await http.get<ApiResponse<PageResult<MfgTask>>>('/manufacturing/tasks', { params })
  return response.data.data
}

export async function createMfgTask(payload: MfgTaskPayload) {
  const response = await http.post<ApiResponse<MfgTask>>('/manufacturing/tasks', payload)
  return response.data.data
}

export async function changeMfgTaskStatus(id: number, status: string) {
  const response = await http.put<ApiResponse<MfgTask>>(`/manufacturing/tasks/${id}/status`, { status })
  return response.data.data
}

export async function fetchMaterialIssues(params: { pageNo: number; pageSize: number; keyword?: string; status?: string; taskId?: number }) {
  const response = await http.get<ApiResponse<PageResult<MfgMaterialIssue>>>('/manufacturing/material-issues', { params })
  return response.data.data
}

export async function createMaterialIssue(payload: MfgMaterialIssuePayload) {
  const response = await http.post<ApiResponse<{ issue: MfgMaterialIssue }>>('/manufacturing/material-issues', payload)
  return response.data.data
}

export async function changeMaterialIssueStatus(id: number, status: string) {
  const response = await http.put<ApiResponse<MfgMaterialIssue>>(`/manufacturing/material-issues/${id}/status`, { status })
  return response.data.data
}
