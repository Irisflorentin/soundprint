<script setup lang="ts">
import { computed } from 'vue';
import type { EChartsOption } from 'echarts';
import BaseChart from './BaseChart.vue';
import type { GenreDistributionItem } from '@/types/stats';
import { SOUNDPRINT_CHART_COLORS } from '@/utils/echarts-theme';

const props = defineProps<{
  data: GenreDistributionItem[];
  loading?: boolean;
}>();

const option = computed<EChartsOption>(() => ({
  title: {
    text: '流派分布',
    left: 0,
    top: 0,
  },
  tooltip: {
    trigger: 'item',
    formatter: '{b}<br />{c} 次 · {d}%',
  },
  legend: {
    orient: 'vertical',
    right: 0,
    top: 48,
    itemWidth: 10,
    itemHeight: 10,
  },
  series: [
    {
      name: '流派',
      type: 'pie',
      radius: ['48%', '72%'],
      center: ['36%', '56%'],
      avoidLabelOverlap: true,
      itemStyle: {
        borderColor: SOUNDPRINT_CHART_COLORS.cardBg,
        borderWidth: 3,
      },
      label: {
        color: SOUNDPRINT_CHART_COLORS.secondaryText,
        formatter: '{b}',
      },
      labelLine: {
        lineStyle: {
          color: 'rgba(255, 255, 255, 0.18)',
        },
      },
      emphasis: {
        scale: true,
        scaleSize: 8,
        itemStyle: {
          shadowBlur: 24,
          shadowColor: 'rgba(200, 168, 98, 0.5)',
        },
      },
      data: props.data.map((item) => ({
        name: item.genre || '未分类',
        value: item.count,
      })),
    },
  ],
}));
</script>

<template>
  <BaseChart :option="option" :loading="loading" height="360px" />
</template>
