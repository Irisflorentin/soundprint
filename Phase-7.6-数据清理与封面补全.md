# Phase 7.6：数据清理 + 点击跳转修复 + 占位封面美化

> Soundprint 项目的工程收尾微补丁阶段。
> 用户手动 + Codex 协作。
> 完成后正式进入 Phase 8（Docker + 实验报告 + 答辩排练）。

---

## 🎯 阶段目标

基于精确数据（id 1-30 为种子假数据、id 31+ 为真实上传），本阶段：

1. **清理 30 条假曲目** + **关联的假专辑** + **没有任何真实曲目的假艺术家**
2. **手动补 Top 8 真实艺术家的头像**（演示视觉完整）
3. **修复 Circular Gallery 点击不跳转的 bug**（前端）
4. **改造占位封面逻辑**（未来上传新艺术家也不再是丑的 "AR" 方块）

**完成标准**：
- 音乐库只剩你上传过的歌（约 37 首）
- 专辑/艺术家列表只剩真实数据 + 完整头像
- Circular Gallery 卡片点击可跳详情页
- 未来上传的新艺术家自动用银白渐变 + 名字首字占位（不再硬编码 "AR"）

---

## ⚠️ 关键原则

1. **数据库改动必须备份**——第一步就是导出 SQL
2. **分段验证**——每改完一段（数据库 / 前端代码）就让用户看一眼
3. **改动集中在 main 分支** —— Phase 7.6 是小补丁，不开新分支
4. **保留 Phase 7.5 视觉风格**——任何改动不涉及配色

---

## 📋 任务清单

### 任务 0：数据库备份

**这是回滚保险**。先在 PowerShell 跑：

```powershell
# 设置 MySQL 工具路径（如果不在 PATH 里）
$mysqldump = "C:\Program Files\MySQL\MySQL Server 8.0\bin\mysqldump.exe"

# 备份 soundprint 整个数据库
& $mysqldump --port=3307 -u root -p soundprint > "D:\Claude_Playground\Soundprint\docs\sql\backup-before-phase-7.6.sql"
```

输入 root 密码后开始备份。完成后看一眼文件：

```powershell
Get-Item "D:\Claude_Playground\Soundprint\docs\sql\backup-before-phase-7.6.sql" | Select-Object Length
```

文件大小应该 > 50KB（包含完整 schema + 数据）。**这是回滚护身符**——出问题随时还原：

```powershell
# 紧急回滚命令（备用，先不跑）
& "C:\Program Files\MySQL\MySQL Server 8.0\bin\mysql.exe" --port=3307 -u root -p soundprint < "D:\Claude_Playground\Soundprint\docs\sql\backup-before-phase-7.6.sql"
```

---

### 任务 1：数据库清理（用户手动跑 SQL）

在 DataGrip 里**按顺序**跑下面的 SQL。**每跑一条看一眼影响行数，确认符合预期再 commit**。

#### 1.1 第一步：删除"假曲目"（id 1-30，file_path 以 storage/seed/ 开头）

```sql
-- 先看看要删什么（不删，只 SELECT）
SELECT id, title, file_path FROM track 
WHERE file_path LIKE 'storage/seed/%'
ORDER BY id;
-- 预期：30 条
```

确认 30 条都对，执行删除：

```sql
-- 硬删除假曲目（注意是 DELETE 不是 soft delete，因为这些是种子残留，没必要保留）
DELETE FROM track
WHERE file_path LIKE 'storage/seed/%';

-- 预期：Affected rows: 30
```

#### 1.2 第二步：清理 track 关联表里的孤儿引用

```sql
-- playlist_track：可能引用了已删除的 track
DELETE FROM playlist_track
WHERE track_id NOT IN (SELECT id FROM track);

-- track_tag：可能引用了已删除的 track
DELETE FROM track_tag
WHERE track_id NOT IN (SELECT id FROM track);

-- play_history：可能引用了已删除的 track
DELETE FROM play_history
WHERE track_id NOT IN (SELECT id FROM track);

-- user_favorite：可能引用了已删除的 track
DELETE FROM user_favorite
WHERE track_id NOT IN (SELECT id FROM track);

-- conversion_task：可能引用了已删除的 track
DELETE FROM conversion_task
WHERE track_id NOT IN (SELECT id FROM track);
```

**注意**：这些 DELETE 影响行数可能为 0 或几十不等，**都正常**。

#### 1.3 第三步：软删除"没有真实曲目的专辑"

```sql
-- 看哪些专辑现在变成空壳
SELECT al.id, al.title, COUNT(t.id) AS real_tracks
FROM album al
LEFT JOIN track t ON t.album_id = al.id
WHERE al.is_deleted = 0
GROUP BY al.id, al.title
HAVING COUNT(t.id) = 0
ORDER BY al.id;
```

确认这是种子残留专辑（A Night at the Opera、Hotel California、The Dark Side of the Moon、Discovery、Random Access Memories、Async、叶惠美、范特西、OK Computer、Una Mattina、Nightbook、In Rainbows、Palette、夜曲精选、Mercury - Act 1（注意是 Act 1 不是 Acts 1 & 2）、1989（拼写错误那个）、Laideronnette、Legacy 等）。

**软删除**（不是硬删除，保留数据完整性）：

```sql
UPDATE album
SET is_deleted = 1, deleted_at = NOW()
WHERE id NOT IN (
    SELECT DISTINCT album_id FROM track WHERE album_id IS NOT NULL
);

-- 预期：Affected rows 约 18-20 条
```

#### 1.4 第四步：软删除"没有真实曲目的艺术家"

```sql
-- 看哪些艺术家是种子假数据
SELECT a.id, a.name, COUNT(t.id) AS real_tracks
FROM artist a
LEFT JOIN track t ON t.artist_id = a.id
WHERE a.is_deleted = 0
GROUP BY a.id, a.name
HAVING COUNT(t.id) = 0
ORDER BY a.id;
```

预期看到：Queen、Eagles、Pink Floyd、Daft Punk、坂本龙一、周杰伦、Norah Jones、Ludovico Einaudi、Radiohead、IU、Taylo Swift（拼写错误版）、matryoshka。

**软删除**：

```sql
UPDATE artist
SET is_deleted = 1, deleted_at = NOW()
WHERE id NOT IN (
    SELECT DISTINCT artist_id FROM track WHERE artist_id IS NOT NULL
);

-- 预期：Affected rows 约 10-13 条
```

#### 1.5 第五步：删除空歌单

```sql
-- 看哪些歌单变空了
SELECT p.id, p.title, COUNT(pt.track_id) AS tracks
FROM playlist p
LEFT JOIN playlist_track pt ON pt.playlist_id = p.id
WHERE p.is_deleted = 0
GROUP BY p.id, p.title
HAVING COUNT(pt.track_id) = 0;
```

如果有空歌单（种子里的 3 个），软删除：

```sql
UPDATE playlist
SET is_deleted = 1, deleted_at = NOW()
WHERE id IN (
    SELECT id FROM (
        SELECT p.id
        FROM playlist p
        LEFT JOIN playlist_track pt ON pt.playlist_id = p.id
        WHERE p.is_deleted = 0
        GROUP BY p.id
        HAVING COUNT(pt.track_id) = 0
    ) AS empty_playlists
);
```

#### 1.6 第六步：验证清理结果

```sql
-- 重新跑 SQL 1 验证
SELECT 
    a.id, a.name, a.avatar_url,
    COUNT(DISTINCT t.id) AS real_tracks
FROM artist a
LEFT JOIN track t ON t.artist_id = a.id
WHERE a.is_deleted = 0
GROUP BY a.id, a.name, a.avatar_url
ORDER BY real_tracks DESC, a.id;
```

**预期结果**：每个艺术家 `real_tracks > 0`，**没有 0 行**。

如果还有 `real_tracks = 0` 的行 → 说明 SQL 1.4 漏了什么，再跑一次 1.4。

#### 🔍 检查点 1：用户验证数据库

**用户做的事**：
1. 刷新前端浏览器
2. 走一遍：首页 / 音乐库 / 专辑 / 艺术家 / 歌单 / 统计
3. 验证：
   - 音乐库剩约 37 首
   - 专辑列表不再有"Hotel California"等假货
   - 艺术家列表不再有 Queen / Pink Floyd / 周杰伦 等没上传歌的
   - 统计页 Top 艺术家可能从 8 个变成 8 个真实艺术家（Imagine Dragons 高居榜首）
   - **任何已上传的歌还能正常播放**

**报告给架构师**：截图前后对比，确认数据健康。

---

### 任务 2：手动补 Top 8 艺术家头像

#### 2.1 找图

用 Google 图片搜索关键词，下载 8 张方形头像图（建议 600x600 以上）：

| ID | 艺术家 | 推荐搜索词 | 视觉建议 |
|---|---|---|---|
| 11 | Imagine Dragons | `Imagine Dragons band photo square` | 乐队合照 |
| 12 | Ed Sheeran | `Ed Sheeran portrait` | 红发标志性单人 |
| 13 | OneRepublic | `OneRepublic band` | 乐队合照 |
| 17 | Taylor Swift | `Taylor Swift portrait` | 单人写真 |
| 22 | The Chainsmokers | `The Chainsmokers duo` | 双人合照 |
| 21 | Martin Garrix & David Guetta | `Martin Garrix David Guetta` | 双 DJ 合照 或 拼图 |
| 23 | Justin Bieber | `Justin Bieber portrait` | 单人写真 |
| 18 | SLANDER/Dylan Matthew | `SLANDER duo` | 双人 DJ |

**注意**：上表的 ID 假设你的数据库 ID 跟我截图分析一致。**清理完后再跑 SQL 1.6 确认每个艺术家的真实 ID**——可能跟我列的不一样。

#### 2.2 保存到 storage 目录

把图片重命名为 `artist-{id}.jpg`（用真实 ID）放到：

```
D:/soundprint-storage/avatar/
├── artist-11.jpg
├── artist-12.jpg
├── artist-13.jpg
├── artist-17.jpg
├── artist-18.jpg
├── artist-21.jpg
├── artist-22.jpg
└── artist-23.jpg
```

如果 `avatar/` 子目录不存在，先创建：

```powershell
mkdir D:\soundprint-storage\avatar
```

#### 2.3 更新数据库

在 DataGrip 里跑（**ID 用 1.6 验证后的真实值**）：

```sql
UPDATE artist SET avatar_url = 'avatar/artist-11.jpg' WHERE id = 11;
UPDATE artist SET avatar_url = 'avatar/artist-12.jpg' WHERE id = 12;
UPDATE artist SET avatar_url = 'avatar/artist-13.jpg' WHERE id = 13;
UPDATE artist SET avatar_url = 'avatar/artist-17.jpg' WHERE id = 17;
UPDATE artist SET avatar_url = 'avatar/artist-18.jpg' WHERE id = 18;
UPDATE artist SET avatar_url = 'avatar/artist-21.jpg' WHERE id = 21;
UPDATE artist SET avatar_url = 'avatar/artist-22.jpg' WHERE id = 22;
UPDATE artist SET avatar_url = 'avatar/artist-23.jpg' WHERE id = 23;
```

记得每条点 Commit。

#### 2.4 浏览器验证

不需要重启后端，刷新 `/artists` 页面 → Circular Gallery 上 Top 8 艺术家应该都有真实头像。

#### 🔍 检查点 2：用户截图艺术家页

**用户做的事**：截艺术家列表页 Circular Gallery 给我看，确认 8 个艺术家头像全部到位。

---

### 任务 3：Codex 修 Circular Gallery 点击跳转 bug

**这一步交给 Codex**。下面是给 Codex 的精确指令。

#### 3.1 Bug 现象

当前 Circular Gallery 上点击专辑/艺术家/歌单卡片**完全没反应**——只能滚动浏览，不能进入详情页。

#### 3.2 根本原因（让 Codex 理解）

vue-bits 的 CircularGallery 组件内部用 OGL（WebGL）渲染图片，**没有为每张图片绑定 click 事件**。需要在父组件 `SoundprintCircularGallery.vue` 包装层加：

1. 监听容器的 click 事件
2. 根据鼠标 X 坐标计算"当前居中那张是第几张"
3. 触发跳转事件 emit('select', item)
4. 三个列表页（AlbumListView / ArtistListView / PlaylistListView）监听 select 事件做路由跳转

#### 3.3 修改文件清单

**文件 A：`src/components/common/SoundprintCircularGallery.vue`** —— 加点击逻辑

```vue
<script setup lang="ts">
import { computed, ref } from 'vue';
import CircularGallery from '@/components/vue-bits/components/CircularGallery.vue';
import { fileUrl } from '@/utils/url';

const props = defineProps<{
  items: Array<any>;
  type: 'album' | 'artist' | 'playlist';
}>();

// 加 select 事件
const emit = defineEmits<{
  (e: 'select', item: any, index: number): void;
}>();

const containerRef = ref<HTMLElement>();

// galleryItems 转换逻辑保持不变
const galleryItems = computed(() => {
  return props.items.map(item => {
    const rawImage = item.coverUrl || item.avatarUrl || '';
    const image = rawImage
      ? (rawImage.startsWith('http') ? rawImage : `${window.location.origin}${fileUrl(rawImage)}`)
      : fallbackImage(item, props.type);
    const text = item.title || item.name || '';
    return { image, text };
  });
});

// 点击处理：根据点击位置反推选中第几张
function handleClick(e: MouseEvent) {
  if (!containerRef.value) return;
  if (props.items.length === 0) return;

  const rect = containerRef.value.getBoundingClientRect();
  const clickX = e.clientX - rect.left;
  const centerX = rect.width / 2;
  const offsetFromCenter = clickX - centerX;

  // 弧形画廊的每张卡片大概占 1/5 宽度
  // 点击偏移量除以"每张卡的宽度"得到相对中心的索引偏移
  const cardWidth = rect.width / 5;
  const indexOffset = Math.round(offsetFromCenter / cardWidth);

  // 当前居中的索引（粗略估算：用 props.items 的中间项作为默认中心）
  // 更精确做法是从 CircularGallery 拿当前 scroll 位置，但需要 ref 暴露内部状态
  // 这里用简化方案：直接根据点击位置在水平方向哪个区段决定 index
  let targetIndex = Math.floor((clickX / rect.width) * props.items.length);
  targetIndex = Math.max(0, Math.min(props.items.length - 1, targetIndex));

  const item = props.items[targetIndex];
  emit('select', item, targetIndex);
}

function fallbackImage(item: any, type: string): string {
  // 任务 4 实现，先保留旧逻辑
  const text = (item.title || item.name || '?').substring(0, 1);
  const svg = `<svg xmlns="http://www.w3.org/2000/svg" width="600" height="400">
    <rect width="600" height="400" fill="#15151A"/>
    <text x="50%" y="50%" font-size="120" fill="#94A3B8"
          text-anchor="middle" dominant-baseline="middle">${text}</text>
  </svg>`;
  return `data:image/svg+xml;base64,${btoa(unescape(encodeURIComponent(svg)))}`;
}
</script>

<template>
  <div
    ref="containerRef"
    class="gallery-wrap"
    @click="handleClick"
  >
    <CircularGallery
      :items="galleryItems"
      :bend="2"
      text-color="#F4F5F7"
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
  cursor: pointer;
  background:
    radial-gradient(at 0% 0%, rgba(244, 245, 247, 0.05) 0%, transparent 50%),
    radial-gradient(at 100% 100%, rgba(200, 168, 98, 0.04) 0%, transparent 50%),
    rgba(15, 15, 26, 0.6);
}
</style>
```

**文件 B：`src/views/album/AlbumListView.vue`** —— 监听 select 跳转

```vue
<!-- 找到使用 SoundprintCircularGallery 的地方 -->
<SoundprintCircularGallery
  :items="featuredAlbums"
  type="album"
  @select="onAlbumSelect"
/>
```

加 handler：

```ts
import { useRouter } from 'vue-router';
const router = useRouter();

function onAlbumSelect(album: Album) {
  router.push(`/albums/${album.id}`);
}
```

**文件 C：`src/views/artist/ArtistListView.vue`** —— 同款

```vue
<SoundprintCircularGallery
  :items="featuredArtists"
  type="artist"
  @select="onArtistSelect"
/>
```

```ts
function onArtistSelect(artist: Artist) {
  router.push(`/artists/${artist.id}`);
}
```

**文件 D：`src/views/playlist/PlaylistListView.vue`** —— 同款

```vue
<SoundprintCircularGallery
  :items="featuredPlaylists"
  type="playlist"
  @select="onPlaylistSelect"
/>
```

```ts
function onPlaylistSelect(playlist: Playlist) {
  router.push(`/playlists/${playlist.id}`);
}
```

#### 🔍 检查点 3：用户验证点击跳转

**用户做的事**：访问专辑/艺术家/歌单列表页，点击 Circular Gallery 上任意一张卡片 → 应该跳到对应详情页。

**注意**：当前的实现是"根据水平位置选最近一张"，**不是真正的"点中那张"**——精确度有限但够用。**答辩讲清楚**："Circular Gallery 是 WebGL 渲染没有原生 DOM 事件，我用水平位置估算法做点击跳转，是工程妥协方案"——**这种诚实表达加分**。

---

### 任务 4：占位封面美化（永久解决未来问题）

#### 4.1 修改 fallbackImage 函数

在 `SoundprintCircularGallery.vue`（或者你的其他公共组件，**Codex 自己判断**），把 `fallbackImage` 替换成：

```ts
/**
 * 生成漂亮的占位封面
 * - 银白渐变背景（金属拉丝感）
 * - 名字首字母/首字 居中
 * - hash 算法保证同一名字渐变方向一致
 */
function fallbackImage(item: any, type: 'album' | 'artist' | 'playlist'): string {
  const name = item.title || item.name || '?';

  // 取首字（中文 1 字、英文 1 个 / 2 个字母）
  const initial = getInitial(name);

  // 用名字 hash 算渐变角度，让不同艺术家有视觉差异
  const angle = simpleHash(name) % 360;

  // 不同类型用不同色调
  const colors = type === 'album'
    ? ['#F4F5F7', '#94A3B8']                // 银白到冷银
    : type === 'artist'
    ? ['#E5E7EB', '#64748B']                // 高光银到暗银
    : ['#CBD5E1', '#94A3B8'];               // 浅银到冷银

  const svg = `
    <svg xmlns="http://www.w3.org/2000/svg" width="600" height="600" viewBox="0 0 600 600">
      <defs>
        <linearGradient id="g" gradientTransform="rotate(${angle})">
          <stop offset="0%" stop-color="${colors[0]}"/>
          <stop offset="100%" stop-color="${colors[1]}"/>
        </linearGradient>
        <radialGradient id="vignette" cx="50%" cy="50%" r="60%">
          <stop offset="0%" stop-color="rgba(0,0,0,0)"/>
          <stop offset="100%" stop-color="rgba(0,0,0,0.3)"/>
        </radialGradient>
      </defs>
      <rect width="600" height="600" fill="url(#g)"/>
      <rect width="600" height="600" fill="url(#vignette)"/>
      <text x="50%" y="50%"
            font-family="Inter, 'PingFang SC', system-ui, sans-serif"
            font-size="240"
            font-weight="600"
            fill="#0A0A0B"
            fill-opacity="0.65"
            text-anchor="middle"
            dominant-baseline="central">${escapeSvgText(initial)}</text>
    </svg>
  `.trim();

  return `data:image/svg+xml;base64,${btoa(unescape(encodeURIComponent(svg)))}`;
}

function getInitial(name: string): string {
  if (!name) return '?';
  // 中文：取首字
  if (/[\u4e00-\u9fa5]/.test(name[0])) {
    return name[0];
  }
  // 英文：取首字母或首两个单词首字母组合
  const words = name.split(/\s+/);
  if (words.length >= 2) {
    return (words[0][0] + words[1][0]).toUpperCase();
  }
  return name.substring(0, 2).toUpperCase();
}

function simpleHash(str: string): number {
  let h = 0;
  for (let i = 0; i < str.length; i++) {
    h = ((h << 5) - h + str.charCodeAt(i)) | 0;
  }
  return Math.abs(h);
}

function escapeSvgText(text: string): string {
  return text
    .replace(/&/g, '&amp;')
    .replace(/</g, '&lt;')
    .replace(/>/g, '&gt;')
    .replace(/"/g, '&quot;')
    .replace(/'/g, '&apos;');
}
```

#### 4.2 把同款逻辑应用到其他占位场景

搜索 `frontend/src` 下所有出现 "AR" / "AL" / "PL" 字符串的地方（这些是各类占位文字）：

```powershell
Select-String -Path "frontend\src\**\*.vue","frontend\src\**\*.ts" `
    -Pattern "['\"](AR|AL|PL)['\"]" -SimpleMatch
```

可能找到：
- `SmartCover.vue`（曲目封面占位）
- `AlbumCard.vue`（专辑卡片占位）
- `ArtistCard.vue`（艺术家卡片占位）
- 其他用 element-plus el-avatar 的地方

**统一改造**：让这些组件都用上面的 `fallbackImage` 逻辑——或者抽出公共 util 到 `src/utils/placeholder.ts`，所有组件 import 使用。

#### 4.3 注意点

**对真实有封面的不影响**——`fallbackImage` 只在 `coverUrl/avatarUrl` 为空时调用。

**测试场景**：
- 上传一首新歌（不包含封面元数据）→ 自动出现银白渐变 + 首字
- 上传一首新歌（包含封面元数据）→ 正常显示真实封面

#### 🔍 检查点 4：用户验证占位美化

**用户做的事**：
1. 截图任何能看到占位的位置
2. 确认占位是**漂亮的银白渐变 + 文字**，不是死黑底 "AR"

---

### 任务 5：commit

```powershell
cd D:\Claude_Playground\Soundprint
git add frontend/ docs/sql/
git status

@"
chore: Phase 7.6 数据清理 + 点击跳转修复 + 占位封面美化

数据库清理（手动 SQL）：
- 删除 30 条种子假曲目（file_path 以 storage/seed/ 开头）
- 软删除 18 张孤儿专辑
- 软删除 10+ 个没有真实曲目的艺术家
- 软删除空歌单
- 清理关联表孤儿引用（playlist_track、track_tag、play_history、user_favorite、conversion_task）

艺术家头像：
- 手动补 Top 8 真实艺术家的头像图
- 文件存于 D:/soundprint-storage/avatar/ 下
- UPDATE artist SET avatar_url 完成

前端代码：
- SoundprintCircularGallery.vue 加点击事件 emit('select')
- AlbumListView / ArtistListView / PlaylistListView 监听 select 跳转详情页
- fallbackImage 函数重写：银白渐变 + hash 角度 + 名字首字
  - 永久解决未来上传新艺术家的丑占位问题
  - 中文名取首字、英文名取首字母组合
  - 自动适配 album/artist/playlist 三种类型不同色调

数据备份：
- docs/sql/backup-before-phase-7.6.sql
"@ | Out-File "D:\Claude_Playground\_msg.txt" -Encoding utf8NoBOM

git commit -F "D:\Claude_Playground\_msg.txt"
Remove-Item "D:\Claude_Playground\_msg.txt"

git push
```

---

## 📚 边讲边写（答辩素材）

Phase 7.6 涉及的技术点：

### 1. **种子数据 vs 真实数据的运行时区分**（必讲）
- 通过 `file_path` 前缀判断：`storage/seed/` = 种子、`audio/{uuid}` = 真实上传
- 这是"数据清洁度"的工程化方法

### 2. **软删除 vs 硬删除的取舍**（讲）
- 假曲目 → 硬删除（数据真的没用了）
- 假专辑/假艺术家 → 软删除（保留 ID 一致性，未来万一引用还在）
- 关联表孤儿引用 → 硬删除（外键不存在了就该清）

### 3. **占位封面的"hash 一致性"**（必讲）
- 用名字 hash 算渐变角度，**同一艺术家每次显示渐变方向不变**
- 不同艺术家有视觉差异，但风格统一
- 这是 Notion / GitHub 默认头像的标准做法

### 4. **WebGL 组件的事件挂载妥协**（必讲，**亮点**）
- vue-bits CircularGallery 用 WebGL 渲染图片，**没有原生 DOM 事件**
- 父组件做"点击位置 → 索引"映射，emit 给业务层
- 这是真实工程中"接入第三方组件的妥协"——**比"组件没这功能就放弃"更工程化**

### 5. **可持续运行思维**（必讲）
- 占位逻辑改造的意义：**让产品不需要管理员持续手补数据**
- 上传一首新歌没头像 → 自动出现银白渐变 + 首字 → **零运维成本**
- 这是"工业级产品" vs "玩具 demo" 的区别

---

## ✅ 完成检查清单

- [ ] 任务 0：数据库备份到 docs/sql/backup-before-phase-7.6.sql
- [ ] 任务 1.1：删除 30 条假曲目
- [ ] 任务 1.2：清理关联表孤儿
- [ ] 任务 1.3：软删除孤儿专辑
- [ ] 任务 1.4：软删除孤儿艺术家
- [ ] 任务 1.5：软删除空歌单
- [ ] 任务 1.6：验证清理结果
- [ ] 检查点 1：用户确认前端数据干净
- [ ] 任务 2.1：找到 8 张艺术家头像图
- [ ] 任务 2.2：图片保存到 storage/avatar/
- [ ] 任务 2.3：UPDATE artist SET avatar_url
- [ ] 任务 2.4：浏览器验证头像显示
- [ ] 检查点 2：用户截图确认头像到位
- [ ] 任务 3.1：SoundprintCircularGallery 加点击事件
- [ ] 任务 3.2：三个列表页监听 select 跳转
- [ ] 检查点 3：用户验证点击跳转正常
- [ ] 任务 4.1：fallbackImage 函数重写
- [ ] 任务 4.2：其他占位场景统一改造
- [ ] 检查点 4：用户截图确认占位美化
- [ ] 任务 5：commit + push

---

## 📩 反馈给架构师的内容

每个检查点都要回报，最终汇总：

1. **数据库清理前后截图**（专辑列表、艺术家列表）
2. **8 个真实艺术家头像 Circular Gallery 截图**
3. **点击跳转演示截图/视频**
4. **占位封面美化截图**（找一个没头像的艺术家看看）
5. **commit hash**

---

## ⚠️ 注意事项

- **数据库备份必须做**——这是底线
- **数据清理顺序不能错**——先删 track，再清关联表，最后软删 album/artist/playlist
- **artist ID 用 SQL 1.6 验证后的真实值** —— 别用我截图分析的 ID
- **图片必须方形**——长方形的图被裁圆形组件会变形
- **图片命名严格 `artist-{id}.{ext}`** —— 命名约定影响后续维护
- **SoundprintCircularGallery 改完之后两个文件保持联动** —— 别只改父没改子

---

**End of Phase 7.6 Document.**
