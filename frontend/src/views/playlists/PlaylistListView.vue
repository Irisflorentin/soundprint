<script setup lang="ts">
import { onMounted, ref } from 'vue';
import { useRouter } from 'vue-router';
import { Refresh } from '@element-plus/icons-vue';
import PageHeader from '@/components/common/PageHeader.vue';
import LoadingBlock from '@/components/common/LoadingBlock.vue';
import EmptyState from '@/components/common/EmptyState.vue';
import GlassCard from '@/components/common/GlassCard.vue';
import SmartCover from '@/components/common/SmartCover.vue';
import SoundprintCircularGallery from '@/components/common/SoundprintCircularGallery.vue';
import { dashboardApi } from '@/api/dashboard';
import { playlistApi } from '@/api/playlist';
import type { Playlist } from '@/types/playlist';

const router = useRouter();
const featuredPlaylists = ref<Playlist[]>([]);
const playlists = ref<Playlist[]>([]);
const loading = ref(false);
const total = ref(0);

onMounted(async () => {
  await Promise.all([loadFeaturedPlaylists(), loadPlaylists()]);
});

async function loadFeaturedPlaylists() {
  const dashboard = await dashboardApi.get();
  featuredPlaylists.value = dashboard.featuredPlaylists || [];
}

async function loadPlaylists() {
  loading.value = true;
  try {
    const result = await playlistApi.page(1, 48);
    playlists.value = result.records;
    total.value = result.total;
  } finally {
    loading.value = false;
  }
}

function onPlaylistSelect(playlist: { id?: number }) {
  if (playlist.id == null) return;
  router.push(`/playlists/${playlist.id}`);
}
</script>

<template>
  <div class="playlist-list-view">
    <section v-if="featuredPlaylists.length > 0" class="hero-section">
      <div class="hero-label-row">
        <span class="hero-label">FEATURED</span>
        <h2 class="hero-title">精选歌单</h2>
      </div>
      <div class="gallery-container">
        <SoundprintCircularGallery :items="featuredPlaylists" type="playlist" @select="onPlaylistSelect" />
      </div>
    </section>

    <section class="list-section">
      <PageHeader title="所有歌单" subtitle="管理你的播放队列和主题收藏">
        <template #actions>
          <el-button :icon="Refresh" @click="loadPlaylists">刷新</el-button>
        </template>
      </PageHeader>

      <LoadingBlock v-if="loading" text="正在加载歌单..." />
      <EmptyState
        v-else-if="playlists.length === 0"
        title="暂无歌单"
        description="后端歌单接口已就绪，可以通过接口或后续表单创建。"
      />

      <div v-else class="playlist-grid">
        <GlassCard
          v-for="playlist in playlists"
          :key="playlist.id"
          hoverable
          padding="none"
          class="playlist-card"
          @click="router.push(`/playlists/${playlist.id}`)"
        >
          <SmartCover
            :src="playlist.coverUrl"
            :alt="playlist.name"
            :fallback-text="playlist.name"
            placeholder-type="playlist"
            class="cover"
          />
          <div class="body">
            <strong>{{ playlist.name }}</strong>
            <span>{{ playlist.description || '暂无描述' }}</span>
            <small>{{ playlist.trackCount }} 首曲目</small>
          </div>
        </GlassCard>
      </div>

      <p v-if="!loading && total > playlists.length" class="footnote">
        当前展示前 {{ playlists.length }} 个，共 {{ total }} 个。
      </p>
    </section>
  </div>
</template>

<style scoped lang="scss">
.playlist-list-view {
  min-width: 0;
  display: flex;
  flex-direction: column;
  gap: var(--space-6);
}

.hero-section {
  display: flex;
  flex-direction: column;
  gap: var(--space-4);
}

.hero-label-row {
  display: flex;
  align-items: baseline;
  gap: var(--space-3);
}

.hero-label {
  color: var(--color-brand);
  font-size: 11px;
  font-weight: 700;
  letter-spacing: 0.15em;
}

.hero-title {
  margin: 0;
  color: var(--color-fg-primary);
  font-size: 24px;
  font-weight: 700;
}

.gallery-container {
  height: 420px;
  overflow: hidden;
  border-radius: var(--radius-card);
}

.playlist-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(176px, 1fr));
  gap: var(--space-5);
}

.playlist-card {
  overflow: hidden;
}

.cover {
  aspect-ratio: 1;
}

.body {
  display: flex;
  flex-direction: column;
  gap: 5px;
  padding: var(--space-3);

  strong,
  span,
  small {
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
  }

  strong {
    color: var(--color-fg-primary);
    font-size: 14px;
  }

  span {
    color: var(--color-fg-secondary);
    font-size: 13px;
  }

  small {
    color: var(--color-fg-tertiary);
    font-size: 12px;
  }
}

.footnote {
  margin-top: var(--space-4);
  color: var(--color-fg-tertiary);
  font-size: 12px;
}
</style>
