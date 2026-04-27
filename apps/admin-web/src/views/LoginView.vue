<template>
  <div class="login-page">
    <div class="login-bg" aria-hidden="true">
      <div class="login-bg__grid"></div>
      <div class="login-bg__line line-a"></div>
      <div class="login-bg__line line-b"></div>
    </div>

    <header class="login-logo">
      <span class="login-logo__mark">YZ</span>
      <span>易泽 ERP</span>
    </header>

    <main class="login-shell">
      <section class="login-promo">
        <div class="promo-badge">制造业版</div>
        <h1>易泽产销一体 ERP</h1>
        <p>面向前店后厂、销售生产一体化的单公司管理系统。</p>
        <div class="promo-panel">
          <div>
            <strong>采购</strong>
            <span>请购、订单、入库、退货</span>
          </div>
          <div>
            <strong>生产</strong>
            <span>任务、领料、工序、质检</span>
          </div>
          <div>
            <strong>人事</strong>
            <span>考勤、计件、工资汇总</span>
          </div>
        </div>
      </section>

      <section class="login-card">
        <button class="mode-corner" type="button" :title="modeTitle" @click="toggleMode">
          <span v-if="loginMode === 'account'">码</span>
          <span v-else>账</span>
        </button>

        <template v-if="loginMode === 'account'">
          <h2>账号登录</h2>
          <a-form layout="vertical" :model="form" @finish="submit">
            <a-form-item name="account">
              <a-input v-model:value="form.account" size="large" autocomplete="username" placeholder="请输入账号/手机号">
                <template #prefix><span class="input-icon">人</span></template>
              </a-input>
            </a-form-item>
            <a-form-item name="password">
              <a-input-password v-model:value="form.password" size="large" autocomplete="current-password" placeholder="请输入密码">
                <template #prefix><span class="input-icon">锁</span></template>
              </a-input-password>
            </a-form-item>
            <div class="login-options">
              <a-checkbox v-model:checked="remember">记住密码</a-checkbox>
              <a href="javascript:void(0)">忘记密码了？</a>
            </div>
            <a-alert v-if="error" class="login-error" type="error" :message="error" show-icon />
            <a-button type="primary" size="large" html-type="submit" block :loading="loading">立即登录</a-button>
          </a-form>
          <button class="wechat-login" type="button">企业微信登录</button>
        </template>

        <template v-else>
          <h2>手机扫码 安全登录</h2>
          <div class="qr-box">
            <div class="qr-grid"></div>
          </div>
          <p class="qr-tip">请打开 手机APP-我的 扫码登录</p>
          <a-button block @click="toggleMode">返回账号密码登录</a-button>
        </template>

        <footer class="login-card__footer">
          <button type="button" @click="experienceOpen = true">体验一下</button>
          <span></span>
          <button type="button">立即注册</button>
        </footer>
      </section>
    </main>

    <div class="login-downloads">
      <span>鸿蒙版</span>
      <span>安卓版</span>
      <span>iPhone版</span>
      <span>Win桌面版</span>
      <span>Mac Intel版</span>
    </div>

    <a-modal v-model:open="experienceOpen" title="选择体验版本" :footer="null" width="720px">
      <div class="version-grid">
        <button v-for="version in versions" :key="version.name" type="button" :class="{ active: version.name === '制造业版' }">
          <strong>{{ version.name }}</strong>
          <span>{{ version.desc }}</span>
        </button>
      </div>
    </a-modal>
  </div>
</template>

<script setup lang="ts">
import { computed, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'

const router = useRouter()
const auth = useAuthStore()
const loading = ref(false)
const error = ref('')
const remember = ref(true)
const experienceOpen = ref(false)
const loginMode = ref<'account' | 'qr'>('account')
const form = reactive({
  account: 'admin',
  password: 'admin123'
})

const versions = [
  { name: '标准版', desc: '覆盖采购、销售、库存和基础财务。' },
  { name: '专业版', desc: '增强往来、报表、权限和多岗位协同。' },
  { name: '制造业版', desc: '增强生产加工模块，适用前店后厂、销售生产一体化模式。' },
  { name: '连锁版', desc: '适用多门店经营、调拨和门店销售。' },
  { name: '仓管版', desc: '面向仓库扫码、上下架、出入库作业。' }
]

const modeTitle = computed(() => (loginMode.value === 'account' ? '扫码登录' : '账号密码登录'))

function toggleMode() {
  loginMode.value = loginMode.value === 'account' ? 'qr' : 'account'
}

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
  position: relative;
  display: grid;
  min-height: 100vh;
  overflow: hidden;
  background: #0c1f2f;
  color: #fff;
}

.login-bg,
.login-bg__grid {
  position: absolute;
  inset: 0;
}

.login-bg {
  background:
    linear-gradient(90deg, rgb(4 17 30 / 92%), rgb(11 38 55 / 80%)),
    radial-gradient(circle at 72% 18%, rgb(21 116 154 / 58%), transparent 30%),
    linear-gradient(135deg, #10283a, #263b45 42%, #081826);
}

.login-bg__grid {
  background-image:
    linear-gradient(rgb(255 255 255 / 5%) 1px, transparent 1px),
    linear-gradient(90deg, rgb(255 255 255 / 5%) 1px, transparent 1px);
  background-size: 64px 64px;
  mask-image: linear-gradient(90deg, transparent, #000 18%, #000 88%, transparent);
}

.login-bg__line {
  position: absolute;
  width: 48vw;
  height: 2px;
  background: linear-gradient(90deg, transparent, rgb(75 184 220 / 72%), transparent);
  transform: rotate(-18deg);
}

.line-a {
  top: 21%;
  left: 10%;
}

.line-b {
  right: 5%;
  bottom: 23%;
}

.login-logo {
  position: absolute;
  top: 30px;
  left: 42px;
  z-index: 2;
  display: flex;
  align-items: center;
  gap: 10px;
  font-size: 18px;
  font-weight: 700;
}

.login-logo__mark {
  display: grid;
  width: 42px;
  height: 42px;
  place-items: center;
  background: #1677ff;
  border-radius: 2px;
  font-size: 15px;
}

.login-shell {
  position: relative;
  z-index: 1;
  display: grid;
  grid-template-columns: 1fr 350px;
  gap: 72px;
  width: min(1024px, calc(100vw - 48px));
  align-self: center;
  justify-self: center;
  align-items: center;
}

.login-promo h1 {
  margin: 16px 0 14px;
  font-size: 44px;
  line-height: 1.15;
  letter-spacing: 0;
}

.login-promo p {
  max-width: 480px;
  margin: 0 0 30px;
  color: rgb(255 255 255 / 82%);
  font-size: 17px;
  line-height: 1.8;
}

.promo-badge {
  display: inline-flex;
  align-items: center;
  height: 30px;
  padding: 0 12px;
  background: rgb(255 255 255 / 14%);
  border: 1px solid rgb(255 255 255 / 24%);
  border-radius: 2px;
}

.promo-panel {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  max-width: 560px;
  border: 1px solid rgb(255 255 255 / 18%);
  background: rgb(5 16 27 / 36%);
}

.promo-panel div {
  padding: 18px;
  border-right: 1px solid rgb(255 255 255 / 14%);
}

.promo-panel div:last-child {
  border-right: 0;
}

.promo-panel strong,
.promo-panel span {
  display: block;
}

.promo-panel strong {
  margin-bottom: 8px;
  font-size: 17px;
}

.promo-panel span {
  color: rgb(255 255 255 / 72%);
  line-height: 1.6;
}

.login-card {
  position: relative;
  min-height: 388px;
  padding: 34px 30px 18px;
  background: rgb(255 255 255 / 92%);
  border-radius: 2px;
  box-shadow: 0 20px 60px rgb(0 0 0 / 28%);
  color: #1f2937;
}

.login-card h2 {
  margin: 0 0 24px;
  color: #1f2937;
  font-size: 22px;
  font-weight: 600;
}

.mode-corner {
  position: absolute;
  top: 0;
  right: 0;
  width: 54px;
  height: 54px;
  color: #fff;
  background: #1677ff;
  border: 0;
  cursor: pointer;
  font-size: 18px;
  font-weight: 700;
  clip-path: polygon(100% 0, 100% 100%, 0 0);
}

.mode-corner span {
  position: absolute;
  top: 8px;
  right: 8px;
}

.input-icon {
  color: #8b95a1;
  font-size: 13px;
}

.login-options {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin: -2px 0 16px;
  font-size: 13px;
}

.login-error {
  margin-bottom: 12px;
}

.wechat-login {
  width: 100%;
  height: 38px;
  margin-top: 14px;
  color: #177245;
  background: #f5fbf8;
  border: 1px solid #cae6d7;
  border-radius: 2px;
  cursor: pointer;
}

.qr-box {
  display: grid;
  width: 190px;
  height: 190px;
  place-items: center;
  margin: 18px auto 16px;
  background: #fff;
  border: 1px solid #e4e7ec;
}

.qr-grid {
  width: 150px;
  height: 150px;
  background:
    linear-gradient(90deg, #111 10px, transparent 10px) 0 0 / 20px 20px,
    linear-gradient(#111 10px, transparent 10px) 0 0 / 20px 20px,
    #fff;
}

.qr-tip {
  margin: 0 0 22px;
  color: #667085;
  text-align: center;
}

.login-card__footer {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 12px;
  margin-top: 20px;
  padding-top: 16px;
  border-top: 1px solid #edf0f3;
}

.login-card__footer button {
  color: #1677ff;
  background: transparent;
  border: 0;
  cursor: pointer;
}

.login-card__footer span {
  width: 1px;
  height: 14px;
  background: #d0d5dd;
}

.login-downloads {
  position: absolute;
  right: 40px;
  bottom: 26px;
  z-index: 2;
  display: flex;
  gap: 18px;
  color: rgb(255 255 255 / 70%);
  font-size: 13px;
}

.version-grid {
  display: grid;
  grid-template-columns: repeat(5, minmax(0, 1fr));
  gap: 12px;
}

.version-grid button {
  min-height: 132px;
  padding: 16px 12px;
  text-align: left;
  background: #fff;
  border: 1px solid #e5e7eb;
  border-radius: 2px;
  cursor: pointer;
}

.version-grid button.active {
  border-color: #1677ff;
  box-shadow: 0 0 0 2px rgb(22 119 255 / 12%);
}

.version-grid strong,
.version-grid span {
  display: block;
}

.version-grid strong {
  margin-bottom: 10px;
  color: #1f2937;
}

.version-grid span {
  color: #667085;
  font-size: 13px;
  line-height: 1.7;
}

@media (max-width: 860px) {
  .login-logo {
    left: 24px;
  }

  .login-shell {
    grid-template-columns: 1fr;
    width: min(390px, calc(100vw - 32px));
  }

  .login-promo {
    display: none;
  }

  .login-downloads {
    display: none;
  }
}
</style>
