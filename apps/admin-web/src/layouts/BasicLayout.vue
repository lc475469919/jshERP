<template>
  <a-layout class="layout">
    <a-layout-sider width="232" theme="dark">
      <div class="brand">
        <span class="brand-mark">YZ</span>
        <span>易泽 ERP</span>
      </div>
      <a-menu theme="dark" mode="inline" :selectedKeys="[route.path]" :openKeys="openKeys">
        <a-menu-item key="/dashboard" @click="router.push('/dashboard')">系统首页</a-menu-item>
        <template v-for="item in visibleMenus" :key="item.id">
          <component :is="renderMenu(item)" />
        </template>
        <a-menu-item v-for="item in placeholderMenus" :key="item.key" disabled>{{ item.title }}</a-menu-item>
      </a-menu>
    </a-layout-sider>
    <a-layout>
      <a-layout-header class="header">
        <div>
          <strong>单公司制造业 ERP</strong>
          <span class="header-sub">菜单按角色权限动态展示</span>
        </div>
        <a-space>
          <span class="user-name">{{ auth.user?.name || '未登录' }}</span>
          <a-button size="small" @click="logout">退出</a-button>
        </a-space>
      </a-layout-header>
      <a-layout-content>
        <router-view />
      </a-layout-content>
    </a-layout>
  </a-layout>
</template>

<script setup lang="ts">
import { computed, h, type VNode } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { Menu } from 'ant-design-vue'
import { useAuthStore, type MenuNode } from '@/stores/auth'

const route = useRoute()
const router = useRouter()
const auth = useAuthStore()

const placeholderMenus = [
  { key: '/inventory', title: '库管' },
  { key: '/purchase', title: '采购' },
  { key: '/sales', title: '销售' },
  { key: '/manufacturing', title: '生产' },
  { key: '/logistics', title: '物流' },
  { key: '/finance', title: '财务' },
  { key: '/general-ledger', title: '总账' },
  { key: '/crm', title: '客户' },
  { key: '/hr', title: '人事' }
]

const visibleMenus = computed(() => auth.menus.filter((item) => item.menuType !== 'BUTTON' && item.visible !== 0))
const openKeys = computed(() => {
  const keys: string[] = []
  collectOpenKeys(visibleMenus.value, keys)
  return keys
})

function collectOpenKeys(items: MenuNode[], keys: string[]) {
  items.forEach((item) => {
    if (route.path.startsWith(item.routePath || '---') && item.children?.length) {
      keys.push(String(item.id))
    }
    collectOpenKeys(item.children || [], keys)
  })
}

function renderMenu(item: MenuNode): VNode {
  const children = (item.children || []).filter((child) => child.menuType !== 'BUTTON' && child.visible !== 0)
  if (children.length) {
    return h(Menu.SubMenu, { key: String(item.id), title: item.menuName }, () => children.map((child) => renderMenu(child)))
  }
  return h(Menu.Item, { key: item.routePath, onClick: () => item.routePath && router.push(item.routePath) }, () => item.menuName)
}

async function logout() {
  await auth.logout()
  await router.push('/login')
}
</script>

<style scoped>
.layout {
  min-height: 100vh;
}

.brand {
  display: flex;
  align-items: center;
  gap: 10px;
  height: 56px;
  padding: 0 18px;
  color: #fff;
  font-size: 17px;
  font-weight: 700;
  line-height: 56px;
}

.brand-mark {
  display: grid;
  width: 28px;
  height: 28px;
  place-items: center;
  background: #1677ff;
  border-radius: 4px;
  font-size: 12px;
}

.header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  height: 56px;
  padding: 0 20px;
  background: #fff;
  border-bottom: 1px solid #e5e7eb;
  line-height: 56px;
}

.header-sub {
  margin-left: 12px;
  color: #667085;
  font-size: 13px;
  font-weight: 400;
}

.user-name {
  color: #475467;
}
</style>
