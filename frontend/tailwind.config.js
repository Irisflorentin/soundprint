/** @type {import('tailwindcss').Config} */
export default {
  content: [
    "./index.html",
    "./src/**/*.{vue,js,ts,jsx,tsx}",
  ],
  // 重要：避免 Tailwind 和 Element-Plus 类名冲突
  important: false,
  corePlugins: {
    preflight: false,  // 关闭 Tailwind 的 CSS reset，避免覆盖 Element-Plus
  },
  theme: {
    extend: {
      colors: {
        // 主品牌色：紫色
        brand: {
          50:  '#F5F3FF',
          100: '#EDE9FE',
          200: '#DDD6FE',
          300: '#C4B5FD',
          400: '#A78BFA',
          500: '#8B5CF6',
          600: '#7C3AED',  // 主色
          700: '#6D28D9',
          800: '#5B21B6',
          900: '#4C1D95',
        },
        // 强调色：青色
        accent: {
          400: '#22D3EE',
          500: '#06B6D4',
          600: '#0891B2',
        },
        // 状态色（success/warning/danger/info）
        success: {
          400: '#4ADE80',
          500: '#22C55E',
          600: '#16A34A',
          bg: 'rgba(34, 197, 94, 0.15)',
        },
        warning: {
          400: '#FBBF24',
          500: '#F59E0B',
          600: '#D97706',
          bg: 'rgba(245, 158, 11, 0.15)',
        },
        danger: {
          400: '#F87171',
          500: '#EF4444',
          600: '#DC2626',
          bg: 'rgba(239, 68, 68, 0.15)',
        },
        info: {
          400: '#60A5FA',
          500: '#3B82F6',
          600: '#2563EB',
          bg: 'rgba(59, 130, 246, 0.15)',
        },
        // 深色背景体系
        ink: {
          950: '#0A0A14',  // 最深，body
          900: '#0F0F1E',  // 卡片基底
          800: '#1A1530',  // 卡片悬浮态
          700: '#252040',  // 边框
        },
        // 文字
        fg: {
          primary:   '#F5F5F7',
          secondary: '#A1A1AA',
          tertiary:  '#71717A',
          disabled:  '#52525B',
        },
      },
      borderRadius: {
        'card': '16px',
        'btn': '12px',
        'pill': '999px',
      },
      backdropBlur: {
        'glass': '20px',
      },
      backgroundImage: {
        // 渐变背景（登录页、Hero 区用）
        'mesh': 'radial-gradient(at 0% 0%, #4C1D95 0%, transparent 50%), radial-gradient(at 100% 100%, #1E40AF 0%, transparent 50%), #0A0A14',
        'brand-gradient': 'linear-gradient(135deg, #7C3AED 0%, #06B6D4 100%)',
      },
      boxShadow: {
        'glass': '0 8px 32px 0 rgba(0, 0, 0, 0.37)',
        'glow': '0 0 24px rgba(124, 58, 237, 0.4)',
      },
      transitionTimingFunction: {
        'soundprint': 'cubic-bezier(0.4, 0, 0.2, 1)',
      },
      fontFamily: {
        sans: ['Inter', 'PingFang SC', 'Noto Sans SC', 'system-ui', 'sans-serif'],
        mono: ['SF Mono', 'JetBrains Mono', 'Cascadia Code', 'Consolas', 'monospace'],
      },
    },
  },
  plugins: [],
};
