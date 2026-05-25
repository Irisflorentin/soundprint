<script setup lang="ts">
import { onMounted, ref } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import draggable from 'vuedraggable';
import { ArrowLeft, Delete, Plus, Rank, VideoPlay } from '@element-plus/icons-vue';
import { ElMessage, ElMessageBox } from 'element-plus';
import PageHeader from '@/components/common/PageHeader.vue';
import LoadingBlock from '@/components/common/LoadingBlock.vue';
import EmptyState from '@/components/common/EmptyState.vue';
import SmartCover from '@/components/common/SmartCover.vue';
import { playlistApi } from '@/api/playlist';
import { trackApi } from '@/api/track';
import { usePlayerStore } from '@/stores/player';
import { useFormat } from '@/composables/useFormat';
import type { PlaylistDetail } from '@/types/playlist';
import type { Track } from '@/types/track';

const route = useRoute();
const router = useRouter();
const player = usePlayerStore();
const { formatDuration } = useFormat();

const playlist = ref<PlaylistDetail | null>(null);
const tracks = ref<Track[]>([]);
const loading = ref(false);
const savingOrder = ref(false);

const addDialogVisible = ref(false);
const candidateTracks = ref<Track[]>([]);
const candidateLoading = ref(false);
const selectedTrackId = ref<number | null>(null);

onMounted(loadPlaylist);

async function loadPlaylist() {
  loading.value = true;
  try {
    playlist.value = await playlistApi.detail(Number(route.params.id));
    tracks.value = [...playlist.value.tracks];
  } finally {
    loading.value = false;
  }
}

function playTrack(track: Track) {
  const index = tracks.value.findIndex((item) => item.id === track.id);
  player.playTrack(track, tracks.value, index);
}

function playAll() {
  if (!tracks.value.length) return;
  player.playTrack(tracks.value[0], tracks.value, 0);
}

async function saveOrder() {
  if (!playlist.value) return;
  savingOrder.value = true;
  try {
    await playlistApi.reorder(playlist.value.id, tracks.value.map((track) => track.id));
    ElMessage.success('排序已保存');
  } finally {
    savingOrder.value = false;
  }
}

async function removeTrack(track: Track) {
  if (!playlist.value) return;
  try {
    await ElMessageBox.confirm(`确认从歌单移除《${track.title}》吗？`, '移除曲目', {
      type: 'warning',
      confirmButtonText: '移除',
      cancelButtonText: '取消',
    });
    await playlistApi.removeTrack(playlist.value.id, track.id);
    tracks.value = tracks.value.filter((item) => item.id !== track.id);
    ElMessage.success('已移出歌单');
  } catch {
    // 用户取消时不提示错误。
  }
}

async function openAddDialog() {
  addDialogVisible.value = true;
  selectedTrackId.value = null;
  candidateLoading.value = true;
  try {
    const result = await trackApi.page({ page: 1, size: 200 });
    const existing = new Set(tracks.value.map((track) => track.id));
    candidateTracks.value = result.records.filter((track) => !existing.has(track.id));
  } finally {
    candidateLoading.value = false;
  }
}

async function addSelectedTrack() {
  if (!playlist.value || selectedTrackId.value == null) return;
  await playlistApi.addTrack(playlist.value.id, selectedTrackId.value);
  ElMessage.success('已添加到歌单');
  addDialogVisible.value = false;
  await loadPlaylist();
}
</script>

<template>
  <div class="playlist-detail-view">
    <PageHeader title="歌单详情" :subtitle="playlist?.description || '拖拽曲目调整播放顺序'">
      <template #actions>
        <el-button :icon="ArrowLeft" @click="router.push('/playlists')">返回歌单</el-button>
        <el-button :icon="Plus" @click="openAddDialog">添加曲目</el-button>
        <el-button type="primary" :icon="VideoPlay" :disabled="tracks.length === 0" @click="playAll">
          播放全部
        </el-button>
      </template>
    </PageHeader>

    <LoadingBlock v-if="loading" text="正在加载歌单..." />
    <EmptyState v-else-if="!playlist" title="歌单不存在" description="请返回歌单列表重新选择。" />

    <template v-else>
      <section class="hero">
        <SmartCover :src="playlist.coverUrl" :alt="playlist.name" :fallback-text="playlist.name" class="cover" />
        <div class="info">
          <p class="eyebrow">Playlist</p>
          <h2>{{ playlist.name }}</h2>
          <p>{{ playlist.description || '暂无描述' }}</p>
          <span>{{ tracks.length }} 首曲目</span>
        </div>
      </section>

      <section class="track-panel" :class="{ saving: savingOrder }">
        <div v-if="tracks.length === 0" class="empty-row">这个歌单还没有曲目</div>
        <draggable
          v-else
          v-model="tracks"
          item-key="id"
          handle=".drag-handle"
          tag="div"
          class="track-list"
          @end="saveOrder"
        >
          <template #item="{ element, index }">
            <div class="track-row">
              <button class="drag-handle" type="button" title="拖拽排序">
                <el-icon><Rank /></el-icon>
              </button>
              <span class="index">{{ index + 1 }}</span>
              <SmartCover
                :src="element.coverUrl || element.albumCoverUrl"
                :alt="element.title"
                :fallback-text="element.title"
                class="thumb"
              />
              <button class="track-main" type="button" @click="playTrack(element)">
                <strong>{{ element.title }}</strong>
                <small>{{ element.artistName || '未知艺术家' }} · {{ element.albumTitle || '未知专辑' }}</small>
              </button>
              <span class="duration">{{ formatDuration(element.duration) }}</span>
              <el-button :icon="Delete" circle size="small" type="danger" @click="removeTrack(element)" />
            </div>
          </template>
        </draggable>
      </section>
    </template>

    <el-dialog v-model="addDialogVisible" title="添加曲目" width="520px">
      <LoadingBlock v-if="candidateLoading" text="正在加载音乐库..." />
      <el-select
        v-else
        v-model="selectedTrackId"
        filterable
        placeholder="选择一首曲目"
        class="track-select"
      >
        <el-option
          v-for="track in candidateTracks"
          :key="track.id"
          :label="`${track.title} · ${track.artistName || '未知艺术家'}`"
          :value="track.id"
        />
      </el-select>
      <template #footer>
        <el-button @click="addDialogVisible = false">取消</el-button>
        <el-button type="primary" :disabled="selectedTrackId == null" @click="addSelectedTrack">添加</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped lang="scss">
.hero {
  display: grid;
  grid-template-columns: 180px minmax(0, 1fr);
  gap: var(--space-6);
  align-items: end;
  margin-bottom: var(--space-6);
  padding: var(--space-6);
  border: 1px solid rgba(255, 255, 255, 0.06);
  border-radius: var(--radius-card);
  background: rgba(255, 255, 255, 0.03);
}

.cover {
  width: 180px;
  height: 180px;
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
  font-size: 34px;
}

.info p:not(.eyebrow) {
  margin: var(--space-3) 0;
  color: var(--color-fg-secondary);
}

.info span {
  color: var(--color-fg-tertiary);
  font-size: 13px;
}

.track-panel {
  overflow: hidden;
  border: 1px solid rgba(255, 255, 255, 0.06);
  border-radius: var(--radius-card);
  background: rgba(255, 255, 255, 0.03);
}

.track-panel.saving {
  opacity: 0.82;
}

.track-row {
  display: grid;
  grid-template-columns: 32px 36px 44px minmax(0, 1fr) 70px 40px;
  align-items: center;
  gap: var(--space-3);
  padding: var(--space-3) var(--space-5);
  border-bottom: 1px solid rgba(255, 255, 255, 0.05);
}

.drag-handle,
.track-main {
  padding: 0;
  color: inherit;
  background: transparent;
  border: 0;
}

.drag-handle {
  color: var(--color-fg-tertiary);
  cursor: grab;
}

.index,
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
  text-align: left;
  cursor: pointer;

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

.track-select {
  width: 100%;
}

@media (max-width: 820px) {
  .hero {
    grid-template-columns: 1fr;
  }

  .track-row {
    grid-template-columns: 32px 36px 44px minmax(0, 1fr) 40px;
  }

  .duration {
    display: none;
  }
}
</style>
