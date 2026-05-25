<script setup lang="ts">
import { ref } from 'vue';
import { useRouter } from 'vue-router';
import { useUserStore } from '@/stores/user';
import SoundprintLogo from '@/components/common/SoundprintLogo.vue';

const router = useRouter();
const userStore = useUserStore();
const username = ref('admin');
const password = ref('');

function handleLogin() {
  userStore.login(username.value, password.value);
  router.push('/');
}
</script>

<template>
  <div class="login">
    <div class="card">
      <div class="brand">
        <SoundprintLogo class="logo" />
        <span class="name">Soundprint</span>
      </div>
      <p class="tagline">个人无损音乐库 · 在线播放 · 格式转换</p>

      <el-input v-model="username" placeholder="用户名" size="large" class="field" />
      <el-input
        v-model="password"
        type="password"
        placeholder="密码"
        size="large"
        class="field"
        show-password
        @keyup.enter="handleLogin"
      />
      <el-button type="primary" size="large" class="btn" @click="handleLogin">登 录</el-button>

      <p class="hint">Phase 4 占位登录 · 任意账号密码可进（Phase 7 加 Galaxy 背景与真实鉴权）</p>
    </div>
  </div>
</template>

<style scoped lang="scss">
.login {
  height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: radial-gradient(at 0% 0%, #4C1D95 0%, transparent 50%),
              radial-gradient(at 100% 100%, #1E40AF 0%, transparent 50%),
              var(--color-bg-base);
}
.card {
  width: 380px;
  padding: var(--space-8);
  border-radius: var(--radius-card);
  background: rgba(255, 255, 255, 0.03);
  border: 1px solid rgba(255, 255, 255, 0.06);
  backdrop-filter: blur(20px);
  box-shadow: var(--shadow-glass);
  text-align: center;
}
.brand {
  display: flex; align-items: center; justify-content: center; gap: var(--space-3);
  .logo { width: 32px; height: 32px; }
  .name {
    font-size: 26px; font-weight: 700;
    background: var(--brand-gradient);
    -webkit-background-clip: text; background-clip: text; color: transparent;
  }
}
.tagline { margin: var(--space-2) 0 var(--space-6); font-size: 13px; color: var(--color-fg-secondary); }
.field { margin-bottom: var(--space-4); }
.btn { width: 100%; margin-top: var(--space-2); }
.hint { margin-top: var(--space-5); font-size: 12px; color: var(--color-fg-tertiary); }
</style>
