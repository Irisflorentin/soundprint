<script setup lang="ts">
import { onMounted, ref } from 'vue';
import PageHeader from '@/components/common/PageHeader.vue';
import KpiCard from '@/components/charts/KpiCard.vue';
import GenrePieChart from '@/components/charts/GenrePieChart.vue';
import TopArtistsBarChart from '@/components/charts/TopArtistsBarChart.vue';
import MonthlyTrendChart from '@/components/charts/MonthlyTrendChart.vue';
import PlayHeatmap from '@/components/charts/PlayHeatmap.vue';
import { statsApi } from '@/api/stats';
import type {
  GenreDistributionItem,
  HeatmapItem,
  MonthlyTrendItem,
  StatsOverview,
  TopArtistItem,
} from '@/types/stats';

const loading = ref(true);
const overview = ref<StatsOverview | null>(null);
const genres = ref<GenreDistributionItem[]>([]);
const topArtists = ref<TopArtistItem[]>([]);
const monthly = ref<MonthlyTrendItem[]>([]);
const heatmap = ref<HeatmapItem[]>([]);

onMounted(loadStats);

async function loadStats() {
  loading.value = true;
  try {
    const [overviewData, genreData, topArtistData, monthlyData, heatmapData] = await Promise.all([
      statsApi.overview(),
      statsApi.genres(),
      statsApi.topArtists(10),
      statsApi.monthlyTrend(12),
      statsApi.heatmap(365),
    ]);
    overview.value = overviewData;
    genres.value = genreData;
    topArtists.value = topArtistData;
    monthly.value = monthlyData;
    heatmap.value = heatmapData;
  } finally {
    loading.value = false;
  }
}

function formatHours(seconds: number) {
  return Math.round(seconds / 3600);
}
</script>

<template>
  <div class="stats-view">
    <PageHeader title="听歌报告" subtitle="流派分布 / 月度趋势 / Top 艺术家 / 热力图" />

    <section v-if="overview" class="kpi-row">
      <KpiCard
        label="累计曲目"
        :value="overview.totalTracks"
        unit="首"
        icon="♪"
        accent="brand"
      />
      <KpiCard
        label="累计听歌时长"
        :value="formatHours(overview.totalPlayedSeconds)"
        unit="小时"
        icon="◷"
        accent="accent"
      />
      <KpiCard
        label="累计播放"
        :value="overview.totalPlays"
        unit="次"
        icon="▶"
        accent="success"
      />
      <KpiCard
        label="收藏"
        :value="overview.favoriteCount"
        unit="首"
        icon="♥"
        accent="warning"
      />
    </section>

    <section class="chart-block heatmap-block">
      <PlayHeatmap :data="heatmap" :loading="loading" />
    </section>

    <section class="chart-grid-2">
      <div class="chart-block">
        <GenrePieChart :data="genres" :loading="loading" />
      </div>
      <div class="chart-block">
        <MonthlyTrendChart :data="monthly" :loading="loading" />
      </div>
    </section>

    <section class="chart-block">
      <TopArtistsBarChart :data="topArtists" :loading="loading" />
    </section>
  </div>
</template>

<style lang="scss" scoped>
.stats-view {
  display: flex;
  flex-direction: column;
  gap: var(--space-5);
}

.kpi-row {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: var(--space-4);
}

.chart-grid-2 {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: var(--space-5);
}

.chart-block {
  min-width: 0;
  padding: var(--space-5);
  border: 1px solid rgba(255, 255, 255, 0.06);
  border-radius: var(--radius-card);
  background: rgba(255, 255, 255, 0.035);
}

.heatmap-block {
  overflow: hidden;
}

@media (max-width: 1200px) {
  .kpi-row,
  .chart-grid-2 {
    grid-template-columns: 1fr;
  }
}
</style>
