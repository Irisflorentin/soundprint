# Phase 4：前端骨架与第一个能看的页面

> Soundprint 项目的第五个阶段文档。
> 把整份文档完整复制粘贴给 Claude Code，作为它的任务指令。
> 本阶段产出后，浏览器打开 http://localhost:5173 能看到 Soundprint 真正的样子——
> 深色磨砂玻璃风、紫色主题、能浏览你上传的音乐。

---

## 🎯 阶段目标

把项目从"只有后端 + Knife4j 调试界面"扩展为"有真正前端可看"：

1. **Vite 5 + Vue 3 + TypeScript 工程初始化**
2. **Tailwind CSS 3.x + Element-Plus 2.x 共存配置**
3. **设计令牌系统**（深色 + 紫色 + 磨砂玻璃，所有页面共用）
4. **Pinia 状态管理 + Vue Router + Axios** 配置
5. **全局 Layout**：左侧栏 + 顶栏 + 主内容区 + 全局播放器底栏（占位，Phase 5 实装）
6. **登录页**（简洁版，无 Galaxy 背景——那是 Phase 7 的事）
7. **首页（Dashboard）**：调通 `/api/dashboard` 接口，展示问候语和几个横向滚动行
8. **音乐库页**：调通 `/api/tracks`，分页表格 + 搜索 + 格式筛选
9. **专辑列表 + 艺术家列表**（网格卡片布局）
10. **API 通用封装**：axios 拦截器 + 错误处理 + 类型化请求
11. **顺手修掉后端 Knife4j 的 `x-www-form-urlencoded` 小瑕疵**

**重要约束**：
- 本阶段**不集成 vue-bits**（那是 Phase 7）
- 本阶段**不实现播放器内部逻辑**（那是 Phase 5）
- 本阶段**不实现转换工坊和统计页**（统计是 Phase 6）
- 本阶段聚焦"骨架 + 设计语言 + 3-4 个能看的业务页"

---

## 📋 任务清单

### 任务 0：环境核验

```powershell
cd D:\Claude_Playground\Soundprint
node -v       # 应为 v18+
npm -v        # 应为 9+
```

后端**不要启动**，Phase 4 前端开发主要不需要后端在线（除了任务 7-9 联调时）。

### 任务 1：用 Vite 创建 Vue 3 + TypeScript 工程

在 `D:\Claude_Playground\Soundprint\` 下执行：

```powershell
cd D:\Claude_Playground\Soundprint
npm create vite@latest frontend -- --template vue-ts
```

如果交互式提示，**接受默认**继续。完成后：

```powershell
cd frontend
npm install
```

第一次 `npm install` 会比较慢（300+ 包），耐心等。完成后验证：

```powershell
npm run dev
```

应该看到 Vite 启动信息和 `Local: http://localhost:5173/`。浏览器打开能看到默认 Vue Logo 页面 → **立刻 Ctrl+C 停掉**，我们要重构这个项目。

### 任务 2：安装核心依赖

```powershell
cd D:\Claude_Playground\Soundprint\frontend

# 业务依赖
npm install vue-router@4 pinia axios element-plus

# 样式 / 设计系统
npm install -D tailwindcss@3 postcss autoprefixer sass

# 工具库
npm install dayjs lodash-es
npm install -D @types/lodash-es

# 图标
npm install @element-plus/icons-vue
```

**注意 Tailwind 锁定 3.x**（不要用 4.x，4.x 是全新架构，vue-bits 还没完全适配）。

初始化 Tailwind：

```powershell
npx tailwindcss init -p
```

会生成 `tailwind.config.js` 和 `postcss.config.js`。

### 任务 3：项目目录结构改造

把 Vite 默认生成的 `src/` 改造成：

```
frontend/src/
├── main.ts                          ← 入口，挂载 Element-Plus、Pinia、Router、全局样式
├── App.vue                          ← 根组件
├── vite-env.d.ts                    ← 保留
│
├── router/
│   ├── index.ts                     ← 路由配置
│   └── guards.ts                    ← 路由守卫（登录检查）
│
├── stores/
│   ├── index.ts                     ← Pinia 实例
│   ├── user.ts                      ← 当前用户 store
│   ├── player.ts                    ← 播放器状态 store（Phase 4 留接口，Phase 5 实装）
│   └── library.ts                   ← 音乐库筛选状态
│
├── api/
│   ├── client.ts                    ← Axios 实例 + 拦截器
│   ├── types.ts                     ← Result、PageResult 通用类型
│   ├── track.ts                     ← /api/tracks/* 调用封装
│   ├── album.ts
│   ├── artist.ts
│   ├── playlist.ts
│   ├── tag.ts
│   ├── favorite.ts
│   ├── playHistory.ts
│   ├── dashboard.ts
│   ├── stats.ts
│   ├── conversion.ts
│   └── stream.ts                    ← 流式播放 URL 拼接
│
├── types/                           ← 业务类型定义
│   ├── track.ts
│   ├── album.ts
│   ├── artist.ts
│   ├── playlist.ts
│   ├── tag.ts
│   ├── user.ts
│   ├── stats.ts
│   └── common.ts
│
├── views/                           ← 页面级组件（路由对应）
│   ├── auth/
│   │   └── LoginView.vue
│   ├── dashboard/
│   │   └── DashboardView.vue        ← 首页
│   ├── library/
│   │   └── LibraryView.vue          ← 音乐库
│   ├── albums/
│   │   ├── AlbumListView.vue
│   │   └── AlbumDetailView.vue
│   ├── artists/
│   │   ├── ArtistListView.vue
│   │   └── ArtistDetailView.vue
│   ├── playlists/
│   │   ├── PlaylistListView.vue
│   │   └── PlaylistDetailView.vue
│   ├── studio/
│   │   └── StudioView.vue           ← 转换工坊（Phase 4 占位，Phase 5 实装）
│   ├── stats/
│   │   └── StatsView.vue            ← 统计（Phase 4 占位，Phase 6 实装）
│   └── settings/
│       └── SettingsView.vue
│
├── components/                      ← 可复用业务组件
│   ├── layout/
│   │   ├── AppLayout.vue            ← 主框架（侧栏+顶栏+内容+底栏）
│   │   ├── SidebarNav.vue           ← 左侧导航
│   │   ├── TopBar.vue               ← 顶部
│   │   └── PlayerBar.vue            ← 底部播放器（Phase 4 仅占位 UI）
│   ├── common/
│   │   ├── GlassCard.vue            ← 磨砂玻璃卡片（设计系统核心组件）
│   │   ├── EmptyState.vue           ← 空状态提示
│   │   ├── PageHeader.vue           ← 页面标题区
│   │   ├── LoadingBlock.vue         ← 加载占位
│   │   └── SoundprintLogo.vue       ← Logo SVG
│   ├── track/
│   │   ├── TrackList.vue            ← 曲目列表（用在多个地方）
│   │   ├── TrackRow.vue             ← 单行曲目
│   │   └── TrackCover.vue           ← 封面（带 hover 播放按钮）
│   ├── album/
│   │   ├── AlbumGrid.vue
│   │   └── AlbumCard.vue
│   ├── artist/
│   │   ├── ArtistGrid.vue
│   │   └── ArtistCard.vue
│   └── shelf/
│       └── HorizontalShelf.vue      ← 横向滚动行（首页用，类似 Apple Music）
│
├── composables/                     ← 组合式函数（Vue 3 推荐模式）
│   ├── useApi.ts                    ← 通用 API 调用封装
│   ├── usePagination.ts             ← 分页状态
│   └── useFormat.ts                 ← 时长、文件大小、日期格式化
│
├── styles/
│   ├── index.scss                   ← 入口样式，引入 Tailwind 和变量
│   ├── tokens.scss                  ← 设计令牌（CSS 变量定义）
│   └── element-overrides.scss       ← Element-Plus 主题覆盖
│
└── assets/                          ← 静态资源
    └── images/
```

删除 Vite 默认生成的 `src/components/HelloWorld.vue`、`src/assets/vue.svg`、`src/style.css`，但**保留 `App.vue` 和 `main.ts`**（我们要重构它们的内容）。

### 任务 4：配置文件改造

#### 4.1 `vite.config.ts`

```ts
import { defineConfig } from 'vite';
import vue from '@vitejs/plugin-vue';
import { resolve } from 'path';

export default defineConfig({
  plugins: [vue()],
  resolve: {
    alias: {
      '@': resolve(__dirname, 'src'),
    },
  },
  server: {
    port: 5173,
    open: false,
    proxy: {
      // 把所有 /api 请求代理到后端 8080，解决开发期跨域
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true,
      },
    },
  },
});
```

#### 4.2 `tsconfig.json`

确认包含路径别名（在 Vite 生成的基础上补 `@` 别名）：

```json
{
  "compilerOptions": {
    "target": "ES2020",
    "module": "ESNext",
    "moduleResolution": "bundler",
    "strict": true,
    "jsx": "preserve",
    "esModuleInterop": true,
    "skipLibCheck": true,
    "isolatedModules": true,
    "useDefineForClassFields": true,
    "lib": ["ES2020", "DOM", "DOM.Iterable"],
    "baseUrl": ".",
    "paths": {
      "@/*": ["src/*"]
    },
    "types": ["vite/client"]
  },
  "include": ["src/**/*", "src/**/*.vue"],
  "exclude": ["node_modules", "dist"]
}
```

#### 4.3 `tailwind.config.js`（这是设计语言的核心）

```js
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
```

#### 4.4 `postcss.config.js`

```js
export default {
  plugins: {
    tailwindcss: {},
    autoprefixer: {},
  },
};
```

#### 4.5 `src/styles/tokens.scss`（CSS 变量，业务组件用）

```scss
:root {
  // 颜色变量（与 Tailwind 同步）
  --color-bg-base: #0A0A14;
  --color-bg-card: #0F0F1E;
  --color-bg-hover: #1A1530;
  --color-border: #25204060;

  --color-brand: #7C3AED;
  --color-brand-hover: #8B5CF6;
  --color-accent: #06B6D4;

  --color-fg-primary: #F5F5F7;
  --color-fg-secondary: #A1A1AA;
  --color-fg-tertiary: #71717A;

  // 间距系统（8 的倍数）
  --space-1: 4px;
  --space-2: 8px;
  --space-3: 12px;
  --space-4: 16px;
  --space-5: 24px;
  --space-6: 32px;
  --space-8: 48px;
  --space-10: 64px;

  // 圆角
  --radius-card: 16px;
  --radius-btn: 12px;
  --radius-pill: 999px;

  // 阴影
  --shadow-glass: 0 8px 32px 0 rgba(0, 0, 0, 0.37);
  --shadow-glow: 0 0 24px rgba(124, 58, 237, 0.4);

  // 动效
  --ease: cubic-bezier(0.4, 0, 0.2, 1);
  --duration-base: 300ms;
}
```

#### 4.6 `src/styles/element-overrides.scss`（Element-Plus 主题对接）

```scss
// Element-Plus 主题色覆盖，使其和 Tailwind brand 色一致
:root {
  --el-color-primary: #7C3AED;
  --el-color-primary-light-3: #8B5CF6;
  --el-color-primary-light-5: #A78BFA;
  --el-color-primary-light-7: #C4B5FD;
  --el-color-primary-light-9: #EDE9FE;
  --el-color-primary-dark-2: #6D28D9;

  --el-bg-color: #0F0F1E;
  --el-bg-color-page: #0A0A14;
  --el-bg-color-overlay: #1A1530;
  --el-text-color-primary: #F5F5F7;
  --el-text-color-regular: #A1A1AA;
  --el-text-color-secondary: #71717A;
  --el-border-color: #252040;
  --el-border-color-light: #1A1530;
}

html.dark {
  // Element-Plus 暗色主题变量
  color-scheme: dark;
}

// 表格暗色主题微调
.el-table {
  --el-table-bg-color: transparent;
  --el-table-tr-bg-color: transparent;
  --el-table-header-bg-color: rgba(255, 255, 255, 0.02);
  --el-table-row-hover-bg-color: rgba(124, 58, 237, 0.08);
  --el-table-border-color: rgba(255, 255, 255, 0.05);
}

// 分页器暗色
.el-pagination {
  --el-pagination-bg-color: transparent;
  --el-pagination-button-disabled-bg-color: transparent;
}
```

#### 4.7 `src/styles/index.scss`（入口）

```scss
@import 'element-plus/dist/index.css';
@import 'element-plus/theme-chalk/dark/css-vars.css';

@tailwind base;
@tailwind components;
@tailwind utilities;

@import './tokens.scss';
@import './element-overrides.scss';

// 全局基础样式
html, body, #app {
  height: 100%;
  margin: 0;
  padding: 0;
  background: var(--color-bg-base);
  color: var(--color-fg-primary);
  font-family: 'Inter', 'PingFang SC', 'Noto Sans SC', system-ui, sans-serif;
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
}

* {
  box-sizing: border-box;
}

// 自定义滚动条
::-webkit-scrollbar {
  width: 8px;
  height: 8px;
}
::-webkit-scrollbar-track {
  background: transparent;
}
::-webkit-scrollbar-thumb {
  background: rgba(255, 255, 255, 0.1);
  border-radius: 4px;
}
::-webkit-scrollbar-thumb:hover {
  background: rgba(255, 255, 255, 0.2);
}

// 全局动效类
.fade-enter-active, .fade-leave-active {
  transition: opacity 0.3s var(--ease);
}
.fade-enter-from, .fade-leave-to {
  opacity: 0;
}
```

### 任务 5：基础设施代码

#### 5.1 `src/api/types.ts`

```ts
export interface Result<T> {
  code: number;
  message: string;
  data: T;
  timestamp: number;
}

export interface PageResult<T> {
  records: T[];
  total: number;
  current: number;
  size: number;
  pages: number;
}
```

#### 5.2 `src/api/client.ts`

```ts
import axios, { AxiosInstance, AxiosResponse, AxiosError } from 'axios';
import { ElMessage } from 'element-plus';
import type { Result } from './types';

const client: AxiosInstance = axios.create({
  baseURL: '/api',           // 通过 Vite 代理转发到 8080
  timeout: 30000,
  headers: { 'Content-Type': 'application/json' },
});

// 请求拦截器
client.interceptors.request.use(
  (config) => {
    // Phase 4 之后加 JWT，目前不动
    return config;
  },
  (error) => Promise.reject(error)
);

// 响应拦截器：剥壳 + 错误提示
client.interceptors.response.use(
  (response: AxiosResponse<Result<unknown>>) => {
    // 文件下载等二进制响应直接返回原始 response
    if (response.config.responseType === 'blob') {
      return response as any;
    }
    const result = response.data;
    if (result.code === 200) {
      return result.data as any;   // 直接返回 data，调用方不用每次 .data.data
    }
    ElMessage.error(result.message || '请求失败');
    return Promise.reject(new Error(result.message));
  },
  (error: AxiosError<Result<unknown>>) => {
    const msg = error.response?.data?.message || error.message || '网络错误';
    ElMessage.error(msg);
    return Promise.reject(error);
  }
);

export default client;
```

#### 5.3 `src/types/track.ts`（其他类型类似）

```ts
export interface Track {
  id: number;
  title: string;
  artistId: number | null;
  artistName: string | null;
  albumId: number | null;
  albumTitle: string | null;
  albumCoverUrl: string | null;
  coverUrl: string | null;
  format: string;
  duration: number;        // 秒
  bitrate: number;
  sampleRate: number;
  fileSizeBytes: number;
  createdAt: string;
}

export interface TrackDetail extends Track {
  lyrics: string | null;
  tags: { id: number; name: string; color: string }[];
  isFavorited: boolean;
}

export interface TrackQuery {
  page?: number;
  size?: number;
  keyword?: string;
  artistId?: number;
  albumId?: number;
  format?: string;
}
```

#### 5.4 `src/api/track.ts`

```ts
import client from './client';
import type { PageResult } from './types';
import type { Track, TrackDetail, TrackQuery } from '@/types/track';

export const trackApi = {
  page: (query: TrackQuery): Promise<PageResult<Track>> =>
    client.get('/tracks', { params: query }),

  detail: (id: number): Promise<TrackDetail> =>
    client.get(`/tracks/${id}`),

  search: (keyword: string, page = 1, size = 20): Promise<PageResult<Track>> =>
    client.get('/tracks/search', { params: { keyword, page, size } }),

  upload: (file: File, onProgress?: (percent: number) => void): Promise<Track> => {
    const formData = new FormData();
    formData.append('file', file);
    return client.post('/tracks/upload', formData, {
      headers: { 'Content-Type': 'multipart/form-data' },
      onUploadProgress: (e) => {
        if (e.total && onProgress) onProgress(Math.round((e.loaded / e.total) * 100));
      },
    });
  },

  update: (id: number, body: Partial<Track>): Promise<Track> =>
    client.put(`/tracks/${id}`, body),

  remove: (id: number): Promise<void> =>
    client.delete(`/tracks/${id}`),

  getLyrics: (id: number): Promise<string> =>
    client.get(`/tracks/${id}/lyrics`),

  updateLyrics: (id: number, lyrics: string): Promise<void> =>
    client.put(`/tracks/${id}/lyrics`, lyrics, {
      headers: { 'Content-Type': 'text/plain' },
    }),
};

export const streamUrl = (id: number) => `/api/stream/${id}`;
```

类似为其他模块（album、artist、playlist、tag、favorite、playHistory、dashboard、stats、conversion）写 `api/*.ts` 和 `types/*.ts`。**Phase 4 全部写齐**，因为后端这些接口都做好了。

#### 5.5 `src/stores/user.ts`

```ts
import { defineStore } from 'pinia';
import { ref } from 'vue';

interface User {
  id: number;
  username: string;
  nickname: string;
  avatarUrl: string | null;
}

export const useUserStore = defineStore('user', () => {
  // Phase 4 暂时硬编码默认用户，等 Phase 4 末尾加登录后从后端取
  const currentUser = ref<User>({
    id: 1,
    username: 'admin',
    nickname: 'Kaidi',
    avatarUrl: null,
  });

  const isLoggedIn = ref(true);   // Phase 4 默认已登录，登录页只走形式

  function login(username: string, password: string) {
    // 占位实现：任何账号密码都成功
    isLoggedIn.value = true;
  }

  function logout() {
    isLoggedIn.value = false;
  }

  return { currentUser, isLoggedIn, login, logout };
});
```

#### 5.6 `src/stores/player.ts`（Phase 4 仅占位）

```ts
import { defineStore } from 'pinia';
import { ref } from 'vue';
import type { Track } from '@/types/track';

export const usePlayerStore = defineStore('player', () => {
  const currentTrack = ref<Track | null>(null);
  const isPlaying = ref(false);
  const currentTime = ref(0);
  const duration = ref(0);
  const volume = ref(0.8);
  const queue = ref<Track[]>([]);
  const queueIndex = ref(-1);

  // Phase 5 真正实装播放
  function play(track: Track) {
    currentTrack.value = track;
    isPlaying.value = true;
  }

  function pause() {
    isPlaying.value = false;
  }

  function resume() {
    isPlaying.value = true;
  }

  return {
    currentTrack, isPlaying, currentTime, duration, volume, queue, queueIndex,
    play, pause, resume,
  };
});
```

### 任务 6：核心 UI 组件实现

#### 6.1 `src/components/common/GlassCard.vue`（设计系统核心组件）

这是整个项目用得最多的容器组件。**所有的"卡片"都走这个，保证视觉一致**。

```vue
<script setup lang="ts">
withDefaults(defineProps<{
  hoverable?: boolean;
  padding?: 'sm' | 'md' | 'lg' | 'none';
}>(), {
  hoverable: false,
  padding: 'md',
});
</script>

<template>
  <div
    class="glass-card"
    :class="[
      `padding-${padding}`,
      hoverable && 'hoverable',
    ]"
  >
    <slot />
  </div>
</template>

<style lang="scss" scoped>
.glass-card {
  background: rgba(255, 255, 255, 0.03);
  border: 1px solid rgba(255, 255, 255, 0.06);
  border-radius: var(--radius-card);
  backdrop-filter: blur(20px);
  -webkit-backdrop-filter: blur(20px);
  transition: all var(--duration-base) var(--ease);

  &.padding-sm  { padding: var(--space-3); }
  &.padding-md  { padding: var(--space-5); }
  &.padding-lg  { padding: var(--space-6); }
  &.padding-none { padding: 0; }

  &.hoverable {
    cursor: pointer;
    &:hover {
      background: rgba(255, 255, 255, 0.05);
      border-color: rgba(124, 58, 237, 0.3);
      transform: translateY(-2px);
      box-shadow: var(--shadow-glass);
    }
  }
}
</style>
```

#### 6.2 `src/components/layout/AppLayout.vue`（主框架）

```vue
<script setup lang="ts">
import { computed } from 'vue';
import { useRoute } from 'vue-router';
import SidebarNav from './SidebarNav.vue';
import TopBar from './TopBar.vue';
import PlayerBar from './PlayerBar.vue';

const route = useRoute();
const hideChrome = computed(() => route.meta.hideChrome === true);
</script>

<template>
  <div v-if="hideChrome" class="full-page">
    <router-view />
  </div>

  <div v-else class="app-layout">
    <SidebarNav class="sidebar" />
    <div class="main-area">
      <TopBar class="top-bar" />
      <main class="content">
        <router-view v-slot="{ Component }">
          <transition name="fade" mode="out-in">
            <component :is="Component" />
          </transition>
        </router-view>
      </main>
    </div>
    <PlayerBar class="player-bar" />
  </div>
</template>

<style lang="scss" scoped>
.app-layout {
  display: grid;
  grid-template-columns: 240px 1fr;
  grid-template-rows: 1fr 88px;
  grid-template-areas:
    "sidebar main"
    "player  player";
  height: 100vh;
  background: var(--color-bg-base);
}

.sidebar    { grid-area: sidebar; border-right: 1px solid rgba(255,255,255,0.05); }
.main-area  { grid-area: main; display: flex; flex-direction: column; overflow: hidden; }
.player-bar { grid-area: player; border-top: 1px solid rgba(255,255,255,0.05); }

.top-bar    { flex-shrink: 0; height: 64px; }
.content    { flex: 1; overflow-y: auto; padding: var(--space-6); }

.full-page  { height: 100vh; background: var(--color-bg-base); }
</style>
```

#### 6.3 `src/components/layout/SidebarNav.vue`

```vue
<script setup lang="ts">
import { useRoute, useRouter } from 'vue-router';
import {
  House, Headset, Disc, User as UserIcon, Files, Setting, DataAnalysis, MagicStick,
} from '@element-plus/icons-vue';
import SoundprintLogo from '@/components/common/SoundprintLogo.vue';

const route = useRoute();
const router = useRouter();

const navItems = [
  { path: '/',          label: '首页',    icon: House },
  { path: '/library',   label: '音乐库',  icon: Headset },
  { path: '/albums',    label: '专辑',    icon: Disc },
  { path: '/artists',   label: '艺术家',  icon: UserIcon },
  { path: '/playlists', label: '歌单',    icon: Files },
  { path: '/studio',    label: '转换工坊', icon: MagicStick },
  { path: '/stats',     label: '听歌报告', icon: DataAnalysis },
];

const settingsItem = { path: '/settings', label: '设置', icon: Setting };

function isActive(path: string) {
  if (path === '/') return route.path === '/';
  return route.path.startsWith(path);
}
</script>

<template>
  <aside class="sidebar-nav">
    <div class="brand" @click="router.push('/')">
      <SoundprintLogo class="logo" />
      <span class="brand-name">Soundprint</span>
    </div>

    <nav class="nav-list">
      <button
        v-for="item in navItems"
        :key="item.path"
        class="nav-item"
        :class="{ active: isActive(item.path) }"
        @click="router.push(item.path)"
      >
        <el-icon :size="20"><component :is="item.icon" /></el-icon>
        <span>{{ item.label }}</span>
      </button>
    </nav>

    <div class="bottom">
      <button
        class="nav-item"
        :class="{ active: isActive(settingsItem.path) }"
        @click="router.push(settingsItem.path)"
      >
        <el-icon :size="20"><Setting /></el-icon>
        <span>设置</span>
      </button>
    </div>
  </aside>
</template>

<style lang="scss" scoped>
.sidebar-nav {
  display: flex;
  flex-direction: column;
  padding: var(--space-5) var(--space-3);
  height: 100%;
  background: rgba(0, 0, 0, 0.2);
  backdrop-filter: blur(20px);
}

.brand {
  display: flex; align-items: center; gap: var(--space-3);
  padding: var(--space-2) var(--space-3);
  margin-bottom: var(--space-6);
  cursor: pointer;
  .logo { width: 28px; height: 28px; }
  .brand-name {
    font-size: 18px; font-weight: 700;
    background: var(--brand-gradient, linear-gradient(135deg, #7C3AED 0%, #06B6D4 100%));
    -webkit-background-clip: text;
    background-clip: text;
    color: transparent;
  }
}

.nav-list { flex: 1; display: flex; flex-direction: column; gap: 4px; }
.bottom   { padding-top: var(--space-4); border-top: 1px solid rgba(255,255,255,0.05); }

.nav-item {
  display: flex; align-items: center; gap: var(--space-3);
  width: 100%;
  padding: 10px var(--space-3);
  background: transparent;
  border: none;
  border-radius: var(--radius-btn);
  color: var(--color-fg-secondary);
  font-size: 14px;
  text-align: left;
  cursor: pointer;
  transition: all 200ms var(--ease);

  &:hover {
    background: rgba(255,255,255,0.05);
    color: var(--color-fg-primary);
  }

  &.active {
    background: rgba(124, 58, 237, 0.15);
    color: var(--color-brand);
    font-weight: 600;
  }
}
</style>
```

#### 6.4 `src/components/layout/TopBar.vue`

```vue
<script setup lang="ts">
import { ref } from 'vue';
import { useRouter } from 'vue-router';
import { Search, Upload, Bell } from '@element-plus/icons-vue';
import { useUserStore } from '@/stores/user';

const router = useRouter();
const userStore = useUserStore();
const searchKeyword = ref('');

function handleSearch() {
  if (searchKeyword.value.trim()) {
    router.push({ path: '/library', query: { keyword: searchKeyword.value.trim() } });
  }
}

function goUpload() {
  router.push('/library?upload=true');
}
</script>

<template>
  <header class="top-bar">
    <div class="search">
      <el-input
        v-model="searchKeyword"
        placeholder="搜索曲目、艺术家、专辑..."
        :prefix-icon="Search"
        clearable
        @keyup.enter="handleSearch"
        size="large"
      />
    </div>

    <div class="actions">
      <el-button type="primary" :icon="Upload" @click="goUpload">上传</el-button>
      <el-button :icon="Bell" circle />
      <el-avatar :size="36" class="avatar">
        {{ userStore.currentUser.nickname?.[0] || 'U' }}
      </el-avatar>
    </div>
  </header>
</template>

<style lang="scss" scoped>
.top-bar {
  display: flex; align-items: center; gap: var(--space-5);
  padding: 0 var(--space-6);
  height: 64px;
  background: rgba(0,0,0,0.2);
  backdrop-filter: blur(20px);
  border-bottom: 1px solid rgba(255,255,255,0.05);
}
.search { flex: 1; max-width: 480px; }
.actions { display: flex; align-items: center; gap: var(--space-3); }
.avatar {
  background: linear-gradient(135deg, #7C3AED, #06B6D4);
  font-weight: 600;
  cursor: pointer;
}
</style>
```

#### 6.5 `src/components/layout/PlayerBar.vue`（Phase 4 占位）

```vue
<script setup lang="ts">
import { usePlayerStore } from '@/stores/player';
import { storeToRefs } from 'pinia';
import { VideoPlay, VideoPause } from '@element-plus/icons-vue';

const playerStore = usePlayerStore();
const { currentTrack, isPlaying } = storeToRefs(playerStore);
</script>

<template>
  <footer class="player-bar">
    <div v-if="!currentTrack" class="placeholder">
      <span>从音乐库选一首歌开始播放</span>
    </div>

    <div v-else class="now-playing">
      <div class="cover-info">
        <div class="cover" />
        <div class="meta">
          <div class="title">{{ currentTrack.title }}</div>
          <div class="artist">{{ currentTrack.artistName }}</div>
        </div>
      </div>

      <div class="controls">
        <el-button :icon="isPlaying ? VideoPause : VideoPlay" circle size="large"
          @click="isPlaying ? playerStore.pause() : playerStore.resume()" />
        <div class="progress-placeholder">
          <span class="badge">Phase 5 将实装波形播放器</span>
        </div>
      </div>

      <div class="right" />
    </div>
  </footer>
</template>

<style lang="scss" scoped>
.player-bar {
  display: flex; align-items: center;
  padding: 0 var(--space-5);
  height: 88px;
  background: rgba(15,15,30,0.9);
  backdrop-filter: blur(20px);
}
.placeholder {
  width: 100%; text-align: center;
  color: var(--color-fg-tertiary);
  font-size: 13px;
}
.now-playing { width: 100%; display: grid; grid-template-columns: 1fr 2fr 1fr; align-items: center; gap: var(--space-5); }

.cover-info { display: flex; align-items: center; gap: var(--space-3); }
.cover { width: 56px; height: 56px; border-radius: 8px;
  background: linear-gradient(135deg, #7C3AED, #06B6D4); flex-shrink: 0; }
.meta .title { font-weight: 600; color: var(--color-fg-primary); }
.meta .artist { font-size: 12px; color: var(--color-fg-secondary); }

.controls { display: flex; flex-direction: column; align-items: center; gap: 8px; }
.progress-placeholder { width: 100%; text-align: center; }
.badge {
  display: inline-block; padding: 2px 8px;
  font-size: 11px; color: var(--color-brand);
  background: rgba(124,58,237,0.12); border-radius: var(--radius-pill);
}
</style>
```

#### 6.6 `src/components/common/SoundprintLogo.vue`

简单的 SVG Logo，可以是声波/波形图标：

```vue
<template>
  <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 32 32" fill="none">
    <defs>
      <linearGradient id="logoGrad" x1="0%" y1="0%" x2="100%" y2="100%">
        <stop offset="0%" stop-color="#7C3AED" />
        <stop offset="100%" stop-color="#06B6D4" />
      </linearGradient>
    </defs>
    <rect x="3"  y="13" width="3" height="6"  rx="1.5" fill="url(#logoGrad)" />
    <rect x="8"  y="10" width="3" height="12" rx="1.5" fill="url(#logoGrad)" />
    <rect x="13" y="6"  width="3" height="20" rx="1.5" fill="url(#logoGrad)" />
    <rect x="18" y="10" width="3" height="12" rx="1.5" fill="url(#logoGrad)" />
    <rect x="23" y="14" width="3" height="4"  rx="1.5" fill="url(#logoGrad)" />
  </svg>
</template>
```

#### 6.7 `src/components/common/EmptyState.vue`、`PageHeader.vue`、`LoadingBlock.vue`

按需实现，**保持设计语言一致**（深色 + 紫色 + 磨砂感）。

#### 6.8 `src/components/shelf/HorizontalShelf.vue`（横向滚动行）

首页核心组件，模仿 Apple Music：

```vue
<script setup lang="ts">
defineProps<{
  title: string;
  more?: string;
}>();
</script>

<template>
  <section class="shelf">
    <header class="shelf-header">
      <h2 class="title">{{ title }}</h2>
      <router-link v-if="more" :to="more" class="more">查看全部 →</router-link>
    </header>
    <div class="shelf-scroll">
      <div class="shelf-content">
        <slot />
      </div>
    </div>
  </section>
</template>

<style lang="scss" scoped>
.shelf {
  margin-bottom: var(--space-6);
}
.shelf-header {
  display: flex; justify-content: space-between; align-items: baseline;
  margin-bottom: var(--space-4);
}
.title {
  font-size: 22px; font-weight: 700;
  color: var(--color-fg-primary);
  margin: 0;
}
.more {
  color: var(--color-brand);
  text-decoration: none;
  font-size: 13px;
  &:hover { color: var(--color-brand-hover); }
}
.shelf-scroll {
  overflow-x: auto;
  overflow-y: hidden;
  padding-bottom: 8px;
}
.shelf-content {
  display: flex; gap: var(--space-4);
}
</style>
```

### 任务 7：实现路由

`src/router/index.ts`：

```ts
import { createRouter, createWebHistory, RouteRecordRaw } from 'vue-router';

const routes: RouteRecordRaw[] = [
  {
    path: '/login',
    component: () => import('@/views/auth/LoginView.vue'),
    meta: { hideChrome: true },
  },
  { path: '/',          component: () => import('@/views/dashboard/DashboardView.vue') },
  { path: '/library',   component: () => import('@/views/library/LibraryView.vue') },
  { path: '/albums',    component: () => import('@/views/albums/AlbumListView.vue') },
  { path: '/albums/:id', component: () => import('@/views/albums/AlbumDetailView.vue') },
  { path: '/artists',   component: () => import('@/views/artists/ArtistListView.vue') },
  { path: '/artists/:id', component: () => import('@/views/artists/ArtistDetailView.vue') },
  { path: '/playlists', component: () => import('@/views/playlists/PlaylistListView.vue') },
  { path: '/playlists/:id', component: () => import('@/views/playlists/PlaylistDetailView.vue') },
  { path: '/studio',    component: () => import('@/views/studio/StudioView.vue') },
  { path: '/stats',     component: () => import('@/views/stats/StatsView.vue') },
  { path: '/settings',  component: () => import('@/views/settings/SettingsView.vue') },
];

export default createRouter({
  history: createWebHistory(),
  routes,
});
```

### 任务 8：实现 `main.ts` 和 `App.vue`

`src/main.ts`：

```ts
import { createApp } from 'vue';
import { createPinia } from 'pinia';
import App from './App.vue';
import router from './router';
import ElementPlus from 'element-plus';
import zhCn from 'element-plus/es/locale/lang/zh-cn';

import './styles/index.scss';

const app = createApp(App);
app.use(createPinia());
app.use(router);
app.use(ElementPlus, { locale: zhCn });

// 启用 Element-Plus 暗色主题
document.documentElement.classList.add('dark');

app.mount('#app');
```

`src/App.vue`：

```vue
<script setup lang="ts">
import AppLayout from '@/components/layout/AppLayout.vue';
</script>

<template>
  <AppLayout />
</template>
```

### 任务 9：实现核心页面（关键产出）

#### 9.1 `DashboardView.vue`（首页）

调用 `/api/dashboard`，展示问候语 + 横向滚动行（最近添加 / 最近播放 / 收藏）。布局：

- 顶部一个大的 Hero 区，显示问候语 "Good evening, Kaidi" + 副标题
- 下方 3 个 HorizontalShelf：最近添加、最近播放、我的收藏
- 每个 shelf 里塞 6 张专辑/曲目封面卡片

**Infinite Menu 球体的位置预留，Phase 7 集成**——本阶段在 Hero 区放个占位卡片。

#### 9.2 `LibraryView.vue`（音乐库）

调用 `/api/tracks`，展示 Element-Plus 表格：

- 顶部统计信息："共 N 首 · 总时长 X 小时"
- 搜索框 + 格式筛选下拉
- 表格列：封面缩略图 | 标题 | 艺术家 | 专辑 | 时长 | 格式 | 操作（播放/收藏/...）
- 底部分页器
- 点击行 → 调用 `playerStore.play(track)`（Phase 5 实际播放）

实现要点：
- 用 Element-Plus 的 `el-table` + `el-pagination`
- 列宽合理，封面列窄、艺术家/专辑列宽
- hover 行高亮（已在 element-overrides 配置）

#### 9.3 `AlbumListView.vue` + `ArtistListView.vue`

调用 `/api/albums` 和 `/api/artists`，网格卡片布局：

- 每行 6 列（在 1080p 屏幕下），CSS Grid 实现
- 卡片：封面 + 标题 + 副标题
- hover 浮起 + 显示曲目数
- 点击卡片 → 跳详情页

#### 9.4 `LoginView.vue`（占位，Phase 7 加 Galaxy 背景）

中央磨砂卡片，用户名密码输入框 + 登录按钮。点击登录直接调 `userStore.login()`（占位）然后跳首页。**Phase 4 不做真实鉴权**。

#### 9.5 其他 View（占位即可）

`PlaylistListView`、`StudioView`、`StatsView`、`SettingsView`：每个里面放一个 `EmptyState` 组件，提示"Phase X 实装中"。这样路由能正常切换、侧栏点击不报错。

### 任务 10：联调测试

```powershell
# 终端 1：启后端
cd D:\Claude_Playground\Soundprint\backend
mvn spring-boot:run

# 终端 2：启前端
cd D:\Claude_Playground\Soundprint\frontend
npm run dev
```

浏览器打开 http://localhost:5173

**验收清单**：

- [ ] 看到深色背景、紫色品牌色、左侧 Soundprint logo
- [ ] 侧栏点击切换不同页面，路由正常
- [ ] 首页能看到种子数据 / 你上传的 Imagine Dragons
- [ ] 音乐库页表格有 30+ 行（含你 Phase 3 上传的那首）
- [ ] 专辑页能看到 15 张专辑卡片
- [ ] 艺术家页能看到 10+ 艺术家
- [ ] 顶栏搜索能跳转到库页并带 keyword 参数
- [ ] 底部播放器占位显示"Phase 5 将实装"
- [ ] 浏览器 Console 无红色报错

### 任务 11：顺手修 Knife4j 的 `x-www-form-urlencoded` 小瑕疵

后端 `application.yml` 加一行（或在 `Knife4jConfig.java` 里加）：

```yaml
springdoc:
  default-consumes-media-type: application/json
  default-produces-media-type: application/json
```

让 `GET` 接口在文档里不再标 form-urlencoded。

### 任务 12：commit

```powershell
cd D:\Claude_Playground\Soundprint
git add frontend/ backend/
@"
feat: 完成前端骨架与基础页面（Phase 4）

工程：
- Vite 5 + Vue 3.4 + TypeScript 5
- Element-Plus 2 + Tailwind CSS 3（共存配置，preflight 关）
- Pinia + Vue Router + Axios（拦截器自动剥壳）

设计系统：
- 设计令牌：紫色主色 #7C3AED + 青色强调 + 深空背景体系
- GlassCard 磨砂玻璃组件
- Element-Plus 主题色与 Tailwind brand 色对齐
- 暗色主题全局生效

布局：
- AppLayout：CSS Grid 三段式（侧栏+主区+底栏）
- SidebarNav / TopBar / PlayerBar（Phase 5 实装播放器内部）

页面：
- DashboardView：首页问候 + 3 个横向滚动行（最近添加/最近播放/收藏）
- LibraryView：音乐库表格 + 搜索 + 格式筛选 + 分页
- AlbumListView / ArtistListView：网格卡片
- 其他页面占位（StudioView/StatsView/PlaylistView 等）

基础设施：
- API 模块化：tracks/albums/artists/playlists/tags/favorites/playHistory/dashboard/stats/conversions
- Types：业务类型定义
- 流式播放 URL 拼接（streamUrl）

附带：
- 修复 Knife4j GET 接口默认标 x-www-form-urlencoded 的小瑕疵
"@ | Out-File "D:\Claude_Playground\_msg.txt" -Encoding utf8NoBOM
git commit -F "D:\Claude_Playground\_msg.txt"
Remove-Item "D:\Claude_Playground\_msg.txt"
```

push 与否听用户。

---

## 📚 边写边讲要求（必讲清单）

Phase 4 要讲透的技术点：

### 1. **Vite vs Webpack**（必讲）
- 原生 ESM、按需编译、HMR 速度
- 答辩话术

### 2. **TypeScript 在 Vue 项目里的价值**（必讲）
- 接口契约、自动补全、重构安全
- 答辩话术

### 3. **Tailwind 和 Element-Plus 怎么共存**（必讲）
- `preflight: false` 的关键作用
- 何时用 Tailwind utility class，何时用 Element-Plus 组件
- 设计令牌的统一

### 4. **设计令牌系统**（必讲，答辩高频）
- 颜色/间距/圆角/阴影集中管理
- 改一处，全站统一
- 工业界做法

### 5. **CSS Grid 三段式布局**（讲）
- grid-template-areas 的可读性
- 比 Flex 嵌套更适合页面级布局

### 6. **Axios 拦截器自动剥壳**（必讲）
- 为什么前端不写 `res.data.data`
- 错误统一处理

### 7. **Pinia vs Vuex**（讲）
- Composition API 风格
- TypeScript 支持
- 为什么 Vue 3 官方换掉 Vuex

### 8. **Vue Router 的 `meta` 字段**（讲）
- `hideChrome` 控制登录页不出现侧栏

### 9. **Vite 代理解决跨域**（必讲）
- 开发期前端 5173 调后端 8080
- `/api` 代理到 `http://localhost:8080`
- 生产环境用 Nginx 同等转发

### 10. **磨砂玻璃 `backdrop-filter`**（讲）
- 现代 CSS 特性
- 性能注意（不要套太多层）
- 浏览器兼容

---

## ✅ 完成检查清单

- [ ] Vite + Vue 3 + TS 工程跑得起 `npm run dev`
- [ ] Tailwind 配置 + 设计令牌生效（页面是深色 + 紫色主题）
- [ ] Element-Plus 组件能用，且颜色与 brand 一致
- [ ] 侧栏导航 + 顶栏 + 底部播放器占位三件套完整
- [ ] 全部 API 模块（约 10 个）已封装
- [ ] DashboardView 能从 `/api/dashboard` 拿到数据
- [ ] LibraryView 能展示 30+ 曲目，搜索能用
- [ ] AlbumListView、ArtistListView 网格卡片正常
- [ ] 其他页面占位，路由能切换
- [ ] 浏览器无 Console 报错
- [ ] Knife4j 那个小瑕疵已修
- [ ] commit 完成

---

## 📩 反馈给架构师的内容

完成本阶段后请反馈：

1. **首页截图**：能看到问候语 + 横向滚动行
2. **音乐库截图**：能看到种子数据 + 你上传的那首
3. **专辑列表截图**：网格卡片布局
4. **侧栏 / 顶栏 / 底部播放器** 视觉效果如何，整体协调感
5. **任何视觉上你觉得"差点意思"的地方**——这是 Phase 7 视觉精修的输入
6. **是否准备进 Phase 5（播放器 + 转换工坊真实装）**

---

## ⚠️ 注意事项

- **不要在本阶段集成 vue-bits**（Phase 7 才做，避免提前打乱节奏）
- **不要实现真实播放逻辑**（Phase 5）
- **不要实现 ECharts 统计页**（Phase 6）
- **不要硬编码颜色**，所有颜色走设计令牌（Tailwind config 或 CSS 变量）
- **保持后端与前端两个终端窗口分开**，避免日志混在一起
- **`application-dev.yml` 仍然不能进 git**，验证 commit 前用 `git status`
- 如果某个 API 返回的 JSON 结构和前端类型对不上，**停下来报告**，不要擅自改后端

---

**End of Phase 4 Document.**
