import { createRouter, createWebHistory } from 'vue-router'
import BasicLayout from '@/layouts/BasicLayout.vue'
import DashboardView from '@/views/dashboard/DashboardView.vue'
import LoginView from '@/views/LoginView.vue'
import MenuManageView from '@/views/system/MenuManageView.vue'
import SystemListView from '@/views/system/SystemListView.vue'
import { useAuthStore } from '@/stores/auth'

const router = createRouter({
  history: createWebHistory(),
  routes: [
    { path: '/login', component: LoginView },
    {
      path: '/',
      component: BasicLayout,
      children: [
        { path: '', redirect: '/dashboard' },
        { path: 'dashboard', component: DashboardView },
        { path: 'system/menus', component: MenuManageView },
        { path: 'system/:kind(users|roles|dicts|number-rules|logs)', component: SystemListView }
      ]
    }
  ]
})

router.beforeEach(async (to) => {
  const auth = useAuthStore()
  if (to.path === '/login') return true
  if (!auth.token) return '/login'
  if (!auth.user) {
    await auth.loadCurrent()
  }
  return true
})

export default router
