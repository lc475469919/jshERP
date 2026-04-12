<template>
  <a-card :style="cardStyle" :bordered="false">
    <div class="table-operator">
      <a-button type="primary" icon="plus" @click="addConfig">新增审批流</a-button>
      <a-button type="primary" icon="reload" @click="loadData">刷新</a-button>
    </div>
    <a-alert
      message="审批配置在电脑端维护。每条流程选择一个适用单据，并按步骤顺序逐级审批，最后一步通过后单据才会审核。"
      type="info"
      show-icon
      style="margin-bottom: 16px" />
    <a-table
      rowKey="rowKey"
      size="middle"
      bordered
      :columns="columns"
      :dataSource="dataSource"
      :pagination="false"
      :loading="loading">
      <template slot="bill" slot-scope="text, record">
        <a-select
          style="width: 180px"
          showSearch
          allowClear
          optionFilterProp="children"
          placeholder="请选择单据"
          v-model="record.moduleKey"
          :disabled="!!record.id"
          @change="value => onBillTypeChange(value, record)">
          <a-select-option v-for="item in billTypeList" :key="item.key" :value="item.key">
            {{ item.value }}
          </a-select-option>
        </a-select>
      </template>
      <template slot="steps" slot-scope="text, record">
        <div class="step-list">
          <div v-for="(step, index) in record.steps" :key="index" class="step-row">
            <span class="step-no">第 {{ index + 1 }} 级</span>
            <a-select
              style="width: 220px"
              showSearch
              allowClear
              optionFilterProp="children"
              placeholder="请选择审批角色"
              v-model="step.approverRoleName"
              @change="value => onStepRoleChange(value, step)">
              <a-select-option v-for="role in roleList" :key="role.id" :value="role.name">
                {{ role.name }}
              </a-select-option>
            </a-select>
            <a-button size="small" style="margin-left: 8px" @click="moveStep(record, index, -1)" :disabled="index === 0">上移</a-button>
            <a-button size="small" style="margin-left: 8px" @click="moveStep(record, index, 1)" :disabled="index === record.steps.length - 1">下移</a-button>
            <a-button size="small" style="margin-left: 8px" @click="removeStep(record, index)" :disabled="record.steps.length === 1">删除</a-button>
          </div>
          <a-button size="small" icon="plus" @click="addStep(record)">新增审批级别</a-button>
        </div>
      </template>
      <template slot="enabled" slot-scope="text, record">
        <a-switch checked-children="启用" un-checked-children="关闭" v-model="record.enabled" />
      </template>
      <template slot="action" slot-scope="text, record">
        <a-button type="primary" size="small" :loading="record.saving" @click="saveConfig(record)">保存</a-button>
        <a-popconfirm title="确定删除该审批流吗？" @confirm="deleteConfig(record)">
          <a-button size="small" style="margin-left: 8px" :loading="record.deleting">删除</a-button>
        </a-popconfirm>
      </template>
    </a-table>
  </a-card>
</template>

<script>
import { getAction, postAction } from '@/api/manage'

export default {
  name: 'ApprovalConfigList',
  data() {
    return {
      cardStyle: '',
      loading: false,
      roleList: [],
      dataSource: [],
      tempId: 0,
      billTypeList: [
        { key: 'LSCK', value: '零售出库' },
        { key: 'LSTH', value: '零售退货' },
        { key: 'QGD', value: '请购单' },
        { key: 'CGDD', value: '采购订单' },
        { key: 'CGRK', value: '采购入库' },
        { key: 'CGTH', value: '采购退货' },
        { key: 'XSDD', value: '销售订单' },
        { key: 'XSCK', value: '销售出库' },
        { key: 'XSTH', value: '销售退货' },
        { key: 'QTRK', value: '其它入库单' },
        { key: 'QTCK', value: '其它出库单' },
        { key: 'DBCK', value: '调拨出库' },
        { key: 'ZZD', value: '组装单' },
        { key: 'CXD', value: '拆卸单' },
        { key: 'SR', value: '收入单' },
        { key: 'ZC', value: '支出单' },
        { key: 'SK', value: '收款单' },
        { key: 'FK', value: '付款单' },
        { key: 'ZZ', value: '转账单' },
        { key: 'SYF', value: '收预付款单' }
      ],
      columns: [
        { title: '适用单据', dataIndex: 'moduleKey', scopedSlots: { customRender: 'bill' }, width: 200 },
        { title: '审批流程', dataIndex: 'steps', scopedSlots: { customRender: 'steps' }, width: 520 },
        { title: '状态', dataIndex: 'enabled', scopedSlots: { customRender: 'enabled' }, width: 120 },
        { title: '操作', dataIndex: 'action', scopedSlots: { customRender: 'action' }, width: 170 }
      ]
    }
  },
  created() {
    this.loadRoles()
    this.loadData()
  },
  methods: {
    loadRoles() {
      const search = JSON.stringify({ name: '', description: '' })
      getAction('/role/list', { pageNo: 1, pageSize: 200, search }).then(res => {
        this.roleList = (res && res.data && res.data.rows) || res.rows || []
      })
    },
    loadData() {
      this.loading = true
      getAction('/approval/config/list').then(res => {
        const rows = (res && res.data) || []
        this.dataSource = rows.map(item => Object.assign({}, item, {
          rowKey: item.id,
          enabled: item.enabled !== false,
          steps: this.normalizeSteps(item),
          saving: false,
          deleting: false
        }))
      }).finally(() => {
        this.loading = false
      })
    },
    addConfig() {
      this.tempId += 1
      this.dataSource.unshift({
        rowKey: `new-${this.tempId}`,
        id: null,
        moduleKey: '',
        moduleName: '',
        billType: '',
        billSubType: '',
        enabled: true,
        steps: [{ stepNo: 1, approverRoleId: null, approverRoleName: '' }],
        saving: false,
        deleting: false
      })
    },
    onBillTypeChange(value, record) {
      const item = this.billTypeList.find(billType => billType.key === value)
      record.moduleName = item ? item.value : ''
      record.billSubType = item ? item.value : ''
    },
    normalizeSteps(record) {
      const source = record.steps && record.steps.length ? record.steps : [{
        approverRoleId: record.approverRoleId,
        approverRoleName: record.approverRoleName
      }]
      const steps = source.map((item, index) => ({
        stepNo: index + 1,
        approverRoleId: item.approverRoleId || null,
        approverRoleName: item.approverRoleName || ''
      }))
      return steps.length ? steps : [{ stepNo: 1, approverRoleId: null, approverRoleName: '' }]
    },
    onStepRoleChange(value, step) {
      const role = this.roleList.find(item => item.name === value)
      step.approverRoleId = role ? role.id : null
    },
    addStep(record) {
      record.steps.push({
        stepNo: record.steps.length + 1,
        approverRoleId: null,
        approverRoleName: ''
      })
    },
    removeStep(record, index) {
      record.steps.splice(index, 1)
      this.refreshStepNo(record)
    },
    moveStep(record, index, direction) {
      const nextIndex = index + direction
      if (nextIndex < 0 || nextIndex >= record.steps.length) {
        return
      }
      const current = record.steps.splice(index, 1)[0]
      record.steps.splice(nextIndex, 0, current)
      this.refreshStepNo(record)
    },
    refreshStepNo(record) {
      record.steps.forEach((step, index) => {
        step.stepNo = index + 1
      })
    },
    validateSteps(record) {
      if (!record.moduleKey) {
        this.$message.warning('请选择适用单据')
        return false
      }
      if (!record.steps || !record.steps.length) {
        this.$message.warning('请至少配置一个审批级别')
        return false
      }
      const invalid = record.steps.find(item => !item.approverRoleId && !item.approverRoleName)
      if (invalid) {
        this.$message.warning('审批级别中的角色不能为空')
        return false
      }
      return true
    },
    saveConfig(record) {
      if (!this.validateSteps(record)) {
        return
      }
      this.refreshStepNo(record)
      record.saving = true
      const firstStep = record.steps[0]
      const billType = this.billTypeList.find(item => item.key === record.moduleKey)
      postAction('/approval/config/save', {
        id: record.id,
        moduleKey: record.moduleKey,
        moduleName: record.moduleName || (billType ? billType.value : ''),
        billType: record.billType,
        billSubType: record.billSubType || (billType ? billType.value : ''),
        approverRoleId: firstStep.approverRoleId,
        approverRoleName: firstStep.approverRoleName,
        enabled: record.enabled,
        steps: record.steps
      }).then(res => {
        if (!res || res.code !== 200) {
          this.$message.error((res && res.data && res.data.message) || (res && res.data) || '保存失败')
          return
        }
        this.$message.success('保存成功')
        this.loadData()
      }).finally(() => {
        record.saving = false
      })
    },
    deleteConfig(record) {
      if (!record.id) {
        this.dataSource = this.dataSource.filter(item => item.rowKey !== record.rowKey)
        return
      }
      record.deleting = true
      postAction('/approval/config/delete', { id: record.id }).then(res => {
        if (!res || res.code !== 200) {
          this.$message.error((res && res.data && res.data.message) || (res && res.data) || '删除失败')
          return
        }
        this.$message.success('删除成功')
        this.loadData()
      }).finally(() => {
        record.deleting = false
      })
    }
  }
}
</script>

<style scoped>
.step-row {
  margin-bottom: 8px;
}

.step-no {
  display: inline-block;
  width: 70px;
}
</style>
