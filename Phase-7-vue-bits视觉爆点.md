# Phase 7：vue-bits 视觉爆点（Galaxy + Circular Gallery + Antigravity + Balatro）

> Soundprint 项目的第八个阶段文档。
> Codex 主力 + Claude Code 审计模式延续。
> 本阶段完成后，Soundprint 完成"工业级 → 作品级"的视觉跃迁。
> 这是项目颜值的终极冲刺，做完答辩演示老师一进系统就会"哇"。

---

## 🎯 阶段目标

把 4 个 vue-bits 组件集成进 Soundprint，全部用品牌紫青色定制，**而不是 vue-bits 默认的丑配色**：

| 组件 | 集成位置 | 视觉作用 | 默认色 → 改为 |
|---|---|---|---|
| **Galaxy** | 登录页 + 首页 Hero | 全屏紫色星空，沉浸感 | 青绿星空 → 紫色星空 |
| **Circular Gallery** | 专辑 / 艺术家 / 歌单 Hero 区 | 横向弧形画廊浏览入口 | 灰度图 → 真实封面 + 紫色文字 |
| **Antigravity** | NowPlayingView 右侧背景 | 替换右侧大片空白 | 荧光绿粒子 → 紫色粒子 |
| **Balatro** | 转换工坊 RUNNING 状态背景 | 处理中的视觉反馈 | 红蓝丑色 → 紫青暗三色 |

**Soundprint 配色基准**（贯穿四个组件）：
- 主紫：`#7C3AED`
- 强调青：`#06B6D4`
- 深紫底：`#5B21B6`
- 暗黑底：`#0F0F1E`

**关键约束**：
- 同屏**最多一个 WebGL 组件**（性能保护，登录页 Galaxy 离开后必须 dispose）
- 不破坏 Phase 4-6 已有的页面结构和功能
- 所有 vue-bits 组件**作为背景层**（z-index: 0），业务内容浮在上层（z-index: 10+）
- 加载/转场动画**先不引入**——你 Phase 8 觉得不够再补
- 组件版权要在源码顶部加注释（vue-bits 是 MIT + Commons Clause）

---

## 📋 任务清单

### 任务 0：环境与依赖

```powershell
cd D:\Claude_Playground\Soundprint\frontend

# 安装 OGL（Galaxy/CircularGallery/Balatro 共用）
npm install ogl

# 安装 Three.js（仅 Antigravity 用）
npm install three
npm install -D @types/three
```

OGL 是轻量 WebGL 抽象库（约 100KB），Three.js 是完整 3D 引擎（约 1MB）。**这次集成完毕后两个库都常驻**，**总体 bundle 增加约 1.1 MB**——可接受，作品级颜值的必要代价。

验证：
```powershell
npm ls ogl three
```

应该都能看到版本号。

---

### 任务 1：项目骨架——vue-bits 组件目录

#### 1.1 创建独立目录隔离 vue-bits 组件

```powershell
mkdir frontend\src\components\vue-bits
mkdir frontend\src\components\vue-bits\backgrounds
mkdir frontend\src\components\vue-bits\components
mkdir frontend\src\components\vue-bits\animations
```

**为什么单独建目录**：
1. 隔离第三方代码，未来 vue-bits 升级容易做 diff
2. 标记清楚版权归属，避免被当成原创代码
3. 目录结构对应 vue-bits 官网分类，便于查找

#### 1.2 创建版权注释模板

每个 vue-bits 组件文件**顶部必须加这段注释**：

```vue
<!--
  Component: <ComponentName>
  Source: vue-bits (https://vue-bits.dev/<category>/<name>)
  License: MIT + Commons Clause
  Modifications for Soundprint:
    - <列出你做的所有修改，例如 "调整默认颜色为品牌紫青" "添加 Soundprint 透明背景">
-->
```

这段注释是**开源合规 + 工程素养**的体现，**答辩时如果老师问"这些动效是你写的吗"，你可以坦荡回答**："核心 shader 代码引用自 vue-bits 开源库，符合 MIT 协议，我做了配色定制和性能优化集成"。

#### 1.3 复制 4 个组件源码到对应目录

把用户提供的源码原样复制到下面这些位置，**只在文件顶部加版权注释**，**源码本身不动**：

```
frontend/src/components/vue-bits/
├── backgrounds/
│   └── Galaxy.vue                ← 复制用户提供的 Galaxy 源码
├── components/
│   └── CircularGallery.vue       ← 复制用户提供的 Circular Gallery 源码
├── animations/
│   ├── Antigravity.vue           ← 复制用户提供的 Antigravity 源码
│   └── Balatro.vue               ← 复制用户提供的 Balatro 源码
```

**严格不动源码逻辑**——通过 props 配置 + 父组件包装来定制效果。

#### 1.4 创建 `docs/vue-bits-references.md`

记录每个组件的引用来源和修改清单，方便审计和答辩。模板：

```markdown
# vue-bits Component References

This project uses the following components from vue-bits (https://vue-bits.dev),
licensed under MIT with Commons Clause.

## Galaxy
- Source: https://vue-bits.dev/backgrounds/galaxy
- File: `src/components/vue-bits/backgrounds/Galaxy.vue`
- Modifications: Default `hueShift: 260` for Soundprint brand purple instead of 140.

## CircularGallery
- Source: https://vue-bits.dev/components/circular-gallery
- File: `src/components/vue-bits/components/CircularGallery.vue`
- Modifications: Default `textColor: '#F5F5F7'`, `font: 'bold 22px Inter'`.

## Antigravity
- Source: https://vue-bits.dev/animations/antigravity
- File: `src/components/vue-bits/animations/Antigravity.vue`
- Modifications: Default `color: '#7C3AED'` for brand purple.

## Balatro
- Source: https://vue-bits.dev/animations/balatro
- File: `src/components/vue-bits/animations/Balatro.vue`
- Modifications: Default `color1/2/3` set to Soundprint brand purple/cyan/ink.
```

---

### 任务 2：Galaxy 集成 —— 登录页 + 首页 Hero

Galaxy 是 vue-bits 默认 hue 140（青绿）。**Soundprint 用 hue 260（紫色）**。

#### 2.1 登录页 `LoginView.vue` 重做

**目标视觉**：全屏紫色星空 + 中央磨砂玻璃登录卡片。

```vue
<script setup lang="ts">
import { ref } from 'vue';
import { useRouter } from 'vue-router';
import { useUserStore } from '@/stores/user';
import Galaxy from '@/components/vue-bits/backgrounds/Galaxy.vue';
import SoundprintLogo from '@/components/common/SoundprintLogo.vue';

const router = useRouter();
const userStore = useUserStore();

const username = ref('admin');
const password = ref('');
const loading = ref(false);

async function handleLogin() {
  loading.value = true;
  try {
    userStore.login(username.value, password.value);
    router.push('/');
  } finally {
    loading.value = false;
  }
}
</script>

<template>
  <div class="login-view">
    <!-- 背景：紫色星空 -->
    <Galaxy
      class="galaxy-bg"
      :hue-shift="260"
      :density="1.2"
      :glow-intensity="0.4"
      :twinkle-intensity="0.5"
      :saturation="0.7"
      :rotation-speed="0.05"
      :mouse-interaction="true"
      :mouse-repulsion="true"
      :transparent="false"
    />

    <!-- 前景：登录卡片 -->
    <div class="login-card">
      <SoundprintLogo class="logo" />
      <h1 class="title">Soundprint</h1>
      <p class="subtitle">你的私人无损音乐库</p>

      <el-form @submit.prevent="handleLogin" class="form">
        <el-input
          v-model="username"
          placeholder="用户名"
          size="large"
          class="input"
        />
        <el-input
          v-model="password"
          type="password"
          placeholder="密码"
          size="large"
          class="input"
          show-password
        />
        <el-button
          type="primary"
          size="large"
          :loading="loading"
          native-type="submit"
          @click="handleLogin"
          class="login-btn"
        >
          进入音乐空间
        </el-button>
      </el-form>

      <p class="hint">演示账号已预设，任意密码即可登录</p>
    </div>
  </div>
</template>

<style lang="scss" scoped>
.login-view {
  position: relative;
  width: 100vw;
  height: 100vh;
  background: var(--color-bg-base);
  overflow: hidden;
}

.galaxy-bg {
  position: absolute;
  inset: 0;
  z-index: 0;
}

.login-card {
  position: relative;
  z-index: 10;
  width: 100%;
  max-width: 400px;
  margin: 0 auto;
  padding: var(--space-8) var(--space-6);
  background: rgba(15, 15, 30, 0.6);
  border: 1px solid rgba(255, 255, 255, 0.08);
  border-radius: 24px;
  backdrop-filter: blur(40px);
  -webkit-backdrop-filter: blur(40px);
  box-shadow: 0 24px 80px rgba(0, 0, 0, 0.5), 0 0 0 1px rgba(124, 58, 237, 0.1);

  position: absolute;
  left: 50%;
  top: 50%;
  transform: translate(-50%, -50%);
}

.logo {
  width: 48px;
  height: 48px;
  margin: 0 auto var(--space-4);
  display: block;
}

.title {
  font-size: 36px;
  font-weight: 700;
  text-align: center;
  margin: 0;
  background: linear-gradient(135deg, #7C3AED 0%, #06B6D4 100%);
  -webkit-background-clip: text;
  background-clip: text;
  color: transparent;
}

.subtitle {
  text-align: center;
  color: var(--color-fg-secondary);
  margin: var(--space-2) 0 var(--space-6);
}

.form {
  display: flex;
  flex-direction: column;
  gap: var(--space-3);
}

.login-btn {
  margin-top: var(--space-3);
  font-size: 16px;
  height: 48px;
}

.hint {
  text-align: center;
  color: var(--color-fg-tertiary);
  font-size: 12px;
  margin-top: var(--space-5);
}
</style>
```

**视觉验收点**：
- 紫色星空全屏铺满，**鼠标移动星星会被排斥**（鼠标交互生效）
- 登录卡片磨砂玻璃质感，**透过卡片能看到模糊的星空**（backdrop-filter 起作用）
- Logo + 渐变文字 + 表单清晰可读
- "进入音乐空间" 紫色主按钮

#### 2.2 首页 `DashboardView.vue` 改造

把现有的紫色磨砂 Hero 区**底层加 Galaxy 背景**——星空只在 Hero 这一块，不全屏（**避免影响下方横向滚动行的可读性**）。

修改方式：Hero 容器加相对定位，里面叠 Galaxy + 内容：

```vue
<!-- 现有的 Hero 区域改成这样 -->
<section class="hero">
  <!-- 底层：星空 -->
  <Galaxy
    class="hero-galaxy"
    :hue-shift="260"
    :density="0.8"
    :glow-intensity="0.3"
    :twinkle-intensity="0.4"
    :rotation-speed="0.03"
    :mouse-interaction="true"
    :transparent="true"
  />

  <!-- 顶层：原有的问候内容 -->
  <div class="hero-content">
    <span class="hero-label">SOUNDPRINT LIBRARY</span>
    <h1 class="hero-title">{{ greeting }}，{{ nickname }}</h1>
    <p class="hero-sub">从最近添加、播放历史和收藏中继续探索你的无损音乐库。</p>
  </div>

  <!-- 右侧 Phase 7 视觉位 -->
  <div class="hero-right">
    <!-- Phase 7 之前的占位卡片现在可以删除了，因为 Galaxy 已经撑场 -->
  </div>
</section>
```

样式补充：

```scss
.hero {
  position: relative;
  border-radius: var(--radius-card);
  overflow: hidden;
  min-height: 240px;
  display: grid;
  grid-template-columns: 2fr 1fr;
}

.hero-galaxy {
  position: absolute;
  inset: 0;
  z-index: 0;
  opacity: 0.85;
}

.hero-content {
  position: relative;
  z-index: 10;
  padding: var(--space-6);
}
// ... 其他保留 Phase 4 已有样式 ...
```

**视觉验收点**：
- Hero 区域内紫色星空，**不影响下方横向滚动行**
- 问候语清晰可读，**Galaxy 作为衬底而不是主角**
- `:transparent="true"` 让 Galaxy 不挡住下面深色背景

#### 2.3 性能保护：路由切走时 dispose

Galaxy 的源码已经实现了 `onUnmounted` 清理（`gl.getExtension('WEBGL_lose_context')?.loseContext()`）。**这是 vue-bits 做得对的地方**。Codex 集成时**不要破坏这个生命周期**——不要把 Galaxy 放进永不销毁的全局 Layout，**仅放在登录页和首页的局部容器内**，路由切走自然销毁。

---

### 任务 3：Circular Gallery 集成 —— 专辑/艺术家/歌单 Hero 区

这是 Phase 7 **改动最大**的部分。三个列表页（AlbumListView / ArtistListView / PlaylistListView）都要在**顶部加 Circular Gallery 作为 Hero 入口**，**下方保留 Phase 4 的网格列表**。

#### 3.1 设计原则

**两段式布局**：

```
┌──────────────────────────────────────────────┐
│  上半部分 (400px 高)                          │
│  Circular Gallery (精选 12-15 张专辑)         │
│  弧形画廊，滚轮/拖拽切换                       │
└──────────────────────────────────────────────┘
┌──────────────────────────────────────────────┐
│  下半部分                                     │
│  完整网格列表 (Phase 4 已实现，不动)          │
│  - 搜索框                                     │
│  - 6 列网格卡片                               │
│  - 分页                                       │
└──────────────────────────────────────────────┘
```

**关键约束**：
- Circular Gallery **只展示 12-15 张精选**（不能塞全部，性能 + 视觉双输）
- 数据复用 Dashboard 的 `featuredAlbums` / `featuredArtists` 字段（Phase 3 已经实现过）
- 点击 Circular Gallery 中央那张可以跳详情页（**可选增强**）

#### 3.2 数据准备 —— 后端聚合接口微调

Dashboard 接口已经有 `featuredAlbums` 和 `featuredArtists`，但歌单**还没有**类似字段。**Codex 视情况判断**：

**方案 A**（推荐）：Dashboard 接口扩展 `featuredPlaylists` 字段
- 改后端 `DashboardServiceImpl`，加一段查询：按 `created_at DESC` 取前 12 个 playlist
- 改 `DashboardResponse`，加 `featuredPlaylists: PlaylistResponse[]`
- 前端 dashboard.ts 类型补字段

**方案 B**：歌单页直接调 `/api/playlists?page=1&size=12` 拿数据
- 不动后端
- 前端在 PlaylistListView 里调两个接口（精选 + 完整列表，**精选取前 12 个完整列表是子集**，逻辑稍乱）

Codex 选 A，**改动小且语义清晰**。

#### 3.3 创建 Circular Gallery 适配器

vue-bits 的 Circular Gallery 接受 `items: { image: string; text: string }[]`。**我们的专辑/艺术家/歌单数据需要转换成这个格式**：

`src/components/common/SoundprintCircularGallery.vue`：

```vue
<script setup lang="ts">
import { computed } from 'vue';
import CircularGallery from '@/components/vue-bits/components/CircularGallery.vue';
import { fileUrl } from '@/utils/url';

const props = defineProps<{
  items: Array<{
    coverUrl?: string | null;
    avatarUrl?: string | null;
    title?: string;
    name?: string;
  }>;
  type: 'album' | 'artist' | 'playlist';
}>();

// 把业务对象转成 Circular Gallery 接受的格式
const galleryItems = computed(() => {
  return props.items.map(item => {
    // 取封面 URL，多个字段兜底
    const rawImage = item.coverUrl || item.avatarUrl || '';
    const image = rawImage
      ? (rawImage.startsWith('http') ? rawImage : `${window.location.origin}${fileUrl(rawImage)}`)
      : fallbackImage(props.type);

    // 取显示文字
    const text = item.title || item.name || '';

    return { image, text };
  });
});

// 占位封面 —— 用 SVG dataurl 生成紫青渐变方块
function fallbackImage(type: string): string {
  const svg = `<svg xmlns="http://www.w3.org/2000/svg" width="600" height="400">
    <defs>
      <linearGradient id="g" x1="0%" y1="0%" x2="100%" y2="100%">
        <stop offset="0%" stop-color="#7C3AED"/>
        <stop offset="100%" stop-color="#06B6D4"/>
      </linearGradient>
    </defs>
    <rect width="600" height="400" fill="url(#g)"/>
    <text x="50%" y="50%" font-size="80" font-family="Inter" font-weight="700"
      fill="white" text-anchor="middle" dominant-baseline="middle" opacity="0.9">
      ${type === 'album' ? '♪' : type === 'artist' ? '👤' : '♬'}
    </text>
  </svg>`;
  return `data:image/svg+xml;base64,${btoa(svg)}`;
}
</script>

<template>
  <div class="gallery-wrap">
    <CircularGallery
      :items="galleryItems"
      :bend="2"
      text-color="#F5F5F7"
      :border-radius="0.04"
      font="bold 22px Inter"
      :scroll-speed="2.5"
      :scroll-ease="0.05"
    />
  </div>
</template>

<style lang="scss" scoped>
.gallery-wrap {
  width: 100%;
  height: 100%;
  overflow: hidden;
  border-radius: var(--radius-card);
  background:
    radial-gradient(at 0% 0%, rgba(124, 58, 237, 0.15) 0%, transparent 50%),
    radial-gradient(at 100% 100%, rgba(6, 182, 212, 0.08) 0%, transparent 50%),
    rgba(15, 15, 30, 0.6);
}
</style>
```

**关键设计**（Codex 必须理解）：

1. **图片 URL 处理**：Circular Gallery 用原生 `Image` 加载图，**默认 fetch 是跨域的**。我们的后端静态文件服务在 `http://localhost:8080/files/...`，前端在 `localhost:5173`，**直接传相对路径会失败**。所以这里拼成完整的 `window.location.origin + /files/...` URL，**配合 Vite 代理**自动路由到后端。
2. **占位封面用 SVG dataurl**：避免 CORS、避免额外网络请求，**封面缺失时也有视觉**。
3. **bend=2 微弧**：不太弯也不太平，看起来"立体但不晕"。
4. **scroll-speed=2.5**：vue-bits 默认 2，我们调快一点让滚动响应更跟手。

#### 3.4 AlbumListView 改造

```vue
<script setup lang="ts">
import { ref, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import { albumApi } from '@/api/album';
import { dashboardApi } from '@/api/dashboard';
import type { Album } from '@/types/album';
import SoundprintCircularGallery from '@/components/common/SoundprintCircularGallery.vue';
// ... 其他原有 import 保留 ...

const router = useRouter();

// 精选数据（给 Circular Gallery 用）
const featuredAlbums = ref<Album[]>([]);

// 完整列表数据（给原有网格用，Phase 4 已实现的逻辑保留）
const albums = ref<Album[]>([]);
// ... 其他原有 ref 保留 ...

onMounted(async () => {
  // 并行获取精选 + 完整列表
  const [dashboard, listRes] = await Promise.all([
    dashboardApi.get(),
    albumApi.page({ page: 1, size: 30 }),
  ]);
  featuredAlbums.value = dashboard.featuredAlbums || [];
  albums.value = listRes.records;
});
</script>

<template>
  <div class="album-list-view">
    <!-- 上半段：Circular Gallery Hero -->
    <section class="hero-section" v-if="featuredAlbums.length > 0">
      <div class="hero-label-row">
        <span class="hero-label">FEATURED</span>
        <h2 class="hero-title">精选专辑</h2>
      </div>
      <div class="gallery-container">
        <SoundprintCircularGallery
          :items="featuredAlbums"
          type="album"
        />
      </div>
    </section>

    <!-- 下半段：完整列表（保留 Phase 4 已实现的部分） -->
    <section class="list-section">
      <PageHeader title="所有专辑" subtitle="按专辑浏览音乐库" />
      <!-- 搜索框 + 网格卡片 + 分页（Phase 4 已有代码） -->
    </section>
  </div>
</template>

<style lang="scss" scoped>
.album-list-view {
  display: flex;
  flex-direction: column;
  gap: var(--space-6);
}

.hero-section {
  display: flex;
  flex-direction: column;
  gap: var(--space-4);
}

.hero-label-row {
  display: flex;
  align-items: baseline;
  gap: var(--space-3);
}

.hero-label {
  font-size: 11px;
  font-weight: 600;
  letter-spacing: 0.15em;
  color: var(--color-brand);
}

.hero-title {
  margin: 0;
  font-size: 24px;
  font-weight: 700;
  color: var(--color-fg-primary);
}

.gallery-container {
  height: 420px;
  border-radius: var(--radius-card);
  overflow: hidden;
}
</style>
```

**ArtistListView 和 PlaylistListView 完全同款改造**——只是 `type="artist"` / `type="playlist"`，数据源相应换成 `featuredArtists` / `featuredPlaylists`。

#### 3.5 性能保护

Circular Gallery 用 OGL + 12-15 张图片纹理 → **不算重**。但要注意：

- **路由切走时必须销毁**：源码已经有 `onUnmounted` 调用 `app.destroy()`，**Codex 不要改这个**
- **同屏只一个 Gallery**：用户在专辑页时，艺术家页和歌单页的 Gallery **不应该已经初始化**。Vue Router 的 lazy import + 默认不缓存路由 → 自动满足这个约束

---

### 任务 4：Antigravity 集成 —— NowPlayingView 背景重做

Phase 5 的 NowPlayingView 右侧有大片空白。**Antigravity 紫色粒子环正好填满**。

#### 4.1 重做 NowPlayingView 布局

**目标视觉**：左侧大封面（保留 Phase 5 的设计），右侧 Antigravity 紫色粒子作为衬底，前面浮波形 + 歌词。

```vue
<script setup lang="ts">
// ... 原有 import 保留 ...
import Antigravity from '@/components/vue-bits/animations/Antigravity.vue';
</script>

<template>
  <div v-if="currentTrack" class="now-playing-view">
    <el-button :icon="Close" circle class="close-btn" @click="close" />

    <div class="content">
      <!-- 左：大封面 + 元信息（Phase 5 已有） -->
      <section class="cover-section">
        <SmartCover
          :src="currentTrack.coverUrl || currentTrack.albumCoverUrl"
          class="big-cover"
        />
        <div class="track-meta">
          <h1 class="title">{{ currentTrack.title }}</h1>
          <p class="artist">{{ currentTrack.artistName || '未知艺术家' }}</p>
          <p class="album">{{ currentTrack.albumTitle || '未知专辑' }}</p>
        </div>
      </section>

      <!-- 右：Antigravity 背景 + 波形 + 歌词 -->
      <section class="info-section">
        <!-- 底层：Antigravity 粒子 -->
        <Antigravity
          class="antigravity-bg"
          :count="200"
          :magnet-radius="8"
          :ring-radius="6"
          :particle-size="1.5"
          color="#7C3AED"
          :auto-animate="true"
          :wave-speed="0.3"
          :wave-amplitude="0.8"
          :rotation-speed="0.05"
          :depth-factor="1.2"
          :pulse-speed="2"
          particle-shape="capsule"
        />

        <!-- 上层：波形 + 歌词，半透明卡片 -->
        <div class="info-overlay">
          <WaveformDisplay class="waveform" />
          <LyricsPanel :lyrics="lyrics" :current-time="currentTime" class="lyrics" />
        </div>
      </section>
    </div>
  </div>
</template>

<style lang="scss" scoped>
.now-playing-view {
  position: fixed;
  inset: 0;
  background: radial-gradient(circle at 30% 30%, #1A1530 0%, #0A0A14 60%);
  z-index: 100;
  padding: var(--space-6);
}

.close-btn {
  position: absolute;
  top: var(--space-5);
  right: var(--space-5);
  z-index: 200;
}

.content {
  display: grid;
  grid-template-columns: minmax(0, 1fr) minmax(0, 1.2fr);
  gap: var(--space-8);
  max-width: 1400px;
  margin: var(--space-8) auto;
  align-items: stretch;
  height: calc(100vh - 200px);
}

/* 左侧：保留 Phase 5 样式 */
.cover-section {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: var(--space-5);
  justify-content: center;
}
.big-cover {
  width: 100%;
  max-width: 480px;
  aspect-ratio: 1;
  border-radius: 24px;
  box-shadow:
    0 24px 64px rgba(0, 0, 0, 0.5),
    0 0 80px rgba(124, 58, 237, 0.2),    /* 紫色光晕，新加 */
    0 0 0 1px rgba(255, 255, 255, 0.05);
}
.track-meta { text-align: center; }
.title {
  font-size: 36px;
  font-weight: 700;
  color: var(--color-fg-primary);
  margin: 0 0 var(--space-2);
}
.artist { font-size: 18px; color: var(--color-fg-secondary); margin: 0; }
.album  { font-size: 14px; color: var(--color-fg-tertiary); margin: var(--space-2) 0 0; }

/* 右侧：Antigravity 衬底 + 内容浮层 */
.info-section {
  position: relative;
  border-radius: 24px;
  overflow: hidden;
  background: rgba(15, 15, 30, 0.3);
}
.antigravity-bg {
  position: absolute;
  inset: 0;
  z-index: 0;
}
.info-overlay {
  position: relative;
  z-index: 10;
  display: flex;
  flex-direction: column;
  gap: var(--space-5);
  padding: var(--space-5);
  height: 100%;
}
.waveform {
  flex-shrink: 0;
  height: 120px;
  background: rgba(15, 15, 30, 0.6);
  border-radius: 12px;
  backdrop-filter: blur(20px);
}
.lyrics {
  flex: 1;
  background: rgba(15, 15, 30, 0.6);
  border-radius: 12px;
  backdrop-filter: blur(20px);
}
</style>
```

**视觉验收点**：
- 左侧大封面 + 紫色光晕（**newly added 增强**）
- 右侧紫色粒子持续飘动（autoAnimate），鼠标进入右侧时粒子环跟着鼠标移动
- 波形和歌词卡片半透明磨砂，**透过卡片能看到背后的紫色粒子**
- 整体视觉重心从"左偏 + 右空"变成"两侧平衡"

#### 4.2 性能保护

Antigravity 是 Three.js InstancedMesh + 200 个粒子，**比 Galaxy 重一些**。源码已经实现：
- `onMounted(setupScene)`
- `onUnmounted(cleanup)`
- cleanup 函数里 dispose 了 geometry / material / renderer

**Codex 不要破坏这些**。NowPlayingView 关闭（用户点 X）时组件销毁，Three.js 上下文释放。

---

### 任务 5：Balatro 集成 —— 转换工坊 RUNNING 状态背景

转换工坊（StudioView.vue）当前 RUNNING 状态只是一个进度条。**Phase 7 在右侧"当前任务"卡片背景加 Balatro**，让"处理中"有强烈视觉反馈。

#### 5.1 修改 StudioView 右侧任务卡片

把现有 `current-task` 元素改造成两层结构：

```vue
<!-- 右侧任务面板的"当前任务"部分 -->
<div v-if="currentTask" class="current-task">
  <!-- 仅 RUNNING 时显示 Balatro 背景 -->
  <Balatro
    v-if="currentTask.status === 'RUNNING'"
    class="balatro-bg"
    :spin-rotation="-1.5"
    :spin-speed="5"
    color1="#7C3AED"
    color2="#06B6D4"
    color3="#0F0F1E"
    :contrast="2.8"
    :lighting="0.5"
    :spin-amount="0.3"
    :pixel-filter="800"
    :spin-ease="1"
    :is-rotate="true"
    :mouse-interaction="false"
  />

  <!-- 内容浮层 -->
  <div class="task-content">
    <div class="task-meta">
      <span class="status" :class="`status-${currentTask.status.toLowerCase()}`">
        {{ currentTask.status }}
      </span>
      <span>{{ currentTask.targetFormat }}</span>
    </div>
    <el-progress
      :percentage="currentTask.progress"
      :status="currentTask.status === 'SUCCESS' ? 'success' : currentTask.status === 'FAILED' ? 'exception' : ''"
    />
    <el-button
      v-if="currentTask.status === 'SUCCESS'"
      type="success"
      @click="download(currentTask)"
    >下载</el-button>
  </div>
</div>
```

需要 import：

```ts
import Balatro from '@/components/vue-bits/animations/Balatro.vue';
```

样式补充：

```scss
.current-task {
  position: relative;
  background: rgba(15, 15, 30, 0.5);
  padding: var(--space-4);
  border-radius: var(--radius-btn);
  margin-bottom: var(--space-4);
  overflow: hidden;
  min-height: 140px;
}

.balatro-bg {
  position: absolute;
  inset: 0;
  z-index: 0;
  opacity: 0.6;     /* 不要太抢戏 */
}

.task-content {
  position: relative;
  z-index: 10;
}
```

**视觉验收点**：
- 提交转换任务后，"当前任务"卡片**立刻出现紫青漩涡背景**
- 进度条在漩涡上方清晰可见
- 任务完成（SUCCESS）时漩涡消失，回归静态磨砂背景
- 漩涡速度感强，给人"系统在拼命算"的感觉

#### 5.2 Balatro 注意事项

源码里 Balatro 的 canvas **`z-index: 100`**——这是源码里写死的：

```js
gl.canvas.style.zIndex = '100';
```

**这个会盖住进度条**。Codex 需要处理：

**方案 A**：修改 vue-bits 源码（不推荐，破坏隔离）

**方案 B**：用 CSS 覆盖（推荐）
```scss
.balatro-bg :deep(canvas) {
  z-index: 0 !important;  /* 强制覆盖源码的 z-index: 100 */
}
```

用方案 B。

---

### 任务 6：Phase 7 不引入加载/转场组件的决策记录

用户在 Phase 7 准备阶段提到"加载/转场没想到很好的，让你推荐"，最后选了 **Circular Gallery + Balatro + Antigravity + Galaxy** 四件套。**没有为路由转场专门加组件**。

**这是合理的决策**，理由：
1. Vue Router 自带 `<transition>` 包装，**Phase 4 已实现 fade 过渡**，效果够用
2. 多一个 WebGL 组件就多一份性能风险
3. **过度的转场反而拖慢用户操作节奏**——音乐 app 的核心体验是"快速点歌"

**Codex 不需要新增任何路由转场动画**。如果 Phase 8 答辩准备时觉得不够，再补也来得及。

把这个决策**记进 dev-notes.md 的 Phase 7 段落**。

---

### 任务 7：性能监控与全局保护

集成 4 个 WebGL 组件后，**总是有踩雷风险**。Codex 实装时必须做以下保护：

#### 7.1 WebGL 上下文数量检查

浏览器对单个页面的 WebGL 上下文有上限（一般 8-16 个），**超过会"丢失最早的上下文"**。我们当前最多同屏 1 个 WebGL 组件，远低于上限。**但 Codex 要在 dev-notes 记一下这个上限风险**。

#### 7.2 路由切换的资源清理验证

**Codex 在开发完成后必须执行以下手动测试**：

1. 打开 Chrome DevTools → Performance Monitor
2. 进入登录页（Galaxy）→ 看 "JS heap size" 和 "GPU memory"（如果可见）
3. 跳到首页（Galaxy 第二次出现）→ 数值应该不显著增加
4. 跳到专辑页（Circular Gallery）→ 数值应该和之前差不多（前一个 Galaxy 已经 dispose）
5. 跳到 NowPlayingView（Antigravity）→ 数值应该不显著增加
6. 回到首页 → 数值不再增长

如果发现数值持续上涨 = **某个组件没清理干净**，**停下来排查**。

#### 7.3 浏览器兼容性

vue-bits 组件用 WebGL 1.0 + 基础 ES2020，**所有现代浏览器（Chrome 90+ / Edge / Firefox 89+ / Safari 14+）都支持**。Codex 不需要做 polyfill 处理。

如果用户用老电脑跑得卡：
- 第一选择：把 Antigravity 的 count 从 200 降到 100
- 第二选择：把 Galaxy 的 density 从 1.2 降到 0.8
- 第三选择：在 NowPlayingView 加 "关闭背景动画" 开关

Phase 7 不实现"低性能模式开关"，但记到 dev-notes 作为 Phase 8 可选增强。

---

### 任务 8：联调测试

**完整端到端验证流程**：

#### 8.1 桌面 1080p 屏幕

1. 启动后端 + 前端
2. **登录页**：
   - 紫色星空全屏 ✅
   - 鼠标移动星星被排斥 ✅
   - 登录卡片磨砂可见星空 ✅
   - 登录跳首页 ✅
3. **首页**：
   - Hero 区有紫色星空 ✅
   - 下方横向滚动行未受影响 ✅
4. **专辑列表页**：
   - 顶部 "FEATURED 精选专辑" 标题 + Circular Gallery 弧形画廊
   - 滚轮滚动画廊正常 ✅
   - 鼠标拖拽画廊正常 ✅
   - 真实封面图显示（Imagine Dragons Origins 这种有图的）+ 占位渐变（没图的）✅
   - 下方原有网格列表保留 ✅
5. **艺术家列表页 / 歌单列表页**：同上 ✅
6. **NowPlayingView**：
   - 点底部封面进入大图视图
   - 左侧大封面 + 紫色光晕 ✅
   - 右侧紫色 Antigravity 粒子飘动 ✅
   - 鼠标移到右侧粒子跟随 ✅
   - 波形 + 歌词卡片半透明磨砂 ✅
7. **转换工坊**：
   - 提交一个新转换任务
   - "当前任务"卡片出现紫青漩涡 Balatro 背景 ✅
   - 进度条清晰可见 ✅
   - 任务 SUCCESS 后漩涡消失 ✅

#### 8.2 资源清理验证

按任务 7.2 流程跑一遍 Performance Monitor，**确认无内存泄漏**。

#### 8.3 Console 检查

整个流程过程中浏览器 Console **不应有红色错误**。**WebGL warning 是可接受的**（vue-bits 组件可能有少量），但不能有 `TypeError` / `ReferenceError` / `Cannot read property`。

---

### 任务 9：commit

```powershell
cd D:\Claude_Playground\Soundprint
git add frontend/ docs/vue-bits-references.md
@"
feat: vue-bits 视觉爆点集成（Phase 7）

新依赖：
- ogl（Galaxy / CircularGallery / Balatro 共用 WebGL 库）
- three + @types/three（仅 Antigravity 用）

组件集成（src/components/vue-bits/）：
- backgrounds/Galaxy.vue：登录页全屏 + 首页 Hero 紫色星空（hueShift 260）
- components/CircularGallery.vue：专辑/艺术家/歌单 Hero 弧形画廊
- animations/Antigravity.vue：NowPlayingView 右侧紫色粒子
- animations/Balatro.vue：转换 RUNNING 状态紫青漩涡

页面改造：
- LoginView：全屏 Galaxy + 磨砂登录卡
- DashboardView：Hero 区叠 Galaxy 衬底
- AlbumListView / ArtistListView / PlaylistListView：两段式（Hero Gallery + 网格列表）
- NowPlayingView：右侧 Antigravity 背景，波形/歌词磨砂浮层
- StudioView：RUNNING 时 Balatro 衬底，z-index CSS deep 覆盖

支持基础设施：
- SoundprintCircularGallery 适配器：业务对象 → 画廊 items
- 占位封面 SVG dataurl
- 后端 DashboardResponse 加 featuredPlaylists 字段

配色统一：
- 所有 vue-bits 组件用 Soundprint 品牌紫青配色
- vue-bits 默认色一律不使用

性能：
- 单页同屏最多 1 个 WebGL 组件
- 所有组件 onUnmounted 完整清理 WebGL 上下文
- vue-bits 源码不动，通过 props + 父组件包装定制
- 版权注释 + vue-bits-references.md 记录所有来源
"@ | Out-File "D:\Claude_Playground\_msg.txt" -Encoding utf8NoBOM
git commit -F "D:\Claude_Playground\_msg.txt"
Remove-Item "D:\Claude_Playground\_msg.txt"
```

---

## 📚 边写边讲要求（必讲清单）

### 1. **WebGL 与 Canvas 的区别**（必讲）
WebGL 是 GPU 渲染，Canvas 2D 是 CPU 渲染。vue-bits 用 WebGL → 4K 星空也丝滑，纯 CSS 动画做不到。

### 2. **Fragment Shader 工作原理**（必讲，重点）
Galaxy / Balatro 用片段着色器，**对每个像素**计算颜色。`uHueShift` 这种参数怎么影响 shader 中的 `hsv2rgb` 转换。**这是答辩高频题**。

### 3. **OGL vs Three.js**（讲）
OGL：轻量（100KB），适合 2D shader 背景。
Three.js：完整 3D 引擎（1MB），适合 3D 粒子、模型。
**讲清楚为什么 4 个组件需要两个库共存**。

### 4. **WebGL 上下文丢失与清理**（必讲）
浏览器有上下文上限，vue-bits 组件用 `WEBGL_lose_context` 扩展主动释放。
**`onUnmounted` 不调 cleanup 会导致内存泄漏 + 上下文丢失级联**。

### 5. **OGL 的 Triangle + Fragment Shader 全屏覆盖**（讲）
Galaxy/Balatro 都用一个三角形 + fragment shader 实现"全屏渲染"。**比用 quad 节省顶点**。

### 6. **Circular Gallery 的弧形数学**（讲）
源码里的 `R = (H² + B²) / (2B)` 是圆的几何公式，根据弦长 H 和拱高 B 反推圆心半径。**这是高中数学的现代用法**。

### 7. **Backdrop-filter 与 WebGL 叠加**（讲）
登录卡 / 波形卡 用 `backdrop-filter: blur(40px)` 透过卡片看到背景星空。**为什么这种"磨砂叠加 WebGL"性能好**：blur 在 GPU 做，不卡 CPU。

### 8. **`z-index` 战争**（讲）
Balatro 源码硬编码 `z-index: 100`——我们用 `:deep(canvas) { z-index: 0 !important; }` 覆盖。**讲清楚为什么 `:deep` 是必需的**（scoped CSS 默认不渗透子组件）。

### 9. **图片 CORS 跨域问题**（讲）
Circular Gallery 用原生 `Image` 加载封面，`crossOrigin = 'anonymous'`。**配合后端 ResourceHandler 的 CORS 配置**才能避免跨域错误。

### 10. **开源协议与工程素养**（必讲，答辩亮点）
vue-bits 是 MIT + Commons Clause。**我们的做法**：
- 不修改源码（保持可追溯）
- 顶部加版权注释
- `docs/vue-bits-references.md` 单独记录
- 答辩话术："核心 shader 引用自 vue-bits，符合 MIT，做了配色定制和集成"

---

## ✅ 完成检查清单

- [ ] ogl + three + @types/three 安装完成
- [ ] vue-bits 4 个组件源码复制到 `src/components/vue-bits/`
- [ ] 每个组件顶部加版权注释
- [ ] `docs/vue-bits-references.md` 创建
- [ ] LoginView 全屏 Galaxy 紫色星空 + 磨砂卡片
- [ ] DashboardView Hero 加 Galaxy 衬底
- [ ] SoundprintCircularGallery 适配器组件
- [ ] AlbumListView Circular Gallery Hero + 网格列表两段式
- [ ] ArtistListView 同上
- [ ] PlaylistListView 同上
- [ ] 后端 DashboardResponse 加 featuredPlaylists 字段
- [ ] NowPlayingView 右侧 Antigravity 紫色粒子
- [ ] 波形 + 歌词卡片改为半透明磨砂浮层
- [ ] StudioView RUNNING 状态 Balatro 紫青漩涡
- [ ] CSS `:deep(canvas)` 覆盖 Balatro 的 z-index 强制为 0
- [ ] 所有页面测试无 Console 红色报错
- [ ] 资源清理验证（Performance Monitor 流程）
- [ ] 4 个 vue-bits 组件全部用 Soundprint 紫青配色，**vue-bits 默认色一律不使用**
- [ ] commit 完成

---

## 📩 反馈给架构师的内容

1. **登录页全屏截图**（紫色星空 + 登录卡）
2. **首页 Hero 截图**（含星空背景的问候区）
3. **专辑/艺术家/歌单页截图各一张**（顶部 Circular Gallery + 下方网格）
4. **NowPlayingView 截图**（左封面 + 右紫色粒子 + 波形 + 歌词）
5. **转换工坊 RUNNING 状态截图**（紫青漩涡背景上的进度条）
6. **资源清理验证报告**（Performance Monitor 数值前后对比，文字描述即可）
7. **任何你想微调的地方**（颜色、密度、速度等都可以再改）
8. **是否准备进 Phase 8（Docker + 实验报告 + 答辩排练）—— 最后一个 Phase**

---

## ⚠️ 注意事项

- **绝对不修改 vue-bits 组件源码逻辑**（version stability + 协议合规），只通过 props 和 CSS 定制
- **每个组件顶部必须加版权注释**，缺一个就算违反开源协议
- **vue-bits 默认配色不能保留**——必须改成 Soundprint 紫青
- **同屏不能有 2 个以上 WebGL 组件**——Codex 实装时仔细检查路由
- **WebGL 资源清理必须验证**——做 Performance Monitor 流程，发现泄漏立即停下排查
- **保护 Phase 4-6 的功能不退化**——播放、转换、统计、波形、歌词全部要继续工作
- **`application-dev.yml` 仍然不进 git**——commit 前 `git status` 验证
- **答辩准备物证**：Phase 7 完成后录一段 30 秒视频（从登录到播放到转换），**视觉冲击远胜静态截图**

---

**End of Phase 7 Document.**
