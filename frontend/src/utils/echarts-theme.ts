import * as echarts from 'echarts';

export const SOUNDPRINT_THEME_NAME = 'soundprint-dark';

export const SOUNDPRINT_CHART_COLORS = {
  brand: '#7C3AED',
  brandSoft: '#A78BFA',
  accent: '#06B6D4',
  accentSoft: '#22D3EE',
  success: '#22C55E',
  warning: '#F59E0B',
  danger: '#EF4444',
  info: '#3B82F6',
  cardBg: '#0F0F1E',
  primaryText: '#F5F5F7',
  secondaryText: '#A1A1AA',
  tertiaryText: '#71717A',
  borderSubtle: 'rgba(255, 255, 255, 0.06)',
  borderMedium: 'rgba(255, 255, 255, 0.12)',
  brandAreaStart: 'rgba(124, 58, 237, 0.28)',
  brandAreaEnd: 'rgba(124, 58, 237, 0)',
  heatmapEmpty: 'rgba(255, 255, 255, 0.05)',
  heatmapLow: 'rgba(124, 58, 237, 0.35)',
  heatmapMid: 'rgba(6, 182, 212, 0.72)',
} as const;

const soundprintTheme = {
  darkMode: true,
  color: [
    SOUNDPRINT_CHART_COLORS.brand,
    SOUNDPRINT_CHART_COLORS.accent,
    SOUNDPRINT_CHART_COLORS.success,
    SOUNDPRINT_CHART_COLORS.warning,
    SOUNDPRINT_CHART_COLORS.danger,
    SOUNDPRINT_CHART_COLORS.info,
    SOUNDPRINT_CHART_COLORS.brandSoft,
    SOUNDPRINT_CHART_COLORS.accentSoft,
  ],
  backgroundColor: 'transparent',
  textStyle: {
    color: SOUNDPRINT_CHART_COLORS.secondaryText,
    fontFamily: 'Inter, PingFang SC, Noto Sans SC, system-ui, sans-serif',
  },
  title: {
    textStyle: {
      color: SOUNDPRINT_CHART_COLORS.primaryText,
      fontWeight: 600,
    },
    subtextStyle: {
      color: SOUNDPRINT_CHART_COLORS.tertiaryText,
    },
  },
  legend: {
    textStyle: {
      color: SOUNDPRINT_CHART_COLORS.secondaryText,
    },
  },
  tooltip: {
    backgroundColor: SOUNDPRINT_CHART_COLORS.cardBg,
    borderColor: 'rgba(255, 255, 255, 0.08)',
    textStyle: {
      color: SOUNDPRINT_CHART_COLORS.primaryText,
    },
    extraCssText: 'box-shadow: 0 12px 28px rgba(0, 0, 0, 0.35); border-radius: 12px;',
  },
  grid: {
    borderColor: SOUNDPRINT_CHART_COLORS.borderSubtle,
  },
  categoryAxis: {
    axisLine: {
      lineStyle: {
        color: SOUNDPRINT_CHART_COLORS.borderMedium,
      },
    },
    axisTick: {
      lineStyle: {
        color: SOUNDPRINT_CHART_COLORS.borderMedium,
      },
    },
    axisLabel: {
      color: SOUNDPRINT_CHART_COLORS.secondaryText,
    },
    splitLine: {
      lineStyle: {
        color: SOUNDPRINT_CHART_COLORS.borderSubtle,
      },
    },
  },
  valueAxis: {
    axisLine: {
      lineStyle: {
        color: SOUNDPRINT_CHART_COLORS.borderMedium,
      },
    },
    axisTick: {
      lineStyle: {
        color: SOUNDPRINT_CHART_COLORS.borderMedium,
      },
    },
    axisLabel: {
      color: SOUNDPRINT_CHART_COLORS.secondaryText,
    },
    splitLine: {
      lineStyle: {
        color: SOUNDPRINT_CHART_COLORS.borderSubtle,
      },
    },
  },
};

let registered = false;

export function registerSoundprintTheme() {
  if (registered) return;
  echarts.registerTheme(SOUNDPRINT_THEME_NAME, soundprintTheme);
  registered = true;
}
