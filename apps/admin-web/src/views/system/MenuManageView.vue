<template>
  <div class="page">
    <div class="page-toolbar">
      <div>
        <h1 class="page-title">菜单管理</h1>
        <p class="page-desc">维护目录、页面、按钮权限和侧边栏排序。</p>
      </div>
      <a-button v-permission="'system:menu:add'" type="primary" @click="openCreate">新增</a-button>
    </div>

    <a-table
      row-key="id"
      size="middle"
      :loading="loading"
      :columns="columns"
      :data-source="menus"
      :pagination="false"
      default-expand-all-rows
    >
      <template #bodyCell="{ column, record }">
        <template v-if="column.key === 'status'">
          <a-tag :color="record.status === 1 ? 'green' : 'red'">
            {{ record.status === 1 ? '启用' : '停用' }}
          </a-tag>
        </template>
        <template v-if="column.key === 'operation'">
          <a-space>
            <a-button v-permission="'system:menu:edit'" type="link" size="small" @click="openEdit(record)">编辑</a-button>
            <a-popconfirm title="确认删除这个菜单？" @confirm="remove(record)">
              <a-button v-permission="'system:menu:delete'" type="link" size="small" danger>删除</a-button>
            </a-popconfirm>
          </a-space>
        </template>
      </template>
    </a-table>

    <a-drawer
      v-model:open="drawerOpen"
      width="520"
      :title="editingId ? '编辑菜单' : '新增菜单'"
      destroy-on-close
    >
      <a-form layout="vertical" :model="form">
        <a-form-item label="上级菜单">
          <a-tree-select
            v-model:value="form.parentId"
            :tree-data="treeOptions"
            tree-default-expand-all
            allow-clear
            placeholder="不选则为顶级菜单"
          />
        </a-form-item>
        <a-form-item label="菜单类型">
          <a-select v-model:value="form.menuType">
            <a-select-option value="DIR">目录</a-select-option>
            <a-select-option value="PAGE">页面</a-select-option>
            <a-select-option value="BUTTON">按钮</a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item label="菜单名称">
          <a-input v-model:value="form.menuName" />
        </a-form-item>
        <a-form-item label="路由地址">
          <a-input v-model:value="form.routePath" />
        </a-form-item>
        <a-form-item label="组件路径">
          <a-input v-model:value="form.componentPath" />
        </a-form-item>
        <a-form-item label="权限码">
          <a-input v-model:value="form.permissionCode" />
        </a-form-item>
        <a-form-item label="图标">
          <a-input v-model:value="form.icon" />
        </a-form-item>
        <a-form-item label="排序">
          <a-input-number v-model:value="form.sortOrder" class="full-input" />
        </a-form-item>
        <a-form-item label="显示">
          <a-select v-model:value="form.visible">
            <a-select-option :value="1">显示</a-select-option>
            <a-select-option :value="0">隐藏</a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item label="状态">
          <a-select v-model:value="form.status">
            <a-select-option :value="1">启用</a-select-option>
            <a-select-option :value="0">停用</a-select-option>
          </a-select>
        </a-form-item>
      </a-form>
      <template #extra>
        <a-space>
          <a-button @click="drawerOpen = false">取消</a-button>
          <a-button type="primary" :loading="saving" @click="save">保存</a-button>
        </a-space>
      </template>
    </a-drawer>
  </div>
</template>

<script setup lang="ts">
import { message } from 'ant-design-vue'
import { computed, onMounted, reactive, ref } from 'vue'
import { deleteData, getData, postData, putData } from '@/api/http'
import type { MenuNode } from '@/stores/auth'

type MenuRow = MenuNode & Record<string, any>

const loading = ref(false)
const saving = ref(false)
const drawerOpen = ref(false)
const editingId = ref<number | null>(null)
const menus = ref<MenuRow[]>([])
const form = reactive<Record<string, any>>({})

const columns = [
  { title: '菜单名称', dataIndex: 'menuName', key: 'menuName' },
  { title: '类型', dataIndex: 'menuType', key: 'menuType', width: 100 },
  { title: '路由', dataIndex: 'routePath', key: 'routePath', width: 180 },
  { title: '权限码', dataIndex: 'permissionCode', key: 'permissionCode', width: 200 },
  { title: '排序', dataIndex: 'sortOrder', key: 'sortOrder', width: 90 },
  { title: '状态', dataIndex: 'status', key: 'status', width: 90 },
  { title: '操作', dataIndex: 'operation', key: 'operation', width: 150 }
]

const treeOptions = computed(() => [
  { title: '顶级菜单', value: 0, children: toTreeOptions(menus.value) }
])

function toTreeOptions(nodes: MenuRow[]): Array<{ title: string; value: number; children?: any[] }> {
  return nodes.map((node) => ({
    title: node.menuName,
    value: node.id,
    children: node.children?.length ? toTreeOptions(node.children as MenuRow[]) : undefined
  }))
}

async function load() {
  loading.value = true
  try {
    menus.value = await getData<MenuRow[]>('/system/menus/tree')
  } finally {
    loading.value = false
  }
}

function resetForm(row?: MenuRow) {
  Object.keys(form).forEach((key) => delete form[key])
  Object.assign(form, {
    parentId: row?.parentId ?? 0,
    menuType: row?.menuType ?? 'PAGE',
    menuName: row?.menuName,
    routePath: row?.routePath,
    componentPath: row?.componentPath,
    permissionCode: row?.permissionCode,
    icon: row?.icon,
    sortOrder: row?.sortOrder ?? 0,
    visible: row?.visible ?? 1,
    status: row?.status ?? 1
  })
}

function openCreate() {
  editingId.value = null
  resetForm()
  drawerOpen.value = true
}

function openEdit(row: MenuRow) {
  editingId.value = row.id
  resetForm(row)
  drawerOpen.value = true
}

async function save() {
  saving.value = true
  try {
    if (editingId.value) {
      await putData<void>(`/system/menus/${editingId.value}`, form)
    } else {
      await postData<void>('/system/menus', form)
    }
    message.success('保存成功')
    drawerOpen.value = false
    await load()
  } finally {
    saving.value = false
  }
}

async function remove(row: MenuRow) {
  await deleteData<void>(`/system/menus/${row.id}`)
  message.success('删除成功')
  await load()
}

onMounted(load)
</script>
