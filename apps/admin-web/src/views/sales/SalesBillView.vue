<template>
  <div class="page sales-bill-page">
    <div class="page-toolbar">
      <div>
        <h1 class="page-title">销售单</h1>
        <p class="page-desc">参考系统路径：销售 / 批发业务 / 销售单。</p>
      </div>
    </div>

    <div class="filter-panel">
      <a-space wrap>
        <span class="field-label">销售日期</span>
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
        <a-button @click="batchOpen = true">批量新增</a-button>
        <a-button :disabled="!selectedRows.length" @click="copyBill">复制</a-button>
        <a-button :disabled="!selectedRows.length" @click="auditBills">审核</a-button>
        <a-button :disabled="!selectedRows.length" @click="reverseAuditBills">反审核</a-button>
        <a-button :disabled="!selectedRows.length" @click="deliveryOpen = true">修改送货信息</a-button>
        <a-dropdown>
          <a-button :disabled="!selectedRows.length">发票管理</a-button>
          <template #overlay>
            <a-menu>
              <a-menu-item key="normal" @click="setInvoiceNeed('需要普票')">需要普票</a-menu-item>
              <a-menu-item key="special" @click="setInvoiceNeed('需要专票')">需要专票</a-menu-item>
              <a-menu-item key="none" @click="setInvoiceNeed('不要发票')">不要发票</a-menu-item>
            </a-menu>
          </template>
        </a-dropdown>
        <a-dropdown>
          <a-button danger :disabled="!selectedRows.length">生成单据</a-button>
          <template #overlay>
            <a-menu>
              <a-menu-item key="whole" @click="generatePurchase('采购单(整单)')">采购单(整单)</a-menu-item>
              <a-menu-item key="supplier" @click="generatePurchase('采购单(按供应商)')">采购单(按供应商)</a-menu-item>
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
              <a-menu-item key="stock" @click="goStockOut">去出库</a-menu-item>
              <a-menu-item key="receipt" @click="goReceipt">去收款</a-menu-item>
            </a-menu>
          </template>
        </a-dropdown>
        <a-dropdown>
          <a-button>导出</a-button>
          <template #overlay>
            <a-menu>
              <a-menu-item key="normal" @click="exportBills('不携带明细')">导出（不携带明细）</a-menu-item>
              <a-menu-item key="detail" @click="exportBills('携带明细')">导出（携带明细）</a-menu-item>
            </a-menu>
          </template>
        </a-dropdown>
      </a-space>
    </div>

    <a-table
      row-key="id"
      size="middle"
      :columns="columns"
      :data-source="records"
      :pagination="pagination"
      :row-selection="{ type: 'checkbox', selectedRowKeys, onChange: onSelectChange }"
      :scroll="{ x: 2840 }"
      @change="handleTableChange"
    >
      <template #bodyCell="{ column, record, text }">
        <a-space v-if="column.key === 'operation'" size="small">
          <a-button type="link" size="small" @click="openEdit(record)">修改</a-button>
          <a-button type="link" size="small" @click="openDetail(record)">查看</a-button>
          <a-button type="link" size="small" @click="repairOpen = true">返修</a-button>
        </a-space>
        <a-tag v-else-if="column.key === 'shzt'" :color="text === 1 ? 'green' : 'red'">{{ text === 1 ? '已审核' : '未审核' }}</a-tag>
        <a-tag v-else-if="column.key === 'ckzt'" :color="text === 1 ? 'green' : text === 2 ? 'blue' : 'default'">{{ stockStatusText(text) }}</a-tag>
        <a-tag v-else-if="column.key === 'skzt'" :color="text === 1 ? 'green' : text === 2 ? 'blue' : 'default'">{{ receiptStatusText(text) }}</a-tag>
        <a-tag v-else-if="column.key === 'kpbz'" :color="text ? 'orange' : 'default'">{{ text ? '需要' : '不需要' }}</a-tag>
        <a-tag v-else-if="column.key === 'kpzt'" :color="text === 2 ? 'green' : text === 1 ? 'blue' : 'default'">{{ invoiceStatusText(text) }}</a-tag>
        <a-tag v-else-if="column.key === 'cgdZt'" :color="text === 2 ? 'green' : text === 1 ? 'blue' : 'default'">{{ documentStatusText(text) }}</a-tag>
        <a-tag v-else-if="column.key === 'dhptDdbz'" :color="text === 1 ? 'blue' : 'default'">{{ text === 1 ? '是' : '否' }}</a-tag>
        <a-button v-else-if="column.key === 'id2'" type="link" size="small" @click="attachmentOpen = true">附件</a-button>
        <span v-else>{{ text }}</span>
      </template>
    </a-table>

    <a-drawer v-model:open="billOpen" width="1000" :title="editingId ? '修改销售单' : '新增销售单'">
      <a-form layout="vertical" :model="form">
        <a-row :gutter="12">
          <a-col :span="8"><a-form-item label="销售单号"><a-input v-model:value="form.djBh" placeholder="由编号规则生成" /></a-form-item></a-col>
          <a-col :span="8"><a-form-item label="销售日期"><a-date-picker v-model:value="form.djrq" class="full-input" /></a-form-item></a-col>
          <a-col :span="8"><a-form-item label="出库仓库"><a-input v-model:value="form.ckMc" /></a-form-item></a-col>
          <a-col :span="12"><a-form-item label="客户名称"><a-input v-model:value="form.khMc" /></a-form-item></a-col>
          <a-col :span="12"><a-form-item label="订单号"><a-input v-model:value="form.ddBh" /></a-form-item></a-col>
          <a-col :span="8"><a-form-item label="销售员"><a-input v-model:value="form.xsrMc" /></a-form-item></a-col>
          <a-col :span="8"><a-form-item label="归属项目"><a-input v-model:value="form.xmmc" /></a-form-item></a-col>
          <a-col :span="8"><a-form-item label="物流单号"><a-input v-model:value="form.shdh" /></a-form-item></a-col>
          <a-col :span="24"><a-form-item label="备注"><a-textarea v-model:value="form.remarks" :rows="3" /></a-form-item></a-col>
        </a-row>
      </a-form>

      <div class="detail-title">商品明细</div>
      <a-table size="small" :columns="detailColumns" :data-source="detailRows" :pagination="false" :scroll="{ x: 1500 }" />

      <template #extra>
        <a-space>
          <a-button @click="billOpen = false">取消</a-button>
          <a-button type="primary" @click="saveBill">保存</a-button>
        </a-space>
      </template>
    </a-drawer>

    <a-modal v-model:open="advancedOpen" title="高级搜索" @ok="query">
      <a-form layout="vertical" :model="filters">
        <a-row :gutter="12">
          <a-col :span="12"><a-form-item label="单据编号"><a-input v-model:value="filters.djBh" allow-clear /></a-form-item></a-col>
          <a-col :span="12"><a-form-item label="项目"><a-input v-model:value="filters.xmmc" allow-clear /></a-form-item></a-col>
          <a-col :span="12"><a-form-item label="销售员"><a-input v-model:value="filters.xsrMc" allow-clear /></a-form-item></a-col>
          <a-col :span="12"><a-form-item label="审核人"><a-input v-model:value="filters.shrMc" allow-clear /></a-form-item></a-col>
          <a-col :span="12"><a-form-item label="订单号"><a-input v-model:value="filters.ddBh" allow-clear /></a-form-item></a-col>
          <a-col :span="12"><a-form-item label="客户订单号"><a-input v-model:value="filters.khddh" allow-clear /></a-form-item></a-col>
          <a-col :span="12"><a-form-item label="出库仓库"><a-select v-model:value="filters.ckId"><a-select-option value="">全部</a-select-option><a-select-option value="raw">原材料库</a-select-option><a-select-option value="finished">成品库</a-select-option></a-select></a-form-item></a-col>
          <a-col :span="12"><a-form-item label="出库状态"><a-select v-model:value="filters.ckzt"><a-select-option value="">全部</a-select-option><a-select-option value="0">未出库</a-select-option><a-select-option value="1">已出库</a-select-option><a-select-option value="2">部分出库</a-select-option></a-select></a-form-item></a-col>
          <a-col :span="12"><a-form-item label="采购单"><a-select v-model:value="filters.cgdZt"><a-select-option value="">全部</a-select-option><a-select-option value="0">未生成</a-select-option><a-select-option value="1">部分生成</a-select-option><a-select-option value="2">全部生成</a-select-option><a-select-option value="3">未生成/部分生成</a-select-option></a-select></a-form-item></a-col>
          <a-col :span="12"><a-form-item label="国家/地区"><a-input v-model:value="filters.gjdqMc" allow-clear /></a-form-item></a-col>
          <a-col :span="12"><a-form-item label="需要发票"><a-select v-model:value="filters.kpbz"><a-select-option value="">全部</a-select-option><a-select-option value="0">不需要</a-select-option><a-select-option value="1">需要</a-select-option></a-select></a-form-item></a-col>
          <a-col :span="12"><a-form-item label="发票状态"><a-select v-model:value="filters.kpzt"><a-select-option value="">全部</a-select-option><a-select-option value="0">未开</a-select-option><a-select-option value="1">部分已开</a-select-option><a-select-option value="2">全部已开</a-select-option></a-select></a-form-item></a-col>
          <a-col :span="12"><a-form-item label="商城订单"><a-select v-model:value="filters.dhptDdbz"><a-select-option value="">全部</a-select-option><a-select-option value="0">否</a-select-option><a-select-option value="1">是</a-select-option></a-select></a-form-item></a-col>
          <a-col :span="12"><a-form-item label="物流单号"><a-input v-model:value="filters.shdh" allow-clear /></a-form-item></a-col>
          <a-col :span="12"><a-form-item label="物流公司"><a-input v-model:value="filters.wlgsmc" allow-clear /></a-form-item></a-col>
          <a-col :span="12"><a-form-item label="收款标志"><a-select v-model:value="filters.skzt"><a-select-option value="">全部</a-select-option><a-select-option value="0">未收款</a-select-option><a-select-option value="1">已收款</a-select-option><a-select-option value="2">部分收款</a-select-option></a-select></a-form-item></a-col>
        </a-row>
      </a-form>
    </a-modal>

    <a-modal v-model:open="deliveryOpen" title="修改送货信息" @ok="deliveryOpen = false">
      <a-form layout="vertical" :model="deliveryForm">
        <a-form-item label="送货方式"><a-select v-model:value="deliveryForm.shfs"><a-select-option value="1">自提</a-select-option><a-select-option value="2">送货</a-select-option><a-select-option value="3">物流</a-select-option></a-select></a-form-item>
        <a-row :gutter="12">
          <a-col :span="12"><a-form-item label="送货时间"><a-date-picker v-model:value="deliveryForm.shsj" class="full-input" /></a-form-item></a-col>
          <a-col :span="12"><a-form-item label="客户联系人"><a-input v-model:value="deliveryForm.shlxr" /></a-form-item></a-col>
          <a-col :span="12"><a-form-item label="联系电话"><a-input v-model:value="deliveryForm.shlxdh" /></a-form-item></a-col>
          <a-col :span="12"><a-form-item label="固定电话"><a-input v-model:value="deliveryForm.shgddh" /></a-form-item></a-col>
          <a-col :span="24"><a-form-item label="送货地址"><a-input v-model:value="deliveryForm.shdz" /></a-form-item></a-col>
          <a-col :span="12"><a-form-item label="物流公司"><a-input v-model:value="deliveryForm.wlgsmc" /></a-form-item></a-col>
          <a-col :span="24"><a-form-item label="物流单号"><a-input v-model:value="deliveryForm.shdh" /></a-form-item></a-col>
        </a-row>
      </a-form>
    </a-modal>

    <a-modal v-model:open="invoiceOpen" title="开票信息" @ok="invoiceOpen = false">
      <a-form layout="vertical">
        <a-form-item label="需要发票"><a-radio-group v-model:value="invoiceState.kpbz"><a-radio :value="0">不需要</a-radio><a-radio :value="1">需要</a-radio></a-radio-group></a-form-item>
        <a-form-item label="开票状态"><a-radio-group v-model:value="invoiceState.kpzt"><a-radio :value="0">未开</a-radio><a-radio :value="1">部分已开</a-radio><a-radio :value="2">全部已开</a-radio></a-radio-group></a-form-item>
      </a-form>
      <a-table size="small" row-key="id" :columns="invoiceColumns" :data-source="invoiceRows" :pagination="false" />
    </a-modal>

    <a-modal v-model:open="repairOpen" title="订单中商品选择" @ok="repairOpen = false">
      <a-table size="small" row-key="id" :columns="repairColumns" :data-source="repairRows" :pagination="false" :scroll="{ x: 700 }" />
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

    <a-modal v-model:open="batchOpen" title="批量新增" @ok="batchOpen = false">
      <a-form layout="vertical">
        <a-form-item label="交货日期"><a-date-picker class="full-input" /></a-form-item>
        <a-form-item label="结算日期"><a-date-picker class="full-input" /></a-form-item>
      </a-form>
    </a-modal>

    <a-modal v-model:open="attachmentOpen" title="附件" @ok="attachmentOpen = false">
      <a-table size="small" row-key="id" :columns="attachmentColumns" :data-source="attachments" :pagination="false" />
    </a-modal>
  </div>
</template>

<script setup lang="ts">
import type { TablePaginationConfig } from 'ant-design-vue'
import { message } from 'ant-design-vue'
import { computed, reactive, ref } from 'vue'

type Row = Record<string, any>

const filters = reactive<Row>({ shzt: '' })
const form = reactive<Row>({})
const deliveryForm = reactive<Row>({ shfs: '1' })
const invoiceState = reactive<Row>({ kpbz: 0, kpzt: 0 })
const billOpen = ref(false)
const advancedOpen = ref(false)
const deliveryOpen = ref(false)
const invoiceOpen = ref(false)
const repairOpen = ref(false)
const customerOpen = ref(false)
const goodsOpen = ref(false)
const batchOpen = ref(false)
const attachmentOpen = ref(false)
const editingId = ref<number | null>(null)
const selectedRowKeys = ref<number[]>([])
const selectedRows = ref<Row[]>([])
const selectedCustomer = ref<Row | null>(null)
const selectedGoods = ref<Row | null>(null)
const page = ref(1)
const pageSize = ref(20)

const records = ref<Row[]>([
  {
    id: 1,
    djBh: 'XS-20260428-0001',
    djrq: '2026-04-28',
    ckrq: '2026-04-28',
    ckMc: '成品库',
    khMc: '杭州示例客户',
    xsslHj: 12,
    xsjeHj: '3,600.00',
    seHj: '468.00',
    qtfyje: '0.00',
    yhje: '0.00',
    yhhje: '4,068.00',
    ddBh: 'SO-20260428-0001',
    khddh: 'KH-0428',
    xmmc: '常规销售',
    ckzt: 0,
    skzt: 2,
    kpbz: 1,
    kpzt: 0,
    cgdZt: 0,
    xsrMc: '王五',
    shdh: 'SF123456',
    shzt: 0,
    shrMc: '',
    shDate: '',
    dhptDdbz: 0,
    dycs: 0,
    remarks: '按参考系统字段展示',
    zdrMc: '管理员',
    createDate: '2026-04-28 14:00'
  }
])

const columns = [
  { key: 'operation', title: '操作', width: 130, fixed: 'left' as const },
  { dataIndex: 'djBh', key: 'djBh', title: '销售单号', width: 110, fixed: 'left' as const },
  { dataIndex: 'djrq', key: 'djrq', title: '销售日期', width: 90, fixed: 'left' as const },
  { dataIndex: 'ckrq', key: 'ckrq', title: '出库日期', width: 90 },
  { dataIndex: 'ckMc', key: 'ckMc', title: '出库仓库', width: 100 },
  { dataIndex: 'khMc', key: 'khMc', title: '客户名称', width: 140 },
  { dataIndex: 'xsslHj', key: 'xsslHj', title: '销售数量', width: 80 },
  { dataIndex: 'xsjeHj', key: 'xsjeHj', title: '金额(￥)', width: 100 },
  { dataIndex: 'seHj', key: 'seHj', title: '税额(￥)', width: 80 },
  { dataIndex: 'qtfyje', key: 'qtfyje', title: '其它费用(￥)', width: 90 },
  { dataIndex: 'yhje', key: 'yhje', title: '优惠金额(￥)', width: 90 },
  { dataIndex: 'yhhje', key: 'yhhje', title: '总计金额(￥)', width: 100 },
  { dataIndex: 'id2', key: 'id2', title: '附件', width: 70 },
  { dataIndex: 'ddBh', key: 'ddBh', title: '订单号', width: 120 },
  { dataIndex: 'khddh', key: 'khddh', title: '客户订单号', width: 110 },
  { dataIndex: 'xmmc', key: 'xmmc', title: '归属项目', width: 110 },
  { dataIndex: 'ckzt', key: 'ckzt', title: '出库状态', width: 70 },
  { dataIndex: 'skzt', key: 'skzt', title: '收款标志', width: 70 },
  { dataIndex: 'kpbz', key: 'kpbz', title: '需要发票', width: 80 },
  { dataIndex: 'kpzt', key: 'kpzt', title: '发票状态', width: 80 },
  { dataIndex: 'cgdZt', key: 'cgdZt', title: '采购单', width: 75 },
  { dataIndex: 'xsrMc', key: 'xsrMc', title: '销售员', width: 80 },
  { dataIndex: 'shdh', key: 'shdh', title: '物流单号', width: 100 },
  { dataIndex: 'shzt', key: 'shzt', title: '状态', width: 70 },
  { dataIndex: 'shrMc', key: 'shrMc', title: '审核人', width: 80 },
  { dataIndex: 'shDate', key: 'shDate', title: '审核时间', width: 130 },
  { dataIndex: 'dhptDdbz', key: 'dhptDdbz', title: '商城订单', width: 70 },
  { dataIndex: 'dycs', key: 'dycs', title: '打印次数', width: 80 },
  { dataIndex: 'remarks', key: 'remarks', title: '备注', width: 180 },
  { dataIndex: 'zdrMc', key: 'zdrMc', title: '制单人', width: 80 },
  { dataIndex: 'createDate', key: 'createDate', title: '制单日期', width: 130 }
]

const detailColumns = [
  { dataIndex: 'spBh', title: '商品编号', width: 100 },
  { dataIndex: 'spMc', title: '商品名称', width: 150 },
  { dataIndex: 'ggxh', title: '规格', width: 100 },
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
  { dataIndex: 'sjhj', title: '含税金额', width: 100 }
]
const detailRows = [{ spBh: 'SP-001', spMc: '示例商品', ggxh: '标准', xssl: 12, dwMc: '件', xsdj: '300.00', xsje: '3,600.00', spzk: 100, spzhdj: '300.00', spzhje: '3,600.00', sl: 13, hsdj: '339.00', se: '468.00', sjhj: '4,068.00' }]
const customers = [{ id: 1, khlbMc: '重点客户', khMc: '杭州示例客户', syLxr: '赵六', syPhone: '13800000000', qkje: '0.00' }]
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
  { dataIndex: 'sptxm', title: '商品条形码', width: 120 },
  { dataIndex: 'ggxh', title: '规格型号', width: 120 },
  { dataIndex: 'mrdwMc', title: '单位', width: 60 }
]
const repairRows = [{ id: 1, spBh: 'SP-001', spMc: '示例商品', ysMc: '蓝', ggxh: '标准', xssl: 12, dwMc: '件', scFxd: '未生成' }]
const repairColumns = [
  { dataIndex: 'spBh', title: '商品编号', width: 90 },
  { dataIndex: 'spMc', title: '商品名称', width: 130 },
  { dataIndex: 'ysMc', title: '颜色', width: 80 },
  { dataIndex: 'ggxh', title: '规格型号', width: 90 },
  { dataIndex: 'xssl', title: '数量', width: 70 },
  { dataIndex: 'dwMc', title: '单位', width: 60 },
  { dataIndex: 'scFxd', title: '返修单', width: 80 }
]
const invoiceRows = [{ id: 1, kprq: '2026-04-28', fpbh: 'FP-001', fpje: '0.00', fpLx: '普票', fptt: '杭州示例客户', remarks: '' }]
const invoiceColumns = [
  { dataIndex: 'kprq', title: '开票日期', width: 100 },
  { dataIndex: 'fpbh', title: '发票编号', width: 100 },
  { dataIndex: 'fpje', title: '发票金额', width: 100 },
  { dataIndex: 'fpLx', title: '类型', width: 80 },
  { dataIndex: 'fptt', title: '发票抬头', width: 130 },
  { dataIndex: 'remarks', title: '备注', width: 120 }
]
const attachments = [{ id: 1, tpmc: '销售附件.pdf' }]
const attachmentColumns = [
  { dataIndex: 'id', title: '操作', width: 80 },
  { dataIndex: 'tpmc', title: '附件', width: 350 }
]

const pagination = computed<TablePaginationConfig>(() => ({ current: page.value, pageSize: pageSize.value, total: records.value.length, showSizeChanger: true }))

function query() { page.value = 1; message.success('查询完成') }
function resetForm(row?: Row) { Object.keys(form).forEach((key) => delete form[key]); Object.assign(form, row || { djBh: '', ckMc: '', khMc: '', xsrMc: '', remarks: '' }) }
function openCreate() { editingId.value = null; resetForm(); billOpen.value = true }
function openEdit(row: Row) { editingId.value = row.id; resetForm(row); billOpen.value = true }
function openDetail(row: Row) { openEdit(row) }
function saveBill() { billOpen.value = false; message.success('保存成功') }
function copyBill() { message.success('已复制为新销售单') }
function auditBills() { selectedRows.value.forEach((row) => { row.shzt = 1; row.shrMc = '管理员'; row.shDate = '2026-04-28 14:30' }); message.success('审核成功') }
function reverseAuditBills() { selectedRows.value.forEach((row) => { row.shzt = 0; row.shrMc = ''; row.shDate = '' }); message.success('反审核成功') }
function setInvoiceNeed(name: string) { selectedRows.value.forEach((row) => { row.kpbz = name === '不要发票' ? 0 : 1; row.kpzt = 0 }); invoiceOpen.value = true; message.success(name) }
function generatePurchase(name: string) { selectedRows.value.forEach((row) => { row.cgdZt = name.includes('整单') ? 2 : 1 }); message.success(`${name}已生成`) }
function printSplit() { message.success('分单打印已进入队列') }
function printMerge() { message.success('合并打印已进入队列') }
function goStockOut() { selectedRows.value.forEach((row) => { row.ckzt = 1 }); message.success('已进入出库管理') }
function goReceipt() { selectedRows.value.forEach((row) => { row.skzt = 1 }); message.success('已进入收款单') }
function exportBills(type: string) { message.success(`导出${type}任务已创建`) }
function stockStatusText(value: number) { return value === 1 ? '已出库' : value === 2 ? '部分出库' : '未出库' }
function receiptStatusText(value: number) { return value === 1 ? '已收款' : value === 2 ? '部分收款' : '未收款' }
function invoiceStatusText(value: number) { return value === 2 ? '全部已开' : value === 1 ? '部分已开' : '未开' }
function documentStatusText(value: number) { return value === 2 ? '全部生成' : value === 1 ? '部分生成' : '未生成' }
function onSelectChange(keys: any[], rows: Row[]) { selectedRowKeys.value = keys as number[]; selectedRows.value = rows }
function handleTableChange(next: TablePaginationConfig) { page.value = next.current || 1; pageSize.value = next.pageSize || 20 }
function customerRow(row: Row) { return { onClick: () => { selectedCustomer.value = row } } }
function chooseCustomer() { if (selectedCustomer.value) filters.khMc = selectedCustomer.value.khMc; customerOpen.value = false }
function goodsRow(row: Row) { return { onClick: () => { selectedGoods.value = row } } }
function chooseGoods() { if (selectedGoods.value) filters.spMc = selectedGoods.value.spMc; goodsOpen.value = false }
</script>

<style scoped>
.sales-bill-page { padding: 14px; }
.filter-panel, .action-toolbar { margin-bottom: 12px; padding: 12px; background: #fff; border: 1px solid #dfe6ee; }
.field-label { color: #334155; font-size: 13px; }
.date-input { width: 128px; }
.date-separator { color: #94a3b8; }
.selector-picker { display: inline-flex; width: 174px; }
.selector-input { width: 126px; }
.status-select { width: 108px; }
.full-input { width: 100%; }
.detail-title { margin: 8px 0 10px; font-weight: 600; color: #1f2d3d; }
.modal-filter { width: 100%; margin-bottom: 12px; }
.modal-select { width: 150px; }
.modal-input { width: 220px; }
.modal-input-wide { width: 280px; }
</style>
