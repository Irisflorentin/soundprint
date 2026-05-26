<script setup lang="ts">
import { nextTick, onMounted, onUnmounted, ref, shallowRef, watch } from 'vue';
import * as echarts from 'echarts';
import { SOUNDPRINT_CHART_COLORS, SOUNDPRINT_THEME_NAME } from '@/utils/echarts-theme';

const props = withDefaults(defineProps<{
  option: echarts.EChartsOption;
  height?: string;
  loading?: boolean;
}>(), {
  height: '320px',
  loading: false,
});

const container = ref<HTMLDivElement | null>(null);
const chart = shallowRef<echarts.ECharts | null>(null);
let resizeObserver: ResizeObserver | null = null;

onMounted(async () => {
  await nextTick();
  if (!container.value) return;
  chart.value = echarts.init(container.value, SOUNDPRINT_THEME_NAME, { renderer: 'canvas' });
  applyOption();
  syncLoading();
  resizeObserver = new ResizeObserver(() => {
    chart.value?.resize();
  });
  resizeObserver.observe(container.value);
  window.addEventListener('resize', handleResize);
});

onUnmounted(() => {
  window.removeEventListener('resize', handleResize);
  resizeObserver?.disconnect();
  resizeObserver = null;
  chart.value?.dispose();
  chart.value = null;
});

watch(() => props.option, applyOption, { deep: true });
watch(() => props.loading, syncLoading);

function applyOption() {
  if (!chart.value) return;
  chart.value.setOption(props.option, {
    notMerge: true,
    lazyUpdate: true,
  });
}

function syncLoading() {
  if (!chart.value) return;
  if (props.loading) {
    chart.value.showLoading('default', {
      text: '',
      color: SOUNDPRINT_CHART_COLORS.brand,
      maskColor: 'rgba(15, 15, 30, 0.35)',
    });
  } else {
    chart.value.hideLoading();
  }
}

function handleResize() {
  chart.value?.resize();
}
</script>

<template>
  <div ref="container" class="base-chart" :style="{ height }" />
</template>

<style lang="scss" scoped>
.base-chart {
  width: 100%;
  min-width: 0;
}
</style>
