<template>
  <a-layout class="layout">
    <a-layout-sider width="220" theme="dark">
      <div class="brand">易泽 ERP</div>
      <a-menu theme="dark" mode="inline" :selectedKeys="[route.path]">
        <a-menu-item key="/dashboard" @click="router.push('/dashboard')">系统首页</a-menu-item>
        <a-sub-menu key="/system" title="系统管理">
          <a-menu-item key="/system/users" @click="router.push('/system/users')">用户管理</a-menu-item>
          <a-menu-item key="/system/roles" @click="router.push('/system/roles')">角色管理</a-menu-item>
          <a-menu-item key="/system/menus" @click="router.push('/system/menus')">菜单管理</a-menu-item>
          <a-menu-item key="/system/dicts" @click="router.push('/system/dicts')">字典管理</a-menu-item>
          <a-menu-item key="/system/number-rules" @click="router.push('/system/number-rules')">编号规则</a-menu-item>
          <a-menu-item key="/system/logs" @click="router.push('/system/logs')">操作日志</a-menu-item>
        </a-sub-menu>
        <a-sub-menu key="/master-data" title="基础资料">
          <a-sub-menu key="/master-data/product" title="商品管理">
            <a-menu-item key="/master-data/products" @click="router.push('/master-data/products')">商品信息</a-menu-item>
            <a-menu-item key="/master-data/product-attrs" @click="router.push('/master-data/product-attrs')">商品属性</a-menu-item>
          </a-sub-menu>
          <a-sub-menu key="/master-data/info" title="信息管理">
            <a-menu-item key="/master-data/suppliers" @click="router.push('/master-data/suppliers')">供应商管理</a-menu-item>
            <a-menu-item key="/master-data/projects" @click="router.push('/master-data/projects')">项目信息</a-menu-item>
            <a-menu-item key="/master-data/logistics" @click="router.push('/master-data/logistics')">物流公司</a-menu-item>
          </a-sub-menu>
        </a-sub-menu>
        <a-menu-item key="/inventory" disabled>库存</a-menu-item>
        <a-menu-item key="/purchase" disabled>采购</a-menu-item>
        <a-menu-item key="/sales" disabled>销售</a-menu-item>
        <a-menu-item key="/manufacturing" disabled>生产</a-menu-item>
        <a-menu-item key="/finance" disabled>财务</a-menu-item>
      </a-menu>
    </a-layout-sider>
    <a-layout>
      <a-layout-header class="header">
        <span>单公司制造业 ERP</span>
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
import { useRoute, useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'

const route = useRoute()
const router = useRouter()
const auth = useAuthStore()

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
  height: 56px;
  padding: 0 20px;
  color: #fff;
  font-size: 18px;
  font-weight: 700;
  line-height: 56px;
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

.user-name {
  color: #475467;
}
</style>
