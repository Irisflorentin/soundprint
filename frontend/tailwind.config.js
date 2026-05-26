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
        // 银白色板（主色系）
        silver: {
          50:  '#F4F5F7',
          100: '#E5E7EB',
          200: '#CBD5E1',
          300: '#94A3B8',
          400: '#64748B',
          500: '#475569',
        },
        // 香槟金（点缀）
        gold: {
          400: '#D4C5A0',
          500: '#C8A862',
          600: '#A88D4E',
        },
        // 炭黑色板（背景）
        ink: {
          950: '#0A0A0B',
          900: '#15151A',
          800: '#1F1F26',
          700: '#2A2A33',
        },
        // 文字（保留原命名，值更新）
        fg: {
          primary:   '#F4F5F7',
          secondary: '#94A3B8',
          tertiary:  '#64748B',
          disabled:  '#475569',
        },
        // 兼容性保留：旧 brand 类名仍可用，但改为银白体系
        brand: {
          50:  '#F8FAFC',
          100: '#F1F5F9',
          200: '#E2E8F0',
          300: '#CBD5E1',
          400: '#94A3B8',
          500: '#64748B',
          600: '#F4F5F7',
          700: '#E5E7EB',
          800: '#CBD5E1',
          900: '#94A3B8',
        },
        // 兼容 accent 命名：指向香槟金
        accent: {
          400: '#D4C5A0',
          500: '#C8A862',
          600: '#A88D4E',
        },
        // 状态色（哑光化）
        success: {
          400: '#A3D9B1',
          500: '#86C594',
          600: '#6CAB7C',
          bg: 'rgba(163, 217, 177, 0.12)',
        },
        warning: {
          400: '#D4C5A0',
          500: '#C8A862',
          600: '#A88D4E',
          bg: 'rgba(200, 168, 98, 0.12)',
        },
        danger: {
          400: '#E89090',
          500: '#D77676',
          600: '#B85D5D',
          bg: 'rgba(232, 144, 144, 0.12)',
        },
        info: {
          400: '#CBD5E1',
          500: '#94A3B8',
          600: '#64748B',
          bg: 'rgba(148, 163, 184, 0.12)',
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
        'mesh': 'radial-gradient(at 0% 0%, rgba(244, 245, 247, 0.10) 0%, transparent 50%), radial-gradient(at 100% 100%, rgba(200, 168, 98, 0.14) 0%, transparent 50%), #0A0A0B',
        'brand-gradient': 'linear-gradient(135deg, #F4F5F7 0%, #94A3B8 50%, #C8A862 100%)',
        'silver-gradient': 'linear-gradient(135deg, #F4F5F7 0%, #94A3B8 100%)',
      },
      boxShadow: {
        'glass': '0 8px 32px 0 rgba(0, 0, 0, 0.37)',
        'glow': '0 0 24px rgba(244, 245, 247, 0.24)',
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
