<template>
  <div class="page">
    <div class="page-toolbar">
      <div>
        <h1 class="page-title">菜单管理</h1>
        <p class="page-desc">维护目录、页面、按钮权限和侧边栏排序。</p>
      </div>
      <a-button type="primary" disabled>新增</a-button>
    </div>

    <a-table
      row-key="id"
      size="middle"
      :loading="loading"
      :columns="columns"
      :data-source="menus"
      :pagination="false"
      default-expand-all-rows
    />
  </div>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { getData } from '@/api/http'
import type { MenuNode } from '@/stores/auth'

const loading = ref(false)
const menus = ref<MenuNode[]>([])
const columns = [
  { title: '菜单名称', dataIndex: 'menuName', key: 'menuName' },
  { title: '类型', dataIndex: 'menuType', key: 'menuType', width: 100 },
  { title: '路由', dataIndex: 'routePath', key: 'routePath', width: 180 },
  { title: '权限码', dataIndex: 'permissionCode', key: 'permissionCode', width: 200 },
  { title: '排序', dataIndex: 'sortOrder', key: 'sortOrder', width: 90 },
  { title: '状态', dataIndex: 'status', key: 'status', width: 90 }
]

async function load() {
  loading.value = true
  try {
    menus.value = await getData<MenuNode[]>('/system/menus/tree')
  } finally {
    loading.value = false
  }
}

onMounted(load)
</script>
