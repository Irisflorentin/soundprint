<script setup lang="ts">
import { computed } from 'vue';
import type { EChartsOption } from 'echarts';
import BaseChart from './BaseChart.vue';
import type { MonthlyTrendItem } from '@/types/stats';
import { SOUNDPRINT_CHART_COLORS } from '@/utils/echarts-theme';

const props = defineProps<{
  data: MonthlyTrendItem[];
  loading?: boolean;
}>();

const option = computed<EChartsOption>(() => ({
  title: {
    text: '月度趋势',
    left: 0,
    top: 0,
  },
  tooltip: {
    trigger: 'axis',
    formatter: (params) => {
      const item = Array.isArray(params) ? params[0] : params;
      const row = props.data[item.dataIndex];
      return `${row.month}<br />播放 ${row.playCount} 次<br />时长 ${formatHours(row.totalSeconds)} 小时`;
    },
  },
  grid: {
    left: 8,
    right: 18,
    top: 56,
    bottom: 8,
    containLabel: true,
  },
  xAxis: {
    type: 'category',
    boundaryGap: false,
    data: props.data.map((item) => item.month),
    axisLabel: {
      formatter: (value: string) => `${value.slice(5)}月`,
    },
  },
  yAxis: {
    type: 'value',
    minInterval: 1,
  },
  series: [
    {
      name: '播放次数',
      type: 'line',
      smooth: true,
      symbol: 'circle',
      symbolSize: 7,
      data: props.data.map((item) => item.playCount),
      lineStyle: {
        width: 3,
        color: {
          type: 'linear',
          x: 0,
          y: 0,
          x2: 1,
          y2: 0,
          colorStops: [
            { offset: 0, color: SOUNDPRINT_CHART_COLORS.brand },
            { offset: 1, color: SOUNDPRINT_CHART_COLORS.info },
          ],
        },
      },
      itemStyle: {
        color: SOUNDPRINT_CHART_COLORS.brand,
      },
      areaStyle: {
        color: {
          type: 'linear',
          x: 0,
          y: 0,
          x2: 0,
          y2: 1,
          colorStops: [
            { offset: 0, color: SOUNDPRINT_CHART_COLORS.brandAreaStart },
            { offset: 1, color: SOUNDPRINT_CHART_COLORS.brandAreaEnd },
          ],
        },
      },
    },
  ],
}));

function formatHours(seconds: number) {
  return (seconds / 3600).toFixed(1);
}
</script>

<template>
  <BaseChart :option="option" :loading="loading" height="320px" />
</template>
