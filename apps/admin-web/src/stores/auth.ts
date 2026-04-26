import { defineStore } from 'pinia'
import { getData, postData } from '@/api/http'

export interface UserProfile {
  id: number
  account: string
  name: string
  mobile?: string
  status: number
  roles: string[]
}

export interface MenuNode {
  id: number
  parentId: number
  menuType: string
  menuName: string
  routePath?: string
  permissionCode?: string
  icon?: string
  visible?: number
  children: MenuNode[]
}

interface LoginPayload {
  tokenName: string
  tokenValue: string
  user: UserProfile
  menus: MenuNode[]
  permissions: string[]
}

export const useAuthStore = defineStore('auth', {
  state: () => ({
    token: localStorage.getItem('yz_token') || '',
    user: null as UserProfile | null,
    menus: [] as MenuNode[],
    permissions: [] as string[]
  }),
  actions: {
    async login(account: string, password: string) {
      const payload = await postData<LoginPayload>('/auth/login', { account, password })
      this.apply(payload)
    },
    async loadCurrent() {
      if (!this.token) return
      const payload = await getData<LoginPayload>('/auth/me')
      this.apply(payload)
    },
    async logout() {
      await postData<void>('/auth/logout')
      this.clear()
    },
    apply(payload: LoginPayload) {
      this.token = payload.tokenValue
      this.user = payload.user
      this.menus = payload.menus
      this.permissions = payload.permissions
      localStorage.setItem('yz_token', payload.tokenValue)
    },
    clear() {
      this.token = ''
      this.user = null
      this.menus = []
      this.permissions = []
      localStorage.removeItem('yz_token')
    }
  }
})
