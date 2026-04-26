<template>
  <a-layout class="layout">
    <a-layout-sider width="220" theme="dark">
      <div class="brand">易泽 ERP</div>
      <a-menu theme="dark" mode="inline" :selectedKeys="[route.path]">
        <a-menu-item key="/dashboard" @click="router.push('/dashboard')">系统首页</a-menu-item>
        <a-menu-item key="/system" disabled>系统底座</a-menu-item>
        <a-menu-item key="/masterdata" disabled>基础资料</a-menu-item>
        <a-menu-item key="/inventory" disabled>库存</a-menu-item>
        <a-menu-item key="/purchase" disabled>采购</a-menu-item>
        <a-menu-item key="/sales" disabled>销售</a-menu-item>
        <a-menu-item key="/manufacturing" disabled>生产</a-menu-item>
        <a-menu-item key="/finance" disabled>财务</a-menu-item>
      </a-menu>
    </a-layout-sider>
    <a-layout>
      <a-layout-header class="header">单公司制造业 ERP</a-layout-header>
      <a-layout-content>
        <router-view />
      </a-layout-content>
    </a-layout>
  </a-layout>
</template>

<script setup lang="ts">
import { useRoute, useRouter } from 'vue-router'

const route = useRoute()
const router = useRouter()
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
  height: 56px;
  padding: 0 20px;
  background: #fff;
  border-bottom: 1px solid #e5e7eb;
  line-height: 56px;
}
</style>
