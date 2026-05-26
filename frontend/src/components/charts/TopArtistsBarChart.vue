<script setup lang="ts">
import { computed } from 'vue';
import type { EChartsOption } from 'echarts';
import BaseChart from './BaseChart.vue';
import type { TopArtistItem } from '@/types/stats';
import { SOUNDPRINT_CHART_COLORS } from '@/utils/echarts-theme';

const props = defineProps<{
  data: TopArtistItem[];
  loading?: boolean;
}>();

const option = computed<EChartsOption>(() => {
  const rows = [...props.data].reverse();
  return {
    title: {
      text: 'Top 艺术家',
      left: 0,
      top: 0,
    },
    tooltip: {
      trigger: 'axis',
      axisPointer: { type: 'shadow' },
      formatter: (params) => {
        const item = Array.isArray(params) ? params[0] : params;
        return `${item.name}<br />播放 ${item.value} 次`;
      },
    },
    grid: {
      left: 8,
      right: 24,
      top: 56,
      bottom: 8,
      containLabel: true,
    },
    xAxis: {
      type: 'value',
      minInterval: 1,
    },
    yAxis: {
      type: 'category',
      data: rows.map((item) => item.artistName || '未知艺术家'),
      axisLabel: {
        width: 120,
        overflow: 'truncate',
      },
    },
    series: [
      {
        name: '播放次数',
        type: 'bar',
        data: rows.map((item) => item.playCount),
        barWidth: 16,
        itemStyle: {
          borderRadius: [0, 8, 8, 0],
          color: {
            type: 'linear',
            x: 0,
            y: 0,
            x2: 1,
            y2: 0,
            colorStops: [
              { offset: 0, color: SOUNDPRINT_CHART_COLORS.brand },
              { offset: 1, color: SOUNDPRINT_CHART_COLORS.accent },
            ],
          },
        },
      },
    ],
  };
});
</script>

<template>
  <BaseChart :option="option" :loading="loading" height="360px" />
</template>
