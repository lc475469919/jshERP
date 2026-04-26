<template>
  <div class="login-page">
    <section class="login-panel">
      <div>
        <h1>易泽 ERP</h1>
        <p>单公司制造业管理系统</p>
      </div>
      <a-form layout="vertical" :model="form" @finish="submit">
        <a-form-item label="账号" name="account">
          <a-input v-model:value="form.account" autocomplete="username" />
        </a-form-item>
        <a-form-item label="密码" name="password">
          <a-input-password v-model:value="form.password" autocomplete="current-password" />
        </a-form-item>
        <a-alert v-if="error" type="error" :message="error" show-icon />
        <a-button type="primary" html-type="submit" block :loading="loading">登录</a-button>
      </a-form>
    </section>
  </div>
</template>

<script setup lang="ts">
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'

const router = useRouter()
const auth = useAuthStore()
const loading = ref(false)
const error = ref('')
const form = reactive({
  account: 'admin',
  password: 'admin123'
})

async function submit() {
  loading.value = true
  error.value = ''
  try {
    await auth.login(form.account, form.password)
    await router.push('/dashboard')
  } catch (exception) {
    error.value = exception instanceof Error ? exception.message : '登录失败'
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.login-page {
  display: grid;
  min-height: 100vh;
  place-items: center;
  background: #eef2f7;
}

.login-panel {
  display: grid;
  width: min(420px, calc(100vw - 32px));
  gap: 24px;
  padding: 32px;
  background: #fff;
  border: 1px solid #dde3ea;
  border-radius: 8px;
  box-shadow: 0 16px 36px rgb(15 23 42 / 8%);
}

h1 {
  margin: 0;
  color: #172033;
  font-size: 24px;
}

p {
  margin: 8px 0 0;
  color: #667085;
}
</style>
