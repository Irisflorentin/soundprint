<script setup lang="ts">
import { onMounted, ref } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { ArrowLeft, VideoPlay } from '@element-plus/icons-vue';
import PageHeader from '@/components/common/PageHeader.vue';
import LoadingBlock from '@/components/common/LoadingBlock.vue';
import EmptyState from '@/components/common/EmptyState.vue';
import SmartCover from '@/components/common/SmartCover.vue';
import { artistApi } from '@/api/artist';
import { usePlayerStore } from '@/stores/player';
import { useFormat } from '@/composables/useFormat';
import type { ArtistDetail } from '@/types/artist';
import type { Track } from '@/types/track';

const route = useRoute();
const router = useRouter();
const player = usePlayerStore();
const { formatDuration } = useFormat();

const artist = ref<ArtistDetail | null>(null);
const loading = ref(false);

onMounted(loadArtist);

async function loadArtist() {
  loading.value = true;
  try {
    artist.value = await artistApi.detail(Number(route.params.id));
  } finally {
    loading.value = false;
  }
}

function playTrack(track: Track) {
  if (!artist.value) return;
  const index = artist.value.tracks.findIndex((item) => item.id === track.id);
  player.playTrack(track, artist.value.tracks, index);
}

function playAll() {
  if (!artist.value?.tracks.length) return;
  player.playTrack(artist.value.tracks[0], artist.value.tracks, 0);
}
</script>

<template>
  <div class="artist-detail-view">
    <PageHeader title="艺术家详情" :subtitle="artist?.country || '按艺术家查看曲目和专辑'">
      <template #actions>
        <el-button :icon="ArrowLeft" @click="router.push('/artists')">返回艺术家</el-button>
        <el-button type="primary" :icon="VideoPlay" :disabled="!artist?.tracks.length" @click="playAll">
          播放全部
        </el-button>
      </template>
    </PageHeader>

    <LoadingBlock v-if="loading" text="正在加载艺术家..." />
    <EmptyState v-else-if="!artist" title="艺术家不存在" description="请返回艺术家列表重新选择。" />

    <template v-else>
      <section class="hero">
        <SmartCover
          :src="artist.avatarUrl"
          :alt="artist.name"
          :fallback-text="artist.name"
          rounded="circle"
          class="avatar"
        />
        <div class="info">
          <p class="eyebrow">Artist</p>
          <h2>{{ artist.name }}</h2>
          <div class="meta">
            <span>{{ artist.country || '未知地区' }}</span>
            <span>{{ artist.formedYear ? `${artist.formedYear} 出道/成立` : '年份未知' }}</span>
            <span>{{ artist.albumCount }} 张专辑</span>
            <span>{{ artist.trackCount }} 首曲目</span>
          </div>
          <p v-if="artist.bio" class="desc">{{ artist.bio }}</p>
        </div>
      </section>

      <section v-if="artist.albums.length" class="album-strip">
        <h3>专辑</h3>
        <div class="album-list">
          <button
            v-for="album in artist.albums"
            :key="album.id"
            type="button"
            class="album-card"
            @click="router.push(`/albums/${album.id}`)"
          >
            <SmartCover :src="album.coverUrl" :alt="album.title" :fallback-text="album.title" class="album-cover" />
            <strong>{{ album.title }}</strong>
            <small>{{ album.releaseYear || '未知年份' }}</small>
          </button>
        </div>
      </section>

      <section class="track-panel">
        <div v-if="artist.tracks.length === 0" class="empty-row">该艺术家暂无曲目</div>
        <button
          v-for="(track, index) in artist.tracks"
          v-else
          :key="track.id"
          type="button"
          class="track-row"
          @click="playTrack(track)"
        >
          <span class="index">{{ index + 1 }}</span>
          <SmartCover
            :src="track.coverUrl || track.albumCoverUrl"
            :alt="track.title"
            :fallback-text="track.title"
            class="thumb"
          />
          <span class="track-main">
            <strong>{{ track.title }}</strong>
            <small>{{ track.albumTitle || '未知专辑' }}</small>
          </span>
          <span class="duration">{{ formatDuration(track.duration) }}</span>
        </button>
      </section>
    </template>
  </div>
</template>

<style scoped lang="scss">
.hero {
  display: grid;
  grid-template-columns: 180px minmax(0, 1fr);
  gap: var(--space-6);
  align-items: center;
  margin-bottom: var(--space-6);
  padding: var(--space-6);
  border: 1px solid rgba(255, 255, 255, 0.06);
  border-radius: var(--radius-card);
  background: rgba(255, 255, 255, 0.03);
}

.avatar {
  width: 180px;
  height: 180px;
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

.meta {
  display: flex;
  flex-wrap: wrap;
  gap: var(--space-3);
  margin-top: var(--space-4);
  color: var(--color-fg-tertiary);
  font-size: 13px;
}

.desc {
  max-width: 760px;
  margin: var(--space-4) 0 0;
  color: var(--color-fg-secondary);
  line-height: 1.7;
}

.album-strip,
.track-panel {
  margin-bottom: var(--space-6);
  border: 1px solid rgba(255, 255, 255, 0.06);
  border-radius: var(--radius-card);
  background: rgba(255, 255, 255, 0.03);
}

.album-strip {
  padding: var(--space-5);
}

h3 {
  margin: 0 0 var(--space-4);
  color: var(--color-fg-primary);
}

.album-list {
  display: flex;
  gap: var(--space-4);
  overflow-x: auto;
  padding-bottom: var(--space-2);
}

.album-card {
  width: 138px;
  flex: 0 0 138px;
  display: flex;
  flex-direction: column;
  gap: 6px;
  padding: 0;
  color: inherit;
  background: transparent;
  border: 0;
  text-align: left;
  cursor: pointer;
}

.album-cover {
  width: 138px;
  height: 138px;
  border-radius: 8px;
}

.album-card strong,
.album-card small {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.album-card strong { color: var(--color-fg-primary); font-size: 13px; }
.album-card small { color: var(--color-fg-tertiary); font-size: 12px; }

.track-panel {
  overflow: hidden;
}

.track-row {
  width: 100%;
  display: grid;
  grid-template-columns: 44px 44px minmax(0, 1fr) 68px;
  align-items: center;
  gap: var(--space-3);
  padding: var(--space-3) var(--space-5);
  color: inherit;
  background: transparent;
  border: 0;
  border-bottom: 1px solid rgba(255, 255, 255, 0.05);
  text-align: left;
  cursor: pointer;

  &:hover {
    background: rgba(124, 58, 237, 0.08);
  }
}

.thumb {
  width: 44px;
  height: 44px;
  border-radius: 8px;
}

.index,
.duration,
.empty-row {
  color: var(--color-fg-tertiary);
  font-size: 13px;
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

.empty-row {
  padding: var(--space-8);
  text-align: center;
}

@media (max-width: 760px) {
  .hero {
    grid-template-columns: 1fr;
  }
}
</style>
