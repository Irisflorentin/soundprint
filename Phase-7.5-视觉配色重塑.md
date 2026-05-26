# Phase 7.5：视觉配色重塑（黑白银 + 香槟金）

> Soundprint 项目的视觉精修阶段。
> Codex 主力执行，每步必须停下来让用户验证。
> 本阶段完成后，Soundprint 从"紫青酷炫风"升级到"黑白银高级感"。
> 视觉气质对标 Linear / Vercel / Apple Music 深色模式 / Native Instruments。

---

## 🎯 阶段目标

将 Phase 4-7 建立的"紫色 + 青色 + 磨砂玻璃"视觉系统，**整体重塑为"炭黑 + 银白 + 香槟金点缀"**：

**新色板（最终拍板版）**：

| 角色 | 旧值 | 新值 | 说明 |
|---|---|---|---|
| 主底色 | `#0A0A14` | `#0A0A0B` | 炭黑（不是死黑）|
| 卡片底 | `#0F0F1E` | `#15151A` | 中性深灰 |
| 悬停底 | `#1A1530` | `#1F1F26` | 中性灰 |
| 主文字 | `#F5F5F7` | `#F4F5F7` | 暖银白（基本不变）|
| 次要文字 | `#A1A1AA` | `#94A3B8` | 冷银 |
| 三级文字 | `#71717A` | `#64748B` | 暗银 |
| 主品牌色 | `#7C3AED` 紫 | `#C8A862` 香槟金（点缀用）|
| 强调色 | `#06B6D4` 青 | `#CBD5E1` 浅银 |
| 状态成功 | `#22C55E` | `#A3D9B1` 哑光绿 |
| 状态危险 | `#EF4444` | `#E89090` 哑光红 |
| 状态警告 | `#F59E0B` | `#C8A862` 香槟金（用金色代替黄色）|

**关键约束**：
- **不动任何功能逻辑**——只改颜色和视觉表达
- **不删除组件**——4 个 vue-bits 组件全部保留，只改参数
- **每步验证**——Codex 改一类就停下来让用户看一眼
- **整个变更集中在一个 commit**——便于一键 revert

---

## ⚠️ 关键原则：强制验证 + 防御性改动

**Codex 必须遵守这两条铁律**：

### 铁律 1：分段验证，不要一次性改完所有文件

将整个变更拆成 **6 个验证检查点**，每个完成后**停下来明确告诉用户**：

> "已完成 [检查点 X]。请刷新浏览器查看 [具体页面]，确认 [具体观察项] 是否符合预期。回复确认后我继续下一步。"

**不要批量改完整个项目再让用户看**——这样如果出 bug，定位困难。

### 铁律 2：保护 Phase 4-7 已有功能

**所有改动不允许影响**：
- 播放器逻辑（暂停/继续/上下首/进度/音量）
- API 调用
- 路由
- 数据流
- 设计令牌结构（CSS 变量命名）—— 只改值，不改名字

如果发现某处改色会涉及功能逻辑改动，**停下来报告**，不要擅自重构。

---

## 📋 任务清单

### 任务 0：备份现状 + 创建专用分支

**这是回滚保险**。

```powershell
cd D:\Claude_Playground\Soundprint
git status

# 确认工作树干净（没有未提交的改动）
# 如果有，先 commit 或 stash

# 当前 main 应该停在 Phase 7 的最后一个 commit
git log --oneline -5

# 创建专用分支
git checkout -b phase-7.5-monochrome
```

**为什么用分支**：如果改完用户不满意，**直接切回 main 就完事**，不用 revert 一堆 commit。

---

### 任务 1：更新设计令牌（`tailwind.config.js`）

**核心改动文件**：`frontend/tailwind.config.js`

完整替换 `theme.extend.colors` 块为：

```js
colors: {
  // ============== 银白色板（主色系）==============
  silver: {
    50:  '#F4F5F7',   // 银白：主文字、Hero 大字
    100: '#E5E7EB',   // 高光银：按钮 hover
    200: '#CBD5E1',   // 浅银：强调色、focus 态
    300: '#94A3B8',   // 冷银：次要文字
    400: '#64748B',   // 暗银：占位、三级文字
    500: '#475569',   // 深暗银：禁用态
  },

  // ============== 香槟金（点缀）==============
  gold: {
    400: '#D4C5A0',   // 浅香槟（可选）
    500: '#C8A862',   // 主香槟金 ★ 全站点缀用
    600: '#A88D4E',   // 深香槟金（可选）
  },

  // ============== 炭黑色板（背景）==============
  ink: {
    950: '#0A0A0B',   // 主底（炭黑，非死黑）
    900: '#15151A',   // 卡片底
    800: '#1F1F26',   // 悬停底
    700: '#2A2A33',   // 边框
  },

  // ============== 文字（保留原命名，值更新）==============
  fg: {
    primary:   '#F4F5F7',
    secondary: '#94A3B8',
    tertiary:  '#64748B',
    disabled:  '#475569',
  },

  // ============== 兼容性保留（让旧 brand 类名不报错，但值改为银白）==============
  brand: {
    50:  '#F8FAFC',
    100: '#F1F5F9',
    200: '#E2E8F0',
    300: '#CBD5E1',
    400: '#94A3B8',
    500: '#64748B',
    600: '#F4F5F7',   // ← 原 brand-600 紫色现在改成银白（核心强调位置）
    700: '#E5E7EB',
    800: '#CBD5E1',
    900: '#94A3B8',
  },

  // 同样兼容 accent 命名（指向 gold，作为点缀强调）
  accent: {
    400: '#D4C5A0',
    500: '#C8A862',
    600: '#A88D4E',
  },

  // ============== 状态色（哑光化，去廉价感）==============
  success: {
    400: '#A3D9B1',
    500: '#86C594',
    600: '#6CAB7C',
    bg:  'rgba(163, 217, 177, 0.12)',
  },
  warning: {
    // warning 改用香槟金，语义统一
    400: '#D4C5A0',
    500: '#C8A862',
    600: '#A88D4E',
    bg:  'rgba(200, 168, 98, 0.12)',
  },
  danger: {
    400: '#E89090',
    500: '#D77676',
    600: '#B85D5D',
    bg:  'rgba(232, 144, 144, 0.12)',
  },
  info: {
    // info 改用浅银
    400: '#CBD5E1',
    500: '#94A3B8',
    600: '#64748B',
    bg:  'rgba(148, 163, 184, 0.12)',
  },
},
```

**其他不动**——`borderRadius`、`backdropBlur`、`boxShadow`、`fontFamily` 等保留。

**关键品牌渐变更新**——把 `backgroundImage.brand-gradient` 改成：

```js
backgroundImage: {
  // ... mesh 等保留 ...

  // 旧：紫到青对角线 #7C3AED → #06B6D4
  // 新：银白到冷银到香槟金，金属拉丝感
  'brand-gradient': 'linear-gradient(135deg, #F4F5F7 0%, #94A3B8 50%, #C8A862 100%)',

  // 加一个纯银渐变（不带金，给某些场景用）
  'silver-gradient': 'linear-gradient(135deg, #F4F5F7 0%, #94A3B8 100%)',
},
```

#### 🔍 检查点 1：让用户验证 Tailwind 编译

```powershell
cd frontend
npm run dev
```

打开浏览器任意页面，**看一眼**：
- 页面不报红错
- 大部分元素应该已经"看起来不一样了"——可能很多紫色变得奇怪（因为 Tailwind 的 brand-600 现在指向银白）

**这一步不要求完美**，只确认 **Tailwind 配置编译通过**。

向用户报告：
> "Tailwind 配置已更新。请刷新 localhost:5173，确认页面没有渲染崩溃（白屏）。视觉可能很奇怪，这是正常的——后续步骤会逐个修复。回复 OK 我继续。"

---

### 任务 2：更新 SCSS 变量（`tokens.scss` + `element-overrides.scss`）

#### 2.1 替换 `frontend/src/styles/tokens.scss`

完整内容替换为：

```scss
:root {
  // ============== 颜色变量 ==============
  --color-bg-base:    #0A0A0B;
  --color-bg-card:    #15151A;
  --color-bg-hover:   #1F1F26;
  --color-border:     rgba(255, 255, 255, 0.06);
  --color-border-hover: rgba(255, 255, 255, 0.12);

  // 主色（替换原紫色系）
  // 注意：保留变量名 --color-brand 以避免大规模重命名
  --color-brand:        #F4F5F7;   // 旧紫色 #7C3AED 现在指向银白
  --color-brand-hover:  #FFFFFF;
  --color-accent:       #C8A862;   // 香槟金，旧 #06B6D4 现在指向金色（点缀）

  --color-fg-primary:   #F4F5F7;
  --color-fg-secondary: #94A3B8;
  --color-fg-tertiary:  #64748B;

  // 状态色（哑光化）
  --color-success:    #A3D9B1;
  --color-success-bg: rgba(163, 217, 177, 0.12);
  --color-warning:    #C8A862;
  --color-warning-bg: rgba(200, 168, 98, 0.12);
  --color-danger:     #E89090;
  --color-danger-bg:  rgba(232, 144, 144, 0.12);
  --color-info:       #94A3B8;
  --color-info-bg:    rgba(148, 163, 184, 0.12);

  // ============== 间距系统（不动）==============
  --space-1: 4px;
  --space-2: 8px;
  --space-3: 12px;
  --space-4: 16px;
  --space-5: 24px;
  --space-6: 32px;
  --space-8: 48px;
  --space-10: 64px;

  // ============== 圆角（不动）==============
  --radius-card: 16px;
  --radius-btn:  12px;
  --radius-pill: 999px;

  // ============== 阴影（更新发光色为银色）==============
  --shadow-glass: 0 8px 32px 0 rgba(0, 0, 0, 0.45);
  // 旧紫色发光 → 改为银白微光
  --shadow-glow:  0 0 24px rgba(244, 245, 247, 0.15);
  // 香槟金发光（关键 CTA 用）
  --shadow-glow-gold: 0 0 32px rgba(200, 168, 98, 0.25);

  // ============== 动效（不动）==============
  --ease: cubic-bezier(0.4, 0, 0.2, 1);
  --duration-base: 300ms;
}
```

**关键设计**：保留 `--color-brand` / `--color-accent` 这些变量名，**仅改值**。这样所有引用这些变量的组件不用动代码。

#### 2.2 替换 `frontend/src/styles/element-overrides.scss`

完整内容替换为：

```scss
// Element-Plus 主题覆盖：从紫色品牌色改为银白
:root {
  // 主色：原 #7C3AED 紫色，改为银白色
  --el-color-primary: #F4F5F7;
  --el-color-primary-light-3: #E5E7EB;
  --el-color-primary-light-5: #CBD5E1;
  --el-color-primary-light-7: #94A3B8;
  --el-color-primary-light-9: rgba(244, 245, 247, 0.12);
  --el-color-primary-dark-2: #64748B;

  // 背景
  --el-bg-color: #15151A;
  --el-bg-color-page: #0A0A0B;
  --el-bg-color-overlay: #1F1F26;

  // 文字
  --el-text-color-primary: #F4F5F7;
  --el-text-color-regular: #94A3B8;
  --el-text-color-secondary: #64748B;
  --el-text-color-disabled: #475569;

  // 边框
  --el-border-color: rgba(255, 255, 255, 0.08);
  --el-border-color-light: rgba(255, 255, 255, 0.05);
  --el-border-color-lighter: rgba(255, 255, 255, 0.03);
  --el-border-color-extra-light: rgba(255, 255, 255, 0.02);

  // 状态色
  --el-color-success: #A3D9B1;
  --el-color-warning: #C8A862;
  --el-color-danger:  #E89090;
  --el-color-info:    #94A3B8;
}

html.dark {
  color-scheme: dark;
}

// 表格暗色微调
.el-table {
  --el-table-bg-color: transparent;
  --el-table-tr-bg-color: transparent;
  --el-table-header-bg-color: rgba(255, 255, 255, 0.02);
  // 旧紫色 hover → 改为银白微光
  --el-table-row-hover-bg-color: rgba(244, 245, 247, 0.04);
  --el-table-border-color: rgba(255, 255, 255, 0.05);
}

.el-pagination {
  --el-pagination-bg-color: transparent;
  --el-pagination-button-disabled-bg-color: transparent;
}

// 主按钮（el-button type="primary"）特殊处理：银白底炭黑字
.el-button--primary {
  --el-button-text-color: #0A0A0B;        // 炭黑字
  --el-button-bg-color: #F4F5F7;          // 银白底
  --el-button-border-color: #F4F5F7;
  --el-button-hover-text-color: #0A0A0B;
  --el-button-hover-bg-color: #FFFFFF;
  --el-button-hover-border-color: #FFFFFF;
  --el-button-active-bg-color: #E5E7EB;
  --el-button-active-border-color: #E5E7EB;
}
```

#### 2.3 全局背景更新（`styles/index.scss`）

找到 `html, body, #app` 块，把 `background: var(--color-bg-base);` 这行确认是新值（应该是 `#0A0A0B`）。**逻辑没变**，因为变量值已经在 tokens.scss 改了。

#### 🔍 检查点 2：让用户验证基础界面

向用户报告：
> "SCSS 变量已更新。请刷新浏览器，**重点确认以下页面整体颜色**：
> - 首页：主底应是炭黑，无紫色调
> - 音乐库：Element-Plus 表格 hover 行应是银白微光（不再是紫色高亮）
> - 任何按钮：主按钮应是银白底炭黑字（不再是紫色或天蓝色）
> 
> Galaxy 这时候还是紫色（任务 3 改）。Antigravity 也还是紫色（任务 3 改）。
> 这些是预期内的，下一步会改。
> 
> 报告任何看起来崩溃或者读不清字的地方。"

**等用户确认后再继续**。

---

### 任务 3：更新 vue-bits 组件参数（**不改源码**）

vue-bits 源码严格不动。**只改父组件使用时传的 props**。

#### 3.1 Galaxy（登录页 + 首页 Hero）

搜索代码里**所有 `<Galaxy` 引用**，更新参数。

**登录页 `LoginView.vue` 里的 Galaxy**：

```vue
<!-- 旧参数 -->
<Galaxy
  :hue-shift="260"
  :saturation="0.7"
  ...
/>

<!-- 新参数 -->
<Galaxy
  :hue-shift="210"
  :saturation="0.15"
  :glow-intensity="0.45"
  :twinkle-intensity="0.55"
  :density="1.2"
  :rotation-speed="0.04"
  :mouse-interaction="true"
  :mouse-repulsion="true"
  :transparent="false"
/>
```

**首页 `DashboardView.vue` 里的 Galaxy**（如果有）：

```vue
<Galaxy
  :hue-shift="210"
  :saturation="0.15"
  :glow-intensity="0.35"      <!-- 比登录页低一点，作为衬底 -->
  :twinkle-intensity="0.45"
  :density="0.9"
  :rotation-speed="0.03"
  :mouse-interaction="true"
  :transparent="true"
/>
```

**关键效果**：`saturation: 0.15` 让星星几乎是灰白色（带极轻微冷调）。**这是"高级感"的核心**。

#### 3.2 Antigravity（NowPlayingView）

搜索 `<Antigravity`，更新：

```vue
<!-- 旧 -->
<Antigravity
  color="#7C3AED"
  ...
/>

<!-- 新 -->
<Antigravity
  :count="350"
  :magnet-radius="14"
  :ring-radius="10"
  :particle-size="1.8"
  :wave-speed="0.35"
  :wave-amplitude="1.2"
  :lerp-speed="0.1"
  color="#CBD5E1"         <!-- 浅银粒子 -->
  :auto-animate="true"
  :rotation-speed="0.08"
  :depth-factor="1.4"
  :pulse-speed="2"
  particle-shape="capsule"
  :field-strength="12"
  :particle-variance="1.2"
/>
```

#### 3.3 Balatro（转换工坊 RUNNING）

搜索 `<Balatro`，更新：

```vue
<!-- 旧 -->
<Balatro
  color1="#7C3AED"
  color2="#06B6D4"
  color3="#0F0F1E"
  ...
/>

<!-- 新 -->
<Balatro
  :spin-rotation="-1.5"
  :spin-speed="5"
  color1="#F4F5F7"         <!-- 银白 -->
  color2="#94A3B8"         <!-- 冷银 -->
  color3="#0A0A0B"         <!-- 炭黑底 -->
  :contrast="2.5"
  :lighting="0.45"
  :spin-amount="0.3"
  :pixel-filter="800"
  :spin-ease="1"
  :is-rotate="true"
  :mouse-interaction="false"
/>
```

#### 3.4 Circular Gallery（专辑/艺术家/歌单）

搜索 `<CircularGallery` 或 `SoundprintCircularGallery`，确认参数：

```vue
<SoundprintCircularGallery
  :items="..."
  type="album"
  <!-- 这些参数应该已经是对的，但确认一下 -->
  <!-- 内部的 CircularGallery 应该 text-color="#F4F5F7"（银白）-->
/>
```

如果 `SoundprintCircularGallery` 内部硬编码了 `text-color`，把它从原来的可能值改为 `#F4F5F7`。

#### 🔍 检查点 3：让用户验证 vue-bits 视觉

向用户报告：
> "vue-bits 4 个组件参数已更新。请验证：
> 1. **登录页**：访问 `/login`，星空应该是**银白色**（不再是紫色），饱和度低，像哈勃望远镜照片
> 2. **首页 Hero**：星空衬底也是银白色
> 3. **专辑/艺术家/歌单列表页**：Circular Gallery 文字仍是银白
> 4. **NowPlayingView**：进入大图视图，右侧粒子应该是**浅银色**（不再是紫色）
> 5. **转换工坊**：提交一个转换任务，RUNNING 时背景漩涡应是**银白冷银配色**（不再是紫青）
> 
> 截图最让你满意/不满意的页面发我。"

**等用户确认后再继续**。

---

### 任务 4：业务组件颜色硬编码替换

虽然大多数颜色走 CSS 变量，但 Phase 4-7 期间**有一些组件直接写了 hex 值**。这一步用 grep 找出所有硬编码并替换。

#### 4.1 全局搜索硬编码紫青色

在 `frontend/src` 下执行（PowerShell 7 或 bash）：

```powershell
# 搜索旧紫色十六进制
Select-String -Path "frontend\src\**\*.vue","frontend\src\**\*.ts","frontend\src\**\*.scss" `
    -Pattern "#7C3AED|#8B5CF6|#A78BFA|#06B6D4|#22D3EE" -SimpleMatch

# 搜索旧紫色 rgba
Select-String -Path "frontend\src\**\*.vue","frontend\src\**\*.ts","frontend\src\**\*.scss" `
    -Pattern "124,\s*58,\s*237|6,\s*182,\s*212" -SimpleMatch
```

#### 4.2 替换映射表

按下表替换所有出现：

| 旧值 | 新值 | 用途 |
|---|---|---|
| `#7C3AED` | `#F4F5F7` | 原品牌紫 → 银白 |
| `#8B5CF6` | `#E5E7EB` | 原 brand-500 → 高光银 |
| `#A78BFA` | `#CBD5E1` | 原 brand-400 → 浅银 |
| `#06B6D4` | `#C8A862` | 原青色强调 → 香槟金 |
| `#22D3EE` | `#D4C5A0` | 原青色亮版 → 浅金 |
| `rgba(124, 58, 237, X)` | `rgba(244, 245, 247, X)` | 紫色透明 → 银白透明 |
| `rgba(124, 58, 237, 0.15)` | `rgba(244, 245, 247, 0.08)` | 紫色发光 → 银白微光（透明度降低）|
| `rgba(6, 182, 212, X)` | `rgba(200, 168, 98, X)` | 青色透明 → 香槟金透明 |

**特殊情况**：

- **`SoundprintLogo.vue`** 里的渐变定义：把 `#7C3AED → #06B6D4` 改为 `#F4F5F7 → #94A3B8 → #C8A862` 三色
- **品牌渐变 `linear-gradient(135deg, #7C3AED 0%, #06B6D4 100%)`**：全改为 `linear-gradient(135deg, #F4F5F7 0%, #94A3B8 50%, #C8A862 100%)`

#### 4.3 侧栏激活态特殊处理

**当前**（SidebarNav.vue）：紫色背景色块 + 紫色文字

**改为**：**移除色块背景**，加左侧 2px 香槟金竖线 + 文字加粗

修改 `SidebarNav.vue` 的 `.nav-item.active` 样式：

```scss
// 旧
&.active {
  background: rgba(124, 58, 237, 0.15);
  color: var(--color-brand);
  font-weight: 600;
}

// 新
&.active {
  background: transparent;            // 移除背景色块
  color: var(--color-fg-primary);     // 银白文字
  font-weight: 600;
  position: relative;

  // 左侧 2px 香槟金竖线
  &::before {
    content: '';
    position: absolute;
    left: 0;
    top: 8px;
    bottom: 8px;
    width: 2px;
    background: #C8A862;
    border-radius: 2px;
  }
}
```

**视觉效果**：极简，但有"被选中"的暗示。

#### 4.4 KPI 卡片顶部细线（StatsView 相关）

`KpiCard.vue` 里有几个 `accent` 变体，每个对应一种顶部渐变细线。**全部更新**：

```scss
// 旧
.kpi-card {
  &::before {
    background: linear-gradient(90deg, var(--color-brand), var(--color-accent));
  }
  &[data-accent="success"]::before { background: linear-gradient(90deg, var(--color-success), var(--color-accent)); }
  &[data-accent="warning"]::before { background: linear-gradient(90deg, var(--color-warning), var(--color-brand)); }
  &[data-accent="accent"]::before  { background: linear-gradient(90deg, var(--color-accent), var(--color-brand)); }
}

// 新（统一银白到香槟金的金属拉丝）
.kpi-card {
  &::before {
    background: linear-gradient(90deg, #F4F5F7, #94A3B8, #C8A862);
  }
  // 所有变体共用同一条银金渐变，去除色彩区分（简约高级）
  // 如果要保留区分，状态色用对应哑光色
  &[data-accent="success"]::before { background: linear-gradient(90deg, #F4F5F7, #A3D9B1); }
  &[data-accent="warning"]::before { background: linear-gradient(90deg, #F4F5F7, #C8A862); }
  &[data-accent="accent"]::before  { background: linear-gradient(90deg, #F4F5F7, #94A3B8, #C8A862); }
}
```

#### 🔍 检查点 4：让用户验证业务组件颜色

向用户报告：
> "业务组件硬编码颜色已替换。请验证：
> 1. **侧栏**：选中项左侧应该出现 **2px 香槟金细线**（不再是紫色色块）
> 2. **Logo**："Soundprint" 文字渐变现在是 **银白→冷银→香槟金** 的金属拉丝
> 3. **顶栏头像**：圆形头像渐变也是金属拉丝色
> 4. **KPI 卡片**：顶部细线变成银白→银→金的渐变
> 5. **音乐库表格**：行 hover 银白微光
> 6. **转换工坊状态徽标**：SUCCESS 哑光绿、FAILED 哑光红、RUNNING 浅银
> 
> 截图任何感觉不对的地方。"

**等用户确认后再继续**。

---

### 任务 5：更新 ECharts 主题（统计页 5 个图表）

`frontend/src/utils/echarts-theme.ts` 完整替换 `SOUNDPRINT_THEME` 中相关字段：

```ts
export const SOUNDPRINT_THEME = {
  // 调色板：5 个色（银白系 + 香槟金）
  color: [
    '#F4F5F7',  // 银白：主数据
    '#94A3B8',  // 冷银：第二
    '#CBD5E1',  // 浅银：第三
    '#64748B',  // 暗银：第四
    '#C8A862',  // 香槟金：重点突出
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
    borderColor: 'rgba(200, 168, 98, 0.4)',      // 香槟金细描边
    borderWidth: 1,
    textStyle: { color: '#F4F5F7', fontSize: 12 },
    extraCssText: 'backdrop-filter: blur(20px); box-shadow: 0 8px 32px rgba(0,0,0,0.5);',
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

  // visualMap 渐变（热力图配色）
  visualMap: {
    textStyle: { color: '#94A3B8' },
    inRange: {
      // 旧紫色梯度 → 新银金梯度
      color: ['#15151A', '#2A2A33', '#64748B', '#94A3B8', '#C8A862'],
      // 黑 → 暗银 → 银 → 香槟金（最热的天是金色）
    },
  },
};
```

#### 单图表配置修改

某些图表里**直接传了 itemStyle.color 硬编码**（绕过主题），需要单独修改：

**GenrePieChart.vue**：
```ts
// 旧 emphasis
emphasis: {
  scale: true,
  scaleSize: 8,
  itemStyle: { shadowBlur: 24, shadowColor: 'rgba(124,58,237,0.4)' },
},

// 新
emphasis: {
  scale: true,
  scaleSize: 8,
  itemStyle: { shadowBlur: 24, shadowColor: 'rgba(200, 168, 98, 0.5)' },  // 香槟金光晕
},
```

**TopArtistsBarChart.vue**：
```ts
// 旧
color: {
  type: 'linear',
  x: 0, y: 0, x2: 1, y2: 0,
  colorStops: [
    { offset: 0, color: '#7C3AED' },
    { offset: 1, color: '#06B6D4' },
  ],
},

// 新（银白渐变到香槟金）
color: {
  type: 'linear',
  x: 0, y: 0, x2: 1, y2: 0,
  colorStops: [
    { offset: 0, color: '#F4F5F7' },
    { offset: 1, color: '#C8A862' },
  ],
},
```

**MonthlyTrendChart.vue**：
```ts
// 旧 lineStyle.color
{ offset: 0, color: '#7C3AED' },
{ offset: 1, color: '#06B6D4' },

// 新（银白渐变）
{ offset: 0, color: '#F4F5F7' },
{ offset: 1, color: '#94A3B8' },

// 旧 areaStyle.color
{ offset: 0, color: 'rgba(124,58,237,0.4)' },
{ offset: 1, color: 'rgba(124,58,237,0)' },

// 新（银白透明渐变）
{ offset: 0, color: 'rgba(244, 245, 247, 0.25)' },
{ offset: 1, color: 'rgba(244, 245, 247, 0)' },

// 旧 itemStyle.color
color: '#7C3AED',

// 新
color: '#F4F5F7',
```

**PlayHeatmap.vue**：
```ts
// 旧 visualMap.pieces
pieces: [
  { min: 0, max: 0, color: 'rgba(255,255,255,0.04)' },
  { min: 1, max: 2, color: '#1A1530' },
  { min: 3, max: 5, color: '#5B21B6' },
  { min: 6, max: 10, color: '#7C3AED' },
  { min: 11, max: 9999, color: '#A78BFA' },
],

// 新（银金梯度，最热的几天是香槟金）
pieces: [
  { min: 0, max: 0,    color: 'rgba(255,255,255,0.03)' },  // 无播放
  { min: 1, max: 2,    color: '#2A2A33' },                  // 暗银
  { min: 3, max: 5,    color: '#64748B' },                  // 中银
  { min: 6, max: 10,   color: '#94A3B8' },                  // 浅银
  { min: 11, max: 9999, color: '#C8A862' },                 // 香槟金（最热）
],
```

**这是 Phase 7.5 的视觉点睛之笔**——热力图最活跃的日子用香槟金标出，**像"金牌日"**。

#### 🔍 检查点 5：让用户验证统计页

向用户报告：
> "ECharts 主题已更新。请访问 `/stats` 页面验证：
> 1. **KPI 卡片**顶部细线银白→银→金渐变
> 2. **流派饼图**：5 种银 + 香槟金的环形
> 3. **Top 艺术家条形图**：银白渐变到香槟金（不再是紫到青）
> 4. **月度趋势折线**：银白线 + 银白渐变面积（不再是紫色）
> 5. **365 天热力图**：黑 → 暗银 → 银 → 香槟金，**最活跃的几天应该是金色**（金牌日效果）
> 6. **任何 tooltip** 应该有香槟金细描边
> 
> 这是 Phase 7.5 的视觉点睛之笔。截图发我。"

**等用户确认后再继续**。

---

### 任务 6：最终全局视觉检查 + commit

#### 6.1 全局扫描遗漏

再跑一次硬编码扫描：

```powershell
Select-String -Path "frontend\src\**\*" -Pattern "#7C3AED|#06B6D4|#8B5CF6|#A78BFA|#22D3EE" -SimpleMatch
```

**结果应该为空**或者只有几个零星的（确认每一处是不是真的不需要改）。

#### 6.2 检查 NowPlayingView 的封面光晕

`NowPlayingView.vue` 或者 `WaveformDisplay.vue` 里可能有：

```scss
box-shadow: 0 0 80px rgba(124, 58, 237, 0.2);   /* 旧紫光晕 */
```

改为：

```scss
box-shadow: 0 0 80px rgba(244, 245, 247, 0.15);  /* 银白光晕 */
```

`WaveformDisplay.vue` 里的波形配色：

```ts
// 旧
waveColor: 'rgba(124, 58, 237, 0.4)',
progressColor: '#7C3AED',
cursorColor: '#06B6D4',

// 新
waveColor: 'rgba(244, 245, 247, 0.4)',
progressColor: '#F4F5F7',
cursorColor: '#C8A862',          // 进度光标用香槟金，跳眼
```

`LyricsPanel.vue` 当前歌词行的发光：

```scss
// 旧
text-shadow: 0 0 24px rgba(124, 58, 237, 0.5);

// 新
text-shadow: 0 0 24px rgba(244, 245, 247, 0.4);
```

#### 6.3 整体走查

按下列顺序**全部访问一遍**，检查：

- [ ] `/login`：银白星空 + 银白磨砂卡 + 银白底炭黑字按钮
- [ ] `/`：首页问候区银白星空衬底，下方专辑封面正常
- [ ] `/library`：表格、搜索框、分页都是冷调
- [ ] `/albums` / `/artists` / `/playlists`：Circular Gallery 银白文字
- [ ] `/playlists/X`：歌单详情拖拽正常
- [ ] `/studio`：转换工坊状态色冷调
- [ ] `/stats`：5 个图表银金配色
- [ ] NowPlayingView：右侧浅银粒子 + 波形 + 歌词

**Console 检查**：F12 看有没有红色错误。

**Performance Monitor 检查**（防内存泄漏）：
1. 打开 Chrome DevTools → Performance Monitor
2. 在 `/login`、`/`、`/now-playing`、`/studio` 之间快速切换 10 次
3. 看 JS heap size 不持续上涨

#### 6.4 commit + push

```powershell
cd D:\Claude_Playground\Soundprint
git add frontend/
git status   # 确认没误添加东西

@"
refactor: 视觉配色重塑为黑白银+香槟金（Phase 7.5）

设计令牌：
- 主底 #0A0A0B 炭黑、卡片 #15151A、悬停 #1F1F26
- 文字 #F4F5F7 银白 / #94A3B8 冷银 / #64748B 暗银
- 强调色：原 #7C3AED 紫色 → #F4F5F7 银白
- 点缀色：原 #06B6D4 青色 → #C8A862 香槟金
- 状态色哑光化：success #A3D9B1, danger #E89090, warning #C8A862

vue-bits 组件参数：
- Galaxy: hueShift 210 + saturation 0.15（冷银白星空）
- Antigravity: color #CBD5E1（浅银粒子）
- Balatro: 三色全部改成 银/冷银/炭黑
- CircularGallery: textColor #F4F5F7（不变）

业务组件：
- 全局硬编码紫青色全部替换
- 侧栏激活态：紫色色块 → 2px 香槟金竖线
- 品牌渐变：紫青对角 → 银白冷银金三色拉丝
- KPI 卡片顶部细线、Logo 渐变、头像渐变 全部更新

统计图表：
- echarts-theme 调色板五色（4 银 + 香槟金）
- 365 热力图：黑 → 暗银 → 银 → 浅银 → 香槟金（金牌日）
- 折线、条形、饼图渐变全部银金化

播放器：
- 波形 waveColor / progressColor 银白
- 波形光标 cursorColor 香槟金（视觉点睛）
- 当前歌词高亮发光改为银白

视觉气质对标 Linear / Vercel / Apple Music 深色模式
"@ | Out-File "D:\Claude_Playground\_msg.txt" -Encoding utf8NoBOM

git commit -F "D:\Claude_Playground\_msg.txt"
Remove-Item "D:\Claude_Playground\_msg.txt"

# 这个分支暂时不 push，等用户最终决定满意了再合并到 main
```

#### 🔍 检查点 6：让用户最终验收

向用户报告：
> "Phase 7.5 全部完成。当前在 `phase-7.5-monochrome` 分支，commit hash: [xxx]。
> 
> **请完整体验一遍 Soundprint**：
> 1. 登录 → 首页 → 库 → 专辑 → 艺术家 → 歌单 → 工坊 → 统计 → 播放
> 2. 截图任何**最让你满意的页面**和**任何感觉不对的地方**
> 
> **回滚预案**（如果不满意）：
> ```powershell
> git checkout main           # 回到 Phase 7 紫青风
> git branch -D phase-7.5-monochrome   # 删除当前分支
> ```
> 
> **采纳预案**（如果满意）：
> ```powershell
> git checkout main
> git merge phase-7.5-monochrome
> git push
> ```
> 
> 请告诉我下一步。"

---

## 📚 边讲边写（必讲技术点）

Phase 7.5 涉及的技术点（答辩素材）：

### 1. **设计系统重塑（必讲）**
- 设计令牌的价值：改一处全站统一
- 为什么不引入"两套主题"而是"重塑唯一主题"——工程经济考虑

### 2. **饱和度 vs 色相 vs 明度 三要素（讲）**
- Galaxy 改色：`saturation: 0.15` 比 `hueShift: 0` 更关键
- 高级感的色彩学定义：低饱和 + 适度对比

### 3. **状态色哑光化（讲）**
- 鲜艳的 `#22C55E` 改成 `#A3D9B1` 哑光绿——降低纯度提升高级感
- 同理 warning 用香槟金代替黄色，**色彩语义**仍然正确

### 4. **品牌色策略变化（讲）**
- Phase 7：双品牌色（紫 + 青）
- Phase 7.5：单品牌色 + 强调色（银白主调 + 香槟金点缀）
- **单一强调色 = 高级感**的设计原则

### 5. **侧栏激活态从"色块"改"细线"（讲）**
- 色块 = 直接、强力但显廉价
- 细线 = 克制、暗示、高级
- 这是 Linear / Notion 这类产品的标准做法

### 6. **金色稀缺原则（讲）**
- 整个 UI 框架金色出现 ≤ 5 处：
  1. Logo 渐变末端
  2. 侧栏激活竖线
  3. KPI 卡片细线尾端
  4. 热力图最活跃日
  5. 波形播放光标
- **稀缺 = 珍贵**，到处用 = 廉价

### 7. **Git 分支隔离风险（必讲）**
- 大改动用专用分支
- 不满意一键回滚
- 这是真实工程实践

---

## ✅ 完成检查清单

- [ ] 任务 0：创建 `phase-7.5-monochrome` 分支
- [ ] 任务 1：tailwind.config.js 色板更新
- [ ] 任务 2：tokens.scss + element-overrides.scss 更新
- [ ] 任务 3.1：Galaxy 参数（hueShift 210 + saturation 0.15）
- [ ] 任务 3.2：Antigravity color #CBD5E1
- [ ] 任务 3.3:Balatro 三色银白冷银炭黑
- [ ] 任务 3.4：Circular Gallery 文字色确认
- [ ] 任务 4.1-4.2：全局硬编码替换
- [ ] 任务 4.3：侧栏激活态改为 2px 香槟金竖线
- [ ] 任务 4.4：KPI 卡片顶部细线更新
- [ ] 任务 5：ECharts 主题更新
- [ ] 任务 5：单图表配色更新（饼图 emphasis、条形渐变、折线渐变、热力图 pieces）
- [ ] 任务 6.1：全局扫描遗漏紫青色（结果应为空）
- [ ] 任务 6.2：NowPlayingView / Waveform / Lyrics 配色更新
- [ ] 任务 6.3：8 个页面 + Console + Performance Monitor 全部检查
- [ ] commit 到 `phase-7.5-monochrome` 分支

---

## 📩 反馈给架构师的内容

每个检查点结束都要回报，最终汇总：

1. **登录页截图**（银白星空 + 磨砂卡）
2. **首页截图**（Hero 区银白星空 + 真实专辑封面）
3. **统计页全屏截图**（5 个图表 + KPI 卡片）
4. **NowPlayingView 截图**（左大封面 + 右浅银粒子 + 银白波形）
5. **转换工坊 RUNNING 截图**（Balatro 银白漩涡）
6. **侧栏激活态特写**（2px 香槟金竖线）
7. **commit hash + 是否满意采纳**

---

## ⚠️ 注意事项

- **每个检查点必须停下来等用户确认**，不要批量改完所有任务
- **vue-bits 源码严格不动**——只改父组件传的 props
- **CSS 变量名严格保留**——只改值，不改名（`--color-brand` 仍然存在，只是现在值是银白）
- **如果发现某处改色涉及功能逻辑改动**，停下来报告
- **不要 push 这个分支到远端**——等用户最终决定满意才合并到 main
- **保留所有 Phase 4-7 功能**：播放、转换、统计、波形、歌词
- **如果某个 vue-bits 组件参数改完后 WebGL 报错**，把参数改回旧值并报告——可能是该组件不支持新色域

---

**End of Phase 7.5 Document.**
