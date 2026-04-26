<template>
  <div class="login-page">
    <div class="login-shell">
      <section class="login-copy">
        <div class="logo-mark">YZ</div>
        <h1>易泽 ERP</h1>
        <p>按制造业版业务逻辑构建，覆盖基础资料、进销存、生产、人事工资和移动协同。</p>
        <div class="version-row">
          <button class="version active" type="button">制造业版</button>
          <button class="version" type="button">单公司模式</button>
          <button class="version" type="button">小程序协同</button>
        </div>
      </section>

      <section class="login-panel">
        <div class="panel-head">
          <h2>账号登录</h2>
          <span>体验账号已预填</span>
        </div>
        <a-form layout="vertical" :model="form" @finish="submit">
          <a-form-item label="账号" name="account">
            <a-input v-model:value="form.account" size="large" autocomplete="username" placeholder="请输入账号/手机号" />
          </a-form-item>
          <a-form-item label="密码" name="password">
            <a-input-password v-model:value="form.password" size="large" autocomplete="current-password" placeholder="请输入密码" />
          </a-form-item>
          <div class="login-options">
            <a-checkbox>记住密码</a-checkbox>
            <a href="javascript:void(0)">忘记密码了？</a>
          </div>
          <a-alert v-if="error" type="error" :message="error" show-icon />
          <a-button type="primary" size="large" html-type="submit" block :loading="loading">立即登录</a-button>
        </a-form>
      </section>
    </div>
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
  overflow: hidden;
  background:
    linear-gradient(120deg, rgb(15 64 115 / 84%), rgb(15 118 110 / 76%)),
    url("https://images.unsplash.com/photo-1497366754035-f200968a6e72?auto=format&fit=crop&w=1800&q=80") center/cover;
}

.login-shell {
  display: grid;
  grid-template-columns: minmax(360px, 1fr) 390px;
  gap: 72px;
  width: min(1040px, calc(100vw - 40px));
  align-items: center;
}

.login-copy {
  color: #fff;
}

.logo-mark {
  display: grid;
  width: 68px;
  height: 68px;
  place-items: center;
  margin-bottom: 28px;
  background: rgb(255 255 255 / 18%);
  border: 1px solid rgb(255 255 255 / 26%);
  border-radius: 8px;
  font-size: 22px;
  font-weight: 800;
}

h1 {
  margin: 0;
  font-size: 44px;
  letter-spacing: 0;
}

p {
  max-width: 520px;
  margin: 18px 0 28px;
  color: rgb(255 255 255 / 86%);
  font-size: 17px;
  line-height: 1.8;
}

.version-row {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}

.version {
  height: 34px;
  padding: 0 14px;
  color: #e5f4ff;
  background: rgb(255 255 255 / 12%);
  border: 1px solid rgb(255 255 255 / 24%);
  border-radius: 4px;
}

.version.active {
  color: #12344d;
  background: #fff;
}

.login-panel {
  padding: 30px;
  background: rgb(255 255 255 / 94%);
  border: 1px solid rgb(255 255 255 / 74%);
  border-radius: 4px;
  box-shadow: 0 18px 48px rgb(15 23 42 / 18%);
}

.panel-head {
  margin-bottom: 24px;
}

.panel-head h2 {
  margin: 0;
  color: #1d2939;
  font-size: 20px;
}

.panel-head span {
  display: block;
  margin-top: 8px;
  color: #667085;
}

.login-options {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin: -4px 0 16px;
}

@media (max-width: 820px) {
  .login-shell {
    grid-template-columns: 1fr;
    gap: 28px;
  }

  .login-copy {
    display: none;
  }

  .login-panel {
    width: min(390px, calc(100vw - 40px));
    justify-self: center;
  }
}
</style>
