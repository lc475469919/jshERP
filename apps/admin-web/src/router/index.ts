import { createRouter, createWebHistory } from 'vue-router'
import BasicLayout from '@/layouts/BasicLayout.vue'
import DashboardView from '@/views/dashboard/DashboardView.vue'
import CustomerInfoView from '@/views/customer/CustomerInfoView.vue'
import LoginView from '@/views/LoginView.vue'
import SalarySettingsView from '@/views/hr/SalarySettingsView.vue'
import MasterDataView from '@/views/masterdata/MasterDataView.vue'
import MenuManageView from '@/views/system/MenuManageView.vue'
import PurchaseOrderView from '@/views/purchase/PurchaseOrderView.vue'
import ReferencePendingView from '@/views/reference/ReferencePendingView.vue'
import SalesQuoteView from '@/views/sales/SalesQuoteView.vue'
import SalesOnlineOrderView from '@/views/sales/SalesOnlineOrderView.vue'
import SalesOrderView from '@/views/sales/SalesOrderView.vue'
import SalesBillView from '@/views/sales/SalesBillView.vue'
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
        { path: 'customer/customers', component: CustomerInfoView },
        { path: 'purchase/orders', component: PurchaseOrderView },
        { path: 'sales/quotes', component: SalesQuoteView },
        { path: 'sales/online-orders', component: SalesOnlineOrderView },
        { path: 'sales/orders', component: SalesOrderView },
        { path: 'sales/bills', component: SalesBillView },
        { path: 'reference-pending', component: ReferencePendingView },
        { path: 'hr/salary-settings', component: SalarySettingsView },
        { path: 'system/menus', component: MenuManageView },
        { path: 'system/:kind(users|roles|dicts|number-rules|logs)', component: SystemListView },
        { path: 'master-data/:kind(products|product-attrs|suppliers|projects|logistics)', component: MasterDataView }
      ]
    }
  ]
})

router.beforeEach(async (to) => {
  const auth = useAuthStore()
  if (to.path === '/login') return true
  if (!auth.token) return '/login'
  if (!auth.user) {
    try {
      await auth.loadCurrent()
    } catch {
      auth.clear()
      return '/login'
    }
  }
  return true
})

export default router
