<script setup lang="ts">
import { ref } from 'vue';
import { useRouter } from 'vue-router';
import { useUserStore } from '@/stores/user';
import SoundprintLogo from '@/components/common/SoundprintLogo.vue';
import SoundprintGalaxy from '@/components/common/SoundprintGalaxy.vue';

const router = useRouter();
const userStore = useUserStore();
const username = ref('admin');
const password = ref('');
const loading = ref(false);

function handleLogin() {
  loading.value = true;
  try {
    userStore.login(username.value, password.value);
    router.push('/');
  } finally {
    loading.value = false;
  }
}
</script>

<template>
  <div class="login-view">
    <SoundprintGalaxy
      class="galaxy-bg"
      :transparent="false"
      :density="1.2"
      :glow-intensity="0.4"
      :twinkle-intensity="0.5"
      :rotation-speed="0.05"
      :saturation="0.24"
    />
    <div class="brand-tint" />

    <div class="login-card">
      <SoundprintLogo class="logo" />
      <h1 class="title">Soundprint</h1>
      <p class="subtitle">你的私人无损音乐库</p>

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
      <el-button
        type="primary"
        size="large"
        class="login-btn"
        :loading="loading"
        @click="handleLogin"
      >
        进入音乐空间
      </el-button>

      <p class="hint">演示账号已预设，任意密码即可登录</p>
    </div>
  </div>
</template>

<style scoped lang="scss">
.login-view {
  position: relative;
  width: 100vw;
  height: 100vh;
  overflow: hidden;
  background: var(--color-bg-base);
}

.galaxy-bg {
  position: absolute;
  inset: 0;
  z-index: 0;
}

.brand-tint {
  position: absolute;
  inset: 0;
  z-index: 1;
  pointer-events: none;
  background:
    radial-gradient(circle at 22% 18%, rgba(124, 58, 237, 0.22), transparent 32%),
    radial-gradient(circle at 78% 72%, rgba(6, 182, 212, 0.14), transparent 36%),
    rgba(10, 10, 20, 0.18);
}

.login-card {
  position: absolute;
  left: 50%;
  top: 50%;
  z-index: 10;
  width: min(calc(100vw - 32px), 400px);
  padding: var(--space-8);
  transform: translate(-50%, -50%);
  border: 1px solid rgba(255, 255, 255, 0.08);
  border-radius: 24px;
  background: rgba(15, 15, 30, 0.6);
  backdrop-filter: blur(40px);
  -webkit-backdrop-filter: blur(40px);
  box-shadow:
    0 24px 80px rgba(0, 0, 0, 0.5),
    0 0 0 1px color-mix(in srgb, var(--color-brand) 10%, transparent);
  text-align: center;
}

.logo {
  display: block;
  width: 48px;
  height: 48px;
  margin: 0 auto var(--space-4);
}

.title {
  margin: 0;
  color: transparent;
  font-size: 36px;
  font-weight: 700;
  letter-spacing: 0;
  background: var(--brand-gradient);
  -webkit-background-clip: text;
  background-clip: text;
}

.subtitle {
  margin: var(--space-2) 0 var(--space-6);
  color: var(--color-fg-secondary);
  font-size: 14px;
}

.field { margin-bottom: var(--space-4); }

.login-btn {
  width: 100%;
  height: 48px;
  margin-top: var(--space-2);
  font-size: 16px;
}

.hint { margin-top: var(--space-5); font-size: 12px; color: var(--color-fg-tertiary); }
</style>
