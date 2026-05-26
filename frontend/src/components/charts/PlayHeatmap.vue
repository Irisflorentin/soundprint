<script setup lang="ts">
import { computed } from 'vue';
import type { EChartsOption } from 'echarts';
import dayjs from 'dayjs';
import BaseChart from './BaseChart.vue';
import type { HeatmapItem } from '@/types/stats';
import { SOUNDPRINT_CHART_COLORS } from '@/utils/echarts-theme';

const props = defineProps<{
  data: HeatmapItem[];
  loading?: boolean;
}>();

const option = computed<EChartsOption>(() => {
  const end = dayjs();
  const start = end.subtract(364, 'day');
  const max = Math.max(1, ...props.data.map((item) => item.count));

  return {
    title: {
      text: '365 天听歌热力图',
      left: 0,
      top: 0,
    },
    tooltip: {
      formatter: (params) => {
        const item = Array.isArray(params) ? params[0] : params;
        const value = Array.isArray(item.value) ? item.value : [];
        const date = value[0] as string;
        const count = Number(value[1] || 0);
        const seconds = Number(value[2] || 0);
        return `${date}<br />播放 ${count} 次<br />时长 ${formatHours(seconds)} 小时`;
      },
    },
    visualMap: {
      min: 0,
      max,
      show: false,
      inRange: {
        color: [
          SOUNDPRINT_CHART_COLORS.heatmapEmpty,
          SOUNDPRINT_CHART_COLORS.heatmapLow,
          SOUNDPRINT_CHART_COLORS.heatmapMid,
          SOUNDPRINT_CHART_COLORS.success,
        ],
      },
    },
    calendar: {
      top: 58,
      left: 0,
      right: 0,
      cellSize: ['auto', 14],
      range: [start.format('YYYY-MM-DD'), end.format('YYYY-MM-DD')],
      itemStyle: {
        color: SOUNDPRINT_CHART_COLORS.heatmapEmpty,
        borderWidth: 2,
        borderColor: SOUNDPRINT_CHART_COLORS.cardBg,
        borderRadius: 3,
      },
      splitLine: {
        lineStyle: {
          color: 'transparent',
        },
      },
      yearLabel: { show: false },
      monthLabel: {
        color: SOUNDPRINT_CHART_COLORS.tertiaryText,
        nameMap: 'ZH',
      },
      dayLabel: {
        color: SOUNDPRINT_CHART_COLORS.tertiaryText,
        nameMap: 'ZH',
      },
    },
    series: [
      {
        type: 'heatmap',
        coordinateSystem: 'calendar',
        data: props.data.map((item) => [item.date, item.count, item.totalSeconds]),
      },
    ],
  };
});

function formatHours(seconds: number) {
  return (seconds / 3600).toFixed(1);
}
</script>

<template>
  <BaseChart :option="option" :loading="loading" height="220px" />
</template>
