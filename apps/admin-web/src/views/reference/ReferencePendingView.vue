<template>
  <div class="page pending-page">
    <section class="pending-panel">
      <div class="pending-head">
        <div>
          <h1 class="page-title">{{ pageName }}</h1>
          <p class="page-desc">参考入口已接入，页面会按参考系统逐页采集后开发。</p>
        </div>
        <a-tag color="blue">待采集</a-tag>
      </div>

      <a-descriptions bordered size="middle" :column="1">
        <a-descriptions-item label="一级模块">{{ moduleName }}</a-descriptions-item>
        <a-descriptions-item label="二级分组">{{ groupName }}</a-descriptions-item>
        <a-descriptions-item label="页面入口">{{ pageName }}</a-descriptions-item>
        <a-descriptions-item label="参考路径">{{ referencePath || '待补充' }}</a-descriptions-item>
      </a-descriptions>

      <div class="capture-box">
        <h2>后续采集内容</h2>
        <div class="capture-grid">
          <span>页面标题</span>
          <span>筛选条件</span>
          <span>工具栏按钮</span>
          <span>表格列</span>
          <span>行内操作</span>
          <span>新增/修改表单</span>
          <span>状态枚举</span>
          <span>审核/反审核规则</span>
          <span>打印/导入/导出</span>
          <span>报表口径</span>
          <span>库存/财务影响</span>
          <span>小程序入口</span>
        </div>
      </div>
    </section>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useRoute } from 'vue-router'

const route = useRoute()
const moduleName = computed(() => String(route.query.module || ''))
const groupName = computed(() => String(route.query.group || ''))
const pageName = computed(() => String(route.query.name || '参考入口'))
const referencePath = computed(() => String(route.query.referencePath || ''))
</script>

<style scoped>
.pending-page {
  padding: 14px;
}

.pending-panel {
  min-height: calc(100vh - 150px);
  padding: 14px;
  background: #fff;
  border: 1px solid #dfe6ee;
}

.pending-head {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  margin-bottom: 16px;
}

.capture-box {
  margin-top: 16px;
  padding: 14px;
  background: #f8fafc;
  border: 1px solid #e3e9f1;
}

.capture-box h2 {
  margin: 0 0 12px;
  color: #1f2937;
  font-size: 16px;
}

.capture-grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 10px;
}

.capture-grid span {
  display: block;
  padding: 9px 10px;
  color: #475467;
  background: #fff;
  border: 1px solid #e3e9f1;
}
</style>
