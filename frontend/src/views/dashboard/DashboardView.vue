<script setup lang="ts">
import { onMounted, ref } from 'vue';
import { useRouter } from 'vue-router';
import { VideoPlay } from '@element-plus/icons-vue';
import PageHeader from '@/components/common/PageHeader.vue';
import LoadingBlock from '@/components/common/LoadingBlock.vue';
import EmptyState from '@/components/common/EmptyState.vue';
import GlassCard from '@/components/common/GlassCard.vue';
import SmartCover from '@/components/common/SmartCover.vue';
import HorizontalShelf from '@/components/shelf/HorizontalShelf.vue';
import { dashboardApi } from '@/api/dashboard';
import type { Dashboard } from '@/types/dashboard';
import type { Track } from '@/types/track';
import type { PlayHistoryItem } from '@/types/playHistory';
import { usePlayerStore } from '@/stores/player';
import { useFormat } from '@/composables/useFormat';

const router = useRouter();
const playerStore = usePlayerStore();
const { formatDuration, formatDate } = useFormat();

const loading = ref(false);
const dashboard = ref<Dashboard | null>(null);

async function loadDashboard() {
  loading.value = true;
  try {
    dashboard.value = await dashboardApi.get();
  } finally {
    loading.value = false;
  }
}

function playTrack(track: Track, queue: Track[]) {
  const index = queue.findIndex((item) => item.id === track.id);
  playerStore.playTrack(track, queue, index);
}

function cardCover(track: Track | PlayHistoryItem) {
  return track.coverUrl || ('albumCoverUrl' in track ? track.albumCoverUrl : null);
}

onMounted(loadDashboard);
</script>

<template>
  <div class="dashboard-view">
    <PageHeader
      title="首页"
      subtitle="最近添加、最近播放、收藏与精选音乐"
    />

    <LoadingBlock v-if="loading" text="正在载入首页数据..." />
    <EmptyState
      v-else-if="!dashboard"
      title="首页数据暂不可用"
      description="请确认后端服务已经启动。"
    >
      <el-button type="primary" @click="loadDashboard">重新加载</el-button>
    </EmptyState>

    <template v-else>
      <GlassCard padding="lg" class="hero">
        <div class="hero-copy">
          <p class="eyebrow">Soundprint Library</p>
          <h2>{{ dashboard.greeting || '欢迎回来' }}</h2>
          <p>从最近添加、播放历史和收藏中继续探索你的无损音乐库。</p>
        </div>
        <div class="hero-panel">
          <span class="panel-label">Phase 7 视觉位</span>
          <strong>Infinite Menu</strong>
          <small>后续接入 vue-bits 球面菜单</small>
        </div>
      </GlassCard>

      <HorizontalShelf title="最近添加" more="/library">
        <GlassCard
          v-for="track in dashboard.recentTracks"
          :key="track.id"
          hoverable
          padding="none"
          class="track-card"
          @click="playTrack(track, dashboard.recentTracks)"
        >
          <SmartCover
            :src="cardCover(track)"
            :alt="track.title"
            :fallback-text="track.title"
            class="cover"
          >
            <el-icon class="play-icon"><VideoPlay /></el-icon>
          </SmartCover>
          <div class="card-body">
            <strong>{{ track.title }}</strong>
            <span>{{ track.artistName || '未知艺术家' }}</span>
            <small>{{ formatDuration(track.duration) }} · {{ track.format }}</small>
          </div>
        </GlassCard>
      </HorizontalShelf>

      <HorizontalShelf title="最近播放" more="/library">
        <GlassCard
          v-for="item in dashboard.recentlyPlayed"
          :key="`${item.trackId}-${item.playedAt}`"
          hoverable
          padding="none"
          class="track-card"
          @click="router.push('/library')"
        >
          <SmartCover
            :src="cardCover(item)"
            :alt="item.title"
            :fallback-text="item.title"
            class="cover"
          />
          <div class="card-body">
            <strong>{{ item.title }}</strong>
            <span>{{ item.artistName || '未知艺术家' }}</span>
            <small>{{ formatDate(item.playedAt, 'MM-DD HH:mm') }}</small>
          </div>
        </GlassCard>
      </HorizontalShelf>

      <HorizontalShelf title="我的收藏" more="/library">
        <GlassCard
          v-for="track in dashboard.favorites"
          :key="track.id"
          hoverable
          padding="none"
          class="track-card"
          @click="playTrack(track, dashboard.favorites)"
        >
          <SmartCover
            :src="cardCover(track)"
            :alt="track.title"
            :fallback-text="track.title"
            class="cover"
          >
            <el-icon class="play-icon"><VideoPlay /></el-icon>
          </SmartCover>
          <div class="card-body">
            <strong>{{ track.title }}</strong>
            <span>{{ track.artistName || '未知艺术家' }}</span>
            <small>{{ track.albumTitle || '未知专辑' }}</small>
          </div>
        </GlassCard>
      </HorizontalShelf>

      <div class="feature-grid">
        <GlassCard padding="lg">
          <h3>精选专辑</h3>
          <div class="mini-list">
            <button
              v-for="album in dashboard.featuredAlbums"
              :key="album.id"
              class="mini-item"
              @click="router.push(`/albums/${album.id}`)"
            >
              <SmartCover
                :src="album.coverUrl"
                :alt="album.title"
                :fallback-text="album.title"
                class="mini-cover"
              />
              <span>
                <strong>{{ album.title }}</strong>
                <small>{{ album.artistName || album.genre || '未知艺术家' }}</small>
              </span>
            </button>
          </div>
        </GlassCard>

        <GlassCard padding="lg">
          <h3>精选艺术家</h3>
          <div class="mini-list">
            <button
              v-for="artist in dashboard.featuredArtists"
              :key="artist.id"
              class="mini-item"
              @click="router.push(`/artists/${artist.id}`)"
            >
              <SmartCover
                :src="artist.avatarUrl"
                :alt="artist.name"
                :fallback-text="artist.name"
                rounded="circle"
                class="avatar"
              />
              <span>
                <strong>{{ artist.name }}</strong>
                <small>{{ artist.country || '未知地区' }}</small>
              </span>
            </button>
          </div>
        </GlassCard>
      </div>
    </template>
  </div>
</template>

<style scoped lang="scss">
.dashboard-view {
  min-width: 0;
}

.hero {
  display: grid;
  grid-template-columns: minmax(0, 1fr) 260px;
  gap: var(--space-6);
  align-items: center;
  margin-bottom: var(--space-6);
  background:
    radial-gradient(circle at 0% 0%, rgba(124, 58, 237, 0.28), transparent 42%),
    radial-gradient(circle at 100% 100%, rgba(6, 182, 212, 0.18), transparent 38%),
    rgba(255, 255, 255, 0.03);
}

.hero-copy {
  .eyebrow {
    margin: 0 0 var(--space-2);
    color: var(--color-brand);
    font-size: 12px;
    font-weight: 700;
    text-transform: uppercase;
  }

  h2 {
    margin: 0;
    color: var(--color-fg-primary);
    font-size: 34px;
    font-weight: 800;
  }

  p:last-child {
    margin: var(--space-3) 0 0;
    color: var(--color-fg-secondary);
    font-size: 15px;
  }
}

.hero-panel {
  min-height: 148px;
  border: 1px dashed rgba(124, 58, 237, 0.45);
  border-radius: var(--radius-card);
  display: flex;
  flex-direction: column;
  justify-content: center;
  gap: var(--space-2);
  padding: var(--space-5);
  background: rgba(10, 10, 20, 0.35);

  .panel-label,
  small {
    color: var(--color-fg-tertiary);
    font-size: 12px;
  }

  strong {
    color: var(--color-fg-primary);
    font-size: 22px;
  }
}

.track-card {
  width: 172px;
  flex: 0 0 172px;
  overflow: hidden;
}

.cover {
  position: relative;
  aspect-ratio: 1;

  .play-icon {
    position: absolute;
    right: 10px;
    bottom: 10px;
    width: 34px;
    height: 34px;
    border-radius: 50%;
    color: var(--color-fg-primary);
    background: rgba(0, 0, 0, 0.45);
    opacity: 0;
    transition: opacity 180ms var(--ease);
  }
}

.track-card:hover .play-icon {
  opacity: 1;
}

.card-body {
  display: flex;
  flex-direction: column;
  gap: 4px;
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

  span,
  small {
    color: var(--color-fg-secondary);
    font-size: 12px;
  }

  small {
    color: var(--color-fg-tertiary);
  }
}

.feature-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: var(--space-5);

  h3 {
    margin: 0 0 var(--space-4);
    color: var(--color-fg-primary);
  }
}

.mini-list {
  display: grid;
  gap: var(--space-3);
}

.mini-item {
  display: flex;
  align-items: center;
  gap: var(--space-3);
  padding: 0;
  color: inherit;
  background: transparent;
  border: none;
  text-align: left;
  cursor: pointer;

  span:last-child {
    min-width: 0;
    display: flex;
    flex-direction: column;
    gap: 3px;
  }

  strong,
  small {
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
  }

  strong {
    color: var(--color-fg-primary);
    font-size: 14px;
  }

  small {
    color: var(--color-fg-tertiary);
    font-size: 12px;
  }
}

.mini-cover,
.avatar {
  width: 42px;
  height: 42px;
  flex: 0 0 42px;
  border-radius: 10px;
}

.avatar {
  border-radius: 50%;
}

@media (max-width: 900px) {
  .hero,
  .feature-grid {
    grid-template-columns: 1fr;
  }
}
</style>
