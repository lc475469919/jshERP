import axios from 'axios'

export interface ApiResponse<T> {
  code: number
  message: string
  data: T
}

export interface PageResult<T> {
  total: number
  page: number
  pageSize: number
  records: T[]
}

export const http = axios.create({
  baseURL: '/api',
  timeout: 15000
})

http.interceptors.request.use((config) => {
  const token = localStorage.getItem('yz_token')
  if (token) {
    config.headers.Authorization = token
  }
  return config
})

http.interceptors.response.use((response) => {
  const payload = response.data as ApiResponse<unknown>
  if (payload.code !== 200) {
    return Promise.reject(new Error(payload.message || '请求失败'))
  }
  return response
})

export async function getData<T>(url: string, params?: Record<string, unknown>) {
  const response = await http.get<ApiResponse<T>>(url, { params })
  return response.data.data
}

export async function postData<T>(url: string, data?: unknown) {
  const response = await http.post<ApiResponse<T>>(url, data)
  return response.data.data
}

export async function putData<T>(url: string, data?: unknown) {
  const response = await http.put<ApiResponse<T>>(url, data)
  return response.data.data
}

export async function deleteData<T>(url: string) {
  const response = await http.delete<ApiResponse<T>>(url)
  return response.data.data
}
