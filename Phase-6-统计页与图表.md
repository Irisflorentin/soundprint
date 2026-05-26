# Phase 6：统计页 + ECharts 图表 + 波形优化

> Soundprint 项目的第七个阶段文档。
> Codex 主力 + Claude Code 审计模式延续。
> 本阶段完成后，"听歌报告" 页变成一块漂亮的数据大屏 —— 这是答辩演示的第二个视觉爆点。

---

## 🎯 阶段目标

1. **听歌报告页**：4 个 KPI 卡片 + 5 个 ECharts 图表，深色 + 紫色统一主题
2. **ECharts 全站统一主题**：写到独立 `echarts-theme.ts`，所有图表共用
3. **后端聚合接口**：Phase 3 留的统计接口字段微调（如有需要）
4. **波形 peaks 接口**：后端预计算波形峰值数据，前端 wavesurfer 用 peaks 加载（避免每次下载完整 FLAC）—— **顺手解决性能隐患**
5. **状态色板补齐**：Phase 5 临时用的状态色（success/warning/danger）正式写入 Tailwind config 作为设计令牌

**关键约束**：
- 不集成 vue-bits（Phase 7）
- 不动 Phase 4/5 已实现的页面（除非小修补）
- 所有图表配色严格走设计令牌
- ECharts 主题必须包含暗色背景，不能有白底闪一下的情况

---

## 📋 任务清单

### 任务 0：环境核验

```powershell
# 前后端启动正常即可
cd D:\Claude_Playground\Soundprint
git status   # 工作树干净，main 在 f259009 之后
```

ECharts 在 Phase 4 已经装过（`echarts: ^5.x`），**确认 package.json 里有 echarts 依赖**。如果没有：

```powershell
cd frontend
npm install echarts@5
```

---

### 任务 1：补齐设计令牌中的状态色板

Phase 5 临时用的 success/warning/danger 色，现在正式纳入设计系统。

#### 1.1 `tailwind.config.js` 补充

在 `theme.extend.colors` 下追加（**不要删已有色板**）：

```js
// 状态色（success/warning/danger/info）
success: {
  400: '#4ADE80',
  500: '#22C55E',
  600: '#16A34A',
  bg:  'rgba(34, 197, 94, 0.15)',
},
warning: {
  400: '#FBBF24',
  500: '#F59E0B',
  600: '#D97706',
  bg:  'rgba(245, 158, 11, 0.15)',
},
danger: {
  400: '#F87171',
  500: '#EF4444',
  600: '#DC2626',
  bg:  'rgba(239, 68, 68, 0.15)',
},
info: {
  400: '#60A5FA',
  500: '#3B82F6',
  600: '#2563EB',
  bg:  'rgba(59, 130, 246, 0.15)',
},
```

#### 1.2 `styles/tokens.scss` 补充

```scss
:root {
  // ... 已有变量不动 ...

  // 状态色
  --color-success: #22C55E;
  --color-success-bg: rgba(34, 197, 94, 0.15);
  --color-warning: #F59E0B;
  --color-warning-bg: rgba(245, 158, 11, 0.15);
  --color-danger:  #EF4444;
  --color-danger-bg: rgba(239, 68, 68, 0.15);
  --color-info:    #3B82F6;
  --color-info-bg: rgba(59, 130, 246, 0.15);
}
```

#### 1.3 替换 Phase 5 中硬编码状态色的地方

搜索 `rgba(34,197,94` / `rgba(124,58,237` / `rgba(239,68,68` 这种硬编码，**全部替换为 CSS 变量**：

```scss
/* 替换前 */
background: rgba(34, 197, 94, 0.15);
color: rgb(34, 197, 94);

/* 替换后 */
background: var(--color-success-bg);
color: var(--color-success);
```

这是个**纯重构**改动，不引入新视觉，**但让设计系统真正统一**。Phase 5 留的技术债清掉。

---

### 任务 2：ECharts 全站统一主题

#### 2.1 创建 `src/utils/echarts-theme.ts`

```ts
import * as echarts from 'echarts';

/**
 * Soundprint ECharts 主题
 *
 * 设计原则：
 * - 深色背景，与 Soundprint 主界面一致
 * - 主数据系列用品牌紫，强调用青色
 * - 网格线极淡，避免抢戏
 * - tooltip 用磨砂玻璃风
 * - 字体与全站一致
 */
export const SOUNDPRINT_THEME_NAME = 'soundprint';

export const SOUNDPRINT_THEME = {
  // 数据系列调色板（按序循环使用）
  color: [
    '#7C3AED',  // 品牌紫
    '#06B6D4',  // 强调青
    '#22C55E',  // 成功绿
    '#F59E0B',  // 警告橙
    '#EF4444',  // 危险红
    '#3B82F6',  // 信息蓝
    '#A78BFA',  // 紫浅
    '#22D3EE',  // 青浅
  ],

  backgroundColor: 'transparent',

  textStyle: {
    fontFamily: 'Inter, "PingFang SC", "Noto Sans SC", system-ui, sans-serif',
    color: '#A1A1AA',
  },

  title: {
    textStyle: {
      color: '#F5F5F7',
      fontWeight: 600,
      fontSize: 16,
    },
    subtextStyle: {
      color: '#71717A',
      fontSize: 12,
    },
  },

  // 坐标轴
  categoryAxis: {
    axisLine:  { lineStyle: { color: 'rgba(255,255,255,0.1)' } },
    axisTick:  { lineStyle: { color: 'rgba(255,255,255,0.1)' } },
    axisLabel: { color: '#A1A1AA', fontSize: 11 },
    splitLine: { lineStyle: { color: 'rgba(255,255,255,0.04)' } },
  },
  valueAxis: {
    axisLine:  { lineStyle: { color: 'rgba(255,255,255,0.1)' } },
    axisTick:  { lineStyle: { color: 'rgba(255,255,255,0.1)' } },
    axisLabel: { color: '#A1A1AA', fontSize: 11 },
    splitLine: { lineStyle: { color: 'rgba(255,255,255,0.04)' } },
  },

  // tooltip 磨砂玻璃风
  tooltip: {
    backgroundColor: 'rgba(15, 15, 30, 0.95)',
    borderColor: 'rgba(124, 58, 237, 0.4)',
    borderWidth: 1,
    textStyle: { color: '#F5F5F7', fontSize: 12 },
    extraCssText: 'backdrop-filter: blur(20px); box-shadow: 0 8px 32px rgba(0,0,0,0.4);',
  },

  // legend
  legend: {
    textStyle: { color: '#A1A1AA', fontSize: 12 },
    icon: 'circle',
    itemGap: 16,
  },

  // 饼图
  pie: {
    itemStyle: {
      borderColor: '#0A0A14',
      borderWidth: 2,
    },
    label: {
      color: '#F5F5F7',
    },
  },

  // 柱图
  bar: {
    itemStyle: {
      borderRadius: [4, 4, 0, 0],
    },
  },

  // 折线图
  line: {
    smooth: true,
    symbolSize: 6,
    lineStyle: { width: 2 },
    areaStyle: { opacity: 0.15 },
  },

  // 热力图
  heatmap: {
    itemStyle: {
      borderColor: 'transparent',
      borderWidth: 1,
    },
  },

  // visualMap 渐变
  visualMap: {
    textStyle: { color: '#A1A1AA' },
    inRange: {
      color: ['#1A1530', '#5B21B6', '#7C3AED', '#A78BFA', '#06B6D4'],
    },
  },
};

/**
 * 全局注册主题。在 main.ts 中调用一次。
 */
export function registerSoundprintTheme() {
  echarts.registerTheme(SOUNDPRINT_THEME_NAME, SOUNDPRINT_THEME);
}
```

#### 2.2 `main.ts` 注册主题

在 `app.mount('#app')` 之前加：

```ts
import { registerSoundprintTheme } from '@/utils/echarts-theme';
registerSoundprintTheme();
```

---

### 任务 3：通用图表组件封装

写一个 `BaseChart.vue`，**所有具体图表都基于它**，避免每个图表都写一遍初始化/销毁/resize 代码。

#### `src/components/charts/BaseChart.vue`

```vue
<script setup lang="ts">
import { ref, onMounted, onUnmounted, watch, shallowRef } from 'vue';
import * as echarts from 'echarts';
import { SOUNDPRINT_THEME_NAME } from '@/utils/echarts-theme';

const props = defineProps<{
  option: echarts.EChartsOption;
  height?: string;
  loading?: boolean;
}>();

const container = ref<HTMLDivElement>();
const chart = shallowRef<echarts.ECharts | null>(null);

function initChart() {
  if (!container.value) return;
  chart.value = echarts.init(container.value, SOUNDPRINT_THEME_NAME);
  chart.value.setOption(props.option);
}

function resizeChart() {
  chart.value?.resize();
}

onMounted(() => {
  initChart();
  window.addEventListener('resize', resizeChart);
});

onUnmounted(() => {
  window.removeEventListener('resize', resizeChart);
  chart.value?.dispose();
});

watch(() => props.option, (opt) => {
  // notMerge=true 避免老数据残留，lazyUpdate=true 提升性能
  chart.value?.setOption(opt, { notMerge: true, lazyUpdate: true });
}, { deep: true });

watch(() => props.loading, (loading) => {
  if (loading) chart.value?.showLoading('default', {
    text: '加载中…',
    color: '#7C3AED',
    textColor: '#A1A1AA',
    maskColor: 'rgba(10, 10, 20, 0.6)',
  });
  else chart.value?.hideLoading();
});
</script>

<template>
  <div
    ref="container"
    class="base-chart"
    :style="{ height: height || '320px' }"
  />
</template>

<style lang="scss" scoped>
.base-chart {
  width: 100%;
}
</style>
```

**核心设计**（Codex 必须理解）：

1. `shallowRef` 包装 `chart` 实例：echarts 实例内部状态复杂，深层响应会拖累性能
2. `dispose` 在 unmount 时调用：**防止 echarts 内存泄漏**（这是 Phase 5 审计要查的同类问题）
3. `window.resize` 监听 + `chart.resize()`：响应式布局必需
4. `notMerge: true` + `lazyUpdate: true`：updateOption 时不残留旧数据 + 下一帧再画提升性能

---

### 任务 4：5 个具体图表组件

每个图表都是 BaseChart 的封装。**Codex 严格按下列每个组件的代码框架实现**。

#### 4.1 `components/charts/KpiCard.vue`（KPI 数字卡片）

不是 ECharts 图，是一个带翻牌动画的数字卡片。

```vue
<script setup lang="ts">
import { ref, watch, onMounted } from 'vue';

const props = withDefaults(defineProps<{
  label: string;
  value: number;
  unit?: string;
  icon?: string;       // emoji 或图标 unicode
  accent?: 'brand' | 'accent' | 'success' | 'warning';
}>(), {
  accent: 'brand',
});

const displayValue = ref(0);

// 数字翻牌动画
function animateValue(from: number, to: number, duration = 1200) {
  const start = performance.now();
  const tick = (now: number) => {
    const t = Math.min((now - start) / duration, 1);
    // easeOutCubic
    const eased = 1 - Math.pow(1 - t, 3);
    displayValue.value = Math.round(from + (to - from) * eased);
    if (t < 1) requestAnimationFrame(tick);
  };
  requestAnimationFrame(tick);
}

onMounted(() => animateValue(0, props.value));
watch(() => props.value, (v) => animateValue(displayValue.value, v));
</script>

<template>
  <div class="kpi-card" :data-accent="accent">
    <div class="icon" v-if="icon">{{ icon }}</div>
    <div class="value">
      {{ displayValue.toLocaleString() }}
      <span v-if="unit" class="unit">{{ unit }}</span>
    </div>
    <div class="label">{{ label }}</div>
  </div>
</template>

<style lang="scss" scoped>
.kpi-card {
  background: rgba(255,255,255,0.03);
  border: 1px solid rgba(255,255,255,0.06);
  border-radius: var(--radius-card);
  backdrop-filter: blur(20px);
  padding: var(--space-5);
  position: relative;
  overflow: hidden;
  transition: all 300ms var(--ease);

  &:hover {
    border-color: rgba(124, 58, 237, 0.3);
    transform: translateY(-2px);
  }

  // 顶部 2px 渐变细线
  &::before {
    content: '';
    position: absolute;
    top: 0; left: 0; right: 0;
    height: 2px;
    background: linear-gradient(90deg, var(--color-brand), var(--color-accent));
  }
  &[data-accent="success"]::before { background: linear-gradient(90deg, var(--color-success), var(--color-accent)); }
  &[data-accent="warning"]::before { background: linear-gradient(90deg, var(--color-warning), var(--color-brand)); }
  &[data-accent="accent"]::before  { background: linear-gradient(90deg, var(--color-accent), var(--color-brand)); }
}

.icon {
  font-size: 20px;
  margin-bottom: var(--space-2);
  opacity: 0.7;
}
.value {
  font-size: 32px;
  font-weight: 700;
  color: var(--color-fg-primary);
  font-variant-numeric: tabular-nums;
  line-height: 1.1;
}
.unit {
  font-size: 14px;
  color: var(--color-fg-secondary);
  font-weight: 400;
  margin-left: 4px;
}
.label {
  margin-top: var(--space-2);
  font-size: 13px;
  color: var(--color-fg-secondary);
}
</style>
```

#### 4.2 `components/charts/GenrePieChart.vue`（流派分布饼图）

```vue
<script setup lang="ts">
import { computed } from 'vue';
import type { EChartsOption } from 'echarts';
import BaseChart from './BaseChart.vue';
import type { GenreDistributionItem } from '@/types/stats';

const props = defineProps<{
  data: GenreDistributionItem[];
  loading?: boolean;
}>();

const option = computed<EChartsOption>(() => ({
  title: {
    text: '流派分布',
    subtext: '按播放次数加权',
    left: 'left',
    top: 0,
  },
  tooltip: {
    trigger: 'item',
    formatter: (params: any) =>
      `${params.name}<br/>播放次数：${params.value}<br/>占比：${params.percent}%`,
  },
  legend: {
    bottom: 0,
    type: 'scroll',
  },
  series: [
    {
      type: 'pie',
      radius: ['45%', '70%'],         // 环形饼图，留中心
      center: ['50%', '52%'],
      avoidLabelOverlap: true,
      itemStyle: {
        borderRadius: 6,
        borderColor: '#0A0A14',
        borderWidth: 3,
      },
      label: {
        show: true,
        formatter: '{b}\n{d}%',
        color: '#F5F5F7',
        fontSize: 12,
      },
      labelLine: {
        lineStyle: { color: 'rgba(255,255,255,0.2)' },
      },
      emphasis: {
        scale: true,
        scaleSize: 8,
        itemStyle: { shadowBlur: 24, shadowColor: 'rgba(124,58,237,0.4)' },
      },
      data: props.data.map(d => ({
        name: d.genre || '未分类',
        value: d.count,
      })),
    },
  ],
}));
</script>

<template>
  <BaseChart :option="option" :loading="loading" height="360px" />
</template>
```

#### 4.3 `components/charts/TopArtistsBarChart.vue`（Top 艺术家横向条形图）

```vue
<script setup lang="ts">
import { computed } from 'vue';
import type { EChartsOption } from 'echarts';
import BaseChart from './BaseChart.vue';
import type { TopArtistItem } from '@/types/stats';

const props = defineProps<{
  data: TopArtistItem[];
  loading?: boolean;
}>();

const option = computed<EChartsOption>(() => {
  // 横向柱图：分类轴在 y，值轴在 x，柱子从下往上长
  const sorted = [...props.data].sort((a, b) => a.playCount - b.playCount);

  return {
    title: {
      text: 'Top 艺术家',
      subtext: '按总播放次数',
      left: 'left',
      top: 0,
    },
    grid: {
      left: 100,
      right: 30,
      top: 60,
      bottom: 20,
      containLabel: false,
    },
    xAxis: {
      type: 'value',
      show: false,
    },
    yAxis: {
      type: 'category',
      data: sorted.map(a => a.artistName),
      axisTick: { show: false },
      axisLine: { show: false },
      axisLabel: { color: '#F5F5F7', fontSize: 13 },
    },
    tooltip: {
      trigger: 'axis',
      axisPointer: { type: 'none' },
    },
    series: [
      {
        type: 'bar',
        data: sorted.map(a => a.playCount),
        barWidth: 14,
        itemStyle: {
          borderRadius: [0, 4, 4, 0],
          color: {
            type: 'linear',
            x: 0, y: 0, x2: 1, y2: 0,
            colorStops: [
              { offset: 0, color: '#7C3AED' },
              { offset: 1, color: '#06B6D4' },
            ],
          },
        },
        label: {
          show: true,
          position: 'right',
          color: '#A1A1AA',
          fontSize: 11,
          formatter: '{c}',
        },
      },
    ],
  };
});
</script>

<template>
  <BaseChart :option="option" :loading="loading" height="360px" />
</template>
```

#### 4.4 `components/charts/MonthlyTrendChart.vue`（月度趋势折线图）

```vue
<script setup lang="ts">
import { computed } from 'vue';
import type { EChartsOption } from 'echarts';
import BaseChart from './BaseChart.vue';
import type { MonthlyTrendItem } from '@/types/stats';

const props = defineProps<{
  data: MonthlyTrendItem[];
  loading?: boolean;
}>();

const option = computed<EChartsOption>(() => ({
  title: {
    text: '月度听歌趋势',
    subtext: '最近 12 个月',
    left: 'left',
    top: 0,
  },
  grid: { left: 50, right: 30, top: 70, bottom: 40 },
  tooltip: {
    trigger: 'axis',
    formatter: (params: any) => {
      const p = params[0];
      const hours = (p.value / 3600).toFixed(1);
      return `${p.name}<br/>${hours} 小时`;
    },
  },
  xAxis: {
    type: 'category',
    data: props.data.map(m => m.month),
    boundaryGap: false,
    axisLabel: { color: '#A1A1AA', fontSize: 11 },
  },
  yAxis: {
    type: 'value',
    axisLabel: {
      formatter: (v: number) => `${(v / 3600).toFixed(0)}h`,
      color: '#A1A1AA',
    },
  },
  series: [
    {
      type: 'line',
      smooth: true,
      symbol: 'circle',
      symbolSize: 8,
      data: props.data.map(m => m.totalSeconds),
      lineStyle: {
        width: 3,
        color: {
          type: 'linear',
          x: 0, y: 0, x2: 1, y2: 0,
          colorStops: [
            { offset: 0, color: '#7C3AED' },
            { offset: 1, color: '#06B6D4' },
          ],
        },
      },
      itemStyle: {
        color: '#7C3AED',
        borderColor: '#0A0A14',
        borderWidth: 2,
      },
      areaStyle: {
        opacity: 0.4,
        color: {
          type: 'linear',
          x: 0, y: 0, x2: 0, y2: 1,
          colorStops: [
            { offset: 0, color: 'rgba(124,58,237,0.4)' },
            { offset: 1, color: 'rgba(124,58,237,0)' },
          ],
        },
      },
      emphasis: {
        focus: 'series',
        scale: 1.5,
      },
    },
  ],
}));
</script>

<template>
  <BaseChart :option="option" :loading="loading" height="320px" />
</template>
```

#### 4.5 `components/charts/PlayHeatmap.vue`（听歌热力图 / GitHub 贡献图样式）

这是答辩重头戏，**视觉冲击最强**。

```vue
<script setup lang="ts">
import { computed } from 'vue';
import type { EChartsOption } from 'echarts';
import dayjs from 'dayjs';
import BaseChart from './BaseChart.vue';
import type { HeatmapItem } from '@/types/stats';

const props = defineProps<{
  data: HeatmapItem[];        // 后端返回 [{ date: '2026-04-12', count: 5, totalSeconds: 1200 }]
  loading?: boolean;
}>();

const option = computed<EChartsOption>(() => {
  // 构建 365 天的完整数据（缺失日期补 0），不然热力图会有空洞
  const dataMap = new Map(props.data.map(d => [d.date, d.count]));
  const today = dayjs();
  const start = today.subtract(364, 'day');

  const fullData: [string, number][] = [];
  let max = 0;
  for (let i = 0; i < 365; i++) {
    const d = start.add(i, 'day').format('YYYY-MM-DD');
    const v = dataMap.get(d) || 0;
    if (v > max) max = v;
    fullData.push([d, v]);
  }

  return {
    title: {
      text: '听歌热力图',
      subtext: `最近 365 天 · 最高单日 ${max} 次`,
      left: 'left',
      top: 0,
    },
    tooltip: {
      formatter: (p: any) => {
        const [date, count] = p.data;
        return `${date}<br/>播放 ${count} 次`;
      },
    },
    visualMap: {
      show: false,           // 隐藏图例，靠 tooltip 看具体值
      min: 0,
      max: Math.max(max, 5),
      type: 'piecewise',
      pieces: [
        { min: 0, max: 0, color: 'rgba(255,255,255,0.04)' },     // 无播放
        { min: 1, max: 2, color: '#1A1530' },
        { min: 3, max: 5, color: '#5B21B6' },
        { min: 6, max: 10, color: '#7C3AED' },
        { min: 11, max: 9999, color: '#A78BFA' },
      ],
    },
    calendar: {
      top: 70,
      left: 30,
      right: 30,
      cellSize: ['auto', 14],
      range: [start.format('YYYY-MM-DD'), today.format('YYYY-MM-DD')],
      itemStyle: {
        borderColor: 'transparent',
        borderWidth: 2,
        borderRadius: 2,
      },
      splitLine: { show: false },
      yearLabel: { show: false },
      monthLabel: {
        color: '#A1A1AA',
        fontSize: 11,
      },
      dayLabel: {
        firstDay: 1,         // 周一为一周第一天
        color: '#71717A',
        fontSize: 10,
        nameMap: ['日', '一', '二', '三', '四', '五', '六'],
      },
    },
    series: [
      {
        type: 'heatmap',
        coordinateSystem: 'calendar',
        data: fullData,
      },
    ],
  };
});
</script>

<template>
  <BaseChart :option="option" :loading="loading" height="200px" />
</template>
```

#### 4.6 `components/charts/FormatPieChart.vue`（格式分布小饼图，可选）

加分项，**Phase 6 可选实现**——展示库里 FLAC/MP3/WAV/AAC 各占多少。需要后端补一个简单的聚合接口，或者前端从 `/api/tracks?size=1000` 拉全量自己 groupBy（数据量小时可接受）。

---

### 任务 5：实现 `StatsView.vue`

```vue
<script setup lang="ts">
import { ref, onMounted } from 'vue';
import PageHeader from '@/components/common/PageHeader.vue';
import KpiCard from '@/components/charts/KpiCard.vue';
import GenrePieChart from '@/components/charts/GenrePieChart.vue';
import TopArtistsBarChart from '@/components/charts/TopArtistsBarChart.vue';
import MonthlyTrendChart from '@/components/charts/MonthlyTrendChart.vue';
import PlayHeatmap from '@/components/charts/PlayHeatmap.vue';
import { statsApi } from '@/api/stats';
import type {
  StatsOverview, GenreDistributionItem,
  TopArtistItem, MonthlyTrendItem, HeatmapItem,
} from '@/types/stats';

const loading = ref(true);

const overview = ref<StatsOverview | null>(null);
const genres = ref<GenreDistributionItem[]>([]);
const topArtists = ref<TopArtistItem[]>([]);
const monthly = ref<MonthlyTrendItem[]>([]);
const heatmap = ref<HeatmapItem[]>([]);

onMounted(async () => {
  loading.value = true;
  try {
    // 并行请求 5 个聚合接口
    const [o, g, t, m, h] = await Promise.all([
      statsApi.overview(),
      statsApi.genres(),
      statsApi.topArtists(10),
      statsApi.monthlyTrend(12),
      statsApi.heatmap(365),
    ]);
    overview.value = o;
    genres.value = g;
    topArtists.value = t;
    monthly.value = m;
    heatmap.value = h;
  } finally {
    loading.value = false;
  }
});

// 格式化总时长（秒）为友好字符串
function formatHours(seconds: number): number {
  return Math.round(seconds / 3600);
}
</script>

<template>
  <div class="stats-view">
    <PageHeader title="听歌报告" subtitle="你的音乐数据可视化" />

    <!-- KPI 卡片行 -->
    <section class="kpi-row" v-if="overview">
      <KpiCard
        label="累计曲目"
        :value="overview.totalTracks"
        unit="首"
        icon="🎵"
        accent="brand"
      />
      <KpiCard
        label="累计听歌时长"
        :value="formatHours(overview.totalPlayedSeconds)"
        unit="小时"
        icon="⏱️"
        accent="accent"
      />
      <KpiCard
        label="累计播放"
        :value="overview.totalPlays"
        unit="次"
        icon="▶️"
        accent="success"
      />
      <KpiCard
        label="收藏"
        :value="overview.totalFavorites"
        unit="首"
        icon="❤️"
        accent="warning"
      />
    </section>

    <!-- 热力图（占整行） -->
    <section class="chart-block heatmap-block">
      <PlayHeatmap :data="heatmap" :loading="loading" />
    </section>

    <!-- 中间两列：饼图 + 月度趋势 -->
    <section class="chart-grid-2">
      <div class="chart-block">
        <GenrePieChart :data="genres" :loading="loading" />
      </div>
      <div class="chart-block">
        <MonthlyTrendChart :data="monthly" :loading="loading" />
      </div>
    </section>

    <!-- Top 艺术家（独占一行，因为是横向条形需要宽度） -->
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
  grid-template-columns: repeat(4, 1fr);
  gap: var(--space-4);
}

.chart-block {
  background: rgba(255,255,255,0.03);
  border: 1px solid rgba(255,255,255,0.05);
  border-radius: var(--radius-card);
  padding: var(--space-5);
}

.chart-grid-2 {
  display: grid;
  grid-template-columns: 1fr 1.2fr;
  gap: var(--space-4);
}

// 响应式：屏幕窄时单列
@media (max-width: 1200px) {
  .kpi-row { grid-template-columns: repeat(2, 1fr); }
  .chart-grid-2 { grid-template-columns: 1fr; }
}
</style>
```

---

### 任务 6：后端 - 检查统计接口字段对齐

Phase 3 已经实现了 5 个统计接口。Codex 实现前端时**逐个对齐字段**，如发现字段缺失或类型不对，**记录下来一次性补**，**不要一边写前端一边改后端**。

预期需要检查/微调的地方（Codex 实际写时验证，可能不需要改）：

- `StatsOverview` 是否包含 `totalTracks`、`totalPlayedSeconds`、`totalPlays`、`totalFavorites`、`totalPlaylists`
- `GenreDistributionItem` 是否含 `genre`（可空）、`count`、`percentage`（可空）
- `TopArtistItem` 是否含 `artistId`、`artistName`、`playCount`、`avatarUrl`（可空）
- `MonthlyTrendItem` 是否含 `month`（格式 `YYYY-MM`）、`totalSeconds`、`playCount`
- `HeatmapItem` 是否含 `date`（格式 `YYYY-MM-DD`）、`count`、`totalSeconds`

如果有不对齐，**统一在 Phase 6 后端补丁里改**（独立 commit），保持前后端一次性对齐。

---

### 任务 7：波形优化 - peaks 接口（顺手做）

#### 7.1 后端：新增 `/api/tracks/{id}/peaks` 接口

在 `TrackController` 加：

```java
@Operation(summary = "获取曲目波形峰值数据（供 wavesurfer 加载）")
@GetMapping("/{id}/peaks")
public Result<TrackPeaksResponse> peaks(
        @PathVariable Long id,
        @RequestParam(defaultValue = "1000") Integer samples
) {
    return Result.success(trackService.getPeaks(id, samples));
}
```

`TrackPeaksResponse` DTO：

```java
@Data
public class TrackPeaksResponse {
    private Integer sampleCount;
    private Integer duration;       // 秒
    private float[] peaks;          // 长度 = sampleCount，每个 [-1, 1]
}
```

Service 实现思路（**讲解时重点说**）：

```java
public TrackPeaksResponse getPeaks(Long id, Integer samples) {
    Track track = getById(id);
    if (track == null) throw new ResourceNotFoundException("Track", id);

    File audioFile = new File(storage.absolutePathOf(track.getFilePath()));
    if (!audioFile.exists()) throw new BusinessException("音频文件丢失");

    // 用 ffmpeg 转成 PCM raw（单声道 + 8kHz 采样足够画波形），
    // 然后等距采样 N 个峰值。
    // 命令：ffmpeg -i input.flac -f f32le -ac 1 -ar 8000 -
    // 输出是 32 位浮点小端字节流，每 4 字节一个 float [-1, 1]

    List<Float> samplesData = readPcmSamples(audioFile);   // 用 ProcessBuilder
    float[] peaks = downsamplePeaks(samplesData, samples);

    TrackPeaksResponse resp = new TrackPeaksResponse();
    resp.setSampleCount(peaks.length);
    resp.setDuration(track.getDurationSeconds());
    resp.setPeaks(peaks);
    return resp;
}

private List<Float> readPcmSamples(File audioFile) throws IOException {
    ProcessBuilder pb = new ProcessBuilder(
        "ffmpeg", "-i", audioFile.getAbsolutePath(),
        "-f", "f32le",         // 32-bit float, little endian
        "-ac", "1",             // 单声道
        "-ar", "8000",          // 8 kHz（画波形足够）
        "-"                     // 输出到 stdout
    );
    pb.redirectErrorStream(false);
    Process proc = pb.start();

    List<Float> result = new ArrayList<>();
    try (DataInputStream in = new DataInputStream(
            new BufferedInputStream(proc.getInputStream()))) {
        byte[] buf = new byte[4];
        while (in.read(buf) == 4) {
            // 小端浮点解码
            int bits = (buf[0] & 0xFF) | ((buf[1] & 0xFF) << 8) |
                       ((buf[2] & 0xFF) << 16) | ((buf[3] & 0xFF) << 24);
            result.add(Float.intBitsToFloat(bits));
        }
    }
    proc.waitFor();
    return result;
}

private float[] downsamplePeaks(List<Float> samples, int targetCount) {
    int bucketSize = samples.size() / targetCount;
    if (bucketSize < 1) bucketSize = 1;
    float[] peaks = new float[targetCount];
    for (int i = 0; i < targetCount; i++) {
        float max = 0;
        int start = i * bucketSize;
        int end = Math.min(start + bucketSize, samples.size());
        for (int j = start; j < end; j++) {
            float abs = Math.abs(samples.get(j));
            if (abs > max) max = abs;
        }
        peaks[i] = max;
    }
    return peaks;
}
```

**缓存优化**：peaks 数据**应该缓存**——同一首歌每次进 NowPlayingView 都重算太浪费。简单做法：

- 第一次计算后把结果序列化（JSON）写到 `storage/peaks/{trackId}.json`
- 后续请求先读文件，没有再算

Codex 实装时**先做无缓存版本**，验证逻辑正确后再加缓存层。

#### 7.2 前端：WaveformDisplay 改用 peaks

```ts
// 之前：
await ws.load(`/api/stream/${currentTrack.value.id}`);

// 改为：
const peaksData = await trackApi.peaks(currentTrack.value.id, 1000);
await ws.load(
    `/api/stream/${currentTrack.value.id}`,  // 仍传 URL，wavesurfer 元数据需要
    [peaksData.peaks],                         // 但 peaks 已经提供，不会下载文件
    peaksData.duration                         // 总时长
);
```

具体 wavesurfer 7.x 的 `load(url, peaks, duration)` 三参数用法看官方文档确认。

**效果**：用户开 NowPlayingView 时，**只下载几 KB 的 peaks JSON，不下载几十 MB 的 FLAC**。波形立刻显示，播放仍走原 `<audio>` 走 stream URL（按 Range 流式）。

---

### 任务 8：联调测试

```powershell
# 终端 1
cd backend; mvn spring-boot:run

# 终端 2
cd frontend; npm run dev
```

**测试场景**：

1. **听歌报告页**：
   - 顶部 4 个 KPI 卡片有数字翻牌动画（从 0 涨到目标值）
   - 4 个图表全部正常渲染，**无白底闪烁**
   - 流派饼图能 hover 显示数据
   - Top 艺术家条形从下往上的渐变紫到青
   - 月度趋势线 + 渐变面积平滑
   - 热力图 365 天方格，按播放次数颜色深浅渐变，**有种子数据的几天能看到紫色块**
2. **响应式**：浏览器宽度缩到 1200px 以下，布局变单列
3. **波形性能**：进入 NowPlayingView 加载一首大 FLAC（你那首 ナチュラル 50MB），**波形应该秒出**，没有"等待几秒下载"的卡顿
4. **状态色板**：转换工坊的 SUCCESS/RUNNING/FAILED 颜色和之前一致（重构后没有视觉退化）
5. **首页未受影响**：Phase 4 的首页、库页、专辑页等没有视觉退化

---

### 任务 9：commit

```powershell
cd D:\Claude_Playground\Soundprint
git add backend/ frontend/
@"
feat: 统计页 + ECharts 图表 + 波形优化（Phase 6）

前端：
- Tailwind/SCSS 补齐状态色板（success/warning/danger/info）作为设计令牌
- 替换 Phase 5 硬编码状态色为 CSS 变量，技术债清零
- echarts-theme.ts：全站统一 ECharts 主题，深色 + 紫青渐变调色板
- BaseChart：echarts 实例管理 + resize 监听 + dispose
- 4 个图表组件：流派饼图、Top 艺术家、月度趋势、365 天热力图
- KpiCard：requestAnimationFrame 翻牌动画
- StatsView：响应式大屏布局
- WaveformDisplay 改用 peaks 数据加载，大 FLAC 不再下载完整文件

后端：
- TrackController 新增 /api/tracks/{id}/peaks
- TrackService.getPeaks：ProcessBuilder 调 ffmpeg PCM f32le 单声道 8kHz
- 等距采样 + 取绝对值峰值
- peaks 缓存到 storage/peaks/{id}.json（文件存在则直接读）

性能：
- 进 NowPlayingView 不再下载完整音频画波形
- 仅传输 ~4KB peaks JSON
"@ | Out-File "D:\Claude_Playground\_msg.txt" -Encoding utf8NoBOM
git commit -F "D:\Claude_Playground\_msg.txt"
Remove-Item "D:\Claude_Playground\_msg.txt"
```

---

## 📚 边写边讲要求（必讲清单）

### 1. **ECharts 主题机制**（必讲）
`registerTheme` + `init(dom, themeName)` 的工作原理。为什么不写到每个图表的 `option` 里。设计系统统一性。

### 2. **echarts `shallowRef` + `dispose`**（必讲）
ECharts 实例内部状态复杂，`ref` 会递归代理拖慢性能。`dispose` 防止内存泄漏。**这两个细节是面试题等级的考点**。

### 3. **饼图 vs 环形图选择**（讲）
为什么我们用环形图（`radius: ['45%', '70%']`）：中心留白可以叠加文字、视觉更现代、与磨砂玻璃风格更搭。

### 4. **GitHub 热力图的实现思路**（必讲）
- ECharts 的 `calendar` 坐标系
- `visualMap.piecewise` 分段着色
- 缺失日期补 0 的必要性（不补会有空格）
- **答辩话术**："365 个数据点，按播放次数映射到 5 个颜色梯度，calendar 坐标系自动按周排列"

### 5. **横向条形图的 grid 配置陷阱**（讲）
`grid.left` 要给类目轴足够空间放艺术家名，否则名字会被切断。`containLabel` 何时用。

### 6. **`requestAnimationFrame` + easing 函数**（讲）
KPI 翻牌动画不用 CSS transition 而用 RAF：CSS 只能动画 CSS 属性，**数字本身的变化要 JS 驱动**。easeOutCubic 给"快速开始慢慢停"的感觉。

### 7. **PCM f32le 字节流解码**（必讲，重点）
peaks 接口涉及二进制流处理：
- FFmpeg 输出 raw PCM 32-bit float little-endian
- Java 用位运算解码 4 字节为 float
- 等距采样到目标数量
- **答辩话术**："不是直接读音频文件解码，而是让 FFmpeg 转码为统一的 PCM 浮点格式，简化前端解码逻辑"

### 8. **波形优化的成本/收益分析**（讲）
- 第一次调 peaks 接口耗时 1-3 秒（要跑 FFmpeg）
- 但后续从文件缓存读，几 ms 返回
- 前端拿到的 JSON 只有几 KB（vs 50MB FLAC）
- **取舍**：第一次稍慢但只一次，之后所有用户进入都飞快

### 9. **ECharts notMerge / lazyUpdate**（讲）
更新 option 时的两个关键参数，为什么 setOption 要这么调。

### 10. **设计令牌闭环**（讲）
Phase 5 的状态色硬编码 → Phase 6 提升到设计令牌 → 未来全站统一引用。**这就是真实工业项目的演进过程**。答辩话术。

---

## ✅ 完成检查清单

- [ ] 设计令牌补充 success/warning/danger/info 状态色
- [ ] 替换 Phase 5 硬编码状态色为 CSS 变量
- [ ] echarts-theme.ts 注册并在 main.ts 调用
- [ ] BaseChart 组件，dispose 在 unmount 触发
- [ ] KpiCard 翻牌动画
- [ ] GenrePieChart 环形饼图
- [ ] TopArtistsBarChart 横向条形 + 渐变
- [ ] MonthlyTrendChart 平滑折线 + 渐变面积
- [ ] PlayHeatmap 365 天热力图
- [ ] StatsView 响应式大屏布局
- [ ] 后端 /api/tracks/{id}/peaks 接口
- [ ] peaks 缓存到 storage/peaks/{id}.json
- [ ] WaveformDisplay 改用 peaks 加载
- [ ] 5 个图表全部正常渲染，无白底闪烁
- [ ] 大 FLAC 进 NowPlayingView 波形秒出
- [ ] 响应式布局生效（1200px 以下单列）
- [ ] 浏览器 Console 无错误
- [ ] commit 完成

---

## 📩 反馈给架构师的内容

1. **听歌报告页全屏截图**（KPI 卡片 + 4 个图表）
2. **热力图近距离截图**（如果数据有亮色块）
3. **波形优化前后对比**：
   - 优化前：在 Network 面板看一首 50MB FLAC 加载时间
   - 优化后：看 peaks 接口返回时间 + 数据大小
4. **任何视觉细节你想调的**（颜色、间距、字号）
5. **是否准备进 Phase 7（vue-bits 视觉爆点）** —— 这是项目颜值的最终冲刺

---

## ⚠️ 注意事项

- **绝对不要给每个图表写一遍 echarts.init**，必须走 BaseChart
- **不要在图表组件内 import 完整 echarts**——按需引入太繁琐，本项目接受全量引入
- **设计令牌严格延续**，不引入新颜色
- **peaks 接口的 ffmpeg 调用必须用 ProcessBuilder 数组参数**，安全考虑
- **peaks 缓存文件存到 storage/peaks/**，目录如不存在要自动创建
- **如果后端聚合接口字段对不上前端类型，整合一次性补 commit**，不要一边写前端一边改后端
- **不要修改 Phase 4/5 已工作的页面布局**，除了替换状态色这种纯重构
- **不要在本阶段集成 vue-bits**

---

**End of Phase 6 Document.**
