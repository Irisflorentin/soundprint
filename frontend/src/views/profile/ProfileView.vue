<script setup lang="ts">
import { computed, onMounted, ref } from 'vue';
import { useRouter } from 'vue-router';
import { storeToRefs } from 'pinia';
import { ElMessageBox } from 'element-plus';
import { useUserStore } from '@/stores/user';
import { statsApi } from '@/api/stats';

const router = useRouter();
const userStore = useUserStore();
const { currentUser } = storeToRefs(userStore);

const stats = ref({
  totalPlays: 0,
  favoriteCount: 0,
  topArtist: '-',
});

const initials = computed(() => {
  const name = currentUser.value?.nickname || currentUser.value?.username || '?';
  return name[0]?.toUpperCase() || '?';
});

onMounted(loadProfileStats);

async function loadProfileStats() {
  try {
    const [overview, topArtists] = await Promise.all([
      statsApi.overview(),
      statsApi.topArtists(1),
    ]);

    stats.value = {
      totalPlays: overview.totalPlays,
      favoriteCount: overview.favoriteCount,
      topArtist: topArtists[0]?.artistName || '-',
    };
  } catch (error) {
    console.warn('加载个人统计失败', error);
  }
}

async function handleLogout() {
  try {
    await ElMessageBox.confirm('确定要注销当前账号吗？', '注销确认', {
      confirmButtonText: '注销',
      cancelButtonText: '取消',
      type: 'warning',
    });
    userStore.logout();
    await router.push('/login');
  } catch {
    // 用户取消注销时保持当前页面。
  }
}
</script>

<template>
  <div v-if="currentUser" class="profile-view">
    <header class="profile-header">
      <h1>个人资料</h1>
      <p>账号信息、聆听统计与个人偏好</p>
    </header>

    <section class="identity-card">
      <div class="avatar-large">
        <span>{{ initials }}</span>
      </div>
      <div class="identity">
        <h2>{{ currentUser.nickname }}</h2>
        <p class="username">@{{ currentUser.username }}</p>
        <p class="registered">注册于 {{ currentUser.registeredAt }}</p>
      </div>
    </section>

    <section class="stats-card">
      <h3 class="section-title">MY STATS</h3>
      <div class="stats-row">
        <div class="stat-item">
          <div class="stat-value">{{ stats.totalPlays }}</div>
          <div class="stat-label">累计聆听</div>
        </div>
        <div class="stat-divider" />
        <div class="stat-item">
          <div class="stat-value">{{ stats.favoriteCount }}</div>
          <div class="stat-label">收藏曲目</div>
        </div>
        <div class="stat-divider" />
        <div class="stat-item">
          <div class="stat-value top-artist">{{ stats.topArtist }}</div>
          <div class="stat-label">最爱艺术家</div>
        </div>
      </div>
    </section>

    <section class="account-card">
      <h3 class="section-title">ACCOUNT</h3>
      <div class="account-info">
        <div class="info-row">
          <span class="info-label">用户名</span>
          <span class="info-value">{{ currentUser.username }}</span>
        </div>
        <div class="info-row">
          <span class="info-label">昵称</span>
          <span class="info-value">{{ currentUser.nickname }}</span>
        </div>
        <div class="info-row">
          <span class="info-label">注册时间</span>
          <span class="info-value">{{ currentUser.registeredAt }}</span>
        </div>
      </div>
      <div class="actions">
        <el-button @click="handleLogout">注销账号</el-button>
      </div>
    </section>
  </div>
</template>

<style lang="scss" scoped>
.profile-view {
  display: flex;
  flex-direction: column;
  gap: var(--space-5);
  max-width: 880px;
  margin: 0 auto;
}

.profile-header {
  margin-bottom: var(--space-2);

  h1 {
    margin: 0 0 var(--space-2);
    color: #E5E7EB;
    font-size: 32px;
    font-weight: 700;
  }

  p {
    margin: 0;
    color: var(--color-fg-secondary);
    font-size: 14px;
  }
}

.identity-card,
.stats-card,
.account-card {
  padding: var(--space-6);
  border: 1px solid rgba(255, 255, 255, 0.04);
  border-radius: var(--radius-card);
  background:
    linear-gradient(180deg, rgba(255, 255, 255, 0.035), rgba(255, 255, 255, 0.018)),
    var(--color-bg-card);
  box-shadow: var(--shadow-glass);
}

.identity-card {
  display: flex;
  align-items: center;
  gap: var(--space-6);
}

.avatar-large {
  display: grid;
  flex-shrink: 0;
  width: 96px;
  height: 96px;
  place-items: center;
  border-radius: 50%;
  background: var(--brand-gradient);
  box-shadow:
    0 14px 40px rgba(0, 0, 0, 0.34),
    0 0 32px rgba(200, 168, 98, 0.16);

  span {
    color: var(--color-bg-base);
    font-size: 42px;
    font-weight: 800;
  }
}

.identity {
  min-width: 0;

  h2 {
    margin: 0 0 var(--space-2);
    color: #E5E7EB;
    font-size: 28px;
    font-weight: 700;
  }
}

.username {
  margin: 0 0 var(--space-1);
  color: var(--color-fg-secondary);
  font-size: 14px;
}

.registered {
  margin: 0;
  color: var(--color-fg-tertiary);
  font-size: 13px;
}

.section-title {
  margin: 0 0 var(--space-4);
  color: var(--color-fg-tertiary);
  font-size: 12px;
  font-weight: 700;
  letter-spacing: 0.12em;
}

.stats-row {
  display: flex;
  align-items: center;
  gap: var(--space-4);
}

.stat-item {
  flex: 1;
  min-width: 0;
  padding: var(--space-3) 0;
  text-align: center;
}

.stat-value {
  margin-bottom: var(--space-2);
  color: #E5E7EB;
  font-size: 32px;
  font-weight: 800;
  line-height: 1;
}

.top-artist {
  overflow: hidden;
  color: transparent;
  font-size: 18px;
  text-overflow: ellipsis;
  white-space: nowrap;
  background: linear-gradient(135deg, #F4F5F7 0%, #D6B86F 100%);
  -webkit-background-clip: text;
  background-clip: text;
}

.stat-label {
  color: var(--color-fg-secondary);
  font-size: 12px;
  letter-spacing: 0.05em;
}

.stat-divider {
  width: 1px;
  height: 48px;
  background: rgba(255, 255, 255, 0.06);
}

.account-info {
  margin-bottom: var(--space-5);
}

.info-row {
  display: flex;
  justify-content: space-between;
  gap: var(--space-4);
  padding: var(--space-3) 0;
  border-bottom: 1px solid rgba(255, 255, 255, 0.035);

  &:last-child {
    border-bottom: 0;
  }
}

.info-label {
  color: var(--color-fg-secondary);
  font-size: 14px;
}

.info-value {
  color: #E5E7EB;
  font-size: 14px;
  font-weight: 600;
}

.actions {
  display: flex;
  justify-content: flex-end;
}

@media (max-width: 720px) {
  .identity-card,
  .stats-row {
    align-items: stretch;
    flex-direction: column;
  }

  .stat-divider {
    width: 100%;
    height: 1px;
  }
}
</style>
