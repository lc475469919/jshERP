<template>
  <div class="page salary-page">
    <section class="salary-panel">
      <div class="page-toolbar">
        <div>
          <h1 class="page-title">员工薪资列表</h1>
          <p class="page-desc">参考系统路径：人事 / 人事管理 / 薪资设置。</p>
        </div>
      </div>

      <a-tabs v-model:active-key="salaryStatus">
        <a-tab-pane key="paid" tab="计薪员工" />
        <a-tab-pane key="unpaid" tab="不计薪员工" />
      </a-tabs>

      <div class="filter-panel">
        <a-space wrap>
          <a-select v-model:value="filters.department" class="department-select" allow-clear placeholder="所属部门">
            <a-select-option value="生产部">生产部</a-select-option>
            <a-select-option value="销售部">销售部</a-select-option>
            <a-select-option value="仓储部">仓储部</a-select-option>
          </a-select>
          <a-input v-model:value="filters.employeeName" class="name-input" allow-clear placeholder="员工姓名" @press-enter="query" />
          <a-button type="primary" @click="query">查询</a-button>
          <a-button @click="reset">重置</a-button>
        </a-space>
      </div>

      <div class="action-toolbar">
        <a-space wrap>
          <a-button type="primary" :disabled="!selectedRow" @click="openSalaryDrawer">修改</a-button>
          <a-button @click="importOpen = true">导入</a-button>
          <a-button>导出</a-button>
        </a-space>
      </div>

      <a-table
        row-key="id"
        size="middle"
        :columns="columns"
        :data-source="filteredRows"
        :pagination="{ pageSize: 20, total: filteredRows.length, showSizeChanger: true }"
        :row-selection="{ type: 'radio', selectedRowKeys, onChange: onSelectChange }"
        :scroll="{ x: 980 }"
      >
        <template #bodyCell="{ column, record, text }">
          <a-tag v-if="column.key === 'status'" :color="record.status === '计薪' ? 'green' : 'default'">{{ text }}</a-tag>
          <a-button v-else-if="column.key === 'action'" type="link" size="small" @click="openSalaryDrawer(record)">修改</a-button>
          <span v-else>{{ text }}</span>
        </template>
      </a-table>
    </section>

    <a-drawer v-model:open="drawerOpen" width="460" title="薪资信息">
      <a-form layout="vertical" :model="salaryForm">
        <a-form-item label="姓名">
          <a-input v-model:value="salaryForm.employeeName" disabled />
        </a-form-item>
        <a-form-item label="所属部门">
          <a-input v-model:value="salaryForm.department" disabled />
        </a-form-item>
        <a-form-item label="基本工资(￥)">
          <a-input-number v-model:value="salaryForm.baseSalary" class="full-input" :min="0" />
        </a-form-item>
        <a-form-item label="岗位工资(￥)">
          <a-input-number v-model:value="salaryForm.positionSalary" class="full-input" :min="0" />
        </a-form-item>
      </a-form>
      <template #extra>
        <a-space>
          <a-button @click="drawerOpen = false">取消</a-button>
          <a-button type="primary" @click="saveSalary">保存</a-button>
        </a-space>
      </template>
    </a-drawer>

    <a-modal v-model:open="importOpen" title="薪资导入" @ok="importOpen = false">
      <div class="import-guide">
        <h3>导入说明</h3>
        <p>请先下载固定模板，按模板填写员工姓名、基本工资和岗位工资。</p>
        <p>请勿修改模板表头，导入时系统会按员工信息匹配薪资档案。</p>
        <a-button>下载导入模板</a-button>
      </div>
    </a-modal>
  </div>
</template>

<script setup lang="ts">
import { computed, reactive, ref } from 'vue'
import { message } from 'ant-design-vue'

interface SalaryRow {
  id: number
  employeeName: string
  status: '计薪' | '不计薪'
  baseSalary: number
  positionSalary: number
  department: string
}

const salaryStatus = ref<'paid' | 'unpaid'>('paid')
const drawerOpen = ref(false)
const importOpen = ref(false)
const selectedRowKeys = ref<number[]>([])
const selectedRow = ref<SalaryRow | null>(null)
const filters = reactive({ department: undefined as string | undefined, employeeName: '' })
const salaryForm = reactive<SalaryRow>({
  id: 0,
  employeeName: '',
  status: '计薪',
  baseSalary: 0,
  positionSalary: 0,
  department: ''
})

const rows = ref<SalaryRow[]>([
  { id: 1, employeeName: '张三', status: '计薪', baseSalary: 0, positionSalary: 0, department: '生产部' },
  { id: 2, employeeName: '李四', status: '计薪', baseSalary: 0, positionSalary: 0, department: '仓储部' },
  { id: 3, employeeName: '王五', status: '不计薪', baseSalary: 0, positionSalary: 0, department: '销售部' }
])

const columns = [
  { dataIndex: 'employeeName', key: 'employeeName', title: '姓名', width: 140 },
  { dataIndex: 'status', key: 'status', title: '状态', width: 100 },
  { dataIndex: 'baseSalary', key: 'baseSalary', title: '基本工资(￥)', width: 140 },
  { dataIndex: 'positionSalary', key: 'positionSalary', title: '岗位工资(￥)', width: 140 },
  { dataIndex: 'department', key: 'department', title: '所属部门', width: 150 },
  { key: 'action', title: '操作', width: 100, fixed: 'right' as const }
]

const filteredRows = computed(() => rows.value.filter((row) => {
  const byStatus = salaryStatus.value === 'paid' ? row.status === '计薪' : row.status === '不计薪'
  const byDepartment = filters.department ? row.department === filters.department : true
  const byName = filters.employeeName ? row.employeeName.includes(filters.employeeName) : true
  return byStatus && byDepartment && byName
}))

function query() {
  selectedRow.value = null
  selectedRowKeys.value = []
}

function reset() {
  filters.department = undefined
  filters.employeeName = ''
  query()
}

function onSelectChange(keys: any[], selectedRows: SalaryRow[]) {
  selectedRowKeys.value = keys as number[]
  selectedRow.value = selectedRows[0] || null
}

function openSalaryDrawer(row?: SalaryRow) {
  const target = row || selectedRow.value
  if (!target) return
  Object.assign(salaryForm, target)
  drawerOpen.value = true
}

function saveSalary() {
  const index = rows.value.findIndex((row) => row.id === salaryForm.id)
  if (index >= 0) rows.value[index] = { ...salaryForm }
  message.success('保存成功')
  drawerOpen.value = false
}
</script>

<style scoped>
.salary-page {
  padding: 14px;
}

.salary-panel {
  min-height: calc(100vh - 150px);
  padding: 14px;
  background: #fff;
  border: 1px solid #dfe6ee;
}

.filter-panel,
.action-toolbar {
  margin-bottom: 12px;
  padding: 12px;
  background: #f8fafc;
  border: 1px solid #e3e9f1;
}

.department-select {
  width: 180px;
}

.name-input {
  width: 180px;
}

.import-guide h3 {
  margin: 0 0 10px;
  color: #1f2937;
  font-size: 16px;
}

.import-guide p {
  margin: 0 0 8px;
  color: #667085;
  line-height: 1.7;
}
</style>
