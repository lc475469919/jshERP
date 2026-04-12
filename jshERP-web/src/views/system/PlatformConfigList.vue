<template>
  <a-row :gutter="24">
    <a-col :md="24">
      <a-card :style="cardStyle" :bordered="false">
        <a-alert
          message="小程序配置已并入平台配置，统一在此维护 AppID、API 地址与 request 合法域名。"
          type="info"
          show-icon
          style="margin-bottom: 16px" />
        <a-spin :spinning="miniConfigLoading">
          <a-form :label-col="{ span: 4 }" :wrapper-col="{ span: 14 }">
            <a-form-item label="小程序 AppID">
              <a-input v-model="miniConfig.miniprogram_appid" placeholder="例如 wxdc0470ce5f2a68df" />
            </a-form-item>
            <a-form-item label="后端 API 地址">
              <a-input v-model="miniConfig.miniprogram_api_base_url" placeholder="例如 http://127.0.0.1:9999/jshERP-boot" />
            </a-form-item>
            <a-form-item label="request 合法域名">
              <a-input v-model="miniConfig.miniprogram_request_domain" placeholder="发布版填写 HTTPS 域名" />
            </a-form-item>
            <a-form-item label="体验二维码地址">
              <a-input v-model="miniConfig.miniprogram_preview_qrcode" placeholder="可填写体验二维码或发布说明地址" />
            </a-form-item>
            <a-form-item label="备注">
              <a-textarea v-model="miniConfig.miniprogram_remark" :rows="3" placeholder="填写发布说明或测试备注" />
            </a-form-item>
            <a-form-item :wrapper-col="{ span: 14, offset: 4 }">
              <a-button type="primary" :loading="miniConfigSaving" @click="saveMiniConfig">保存小程序配置</a-button>
              <a-button style="margin-left: 8px" @click="loadMiniConfig">刷新</a-button>
            </a-form-item>
          </a-form>
        </a-spin>
        <a-divider />
        <!-- table区域-begin -->
        <div>
          <a-table
            ref="table"
            size="middle"
            bordered
            rowKey="id"
            :columns="columns"
            :dataSource="dataSource"
            :pagination="ipagination"
            :scroll="scroll"
            :loading="loading"
            @change="handleTableChange">
            <span slot="action" slot-scope="text, record">
              <a @click="handleEdit(record)">编辑</a>
            </span>
          </a-table>
        </div>
        <!-- table区域-end -->
        <!-- 表单区域 -->
        <platform-config-modal ref="modalForm" @ok="modalFormOk"></platform-config-modal>
      </a-card>
    </a-col>
  </a-row>
</template>
<!-- f r o m 7 5  2 7 1  8 9 2 0 -->
<script>
  import PlatformConfigModal from './modules/PlatformConfigModal'
  import { JeecgListMixin } from '@/mixins/JeecgListMixin'
  import { getAction, postAction, putAction } from '@/api/manage'

  const MINI_CONFIG_ITEMS = [
    { key: 'miniprogram_appid', label: '小程序 AppID' },
    { key: 'miniprogram_api_base_url', label: '小程序后端 API 地址' },
    { key: 'miniprogram_request_domain', label: '小程序 request 合法域名' },
    { key: 'miniprogram_preview_qrcode', label: '小程序体验二维码地址' },
    { key: 'miniprogram_remark', label: '小程序备注' }
  ]
  export default {
    name: "PlatformConfigList",
    mixins:[JeecgListMixin],
    components: {
      PlatformConfigModal
    },
    data () {
      return {
        currentRoleId: '',
        miniConfigLoading: false,
        miniConfigSaving: false,
        miniConfigRecords: {},
        miniConfig: {
          miniprogram_appid: '',
          miniprogram_api_base_url: '',
          miniprogram_request_domain: '',
          miniprogram_preview_qrcode: '',
          miniprogram_remark: ''
        },
        labelCol: {
          span: 5
        },
        wrapperCol: {
          span: 18,
          offset: 1
        },
        // 查询条件
        queryParam: {platformKey:'',},
        // 表头
        columns: [
          {
            title: '#',
            dataIndex: '',
            key:'rowIndex',
            width:40,
            align:"center",
            customRender:function (t,r,index) {
              return parseInt(index)+1;
            }
          },
          {
            title: '操作',
            dataIndex: 'action',
            align:"center",
            width: 100,
            scopedSlots: { customRender: 'action' },
          },
          {
            title: '配置名称',
            dataIndex: 'platformKeyInfo',
            width: 100
          },
          {
            title: '配置值',
            dataIndex: 'platformValue',
            width: 500
          }
        ],
        url: {
          list: "/platformConfig/list",
          delete: "/platformConfig/delete",
          deleteBatch: "/platformConfig/deleteBatch"
        },
      }
    },
    created() {
      this.loadMiniConfig()
    },
    methods: {
      loadMiniConfig() {
        this.miniConfigLoading = true
        Promise.all(MINI_CONFIG_ITEMS.map(item => this.loadMiniConfigByKey(item))).then((rows) => {
          const records = {}
          const values = {}
          rows.forEach((row) => {
            records[row.item.key] = row.record
            values[row.item.key] = row.record && row.record.platformValue ? row.record.platformValue : ''
          })
          this.miniConfigRecords = records
          this.miniConfig = values
        }).finally(() => {
          this.miniConfigLoading = false
        })
      },
      loadMiniConfigByKey(item) {
        return getAction('/platformConfig/getInfoByKey', { platformKey: item.key }).then((res) => {
          return {
            item,
            record: res && res.code === 200 && res.data ? res.data : null
          }
        }).catch(() => ({ item, record: null }))
      },
      saveMiniConfig() {
        this.miniConfigSaving = true
        Promise.all(MINI_CONFIG_ITEMS.map(item => this.saveMiniConfigByKey(item))).then(() => {
          this.$message.success('小程序配置保存成功')
          this.loadMiniConfig()
          this.loadData()
        }).finally(() => {
          this.miniConfigSaving = false
        })
      },
      saveMiniConfigByKey(item) {
        const record = this.miniConfigRecords[item.key]
        const payload = {
          platformKey: item.key,
          platformKeyInfo: item.label,
          platformValue: this.miniConfig[item.key] || ''
        }
        if (record && record.id) {
          return putAction('/platformConfig/update', Object.assign({}, record, payload))
        }
        return postAction('/platformConfig/add', payload)
      },
      handleEdit: function (record) {
        this.$refs.modalForm.edit(record);
        this.$refs.modalForm.title = "编辑";
        this.$refs.modalForm.disableSubmit = false;
        if(this.btnEnableList.indexOf(1)===-1) {
          this.$refs.modalForm.isReadOnly = true
        }
      }
    }
  }
</script>
<style scoped>
  @import '~@assets/less/common.less'
</style>
