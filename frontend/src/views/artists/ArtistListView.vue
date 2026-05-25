<script setup lang="ts">
import { onMounted, ref } from 'vue';
import { useRouter } from 'vue-router';
import { Refresh, Search } from '@element-plus/icons-vue';
import PageHeader from '@/components/common/PageHeader.vue';
import LoadingBlock from '@/components/common/LoadingBlock.vue';
import EmptyState from '@/components/common/EmptyState.vue';
import GlassCard from '@/components/common/GlassCard.vue';
import { artistApi } from '@/api/artist';
import type { Artist } from '@/types/artist';

const router = useRouter();
const artists = ref<Artist[]>([]);
const loading = ref(false);
const keyword = ref('');
const total = ref(0);

async function loadArtists() {
  loading.value = true;
  try {
    const result = await artistApi.page({
      page: 1,
      size: 48,
      keyword: keyword.value || undefined,
    });
    artists.value = result.records;
    total.value = result.total;
  } finally {
    loading.value = false;
  }
}

onMounted(loadArtists);
</script>

<template>
  <div class="artist-list-view">
    <PageHeader
      title="艺术家"
      subtitle="按艺术家浏览曲目、专辑和风格"
    >
      <template #actions>
        <el-input
          v-model="keyword"
          class="search"
          placeholder="搜索艺术家"
          :prefix-icon="Search"
          clearable
          @keyup.enter="loadArtists"
          @clear="loadArtists"
        />
        <el-button :icon="Refresh" @click="loadArtists">刷新</el-button>
      </template>
    </PageHeader>

    <LoadingBlock v-if="loading" text="正在加载艺术家..." />
    <EmptyState
      v-else-if="artists.length === 0"
      title="暂无艺术家"
      description="上传音乐或新建曲目后，这里会显示艺术家信息。"
    />

    <div v-else class="artist-grid">
      <GlassCard
        v-for="artist in artists"
        :key="artist.id"
        hoverable
        padding="lg"
        class="artist-card"
        @click="router.push(`/artists/${artist.id}`)"
      >
        <div
          class="avatar"
          :style="artist.avatarUrl ? { backgroundImage: `url(${artist.avatarUrl})` } : undefined"
        >
          <span v-if="!artist.avatarUrl">{{ artist.name.slice(0, 1) }}</span>
        </div>
        <strong>{{ artist.name }}</strong>
        <span>{{ artist.country || '未知地区' }}</span>
        <small>{{ artist.formedYear ? `${artist.formedYear} 出道/成立` : '年份未知' }}</small>
      </GlassCard>
    </div>

    <p v-if="!loading && total > artists.length" class="footnote">
      当前展示前 {{ artists.length }} 位，共 {{ total }} 位。
    </p>
  </div>
</template>

<style scoped lang="scss">
.artist-list-view {
  min-width: 0;
}

.search {
  width: 240px;
}

.artist-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(164px, 1fr));
  gap: var(--space-5);
}

.artist-card {
  display: flex;
  min-height: 220px;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  text-align: center;
}

.avatar {
  width: 88px;
  height: 88px;
  margin-bottom: var(--space-4);
  border-radius: 50%;
  display: grid;
  place-items: center;
  color: var(--color-fg-primary);
  font-size: 34px;
  font-weight: 800;
  background:
    linear-gradient(135deg, rgba(124, 58, 237, 0.78), rgba(6, 182, 212, 0.62));
  background-position: center;
  background-size: cover;
}

strong,
span,
small {
  max-width: 100%;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

strong {
  color: var(--color-fg-primary);
  font-size: 15px;
}

span {
  margin-top: 6px;
  color: var(--color-fg-secondary);
  font-size: 13px;
}

small {
  margin-top: 3px;
  color: var(--color-fg-tertiary);
  font-size: 12px;
}

.footnote {
  margin-top: var(--space-4);
  color: var(--color-fg-tertiary);
  font-size: 12px;
}
</style>
