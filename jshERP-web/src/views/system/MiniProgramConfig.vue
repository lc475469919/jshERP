<template>
  <a-card :bordered="false">
    <a-alert
      message="小程序配置用于电脑端统一登记 AppID、后端地址和发布域名。小程序首次连接前仍需要在小程序登录页使用可访问的后端地址。"
      type="info"
      showIcon
      style="margin-bottom: 16px"
    />
    <a-spin :spinning="loading">
      <a-form :form="form" :label-col="{ span: 5 }" :wrapper-col="{ span: 14 }">
        <a-form-item label="小程序 AppID">
          <a-input
            placeholder="例如 wxdc0470ce5f2a68df"
            v-decorator="['miniprogram_appid']"
          />
        </a-form-item>
        <a-form-item label="后端 API 地址">
          <a-input
            placeholder="例如 http://192.168.3.42:9999/jshERP-boot"
            v-decorator="['miniprogram_api_base_url']"
          />
        </a-form-item>
        <a-form-item label="request 合法域名">
          <a-input
            placeholder="发布版填写 HTTPS 域名，开发调试可填写局域网地址备注"
            v-decorator="['miniprogram_request_domain']"
          />
        </a-form-item>
        <a-form-item label="体验二维码地址">
          <a-input
            placeholder="可填写预览二维码或发布说明地址"
            v-decorator="['miniprogram_preview_qrcode']"
          />
        </a-form-item>
        <a-form-item label="备注">
          <a-textarea
            :rows="4"
            placeholder="填写发布环境、测试账号、配置说明等"
            v-decorator="['miniprogram_remark']"
          />
        </a-form-item>
        <a-form-item :wrapper-col="{ span: 14, offset: 5 }">
          <a-button type="primary" :loading="saving" @click="handleSave">保存配置</a-button>
          <a-button style="margin-left: 8px" @click="loadConfig">刷新</a-button>
        </a-form-item>
      </a-form>
    </a-spin>
  </a-card>
</template>

<script>
import { getAction, postAction, putAction } from '@/api/manage'

const CONFIG_ITEMS = [
  { key: 'miniprogram_appid', label: '小程序 AppID' },
  { key: 'miniprogram_api_base_url', label: '小程序后端 API 地址' },
  { key: 'miniprogram_request_domain', label: '小程序 request 合法域名' },
  { key: 'miniprogram_preview_qrcode', label: '小程序体验二维码地址' },
  { key: 'miniprogram_remark', label: '小程序备注' }
]

export default {
  name: 'MiniProgramConfig',
  data() {
    return {
      form: this.$form.createForm(this),
      records: {},
      loading: false,
      saving: false
    }
  },
  mounted() {
    this.loadConfig()
  },
  methods: {
    loadConfig() {
      this.loading = true
      Promise.all(CONFIG_ITEMS.map(item => this.loadOne(item))).then(values => {
        const formValue = {}
        const records = {}
        values.forEach(({ item, record }) => {
          records[item.key] = record
          formValue[item.key] = record && record.platformValue ? record.platformValue : ''
        })
        this.records = records
        this.form.setFieldsValue(formValue)
      }).catch(() => {
        this.$message.warning('小程序配置加载失败')
      }).finally(() => {
        this.loading = false
      })
    },
    loadOne(item) {
      return getAction('/platformConfig/getInfoByKey', { platformKey: item.key }).then(res => {
        return {
          item,
          record: res && res.code === 200 && res.data ? res.data : null
        }
      })
    },
    handleSave() {
      this.form.validateFields((err, values) => {
        if (err) {
          return
        }
        this.saving = true
        Promise.all(CONFIG_ITEMS.map(item => this.saveOne(item, values[item.key] || ''))).then(() => {
          this.$message.success('小程序配置已保存')
          this.loadConfig()
        }).catch(() => {
          this.$message.warning('小程序配置保存失败')
        }).finally(() => {
          this.saving = false
        })
      })
    },
    saveOne(item, value) {
      const record = this.records[item.key]
      const payload = {
        platformKey: item.key,
        platformKeyInfo: item.label,
        platformValue: value
      }
      if (record && record.id) {
        return putAction('/platformConfig/update', Object.assign({}, record, payload))
      }
      return postAction('/platformConfig/add', payload)
    }
  }
}
</script>
