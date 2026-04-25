import { createRouter, createWebHistory } from 'vue-router'
import BasicLayout from '@/layouts/BasicLayout.vue'
import DashboardView from '@/views/dashboard/DashboardView.vue'
import ManufacturingHome from '@/views/manufacturing/ManufacturingHome.vue'

const router = createRouter({
  history: createWebHistory(),
  routes: [
    {
      path: '/',
      component: BasicLayout,
      children: [
        { path: '', redirect: '/dashboard' },
        { path: 'dashboard', component: DashboardView },
        { path: 'manufacturing', component: ManufacturingHome }
      ]
    }
  ]
})

export default router
