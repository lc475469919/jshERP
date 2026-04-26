import axios from 'axios'
import { message } from 'ant-design-vue'

export interface ApiResponse<T> {
  code: number
  message: string
  data: T
}

export interface PageResult<T> {
  total: number
  rows: T[]
}

const http = axios.create({
  baseURL: '/api',
  timeout: 15000
})

http.interceptors.response.use(
  (response) => {
    const body = response.data as ApiResponse<unknown>
    if (body && body.code && body.code !== 200) {
      message.error(body.message || '请求失败')
      return Promise.reject(new Error(body.message || '请求失败'))
    }
    return response
  },
  (error) => {
    message.error(error?.response?.data?.message || error.message || '网络异常')
    return Promise.reject(error)
  }
)

export default http
