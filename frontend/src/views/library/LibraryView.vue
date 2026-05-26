<script setup lang="ts">
import { computed, nextTick, onMounted, ref, watch } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { storeToRefs } from 'pinia';
import { Delete, Refresh, Search, Star, StarFilled, Upload, VideoPlay } from '@element-plus/icons-vue';
import { ElMessage, ElMessageBox } from 'element-plus';
import PageHeader from '@/components/common/PageHeader.vue';
import LoadingBlock from '@/components/common/LoadingBlock.vue';
import EmptyState from '@/components/common/EmptyState.vue';
import GlassCard from '@/components/common/GlassCard.vue';
import SmartCover from '@/components/common/SmartCover.vue';
import { trackApi } from '@/api/track';
import { favoriteApi } from '@/api/favorite';
import type { Track } from '@/types/track';
import { useLibraryStore } from '@/stores/library';
import { usePlayerStore } from '@/stores/player';
import { useFormat } from '@/composables/useFormat';

const route = useRoute();
const router = useRouter();
const libraryStore = useLibraryStore();
const playerStore = usePlayerStore();
const { keyword, format, page, size } = storeToRefs(libraryStore);
const { formatDuration, formatFileSize, formatHours, formatDate } = useFormat();

const tracks = ref<Track[]>([]);
const total = ref(0);
const loading = ref(false);
const uploadInput = ref<HTMLInputElement | null>(null);
const uploading = ref(false);
const uploadProgress = ref(0);

const formatOptions = ['FLAC', 'MP3', 'WAV', 'AAC', 'OGG', 'M4A'];

const totalDuration = computed(() =>
  tracks.value.reduce((sum, item) => sum + (item.duration || 0), 0)
);

async function loadTracks() {
  loading.value = true;
  try {
    const result = await trackApi.page({
      page: page.value,
      size: size.value,
      keyword: keyword.value || undefined,
      format: format.value || undefined,
    });
    tracks.value = result.records;
    total.value = result.total;
  } finally {
    loading.value = false;
  }
}

function search() {
  page.value = 1;
  loadTracks();
}

function resetFilters() {
  libraryStore.reset();
  loadTracks();
}

function handlePageChange(nextPage: number) {
  page.value = nextPage;
  loadTracks();
}

function handleSizeChange(nextSize: number) {
  size.value = nextSize;
  page.value = 1;
  loadTracks();
}

function play(track: Track) {
  const index = tracks.value.findIndex((item) => item.id === track.id);
  playerStore.playTrack(track, tracks.value, index);
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

async function removeTrack(track: Track) {
  await ElMessageBox.confirm(`确认删除《${track.title}》吗？`, '删除曲目', {
    type: 'warning',
    confirmButtonText: '删除',
    cancelButtonText: '取消',
  });
  await trackApi.remove(track.id);
  ElMessage.success('曲目已删除');
  loadTracks();
}

function chooseUploadFile() {
  uploadInput.value?.click();
}

async function handleUpload(event: Event) {
  const input = event.target as HTMLInputElement;
  const file = input.files?.[0];
  if (!file) return;

  uploading.value = true;
  uploadProgress.value = 0;
  try {
    await trackApi.upload(file, (percent) => {
      uploadProgress.value = percent;
    });
    ElMessage.success('上传完成');
    page.value = 1;
    await loadTracks();
  } finally {
    uploading.value = false;
    input.value = '';
  }
}

watch(
  () => route.query.keyword,
  (value) => {
    if (typeof value === 'string' && value !== keyword.value) {
      keyword.value = value;
      page.value = 1;
      loadTracks();
    }
  }
);

watch(
  () => route.query.upload,
  async (value) => {
    if (value === 'true') {
      await nextTick();
      chooseUploadFile();
      router.replace({ path: '/library', query: { ...route.query, upload: undefined } });
    }
  }
);

onMounted(() => {
  const queryKeyword = route.query.keyword;
  if (typeof queryKeyword === 'string') keyword.value = queryKeyword;
  loadTracks();
});
</script>

<template>
  <div class="library-view">
    <PageHeader
      title="音乐库"
      subtitle="曲目表格、搜索、格式筛选与分页"
    >
      <template #actions>
        <input
          ref="uploadInput"
          type="file"
          class="hidden-input"
          accept=".flac,.mp3,.wav,.aac,.ogg,.m4a,audio/*"
          @change="handleUpload"
        >
        <el-button :icon="Refresh" @click="loadTracks">刷新</el-button>
        <el-button type="primary" :icon="Upload" :loading="uploading" @click="chooseUploadFile">
          上传音乐
        </el-button>
      </template>
    </PageHeader>

    <GlassCard class="toolbar" padding="md">
      <div class="summary">
        <strong>共 {{ total }} 首</strong>
        <span>当前页 {{ tracks.length }} 首 · {{ formatHours(totalDuration) }}</span>
        <el-progress
          v-if="uploading"
          class="upload-progress"
          :percentage="uploadProgress"
          :stroke-width="8"
          striped
        />
      </div>

      <div class="filters">
        <el-input
          v-model="keyword"
          placeholder="搜索曲目、艺术家、专辑"
          :prefix-icon="Search"
          clearable
          @keyup.enter="search"
          @clear="search"
        />
        <el-select
          v-model="format"
          placeholder="格式"
          clearable
          @change="search"
          @clear="search"
        >
          <el-option
            v-for="item in formatOptions"
            :key="item"
            :label="item"
            :value="item"
          />
        </el-select>
        <el-button type="primary" @click="search">查询</el-button>
        <el-button @click="resetFilters">重置</el-button>
      </div>
    </GlassCard>

    <GlassCard padding="none" class="table-card">
      <LoadingBlock v-if="loading" text="正在加载曲目..." />
      <EmptyState
        v-else-if="tracks.length === 0"
        title="没有找到曲目"
        description="换个关键词试试，或上传一首音乐。"
      />
      <template v-else>
        <el-table
          :data="tracks"
          height="100%"
          @row-click="play"
        >
          <el-table-column width="72">
            <template #default="{ row }">
              <SmartCover
                :src="row.coverUrl || row.albumCoverUrl"
                :alt="row.title"
                :fallback-text="row.title"
                class="table-cover"
              />
            </template>
          </el-table-column>
          <el-table-column label="标题" min-width="220">
            <template #default="{ row }">
              <div class="track-title">{{ row.title }}</div>
              <div class="track-meta">{{ formatFileSize(row.fileSizeBytes) }} · {{ formatDate(row.createdAt) }}</div>
            </template>
          </el-table-column>
          <el-table-column prop="artistName" label="艺术家" min-width="150">
            <template #default="{ row }">{{ row.artistName || '未知艺术家' }}</template>
          </el-table-column>
          <el-table-column prop="albumTitle" label="专辑" min-width="180">
            <template #default="{ row }">{{ row.albumTitle || '未知专辑' }}</template>
          </el-table-column>
          <el-table-column label="时长" width="90">
            <template #default="{ row }">{{ formatDuration(row.duration) }}</template>
          </el-table-column>
          <el-table-column label="格式" width="90">
            <template #default="{ row }">
              <el-tag size="small" effect="dark" class="format-tag">{{ row.format }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="150" fixed="right">
            <template #default="{ row }">
              <el-button :icon="VideoPlay" circle size="small" @click.stop="play(row)" />
              <el-button
                :icon="row.favorited ? StarFilled : Star"
                circle
                size="small"
                :class="{ 'is-fav': row.favorited }"
                @click.stop="toggleFavorite(row)"
              />
              <el-button
                :icon="Delete"
                circle
                size="small"
                type="danger"
                class="delete-action"
                @click.stop="removeTrack(row)"
              />
            </template>
          </el-table-column>
        </el-table>

        <div class="pagination">
          <el-pagination
            v-model:current-page="page"
            v-model:page-size="size"
            :total="total"
            :page-sizes="[10, 20, 30, 50]"
            layout="total, sizes, prev, pager, next"
            background
            @current-change="handlePageChange"
            @size-change="handleSizeChange"
          />
        </div>
      </template>
    </GlassCard>
  </div>
</template>

<style scoped lang="scss">
.library-view {
  min-width: 0;
}

.hidden-input {
  display: none;
}

.toolbar {
  display: flex;
  justify-content: space-between;
  gap: var(--space-5);
  align-items: center;
  margin-bottom: var(--space-5);
}

.summary {
  display: flex;
  flex-direction: column;
  gap: 5px;
  min-width: 220px;

  strong {
    color: var(--color-fg-primary);
    font-size: 18px;
  }

  span {
    color: var(--color-fg-secondary);
    font-size: 13px;
  }
}

.upload-progress {
  width: 220px;
}

.filters {
  display: grid;
  grid-template-columns: minmax(220px, 320px) 120px auto auto;
  gap: var(--space-3);
  align-items: center;
}

.table-card {
  height: calc(100vh - 260px);
  min-height: 420px;
  overflow: hidden;
}

.table-cover {
  width: 44px;
  height: 44px;
  border-radius: 8px;
}

.track-title {
  color: var(--color-fg-primary);
  font-weight: 600;
}

.track-meta {
  margin-top: 3px;
  color: var(--color-fg-tertiary);
  font-size: 12px;
}

.pagination {
  display: flex;
  justify-content: flex-end;
  padding: var(--space-4);
  border-top: 1px solid rgba(255, 255, 255, 0.05);
}

:deep(.is-fav) {
  color: var(--color-brand);
  border-color: rgba(244, 245, 247, 0.38);
}

:deep(.format-tag) {
  --el-tag-bg-color: rgba(200, 168, 98, 0.18);
  --el-tag-border-color: rgba(200, 168, 98, 0.32);
  --el-tag-text-color: #D4C5A0;
  font-weight: 700;
}

:deep(.delete-action) {
  --el-button-text-color: #D4C5A0;
  --el-button-bg-color: rgba(200, 168, 98, 0.12);
  --el-button-border-color: rgba(200, 168, 98, 0.34);
  --el-button-hover-text-color: #0A0A0B;
  --el-button-hover-bg-color: #C8A862;
  --el-button-hover-border-color: #C8A862;
  --el-button-active-bg-color: #D4C5A0;
  --el-button-active-border-color: #D4C5A0;
}

@media (max-width: 1100px) {
  .toolbar {
    align-items: stretch;
    flex-direction: column;
  }

  .filters {
    grid-template-columns: 1fr 120px auto auto;
  }
}
</style>
