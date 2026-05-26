import * as echarts from 'echarts';

export const SOUNDPRINT_THEME_NAME = 'soundprint-dark';

export const SOUNDPRINT_CHART_COLORS = {
  brand: '#F4F5F7',
  brandSoft: '#CBD5E1',
  accent: '#C8A862',
  accentSoft: '#D4C5A0',
  success: '#A3D9B1',
  warning: '#C8A862',
  danger: '#E89090',
  info: '#94A3B8',
  cardBg: '#15151A',
  primaryText: '#F4F5F7',
  secondaryText: '#94A3B8',
  tertiaryText: '#64748B',
  borderSubtle: 'rgba(255, 255, 255, 0.04)',
  borderMedium: 'rgba(255, 255, 255, 0.10)',
  brandAreaStart: 'rgba(244, 245, 247, 0.25)',
  brandAreaEnd: 'rgba(244, 245, 247, 0)',
  heatmapEmpty: 'rgba(255, 255, 255, 0.03)',
  heatmapLow: '#2A2A33',
  heatmapMid: '#64748B',
  heatmapHigh: '#94A3B8',
} as const;

const soundprintTheme = {
  darkMode: true,
  color: [
    '#F4F5F7',
    '#94A3B8',
    '#CBD5E1',
    '#64748B',
    '#C8A862',
  ],
  backgroundColor: 'transparent',
  textStyle: {
    fontFamily: 'Inter, "PingFang SC", "Noto Sans SC", system-ui, sans-serif',
    color: '#94A3B8',
  },
  title: {
    textStyle: {
      color: '#F4F5F7',
      fontWeight: 600,
      fontSize: 16,
    },
    subtextStyle: {
      color: '#64748B',
      fontSize: 12,
    },
  },
  categoryAxis: {
    axisLine:  { lineStyle: { color: 'rgba(255,255,255,0.1)' } },
    axisTick:  { lineStyle: { color: 'rgba(255,255,255,0.1)' } },
    axisLabel: { color: '#94A3B8', fontSize: 11 },
    splitLine: { lineStyle: { color: 'rgba(255,255,255,0.04)' } },
  },
  valueAxis: {
    axisLine:  { lineStyle: { color: 'rgba(255,255,255,0.1)' } },
    axisTick:  { lineStyle: { color: 'rgba(255,255,255,0.1)' } },
    axisLabel: { color: '#94A3B8', fontSize: 11 },
    splitLine: { lineStyle: { color: 'rgba(255,255,255,0.04)' } },
  },
  tooltip: {
    backgroundColor: 'rgba(21, 21, 26, 0.95)',
    borderColor: 'rgba(200, 168, 98, 0.4)',
    borderWidth: 1,
    textStyle: { color: '#F4F5F7', fontSize: 12 },
    extraCssText: 'backdrop-filter: blur(20px); box-shadow: 0 8px 32px rgba(0,0,0,0.5); border-radius: 12px;',
  },
  legend: {
    textStyle: { color: '#94A3B8', fontSize: 12 },
    icon: 'circle',
    itemGap: 16,
  },
  pie: {
    itemStyle: {
      borderColor: '#0A0A0B',
      borderWidth: 2,
    },
    label: {
      color: '#F4F5F7',
    },
  },
  bar: {
    itemStyle: {
      borderRadius: [4, 4, 0, 0],
    },
  },
  line: {
    smooth: true,
    symbolSize: 6,
    lineStyle: { width: 2 },
    areaStyle: { opacity: 0.15 },
  },
  heatmap: {
    itemStyle: {
      borderColor: 'transparent',
      borderWidth: 1,
    },
  },
  visualMap: {
    textStyle: { color: '#94A3B8' },
    inRange: {
      color: ['#15151A', '#2A2A33', '#64748B', '#94A3B8', '#C8A862'],
    },
  },
};

let registered = false;

export function registerSoundprintTheme() {
  if (registered) return;
  echarts.registerTheme(SOUNDPRINT_THEME_NAME, soundprintTheme);
  registered = true;
}
