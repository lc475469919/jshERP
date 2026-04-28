<template>
  <div class="page sales-order-page">
    <div class="page-toolbar">
      <div>
        <h1 class="page-title">销售订单</h1>
        <p class="page-desc">参考系统路径：销售 / 批发业务 / 销售订单。</p>
      </div>
    </div>

    <a-tabs v-model:active-key="activeTab" class="order-tabs" @change="switchTab">
      <a-tab-pane key="normal" tab="正常" />
      <a-tab-pane key="closed" tab="已关闭" />
    </a-tabs>

    <div class="filter-panel">
      <a-space wrap>
        <span class="field-label">订单日期</span>
        <a-date-picker v-model:value="filters.ddrqq" class="date-input" />
        <span class="date-separator">-</span>
        <a-date-picker v-model:value="filters.ddrqz" class="date-input" />
        <a-input-group compact class="selector-picker">
          <a-input v-model:value="filters.khMc" class="selector-input" placeholder="客户" allow-clear @press-enter="query" />
          <a-button @click="customerOpen = true">...</a-button>
        </a-input-group>
        <a-input-group compact class="selector-picker">
          <a-input v-model:value="filters.spMc" class="selector-input" placeholder="商品" allow-clear @press-enter="query" />
          <a-button @click="goodsOpen = true">...</a-button>
        </a-input-group>
        <a-select v-model:value="filters.shzt" class="status-select" placeholder="审核状态" @change="query">
          <a-select-option value="">全部</a-select-option>
          <a-select-option value="0">未审核</a-select-option>
          <a-select-option value="1">已审核</a-select-option>
        </a-select>
        <a-button @click="advancedOpen = true">高级搜索</a-button>
        <a-button type="primary" @click="query">查询</a-button>
      </a-space>
    </div>

    <div class="action-toolbar">
      <a-space wrap>
        <a-button type="primary" @click="openCreate">新增</a-button>
        <a-button :disabled="!selectedRows.length" @click="copyOrder">复制</a-button>
        <a-button :disabled="!selectedRows.length" @click="auditOrders">审核</a-button>
        <a-button :disabled="!selectedRows.length" @click="reverseAuditOrders">反审核</a-button>
        <a-dropdown>
          <a-button danger :disabled="!selectedRows.length">生成单据</a-button>
          <template #overlay>
            <a-menu>
              <a-menu-item key="xsd" @click="generateBill('销售单')">生成销售单</a-menu-item>
              <a-menu-item key="cgd" @click="generateBill('采购单')">生成采购单</a-menu-item>
              <a-menu-item key="cgdd" @click="generateBill('采购订单')">生成采购订单</a-menu-item>
              <a-menu-item key="qgd" @click="generateBill('请购单')">生成请购单</a-menu-item>
            </a-menu>
          </template>
        </a-dropdown>
        <a-dropdown>
          <a-button :disabled="!selectedRows.length">生成生产任务</a-button>
          <template #overlay>
            <a-menu>
              <a-menu-item key="scrw" @click="productionTaskOpen = true">自制加工任务</a-menu-item>
              <a-menu-item key="batch-scrw" @click="productionTaskOpen = true">自制加工任务(批量)</a-menu-item>
              <a-menu-divider />
              <a-menu-item key="wwjg" @click="productionTaskOpen = true">委外加工任务</a-menu-item>
            </a-menu>
          </template>
        </a-dropdown>
        <a-dropdown>
          <a-button>批量打印</a-button>
          <template #overlay>
            <a-menu>
              <a-menu-item key="split" @click="printSplit">分单打印</a-menu-item>
              <a-menu-item key="merge" @click="printMerge">合并打印</a-menu-item>
            </a-menu>
          </template>
        </a-dropdown>
        <a-dropdown>
          <a-button :disabled="!selectedRows.length">更多操作</a-button>
          <template #overlay>
            <a-menu>
              <a-menu-item key="finish" @click="finishDelivery">办结销售发货</a-menu-item>
              <a-menu-item key="deposit" @click="depositOpen = true">收取订金</a-menu-item>
              <a-menu-divider />
              <a-menu-item key="close" @click="closeOrders">关闭</a-menu-item>
              <a-menu-item key="open" @click="openOrders">取消关闭</a-menu-item>
            </a-menu>
          </template>
        </a-dropdown>
        <a-button @click="settingOpen = true">设置</a-button>
        <a-button @click="exportOrders">导出</a-button>
      </a-space>
    </div>

    <a-table
      row-key="id"
      size="middle"
      :columns="columns"
      :data-source="records"
      :pagination="pagination"
      :row-selection="{ type: 'checkbox', selectedRowKeys, onChange: onSelectChange }"
      :scroll="{ x: 2520 }"
      @change="handleTableChange"
    >
      <template #bodyCell="{ column, record, text }">
        <a-space v-if="column.key === 'operation'" size="small">
          <a-button type="link" size="small" @click="openEdit(record)">修改</a-button>
          <a-button type="link" size="small" @click="openDetail(record)">查看</a-button>
        </a-space>
        <a-tag v-else-if="column.key === 'shzt'" :color="text === 1 ? 'green' : 'red'">{{ text === 1 ? '已审核' : '未审核' }}</a-tag>
        <a-tag v-else-if="column.key === 'xsdZt'" :color="text === 2 ? 'green' : text === 1 ? 'blue' : 'default'">{{ documentStatusText(text) }}</a-tag>
        <a-tag v-else-if="column.key === 'ckzt'" :color="text === 1 ? 'green' : 'default'">{{ text === 1 ? '已出库' : '未出库' }}</a-tag>
        <a-tag v-else-if="column.key === 'cgdZt'" :color="text === 1 ? 'green' : 'default'">{{ text === 1 ? '已生成' : '未生成' }}</a-tag>
        <a-tag v-else-if="column.key === 'scjgscBz' || column.key === 'wwjgdZt'" :color="text === 1 ? 'green' : 'default'">{{ text === 1 ? '已生成' : '未生成' }}</a-tag>
        <a-button v-else-if="column.key === 'id2'" type="link" size="small">附件</a-button>
        <span v-else>{{ text }}</span>
      </template>
    </a-table>

    <a-drawer v-model:open="orderOpen" width="980" :title="editingId ? '修改销售订单' : '新增销售订单'">
      <a-form layout="vertical" :model="form">
        <a-row :gutter="12">
          <a-col :span="8"><a-form-item label="销售订单号"><a-input v-model:value="form.djBh" placeholder="由编号规则生成" /></a-form-item></a-col>
          <a-col :span="8"><a-form-item label="订单日期"><a-date-picker v-model:value="form.djrq" class="full-input" /></a-form-item></a-col>
          <a-col :span="8"><a-form-item label="交货日期"><a-date-picker v-model:value="form.jhrq" class="full-input" /></a-form-item></a-col>
          <a-col :span="12"><a-form-item label="客户名称"><a-input v-model:value="form.khMc" /></a-form-item></a-col>
          <a-col :span="12"><a-form-item label="客户订单号"><a-input v-model:value="form.khddh" /></a-form-item></a-col>
          <a-col :span="8"><a-form-item label="销售员"><a-input v-model:value="form.xsrMc" /></a-form-item></a-col>
          <a-col :span="8"><a-form-item label="归属项目"><a-input v-model:value="form.xmmc" /></a-form-item></a-col>
          <a-col :span="8"><a-form-item label="订金(￥)"><a-input-number v-model:value="form.ysJe" class="full-input" :min="0" /></a-form-item></a-col>
          <a-col :span="24"><a-form-item label="备注"><a-textarea v-model:value="form.remarks" :rows="3" /></a-form-item></a-col>
        </a-row>
      </a-form>

      <div class="detail-title">商品明细</div>
      <a-table size="small" :columns="detailColumns" :data-source="detailRows" :pagination="false" :scroll="{ x: 1600 }" />

      <template #extra>
        <a-space>
          <a-button @click="orderOpen = false">取消</a-button>
          <a-button type="primary" @click="saveOrder">保存</a-button>
        </a-space>
      </template>
    </a-drawer>

    <a-modal v-model:open="advancedOpen" title="高级搜索" @ok="query">
      <a-form layout="vertical" :model="filters">
        <a-row :gutter="12">
          <a-col :span="12"><a-form-item label="单据编号"><a-input v-model:value="filters.djBh" allow-clear /></a-form-item></a-col>
          <a-col :span="12"><a-form-item label="项目"><a-input v-model:value="filters.xmmc" allow-clear /></a-form-item></a-col>
          <a-col :span="12"><a-form-item label="销售员"><a-input v-model:value="filters.xsrMc" allow-clear /></a-form-item></a-col>
          <a-col :span="12"><a-form-item label="客户订单号"><a-input v-model:value="filters.khddh" allow-clear /></a-form-item></a-col>
          <a-col :span="12"><a-form-item label="交货日期起"><a-date-picker v-model:value="filters.jhrqq" class="full-input" /></a-form-item></a-col>
          <a-col :span="12"><a-form-item label="交货日期止"><a-date-picker v-model:value="filters.jhrqz" class="full-input" /></a-form-item></a-col>
          <a-col :span="12"><a-form-item label="销售单"><a-select v-model:value="filters.xsdBz"><a-select-option value="">全部</a-select-option><a-select-option value="0">未生成</a-select-option><a-select-option value="1">部分生成</a-select-option><a-select-option value="2">全部生成</a-select-option><a-select-option value="3">未生成/部分生成</a-select-option></a-select></a-form-item></a-col>
          <a-col :span="12"><a-form-item label="生产任务"><a-select v-model:value="filters.scrwBz"><a-select-option value="">全部</a-select-option><a-select-option value="0">未生成</a-select-option><a-select-option value="2">部分生成</a-select-option><a-select-option value="1">全部生成</a-select-option></a-select></a-form-item></a-col>
          <a-col :span="12"><a-form-item label="委外加工"><a-select v-model:value="filters.wwjgBz"><a-select-option value="">全部</a-select-option><a-select-option value="0">未生成</a-select-option><a-select-option value="2">部分生成</a-select-option><a-select-option value="1">全部生成</a-select-option></a-select></a-form-item></a-col>
          <a-col :span="12"><a-form-item label="国家/地区"><a-input v-model:value="filters.gjdqMc" allow-clear /></a-form-item></a-col>
          <a-col :span="12"><a-form-item label="今需交货"><a-select v-model:value="filters.jxjhBz"><a-select-option value="">全部</a-select-option><a-select-option value="1">是</a-select-option></a-select></a-form-item></a-col>
          <a-col :span="12"><a-form-item label="备注"><a-input v-model:value="filters.remarks" allow-clear /></a-form-item></a-col>
        </a-row>
      </a-form>
    </a-modal>

    <a-modal v-model:open="customerOpen" title="选择客户" @ok="chooseCustomer">
      <a-space class="modal-filter" wrap>
        <a-select class="modal-select" placeholder="选择客户类别"><a-select-option value="">全部</a-select-option></a-select>
        <a-input class="modal-input" placeholder="客户名称/联系人/电话" />
        <a-button type="primary">查询</a-button>
      </a-space>
      <a-table size="small" row-key="id" :columns="customerColumns" :data-source="customers" :pagination="false" @row="customerRow" />
    </a-modal>

    <a-modal v-model:open="goodsOpen" title="选择商品" @ok="chooseGoods">
      <a-space class="modal-filter" wrap>
        <a-input class="modal-input-wide" placeholder="商品编号/名称/助记码/规格" />
        <a-button type="primary">查询</a-button>
      </a-space>
      <a-table size="small" row-key="id" :columns="goodsColumns" :data-source="goodsList" :pagination="false" @row="goodsRow" />
    </a-modal>

    <a-modal v-model:open="productionTaskOpen" title="订单中商品选择" @ok="generateProductionTask">
      <a-space class="modal-filter" wrap>
        <a-select class="modal-select" placeholder="仓库"><a-select-option value="">仓库</a-select-option></a-select>
        <a-select class="modal-select" placeholder="全部"><a-select-option value="">全部</a-select-option><a-select-option value="1">仅自制品</a-select-option><a-select-option value="2">仅委外品</a-select-option><a-select-option value="3">委外/自制品</a-select-option></a-select>
        <a-input class="modal-input-wide" placeholder="商品编号/名称/助记码/规格" />
      </a-space>
      <a-table size="small" row-key="id" :columns="productionColumns" :data-source="productionRows" :pagination="false" :scroll="{ x: 820 }" />
    </a-modal>

    <a-modal v-model:open="depositOpen" title="收取订金" @ok="collectDeposit">
      <a-form layout="vertical" :model="depositForm">
        <a-row :gutter="12">
          <a-col :span="12"><a-form-item label="单据日期"><a-date-picker v-model:value="depositForm.djRq" class="full-input" /></a-form-item></a-col>
          <a-col :span="12"><a-form-item label="结算账户"><a-input v-model:value="depositForm.jszh" /></a-form-item></a-col>
          <a-col :span="12"><a-form-item label="订金金额"><a-input-number v-model:value="depositForm.ysJe" class="full-input" :min="0" /></a-form-item></a-col>
          <a-col :span="24"><a-form-item label="备注"><a-textarea v-model:value="depositForm.remarks" :rows="3" /></a-form-item></a-col>
        </a-row>
      </a-form>
    </a-modal>

    <a-modal v-model:open="settingOpen" title="设置" @ok="settingOpen = false">
      <a-form layout="inline">
        <a-form-item label="根据交货日期提前">
          <a-input-number v-model:value="warningDays" :min="0" />
        </a-form-item>
        <span class="inline-text">天预警</span>
      </a-form>
    </a-modal>
  </div>
</template>

<script setup lang="ts">
import type { TablePaginationConfig } from 'ant-design-vue'
import { message } from 'ant-design-vue'
import { computed, reactive, ref } from 'vue'

type Row = Record<string, any>

const activeTab = ref('normal')
const filters = reactive<Row>({ shzt: '', closeFlag: 0 })
const form = reactive<Row>({})
const depositForm = reactive<Row>({})
const orderOpen = ref(false)
const advancedOpen = ref(false)
const customerOpen = ref(false)
const goodsOpen = ref(false)
const productionTaskOpen = ref(false)
const depositOpen = ref(false)
const settingOpen = ref(false)
const editingId = ref<number | null>(null)
const selectedRowKeys = ref<number[]>([])
const selectedRows = ref<Row[]>([])
const selectedCustomer = ref<Row | null>(null)
const selectedGoods = ref<Row | null>(null)
const page = ref(1)
const pageSize = ref(20)
const warningDays = ref(1)

const records = ref<Row[]>([
  {
    id: 1,
    djBh: 'SO-20260428-0001',
    djrq: '2026-04-28',
    khMc: '杭州示例客户',
    khddh: 'KH-0428',
    xsslHj: 80,
    yhhje: '18,800.00',
    ysJe: '2,000.00',
    jhrq: '2026-05-06',
    xsrMc: '王五',
    shzt: 0,
    shrMc: '',
    shDate: '',
    yskJe: '2,000.00',
    wskJe: '16,800.00',
    xsdZt: 0,
    ckzt: 0,
    cgdZt: 0,
    xsslScHj: 0,
    scjgscBz: 0,
    wwjgdZt: 0,
    dycs: 0,
    remarks: '按参考系统字段展示',
    zdrMc: '管理员',
    createDate: '2026-04-28 11:20'
  }
])

const columns = [
  { key: 'operation', title: '操作', width: 110, fixed: 'left' as const },
  { dataIndex: 'djBh', key: 'djBh', title: '销售订单号', width: 120, fixed: 'left' as const },
  { dataIndex: 'djrq', key: 'djrq', title: '订单日期', width: 90 },
  { dataIndex: 'khMc', key: 'khMc', title: '客户名称', width: 140 },
  { dataIndex: 'khddh', key: 'khddh', title: '客户订单号', width: 100 },
  { dataIndex: 'xsslHj', key: 'xsslHj', title: '数量', width: 80 },
  { dataIndex: 'yhhje', key: 'yhhje', title: '金额(￥)', width: 90 },
  { dataIndex: 'ysJe', key: 'ysJe', title: '订金(￥)', width: 90 },
  { dataIndex: 'id2', key: 'id2', title: '附件', width: 70 },
  { dataIndex: 'jhrq', key: 'jhrq', title: '交货日期', width: 90 },
  { dataIndex: 'xsrMc', key: 'xsrMc', title: '销售员', width: 80 },
  { dataIndex: 'shzt', key: 'shzt', title: '状态', width: 70 },
  { dataIndex: 'shrMc', key: 'shrMc', title: '审核人', width: 80 },
  { dataIndex: 'shDate', key: 'shDate', title: '审核时间', width: 130 },
  { dataIndex: 'yskJe', key: 'yskJe', title: '已收款(￥)', width: 90 },
  { dataIndex: 'wskJe', key: 'wskJe', title: '未收款(￥)', width: 90 },
  { dataIndex: 'xsdZt', key: 'xsdZt', title: '销售单', width: 75 },
  { dataIndex: 'ckzt', key: 'ckzt', title: '出库状态', width: 70 },
  { dataIndex: 'cgdZt', key: 'cgdZt', title: '采购单', width: 75 },
  { dataIndex: 'xsslScHj', key: 'xsslScHj', title: '转销售数量', width: 90 },
  { dataIndex: 'scjgscBz', key: 'scjgscBz', title: '生产任务', width: 75 },
  { dataIndex: 'wwjgdZt', key: 'wwjgdZt', title: '委外加工', width: 75 },
  { dataIndex: 'dycs', key: 'dycs', title: '打印次数', width: 80 },
  { dataIndex: 'remarks', key: 'remarks', title: '备注', width: 180 },
  { dataIndex: 'zdrMc', key: 'zdrMc', title: '制单人', width: 80 },
  { dataIndex: 'createDate', key: 'createDate', title: '制单日期', width: 130 }
]

const detailColumns = [
  { dataIndex: 'spBh', title: '商品编号', width: 100 },
  { dataIndex: 'spMc', title: '商品名称', width: 150 },
  { dataIndex: 'ggxh', title: '规格', width: 100 },
  { dataIndex: 'ysMc', title: '颜色', width: 60 },
  { dataIndex: 'xssl', title: '数量', width: 70 },
  { dataIndex: 'dwMc', title: '单位', width: 50 },
  { dataIndex: 'xsdj', title: '单价', width: 80 },
  { dataIndex: 'xsje', title: '金额', width: 80 },
  { dataIndex: 'spzk', title: '折扣(%)', width: 80 },
  { dataIndex: 'spzhdj', title: '折后价', width: 80 },
  { dataIndex: 'spzhje', title: '折后金额', width: 90 },
  { dataIndex: 'sl', title: '税率(%)', width: 80 },
  { dataIndex: 'hsdj', title: '含税单价', width: 90 },
  { dataIndex: 'se', title: '税额', width: 80 },
  { dataIndex: 'sjhj', title: '含税金额(￥)', width: 110 },
  { dataIndex: 'zpbz', title: '赠品', width: 60 },
  { dataIndex: 'sppp', title: '品牌', width: 80 },
  { dataIndex: 'remarks', title: '备注', width: 140 }
]

const detailRows = [{ spBh: 'SP-001', spMc: '示例商品', ggxh: '标准', ysMc: '蓝', xssl: 80, dwMc: '件', xsdj: '235.00', xsje: '18,800.00', spzk: 100, spzhdj: '235.00', spzhje: '18,800.00', sl: 13, hsdj: '265.55', se: '2,444.00', sjhj: '21,244.00', zpbz: '否', sppp: '默认', remarks: '' }]
const customers = [{ id: 1, khlbMc: '默认客户', khMc: '杭州示例客户', syLxr: '赵六', syPhone: '13800000000', qkje: '0.00' }]
const customerColumns = [
  { dataIndex: 'khlbMc', title: '客户类别', width: 100 },
  { dataIndex: 'khMc', title: '客户名称', width: 130 },
  { dataIndex: 'syLxr', title: '联系人', width: 100 },
  { dataIndex: 'syPhone', title: '手机号码', width: 120 }
]
const goodsList = [{ id: 1, spBh: 'SP-001', spMc: '示例商品', sptxm: '690000000001', ggxh: '标准', mrdwMc: '件' }]
const goodsColumns = [
  { dataIndex: 'spBh', title: '商品编号', width: 90 },
  { dataIndex: 'spMc', title: '商品名称', width: 130 },
  { dataIndex: 'sptxm', title: '条形码', width: 120 },
  { dataIndex: 'ggxh', title: '规格型号', width: 120 },
  { dataIndex: 'mrdwMc', title: '单位', width: 60 }
]
const productionRows = [{ id: 1, scWwSl: 20, dwMc: '件', spBh: 'SP-001', spMc: '示例商品', ggxh: '标准', xsddSl: 80, kykcsl: 20, scsl: 0, wwsl: 0, splbMc: '成品' }]
const productionColumns = [
  { dataIndex: 'scWwSl', title: '数量', width: 70 },
  { dataIndex: 'dwMc', title: '单位', width: 50 },
  { dataIndex: 'spBh', title: '商品编号', width: 90 },
  { dataIndex: 'spMc', title: '商品名称', width: 120 },
  { dataIndex: 'ggxh', title: '规格型号', width: 90 },
  { dataIndex: 'xsddSl', title: '订购数量', width: 80 },
  { dataIndex: 'kykcsl', title: '可用库存', width: 80 },
  { dataIndex: 'scsl', title: '已转生产', width: 80 },
  { dataIndex: 'wwsl', title: '已转委外', width: 80 },
  { dataIndex: 'splbMc', title: '商品类别', width: 90 }
]

const pagination = computed<TablePaginationConfig>(() => ({ current: page.value, pageSize: pageSize.value, total: records.value.length, showSizeChanger: true }))

function switchTab() {
  filters.closeFlag = activeTab.value === 'closed' ? 1 : 0
  query()
}

function query() {
  page.value = 1
  message.success('查询完成')
}

function resetForm(row?: Row) {
  Object.keys(form).forEach((key) => delete form[key])
  Object.assign(form, row || { djBh: '', khMc: '', khddh: '', xsrMc: '', ysJe: 0, remarks: '' })
}

function openCreate() { editingId.value = null; resetForm(); orderOpen.value = true }
function openEdit(row: Row) { editingId.value = row.id; resetForm(row); orderOpen.value = true }
function openDetail(row: Row) { openEdit(row) }
function saveOrder() { message.success('保存成功'); orderOpen.value = false }
function copyOrder() { message.success('已复制为新销售订单') }
function auditOrders() { selectedRows.value.forEach((row) => { row.shzt = 1; row.shrMc = '管理员'; row.shDate = '2026-04-28 11:30' }); message.success('审核成功') }
function reverseAuditOrders() { selectedRows.value.forEach((row) => { row.shzt = 0; row.shrMc = ''; row.shDate = '' }); message.success('反审核成功') }
function generateBill(name: string) { selectedRows.value.forEach((row) => { if (name === '销售单') row.xsdZt = 2; if (name === '采购单') row.cgdZt = 1; row.xsslScHj = row.xsslHj }); message.success(`${name}已生成`) }
function generateProductionTask() { selectedRows.value.forEach((row) => { row.scjgscBz = 1 }); productionTaskOpen.value = false; message.success('生产任务已生成') }
function printSplit() { message.success('分单打印已进入队列') }
function printMerge() { message.success('合并打印已进入队列') }
function finishDelivery() { selectedRows.value.forEach((row) => { row.ckzt = 1 }); message.success('销售发货已办结') }
function collectDeposit() { depositOpen.value = false; message.success('订金已登记') }
function closeOrders() { activeTab.value = 'closed'; selectedRows.value = []; selectedRowKeys.value = []; message.success('已关闭') }
function openOrders() { activeTab.value = 'normal'; message.success('已取消关闭') }
function exportOrders() { message.success('导出任务已创建') }
function documentStatusText(value: number) { return value === 2 ? '全部生成' : value === 1 ? '部分生成' : '未生成' }

function onSelectChange(keys: any[], rows: Row[]) {
  selectedRowKeys.value = keys as number[]
  selectedRows.value = rows
}

function handleTableChange(next: TablePaginationConfig) {
  page.value = next.current || 1
  pageSize.value = next.pageSize || 20
}

function customerRow(row: Row) {
  return { onClick: () => { selectedCustomer.value = row } }
}

function chooseCustomer() {
  if (selectedCustomer.value) filters.khMc = selectedCustomer.value.khMc
  customerOpen.value = false
}

function goodsRow(row: Row) {
  return { onClick: () => { selectedGoods.value = row } }
}

function chooseGoods() {
  if (selectedGoods.value) filters.spMc = selectedGoods.value.spMc
  goodsOpen.value = false
}
</script>

<style scoped>
.sales-order-page { padding: 14px; }
.order-tabs { margin-bottom: 8px; background: #fff; border: 1px solid #dfe6ee; padding: 0 12px; }
.filter-panel, .action-toolbar { margin-bottom: 12px; padding: 12px; background: #fff; border: 1px solid #dfe6ee; }
.field-label { color: #334155; font-size: 13px; }
.date-input { width: 128px; }
.date-separator { color: #94a3b8; }
.selector-picker { display: inline-flex; width: 178px; }
.selector-input { width: 130px; }
.status-select { width: 100px; }
.full-input { width: 100%; }
.detail-title { margin: 8px 0 10px; font-weight: 600; color: #1f2d3d; }
.modal-filter { width: 100%; margin-bottom: 12px; }
.modal-select { width: 150px; }
.modal-input { width: 220px; }
.modal-input-wide { width: 280px; }
.inline-text { display: inline-flex; align-items: center; color: #475467; }
</style>
