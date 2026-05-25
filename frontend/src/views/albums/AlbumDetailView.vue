<script setup lang="ts">
import { onMounted, ref } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { ArrowLeft, Star, StarFilled, VideoPlay } from '@element-plus/icons-vue';
import { ElMessage } from 'element-plus';
import PageHeader from '@/components/common/PageHeader.vue';
import LoadingBlock from '@/components/common/LoadingBlock.vue';
import EmptyState from '@/components/common/EmptyState.vue';
import SmartCover from '@/components/common/SmartCover.vue';
import { albumApi } from '@/api/album';
import { favoriteApi } from '@/api/favorite';
import { usePlayerStore } from '@/stores/player';
import { useFormat } from '@/composables/useFormat';
import type { AlbumDetail } from '@/types/album';
import type { Track } from '@/types/track';

const route = useRoute();
const router = useRouter();
const player = usePlayerStore();
const { formatDuration } = useFormat();

const album = ref<AlbumDetail | null>(null);
const loading = ref(false);

onMounted(loadAlbum);

async function loadAlbum() {
  loading.value = true;
  try {
    album.value = await albumApi.detail(Number(route.params.id));
  } finally {
    loading.value = false;
  }
}

function playTrack(track: Track) {
  if (!album.value) return;
  const index = album.value.tracks.findIndex((item) => item.id === track.id);
  player.playTrack(track, album.value.tracks, index);
}

function playAll() {
  if (!album.value?.tracks.length) return;
  player.playTrack(album.value.tracks[0], album.value.tracks, 0);
}

async function toggleFavorite(track: Track) {
  if (track.favorited) {
    await favoriteApi.remove(track.id);
    track.favorited = false;
    ElMessage.success('已取消收藏');
  } else {
    await favoriteApi.add(track.id);
    track.favorited = true;
    ElMessage.success('已收藏');
  }
}
</script>

<template>
  <div class="album-detail-view">
    <PageHeader title="专辑详情" :subtitle="album?.artistName || '按专辑查看曲目'">
      <template #actions>
        <el-button :icon="ArrowLeft" @click="router.push('/albums')">返回专辑</el-button>
        <el-button type="primary" :icon="VideoPlay" :disabled="!album?.tracks.length" @click="playAll">
          播放全部
        </el-button>
      </template>
    </PageHeader>

    <LoadingBlock v-if="loading" text="正在加载专辑..." />
    <EmptyState v-else-if="!album" title="专辑不存在" description="请返回专辑列表重新选择。" />

    <template v-else>
      <section class="hero">
        <SmartCover :src="album.coverUrl" :alt="album.title" :fallback-text="album.title" class="cover" />
        <div class="info">
          <p class="eyebrow">Album</p>
          <h2>{{ album.title }}</h2>
          <p>{{ album.artistName || '未知艺术家' }}</p>
          <div class="meta">
            <span>{{ album.releaseYear || '未知年份' }}</span>
            <span>{{ album.genre || '未分类' }}</span>
            <span>{{ album.trackCount }} 首</span>
          </div>
          <p v-if="album.description" class="desc">{{ album.description }}</p>
        </div>
      </section>

      <section class="track-panel">
        <div v-if="album.tracks.length === 0" class="empty-row">该专辑暂无曲目</div>
        <div
          v-for="(track, index) in album.tracks"
          v-else
          :key="track.id"
          class="track-row"
          @click="playTrack(track)"
        >
          <span class="index">{{ index + 1 }}</span>
          <SmartCover
            :src="track.coverUrl || track.albumCoverUrl || album.coverUrl"
            :alt="track.title"
            :fallback-text="track.title"
            class="thumb"
          />
          <span class="track-main">
            <strong>{{ track.title }}</strong>
            <small>{{ track.artistName || album.artistName || '未知艺术家' }}</small>
          </span>
          <span class="album-name">{{ track.albumTitle || album.title }}</span>
          <span class="duration">{{ formatDuration(track.duration) }}</span>
          <span class="actions">
            <el-button :icon="VideoPlay" circle size="small" @click.stop="playTrack(track)" />
            <el-button
              :icon="track.favorited ? StarFilled : Star"
              circle
              size="small"
              :class="{ 'is-fav': track.favorited }"
              @click.stop="toggleFavorite(track)"
            />
          </span>
        </div>
      </section>
    </template>
  </div>
</template>

<style scoped lang="scss">
.hero {
  display: grid;
  grid-template-columns: 220px minmax(0, 1fr);
  gap: var(--space-6);
  align-items: end;
  margin-bottom: var(--space-6);
  padding: var(--space-6);
  border: 1px solid rgba(255, 255, 255, 0.06);
  border-radius: var(--radius-card);
  background: rgba(255, 255, 255, 0.03);
}

.cover {
  width: 220px;
  height: 220px;
  border-radius: var(--radius-card);
  box-shadow: var(--shadow-glass);
}

.eyebrow {
  margin: 0 0 var(--space-2);
  color: var(--color-brand);
  font-size: 12px;
  font-weight: 800;
  text-transform: uppercase;
}

h2 {
  margin: 0;
  color: var(--color-fg-primary);
  font-size: 36px;
}

.info p:not(.eyebrow):not(.desc) {
  margin: var(--space-2) 0 0;
  color: var(--color-fg-secondary);
}

.meta {
  display: flex;
  flex-wrap: wrap;
  gap: var(--space-3);
  margin-top: var(--space-4);
  color: var(--color-fg-tertiary);
  font-size: 13px;
}

.desc {
  max-width: 720px;
  margin: var(--space-4) 0 0;
  color: var(--color-fg-secondary);
  line-height: 1.7;
}

.track-panel {
  overflow: hidden;
  border: 1px solid rgba(255, 255, 255, 0.06);
  border-radius: var(--radius-card);
  background: rgba(255, 255, 255, 0.03);
}

.track-row {
  display: grid;
  grid-template-columns: 44px 44px minmax(0, 1fr) minmax(140px, 0.6fr) 72px 92px;
  align-items: center;
  gap: var(--space-3);
  padding: var(--space-3) var(--space-5);
  border-bottom: 1px solid rgba(255, 255, 255, 0.05);
  cursor: pointer;

  &:hover {
    background: rgba(124, 58, 237, 0.08);
  }
}

.index,
.album-name,
.duration,
.empty-row {
  color: var(--color-fg-tertiary);
  font-size: 13px;
}

.thumb {
  width: 44px;
  height: 44px;
  border-radius: 8px;
}

.track-main {
  min-width: 0;
  display: flex;
  flex-direction: column;
  gap: 3px;

  strong,
  small {
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
  }

  strong { color: var(--color-fg-primary); }
  small { color: var(--color-fg-secondary); font-size: 12px; }
}

.album-name {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.actions {
  display: flex;
  justify-content: flex-end;
  gap: var(--space-2);
}

.empty-row {
  padding: var(--space-8);
  text-align: center;
}

:deep(.is-fav) {
  color: var(--color-brand);
  border-color: rgba(124, 58, 237, 0.5);
}

@media (max-width: 880px) {
  .hero {
    grid-template-columns: 1fr;
  }

  .cover {
    width: 180px;
    height: 180px;
  }

  .album-name {
    display: none;
  }

  .track-row {
    grid-template-columns: 34px 44px minmax(0, 1fr) 64px 92px;
  }
}
</style>
