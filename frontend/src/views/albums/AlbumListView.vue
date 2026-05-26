<script setup lang="ts">
import { onMounted, ref } from 'vue';
import { useRouter } from 'vue-router';
import { Refresh, Search } from '@element-plus/icons-vue';
import PageHeader from '@/components/common/PageHeader.vue';
import LoadingBlock from '@/components/common/LoadingBlock.vue';
import EmptyState from '@/components/common/EmptyState.vue';
import GlassCard from '@/components/common/GlassCard.vue';
import SmartCover from '@/components/common/SmartCover.vue';
import SoundprintCircularGallery from '@/components/common/SoundprintCircularGallery.vue';
import { albumApi } from '@/api/album';
import { dashboardApi } from '@/api/dashboard';
import type { Album } from '@/types/album';

const router = useRouter();
const featuredAlbums = ref<Album[]>([]);
const albums = ref<Album[]>([]);
const loading = ref(false);
const keyword = ref('');
const total = ref(0);

async function loadAlbums() {
  loading.value = true;
  try {
    const result = await albumApi.page({
      page: 1,
      size: 48,
      keyword: keyword.value || undefined,
    });
    albums.value = result.records;
    total.value = result.total;
  } finally {
    loading.value = false;
  }
}

async function loadFeaturedAlbums() {
  const dashboard = await dashboardApi.get();
  featuredAlbums.value = dashboard.featuredAlbums || [];
}

onMounted(async () => {
  await Promise.all([loadFeaturedAlbums(), loadAlbums()]);
});

function onAlbumSelect(album: { id?: number }) {
  if (album.id == null) return;
  router.push(`/albums/${album.id}`);
}
</script>

<template>
  <div class="album-list-view">
    <section v-if="featuredAlbums.length > 0" class="hero-section">
      <div class="hero-label-row">
        <span class="hero-label">FEATURED</span>
        <h2 class="hero-title">精选专辑</h2>
      </div>
      <div class="gallery-container">
        <SoundprintCircularGallery :items="featuredAlbums" type="album" @select="onAlbumSelect" />
      </div>
    </section>

    <section class="list-section">
      <PageHeader
        title="所有专辑"
        subtitle="按专辑浏览音乐库"
      >
        <template #actions>
          <el-input
            v-model="keyword"
            class="search"
            placeholder="搜索专辑"
            :prefix-icon="Search"
            clearable
            @keyup.enter="loadAlbums"
            @clear="loadAlbums"
          />
          <el-button :icon="Refresh" @click="loadAlbums">刷新</el-button>
        </template>
      </PageHeader>

      <LoadingBlock v-if="loading" text="正在加载专辑..." />
      <EmptyState
        v-else-if="albums.length === 0"
        title="暂无专辑"
        description="上传音乐后，系统会根据元数据建立专辑信息。"
      />

      <div v-else class="album-grid">
        <GlassCard
          v-for="album in albums"
          :key="album.id"
          hoverable
          padding="none"
          class="album-card"
          @click="router.push(`/albums/${album.id}`)"
        >
          <SmartCover
            :src="album.coverUrl"
            :alt="album.title"
            :fallback-text="album.title"
            placeholder-type="album"
            class="cover"
          />
          <div class="body">
            <strong>{{ album.title }}</strong>
            <span>{{ album.artistName || '未知艺术家' }}</span>
            <small>{{ album.releaseYear || '未知年份' }} · {{ album.genre || '未分类' }}</small>
          </div>
        </GlassCard>
      </div>

      <p v-if="!loading && total > albums.length" class="footnote">
        当前展示前 {{ albums.length }} 张，共 {{ total }} 张。
      </p>
    </section>
  </div>
</template>

<style scoped lang="scss">
.album-list-view {
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

.search {
  width: 240px;
}

.album-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(156px, 1fr));
  gap: var(--space-5);
}

.album-card {
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
