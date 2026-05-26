<script setup lang="ts">
import { useRoute, useRouter } from 'vue-router';
import {
  House, Headset, Collection, User as UserIcon, Files, Setting, DataAnalysis, MagicStick,
} from '@element-plus/icons-vue';
import SoundprintLogo from '@/components/common/SoundprintLogo.vue';

const route = useRoute();
const router = useRouter();

const navItems = [
  { path: '/',          label: '首页',    icon: House },
  { path: '/library',   label: '音乐库',  icon: Headset },
  { path: '/albums',    label: '专辑',    icon: Collection },
  { path: '/artists',   label: '艺术家',  icon: UserIcon },
  { path: '/playlists', label: '歌单',    icon: Files },
  { path: '/studio',    label: '转换工坊', icon: MagicStick },
  { path: '/stats',     label: '听歌报告', icon: DataAnalysis },
];

const settingsItem = { path: '/settings', label: '设置', icon: Setting };

function isActive(path: string) {
  if (path === '/') return route.path === '/';
  return route.path.startsWith(path);
}
</script>

<template>
  <aside class="sidebar-nav">
    <div class="brand" @click="router.push('/')">
      <SoundprintLogo class="logo" />
      <span class="brand-name">Soundprint</span>
    </div>

    <nav class="nav-list">
      <button
        v-for="item in navItems"
        :key="item.path"
        class="nav-item"
        :class="{ active: isActive(item.path) }"
        @click="router.push(item.path)"
      >
        <el-icon :size="20"><component :is="item.icon" /></el-icon>
        <span>{{ item.label }}</span>
      </button>
    </nav>

    <div class="bottom">
      <button
        class="nav-item"
        :class="{ active: isActive(settingsItem.path) }"
        @click="router.push(settingsItem.path)"
      >
        <el-icon :size="20"><Setting /></el-icon>
        <span>设置</span>
      </button>
    </div>
  </aside>
</template>

<style lang="scss" scoped>
.sidebar-nav {
  display: flex;
  flex-direction: column;
  padding: var(--space-5) var(--space-3);
  height: 100%;
  background: rgba(0, 0, 0, 0.2);
  backdrop-filter: blur(20px);
}

.brand {
  display: flex; align-items: center; gap: var(--space-3);
  padding: var(--space-2) var(--space-3);
  margin-bottom: var(--space-6);
  cursor: pointer;
  .logo { width: 28px; height: 28px; }
  .brand-name {
    font-size: 18px; font-weight: 700;
    background: var(--brand-gradient);
    -webkit-background-clip: text;
    background-clip: text;
    color: transparent;
  }
}

.nav-list { flex: 1; display: flex; flex-direction: column; gap: 4px; }
.bottom   { padding-top: var(--space-4); border-top: 1px solid rgba(255,255,255,0.05); }

.nav-item {
  display: flex; align-items: center; gap: var(--space-3);
  width: 100%;
  padding: 10px var(--space-3);
  background: transparent;
  border: none;
  border-radius: var(--radius-btn);
  color: var(--color-fg-secondary);
  font-size: 14px;
  text-align: left;
  cursor: pointer;
  transition: all 200ms var(--ease);

  &:hover {
    background: rgba(255,255,255,0.05);
    color: var(--color-fg-primary);
  }

  &.active {
    position: relative;
    background: transparent;
    color: var(--color-fg-primary);
    font-weight: 600;

    &::before {
      content: '';
      position: absolute;
      left: 0;
      top: 8px;
      bottom: 8px;
      width: 2px;
      border-radius: 2px;
      background: #C8A862;
    }
  }
}
</style>
