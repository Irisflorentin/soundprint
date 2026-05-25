<script setup lang="ts">
import { ref } from 'vue';
import { useRouter } from 'vue-router';
import { Search, Upload, Bell } from '@element-plus/icons-vue';
import { useUserStore } from '@/stores/user';

const router = useRouter();
const userStore = useUserStore();
const searchKeyword = ref('');

function handleSearch() {
  if (searchKeyword.value.trim()) {
    router.push({ path: '/library', query: { keyword: searchKeyword.value.trim() } });
  }
}

function goUpload() {
  router.push({ path: '/library', query: { upload: 'true' } });
}
</script>

<template>
  <header class="top-bar">
    <div class="search">
      <el-input
        v-model="searchKeyword"
        placeholder="搜索曲目、艺术家、专辑..."
        :prefix-icon="Search"
        clearable
        size="large"
        @keyup.enter="handleSearch"
      />
    </div>

    <div class="actions">
      <el-button type="primary" :icon="Upload" @click="goUpload">上传</el-button>
      <el-button :icon="Bell" circle />
      <el-avatar :size="36" class="avatar">
        {{ userStore.currentUser.nickname?.[0] || 'U' }}
      </el-avatar>
    </div>
  </header>
</template>

<style lang="scss" scoped>
.top-bar {
  display: flex; align-items: center; gap: var(--space-5);
  padding: 0 var(--space-6);
  height: 64px;
  background: rgba(0,0,0,0.2);
  backdrop-filter: blur(20px);
  border-bottom: 1px solid rgba(255,255,255,0.05);
}
.search { flex: 1; max-width: 480px; }
.actions { display: flex; align-items: center; gap: var(--space-3); }
.avatar {
  background: linear-gradient(135deg, #7C3AED, #06B6D4);
  font-weight: 600;
  cursor: pointer;
}
</style>
