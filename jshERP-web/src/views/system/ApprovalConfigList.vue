<template>
  <a-card :style="cardStyle" :bordered="false">
    <div class="table-operator">
      <a-button type="primary" icon="reload" @click="loadData">刷新</a-button>
    </div>
    <a-alert
      message="审批配置在电脑端维护。每个模块可设置多级审批角色，按步骤顺序逐级审批，最后一步通过后单据才会审核。"
      type="info"
      show-icon
      style="margin-bottom: 16px" />
    <a-table
      rowKey="id"
      size="middle"
      bordered
      :columns="columns"
      :dataSource="dataSource"
      :pagination="false"
      :loading="loading">
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
      columns: [
        { title: '模块', dataIndex: 'moduleName', width: 120 },
        { title: '模块编码', dataIndex: 'moduleKey', width: 120 },
        { title: '单据类型', dataIndex: 'billSubType', width: 120 },
        { title: '审批流程', dataIndex: 'steps', scopedSlots: { customRender: 'steps' }, width: 520 },
        { title: '状态', dataIndex: 'enabled', scopedSlots: { customRender: 'enabled' }, width: 120 },
        { title: '操作', dataIndex: 'action', scopedSlots: { customRender: 'action' }, width: 100 }
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
          enabled: item.enabled !== false,
          steps: this.normalizeSteps(item),
          saving: false
        }))
      }).finally(() => {
        this.loading = false
      })
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
      postAction('/approval/config/save', {
        id: record.id,
        moduleKey: record.moduleKey,
        moduleName: record.moduleName,
        billType: record.billType,
        billSubType: record.billSubType,
        approverRoleId: firstStep.approverRoleId,
        approverRoleName: firstStep.approverRoleName,
        enabled: record.enabled,
        steps: record.steps
      }).then(() => {
        this.$message.success('保存成功')
        this.loadData()
      }).finally(() => {
        record.saving = false
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
