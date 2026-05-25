# Phase 3：后端业务接口（CRUD + 文件上传 + 流式播放 + 统计）

> Soundprint 项目的第四个阶段文档。
> 把整份文档完整复制粘贴给 Claude Code，作为它的任务指令。
> 本阶段产出后，后端所有业务能力齐全，前端可以基于这些接口开始开发。

---

## 🎯 阶段目标

把后端从"骨架 + 一个测试接口"扩展为"完整业务能力"：

1. **5 个核心 CRUD 接口完整实现**（曲目、专辑、艺术家、歌单、标签）——拿满课程基础 30 分
2. **文件上传接口** + jaudiotagger 自动读取元数据
3. **流式播放接口**（HTTP Range Request，**重点讲原理**）
4. **歌单的增删歌、拖拽排序**
5. **收藏 / 取消收藏 / 收藏列表**
6. **播放历史记录写入接口**
7. **首页聚合接口**（最近添加、最近播放、推荐等）
8. **统计聚合接口**（流派分布、月度趋势、Top 艺术家、热力图）
9. **格式转换任务接口骨架**（提交任务、查进度、下载——FFmpeg 真转换 Phase 5 实装）
10. **lyrics 上传/获取接口**

**重要**：本阶段所有接口必须能在 Knife4j 上调通，每个接口都有种子数据可以测。

---

## 📋 任务清单

### 任务 0：前置准备

#### 0.1 确认环境
```powershell
cd D:\Claude_Playground\Soundprint\backend
mvn -version
# 确认 Maven 3.9.16

# 确认后端能启动（启动后立刻 Ctrl+C 停掉，下面要分步开发）
# 跳过这步如果你确认 Phase 2 已 OK
```

#### 0.2 创建文件存储目录

读 `application-dev.yml` 是否已配置 `soundprint.storage` 节点。如果没有，**先停下来问用户**："要把音频文件存到哪个目录？建议 `D:/soundprint-storage/`，避开中文路径。"

得到用户确认后，在 `application.yml` 新增配置：

```yaml
# Soundprint 业务配置
soundprint:
  storage:
    base-dir: D:/soundprint-storage              # 文件存储根目录
    audio-dir: D:/soundprint-storage/audio       # 音频文件
    cover-dir: D:/soundprint-storage/cover       # 封面图片
    avatar-dir: D:/soundprint-storage/avatar     # 头像
    conversion-dir: D:/soundprint-storage/conversion  # 转换后的文件
    temp-dir: D:/soundprint-storage/temp         # 临时文件
  upload:
    max-file-size: 200MB
    allowed-audio-formats: flac,mp3,wav,aac,ogg,m4a
    allowed-image-formats: jpg,jpeg,png,webp
  current-user-id: 1   # 临时方案：用默认用户 1，等 Phase 4 加登录后改成从 SecurityContext 取
```

创建对应目录：
```powershell
$dirs = @("D:/soundprint-storage", "D:/soundprint-storage/audio", "D:/soundprint-storage/cover", "D:/soundprint-storage/avatar", "D:/soundprint-storage/conversion", "D:/soundprint-storage/temp")
foreach ($d in $dirs) { New-Item -ItemType Directory -Path $d -Force | Out-Null }
Get-ChildItem D:/soundprint-storage
```

#### 0.3 配置类读取存储配置

创建 `config/StorageProperties.java`：

```java
package com.soundprint.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "soundprint.storage")
public class StorageProperties {
    private String baseDir;
    private String audioDir;
    private String coverDir;
    private String avatarDir;
    private String conversionDir;
    private String tempDir;
}
```

类似地创建 `UploadProperties` 和一个 `CurrentUserProperties` 读取 `soundprint.current-user-id`。

---

### 任务 1：DTO 体系搭建

在 `dto/request/` 和 `dto/response/` 下按业务领域分文件夹建 DTO。

**核心原则**：
- **Entity 不直接出现在 Controller**（避免泄漏数据库结构、避免循环引用、避免懒加载问题）
- **请求用 Request DTO + Bean Validation**
- **响应用 Response DTO，含必要的关联字段**（如 `TrackResponse` 含 `artistName`、`albumTitle`、`tags`）

按业务领域创建以下 DTO（具体字段定义见后面"⚙️ 技术细节"）：

```
dto/
├── request/
│   ├── track/
│   │   ├── TrackQueryRequest.java
│   │   ├── TrackUpdateRequest.java
│   │   └── TrackUploadRequest.java
│   ├── album/AlbumCreateRequest.java、AlbumUpdateRequest.java、AlbumQueryRequest.java
│   ├── artist/ArtistCreateRequest.java、ArtistUpdateRequest.java、ArtistQueryRequest.java
│   ├── playlist/PlaylistCreateRequest.java、PlaylistUpdateRequest.java、PlaylistTrackAddRequest.java、PlaylistReorderRequest.java
│   ├── tag/TagCreateRequest.java、TagUpdateRequest.java、TrackTagAssignRequest.java
│   ├── playhistory/PlayHistoryRecordRequest.java
│   └── conversion/ConversionSubmitRequest.java
└── response/
    ├── TrackResponse.java
    ├── TrackDetailResponse.java       # 含歌词、标签、收藏状态
    ├── AlbumResponse.java
    ├── AlbumDetailResponse.java       # 含曲目列表
    ├── ArtistResponse.java
    ├── ArtistDetailResponse.java
    ├── PlaylistResponse.java
    ├── PlaylistDetailResponse.java    # 含曲目列表（含艺术家名）
    ├── TagResponse.java
    ├── ConversionTaskResponse.java
    ├── PlayHistoryResponse.java
    ├── DashboardResponse.java         # 首页聚合数据
    └── stats/
        ├── StatsOverviewResponse.java
        ├── GenreDistributionItem.java
        ├── TopArtistItem.java
        ├── MonthlyTrendItem.java
        └── HeatmapItem.java
```

#### DTO ↔ Entity 转换

用 **MapStruct** 还是手工转？为了避免引入额外学习成本，**本项目用手工转换 + 静态工厂方法**，在每个 Response 类里加 `from(Entity)` / `from(List<Entity>)` 静态方法。

示例（`TrackResponse.java`）：

```java
public static TrackResponse from(Track track, Artist artist, Album album) {
    TrackResponse r = new TrackResponse();
    r.setId(track.getId());
    r.setTitle(track.getTitle());
    r.setArtistId(track.getArtistId());
    r.setArtistName(artist != null ? artist.getName() : null);
    r.setAlbumId(track.getAlbumId());
    r.setAlbumTitle(album != null ? album.getTitle() : null);
    // ... 其他字段
    return r;
}
```

---

### 任务 2：通用工具类

创建 `util/` 下：

#### 2.1 `FileStorageUtil.java`
封装文件存储相关：
- 给上传文件分配唯一文件名（UUID + 原扩展名）
- 计算文件大小、判断格式合法
- 提供 `getAbsolutePath(relativePath)`、`getRelativePath(absolutePath)` 互转
- **核心：数据库存相对路径**（如 `audio/uuid.flac`），运行时拼绝对路径。这样换部署机器不会失效

#### 2.2 `AudioMetadataReader.java`
封装 jaudiotagger，提供一个方法：

```java
public AudioMetadata read(File file) {
    // 返回包含 title, artist, album, year, genre, duration, bitrate, sampleRate, channels, lyrics, coverImage 的对象
    // 注意异常处理：损坏文件、不支持格式要 catch 并返回部分元数据
}
```

`AudioMetadata` 是个 POJO，字段对应 `track` 表里能从音频里读出来的部分。

#### 2.3 `CurrentUserUtil.java`
读 `soundprint.current-user-id`，返回当前用户 ID。

```java
@Component
@RequiredArgsConstructor
public class CurrentUserUtil {
    private final CurrentUserProperties props;
    public Long getCurrentUserId() {
        return props.getUserId();
    }
}
```

Phase 4 加登录后，这里改成从 Spring Security Context 取，**其他业务代码不用动**——这是依赖反转的好处。

---

### 任务 3：曲目 CRUD 完整实现（核心 CRUD #1）

#### 3.1 `TrackService` 业务方法

在 `service/TrackService.java` 接口里追加方法（保留 MP 的 `IService<Track>` 默认方法）：

```java
public interface TrackService extends IService<Track> {

    /** 分页查询曲目（含艺术家、专辑、标签信息） */
    PageResult<TrackResponse> pageQuery(TrackQueryRequest query);

    /** 查询曲目详情（含歌词、标签、收藏状态） */
    TrackDetailResponse getDetail(Long id);

    /** 上传音频文件 → 自动读取元数据 → 入库 */
    TrackResponse upload(MultipartFile file);

    /** 更新曲目元数据（标题/艺术家/专辑等） */
    TrackResponse updateMetadata(Long id, TrackUpdateRequest request);

    /** 软删除曲目（数据库 is_deleted = 1，物理文件不立即删） */
    void delete(Long id);

    /** 模糊搜索（按标题/艺术家/专辑） */
    PageResult<TrackResponse> search(String keyword, Long page, Long size);

    /** 获取曲目的音频文件（用于流式播放） */
    File getAudioFile(Long id);

    /** 上传歌词 */
    void updateLyrics(Long id, String lyrics);

    /** 获取歌词 */
    String getLyrics(Long id);
}
```

#### 3.2 `TrackServiceImpl` 实现要点

**上传逻辑**伪代码：

```
1. 校验：文件不能空、格式必须是允许的音频格式、大小 ≤ 200MB
2. 生成唯一文件名（UUID + 原扩展名）
3. 把文件写到 storage/audio/ 下
4. 用 jaudiotagger 读元数据
5. 处理艺术家：
   - 如果元数据有艺术家名 → 查 artist 表，没有就 INSERT，拿到 id
   - 没有艺术家名 → artist_id = null
6. 处理专辑：
   - 如果元数据有专辑名 + 艺术家 id → 查 album 表（artist_id + title 唯一），没有就 INSERT
   - 没有 → album_id = null
7. 处理封面：
   - 如果元数据带封面 → 解码后存到 storage/cover/uuid.jpg，cover_url 记相对路径
   - 没有 → cover_url 用专辑封面
8. 提取的歌词写入 track.lyrics
9. INSERT track 表（注意 file_path 用相对路径）
10. 返回 TrackResponse
11. 任何步骤异常 → 删除已上传文件 + 抛 BusinessException
```

**分页查询逻辑**：
- 用 `PageHelper` 或 MP 的 `IPage` 分页
- WHERE 条件根据 `TrackQueryRequest` 动态拼接（关键词、艺术家筛选、专辑筛选、格式筛选、标签筛选）
- 自定义 SQL（写在 `TrackMapper.xml` 里）做 LEFT JOIN artist + album，避免 N+1

**TrackMapper.xml 新增方法**（在 `resources/mapper/TrackMapper.xml`）：

```xml
<select id="pageWithRelations" resultType="com.soundprint.dto.response.TrackResponse">
    SELECT
        t.id, t.title, t.duration_seconds AS duration, t.format,
        t.bitrate_kbps AS bitrate, t.sample_rate_hz AS sampleRate,
        t.cover_url AS coverUrl, t.created_at AS createdAt,
        t.artist_id AS artistId, ar.name AS artistName,
        t.album_id AS albumId, al.title AS albumTitle, al.cover_url AS albumCoverUrl
    FROM track t
    LEFT JOIN artist ar ON ar.id = t.artist_id AND ar.is_deleted = 0
    LEFT JOIN album al  ON al.id = t.album_id  AND al.is_deleted = 0
    WHERE t.is_deleted = 0
    <if test="keyword != null and keyword != ''">
        AND (t.title LIKE CONCAT('%', #{keyword}, '%')
            OR ar.name LIKE CONCAT('%', #{keyword}, '%')
            OR al.title LIKE CONCAT('%', #{keyword}, '%'))
    </if>
    <if test="artistId != null">AND t.artist_id = #{artistId}</if>
    <if test="albumId != null">AND t.album_id = #{albumId}</if>
    <if test="format != null and format != ''">AND t.format = #{format}</if>
    ORDER BY t.created_at DESC
</select>
```

#### 3.3 `TrackController` 完整接口

```java
@Tag(name = "曲目")
@RestController
@RequestMapping("/api/tracks")
@RequiredArgsConstructor
public class TrackController {

    private final TrackService trackService;

    @Operation(summary = "分页查询曲目")
    @GetMapping
    public Result<PageResult<TrackResponse>> page(TrackQueryRequest query) { ... }

    @Operation(summary = "曲目详情")
    @GetMapping("/{id}")
    public Result<TrackDetailResponse> detail(@PathVariable Long id) { ... }

    @Operation(summary = "上传音频文件")
    @PostMapping("/upload")
    public Result<TrackResponse> upload(@RequestParam("file") MultipartFile file) { ... }

    @Operation(summary = "更新曲目元数据")
    @PutMapping("/{id}")
    public Result<TrackResponse> update(@PathVariable Long id, @RequestBody @Valid TrackUpdateRequest request) { ... }

    @Operation(summary = "删除曲目（软删除）")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) { return Result.success(); }

    @Operation(summary = "模糊搜索曲目")
    @GetMapping("/search")
    public Result<PageResult<TrackResponse>> search(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "1") Long page,
            @RequestParam(defaultValue = "20") Long size) { ... }

    @Operation(summary = "更新歌词")
    @PutMapping("/{id}/lyrics")
    public Result<Void> updateLyrics(@PathVariable Long id, @RequestBody String lyrics) { ... }

    @Operation(summary = "获取歌词")
    @GetMapping("/{id}/lyrics")
    public Result<String> getLyrics(@PathVariable Long id) { ... }

    // 流式播放接口单独写一个 Controller，见任务 6
}
```

---

### 任务 4：专辑 / 艺术家 / 歌单 / 标签 CRUD（核心 CRUD #2~#5）

按照任务 3 的模式实现剩下 4 个 CRUD。**结构相同，只是字段不同**。

每个都要有：
- 分页查询 + 模糊搜索
- 详情查询（含关联数据）
- 创建
- 更新
- 删除（业务实体软删除，关系表硬删除）

**重点关注**：

#### 4.1 专辑 CRUD
- 详情接口返回**该专辑所有曲目**（按 `track_number` 排序）
- 删除时只软删 album 表，track.album_id 设为 NULL（FK 已设 ON DELETE SET NULL，但软删需要手工处理）

#### 4.2 艺术家 CRUD
- 详情接口返回**该艺术家所有专辑和单曲**

#### 4.3 歌单 CRUD + 歌单内操作
歌单有几个特殊接口：

```java
@PostMapping("/{id}/tracks")
public Result<Void> addTrack(@PathVariable Long id, @RequestBody PlaylistTrackAddRequest req);
// 把曲目加到歌单，order_index 自动 = 当前最大值 + 1

@DeleteMapping("/{id}/tracks/{trackId}")
public Result<Void> removeTrack(@PathVariable Long id, @PathVariable Long trackId);

@PutMapping("/{id}/reorder")
public Result<Void> reorder(@PathVariable Long id, @RequestBody PlaylistReorderRequest req);
// req 含 trackIds: [3, 1, 5, 2, 4]，按这个顺序重新设 order_index
```

**reorder 实现关键**：用单一事务批量更新 order_index，避免中间状态。如果列表很大可以用 CASE WHEN 一条 SQL 搞定（高级写法，进阶讲解时讲）。

#### 4.4 标签 CRUD + 打标签
```java
@PostMapping("/api/tracks/{trackId}/tags")
public Result<Void> assignTags(@PathVariable Long trackId, @RequestBody TrackTagAssignRequest req);
// req 含 tagIds: [1, 3, 5]，覆盖式更新（先删原有 track_tag，再插入新的）
```

---

### 任务 5：收藏 / 播放历史

#### 5.1 收藏接口 `FavoriteController`

```java
@PostMapping("/api/favorites/{trackId}")
public Result<Void> add(@PathVariable Long trackId);

@DeleteMapping("/api/favorites/{trackId}")
public Result<Void> remove(@PathVariable Long trackId);

@GetMapping("/api/favorites")
public Result<PageResult<TrackResponse>> list(...);

@GetMapping("/api/favorites/check/{trackId}")
public Result<Boolean> check(@PathVariable Long trackId);
```

`user_favorite` 表有复合主键 (user_id, track_id)，Phase 2 已经把它的 Entity 改成单 `@TableId(INPUT) + @TableField`。**操作时不能用 `getById`、`updateById` 等默认方法**，要写 LambdaQueryWrapper 或自定义 SQL。

#### 5.2 播放历史接口 `PlayHistoryController`

```java
@PostMapping("/api/play-history")
public Result<Void> record(@RequestBody PlayHistoryRecordRequest req);
// req 含 trackId, playedSeconds

@GetMapping("/api/play-history/recent")
public Result<List<PlayHistoryResponse>> recent(@RequestParam(defaultValue = "10") Integer limit);
// 最近播放（去重，同一首歌只保留最近一次）
```

---

### 任务 6：流式播放接口（**重点讲技术原理**）

#### 6.1 `StreamController`

```java
@Tag(name = "音频流")
@RestController
@RequestMapping("/api/stream")
@RequiredArgsConstructor
public class StreamController {

    private final TrackService trackService;
    private final StorageProperties storageProps;

    @Operation(summary = "流式播放曲目")
    @GetMapping("/{trackId}")
    public ResponseEntity<ResourceRegion> stream(
            @PathVariable Long trackId,
            @RequestHeader HttpHeaders headers) throws IOException {

        File audioFile = trackService.getAudioFile(trackId);
        FileSystemResource resource = new FileSystemResource(audioFile);

        long contentLength = resource.contentLength();
        long chunkSize = 1024 * 1024L;  // 1MB per chunk

        // 解析 Range 请求头
        List<HttpRange> ranges = headers.getRange();
        ResourceRegion region;
        HttpStatus status;

        if (ranges.isEmpty()) {
            // 没 Range 头：返回开头一块（避免一次性把整个 100MB FLAC 灌给浏览器）
            long rangeLength = Math.min(chunkSize, contentLength);
            region = new ResourceRegion(resource, 0, rangeLength);
            status = HttpStatus.OK;
        } else {
            HttpRange range = ranges.get(0);
            long start = range.getRangeStart(contentLength);
            long end = range.getRangeEnd(contentLength);
            long rangeLength = Math.min(chunkSize, end - start + 1);
            region = new ResourceRegion(resource, start, rangeLength);
            status = HttpStatus.PARTIAL_CONTENT;  // 206
        }

        return ResponseEntity.status(status)
                .contentType(MediaTypeFactory.getMediaType(resource).orElse(MediaType.APPLICATION_OCTET_STREAM))
                .header(HttpHeaders.ACCEPT_RANGES, "bytes")
                .body(region);
    }
}
```

#### 6.2 必讲技术点：HTTP Range Request

**这是答辩高频题，必讲！**

讲解要点：
1. **问题**：FLAC 文件可能 50MB+。如果直接 `return new FileSystemResource(file)`，浏览器要等整个文件下载完才能开始播放，体验灾难
2. **HTTP 协议提供的解法**：`Range` 请求头 + `206 Partial Content` 响应
3. **客户端行为**：`<audio>` 标签自动发 `Range: bytes=0-` 请求
4. **服务端职责**：
   - 检测请求头里的 `Range`
   - 只返回请求的字节区间
   - 响应头加 `Accept-Ranges: bytes`、`Content-Range: bytes start-end/total`
   - HTTP 状态 `206` 而不是 `200`
5. **进度条拖动**：用户拖到 30%，浏览器自动发 `Range: bytes=<对应字节数>-` 请求中段
6. **Spring 的支持**：`ResourceRegion` + `MediaTypeFactory` 是 Spring Web 提供的封装

答辩话术：

> "流式播放的关键是 HTTP Range Request。如果直接整文件下载，50MB 的 FLAC 要等几秒钟才能开始播放，体验很差。Range 让客户端告诉服务端'我要文件的第 0 到 1MB'，服务端返回 206 Partial Content + 那一段字节，浏览器就能边下边播。用户拖动进度条时，浏览器自动发新的 Range 请求请求中段，所以拖动是即时响应的。Spring 的 ResourceRegion 类把这套逻辑封装好了。"

---

### 任务 7：首页聚合接口

`DashboardController`：

```java
@GetMapping("/api/dashboard")
public Result<DashboardResponse> get();
```

`DashboardResponse` 包含：
- `greeting`：根据时间返回 "Good morning" / "afternoon" / "evening"，加用户昵称
- `recentTracks`：最近添加的 6 首
- `recentlyPlayed`：最近播放的 8 首（去重）
- `favorites`：随机抽 6 首收藏
- `featuredAlbums`：精选 12 张专辑（用于 Infinite Menu）—— 排序规则："被收藏多 + 播放次数高 + 最近发行" 加权，Phase 3 可以简化为按 `created_at DESC` 取前 12
- `featuredArtists`：精选 12 个艺术家（Infinite Menu 艺术家 tab）

---

### 任务 8：统计聚合接口（**进阶 2.3 视觉支撑**）

`StatsController`：

```java
@GetMapping("/api/stats/overview")
public Result<StatsOverviewResponse> overview();
// 总曲目数、总时长、累计播放时长、累计播放次数、收藏数、歌单数

@GetMapping("/api/stats/genres")
public Result<List<GenreDistributionItem>> genres();
// 按流派分组：[{ genre: "Rock", count: 8, percentage: 26.7 }, ...]
// SQL: SELECT al.genre, COUNT(*) FROM play_history ph JOIN track t ON ph.track_id = t.id JOIN album al ON t.album_id = al.id GROUP BY al.genre

@GetMapping("/api/stats/top-artists")
public Result<List<TopArtistItem>> topArtists(@RequestParam(defaultValue = "10") Integer limit);
// 按播放次数排序的 Top 艺术家

@GetMapping("/api/stats/monthly-trend")
public Result<List<MonthlyTrendItem>> monthlyTrend(@RequestParam(defaultValue = "12") Integer months);
// 最近 N 个月每月的播放时长

@GetMapping("/api/stats/heatmap")
public Result<List<HeatmapItem>> heatmap(@RequestParam(defaultValue = "365") Integer days);
// 最近 N 天每天的播放次数（GitHub 贡献图样式）
```

**SQL 示例**（写在 `PlayHistoryMapper.xml`）：

```xml
<select id="selectHeatmap" resultType="com.soundprint.dto.response.stats.HeatmapItem">
    SELECT
        DATE(played_at) AS date,
        COUNT(*) AS count,
        SUM(played_seconds) AS totalSeconds
    FROM play_history
    WHERE user_id = #{userId}
      AND played_at >= DATE_SUB(NOW(), INTERVAL #{days} DAY)
    GROUP BY DATE(played_at)
    ORDER BY date
</select>
```

讲解时要讲：**这种聚合查询如果不在 SQL 里做、而是查所有播放历史然后在 Java 里 groupBy，数据量大时会爆内存**。所以聚合让数据库做，应用拿结果。

---

### 任务 9：转换任务接口骨架

**Phase 3 不实装 FFmpeg**，只搭接口骨架，让前端可以联调。Phase 5 才真正实装。

`ConversionController`：

```java
@PostMapping("/api/conversions")
public Result<ConversionTaskResponse> submit(@RequestBody @Valid ConversionSubmitRequest req);
// 创建 conversion_task 记录，status=PENDING
// Phase 5 会在这里触发异步 FFmpeg 转换

@GetMapping("/api/conversions/{id}")
public Result<ConversionTaskResponse> get(@PathVariable Long id);
// 查任务状态和进度

@GetMapping("/api/conversions")
public Result<PageResult<ConversionTaskResponse>> list(...);
// 我的转换历史

@GetMapping("/api/conversions/{id}/download")
public ResponseEntity<Resource> download(@PathVariable Long id);
// 下载转换后的文件（status=SUCCESS 才允许）
```

提交后**Phase 3 暂时模拟进度**：返回的任务状态是 RUNNING，前端轮询时每次进度 +10%，到 100% 改成 SUCCESS（用 `Thread.sleep` 模拟，**只在 Phase 3 临时这么做**，Phase 5 改成真异步）。

或者更简单：提交后直接把种子数据里那条 SUCCESS 任务的 output_path 复制一份当作"转换结果"。这样前端能完整联调下载流程。

让 Claude Code **选哪个方案问用户**。

---

### 任务 10：异常和参数校验补全

#### 10.1 业务异常细化

在 `exception/` 下补充：

```java
public class ResourceNotFoundException extends BusinessException {
    public ResourceNotFoundException(String resource, Object id) {
        super(404, String.format("%s 不存在 (id=%s)", resource, id));
    }
}
```

并在 `GlobalExceptionHandler` 加：
- `MethodArgumentNotValidException` (Bean Validation 失败) → 提取字段错误信息，400
- `MissingServletRequestPartException` (上传文件缺失) → 400
- `MaxUploadSizeExceededException` (文件过大) → 413

#### 10.2 Request DTO 加 Validation 注解

```java
public class AlbumCreateRequest {
    @NotBlank(message = "专辑名不能为空")
    @Size(max = 200)
    private String title;

    private Long artistId;

    @Min(1900) @Max(2100)
    private Integer releaseYear;

    @Size(max = 50)
    private String genre;
}
```

Controller 参数前加 `@Valid`，触发校验。

---

### 任务 11：联调测试（**关键步骤**）

启动后端：
```powershell
cd D:\Claude_Playground\Soundprint\backend
mvn spring-boot:run
```

在浏览器打开 http://localhost:8080/doc.html，**用 Knife4j 的"调试"功能逐个测试**：

#### 必测接口列表

按顺序测试，每个接口必须返回 200：

```
1. GET  /api/health
2. GET  /api/dashboard
3. GET  /api/tracks?page=1&size=10
4. GET  /api/tracks/1
5. GET  /api/tracks/search?keyword=晴天
6. GET  /api/tracks/1/lyrics                  (新建/编辑歌词后能拿到)
7. PUT  /api/tracks/1/lyrics                   (上传测试歌词)
8. GET  /api/albums?page=1&size=10
9. GET  /api/albums/1
10. GET /api/artists?page=1&size=10
11. GET /api/artists/1
12. GET /api/playlists?page=1&size=10
13. GET /api/playlists/1
14. POST /api/playlists/1/tracks  (req: {trackId: 30})
15. DELETE /api/playlists/1/tracks/30
16. PUT  /api/playlists/1/reorder  (req: {trackIds: [5,2,1,11,18,28]})
17. GET  /api/tags
18. POST /api/tracks/1/tags  (req: {tagIds: [1,3]})
19. POST /api/favorites/3
20. GET  /api/favorites
21. DELETE /api/favorites/3
22. POST /api/play-history (req: {trackId: 1, playedSeconds: 200})
23. GET  /api/play-history/recent
24. GET  /api/stats/overview
25. GET  /api/stats/genres
26. GET  /api/stats/top-artists
27. GET  /api/stats/monthly-trend
28. GET  /api/stats/heatmap
29. GET  /api/stream/1                         (浏览器开新标签直接访问，应能听到声音——但 file_path 是占位，会 404，正常)
30. POST /api/conversions  (req: {sourceTrackId: 1, targetFormat: "MP3", targetBitrate: 320})
31. GET  /api/conversions/1
```

**说明**：
- 接口 29（流式播放）会失败，因为种子数据的 `file_path` 是占位路径，文件不存在。**这是正常的**，Phase 4 前端上传真实文件后会通。Claude Code 测试时要识别这个预期失败
- 上传接口 `POST /api/tracks/upload`：Claude Code 没有真音频文件可用，**让用户准备一个小的 MP3 或 FLAC**（< 50MB），用 Knife4j 调试界面上传测试。这一步对 Phase 5 播放器和 Phase 6 转换很重要

---

### 任务 12：commit

```powershell
cd D:\Claude_Playground\Soundprint
git status
git add backend/
@"
feat: 完成后端业务接口（Phase 3）

接口完成度：
- 5 个核心 CRUD：曲目、专辑、艺术家、歌单、标签
- 文件上传 + jaudiotagger 元数据自动读取
- 流式播放（HTTP Range Request）
- 歌单：加歌/删歌/拖拽排序
- 收藏：增删查 + 状态检查
- 播放历史：写入 + 最近播放
- 首页聚合：dashboard
- 统计聚合：总览、流派、Top艺术家、月度趋势、热力图
- 转换任务：接口骨架（FFmpeg 实装留待 Phase 5）
- 歌词：上传 + 获取

技术亮点：
- DTO 分层（Request/Response），Entity 不出现在 Controller
- 自定义 SQL JOIN 查询，避免 N+1
- Bean Validation 自动校验
- Range Request 流式播放
- 软删除 + 关系表硬删除策略
- 聚合在 SQL 中完成，避免内存爆炸

所有接口已在 Knife4j 调通（除流式播放需要真实音频文件外）。
"@ | Out-File "D:\Claude_Playground\_msg.txt" -Encoding utf8NoBOM
git commit -F "D:\Claude_Playground\_msg.txt"
Remove-Item "D:\Claude_Playground\_msg.txt"
```

---

## 📚 边写边讲要求（必讲清单）

Phase 3 要讲透的技术点：

### 1. **DTO 为什么不直接用 Entity**（必讲，答辩高频）
- 安全：Entity 含 password、is_deleted 等不该暴露的字段
- 解耦：数据库结构改了，对外接口不一定要改
- 关联字段：TrackResponse 含 artistName，Entity Track 只有 artistId

### 2. **HTTP Range Request**（必讲，重磅！）
完整讲一遍，重点：
- 状态码 206
- 请求头 `Range: bytes=0-`
- 响应头 `Accept-Ranges`、`Content-Range`
- 进度条原理

### 3. **N+1 查询问题**（必讲）
- 错误做法：查 30 个 track，每个 track.getArtistId() 查一次 artist → 31 次查询
- 正确做法：一次 LEFT JOIN 把 artist 名字带回来

### 4. **jaudiotagger 怎么读元数据**（讲）
- ID3v1/v2 是什么
- FLAC 用 Vorbis Comment
- 不同格式的元数据规范

### 5. **聚合在 SQL 还是在 Java？**（必讲）
- 用户 10w 条播放记录场景下，Java 内存 groupBy 会 OOM
- 数据库引擎对 GROUP BY 有 B+ 树和哈希优化
- 网络传输量也少

### 6. **MapStruct 还是手工转换？**（讲）
- 我们用手工 + 静态 from 方法
- 优点：依赖少、可控、可读
- 缺点：字段多时啰嗦
- MapStruct 适合大型项目，本项目规模手工足够

### 7. **复合主键的处理**（讲）
- MyBatis-Plus 不直接支持复合主键
- 解决方案：单 @TableId(INPUT) + @TableField，手动维护
- 操作要用 LambdaQueryWrapper，不能用 getById

### 8. **软删除 vs 硬删除策略**（深化 Phase 1 讲过的）
- 业务实体软删（track, album, artist, playlist）
- 关系表硬删（playlist_track, track_tag, user_favorite）
- 行为表无删除（play_history, conversion_task）

### 9. **Bean Validation**（讲）
- @NotBlank, @Size, @Min, @Max
- @Valid 触发校验
- 全局异常处理统一提取字段错误

### 10. **文件存储为什么用相对路径**（讲）
- 数据库存 `audio/uuid.flac`
- 应用层拼绝对路径
- 换部署机器/迁移目录都不用动数据库

---

## ✅ 完成检查清单

- [ ] `application.yml` 配置 storage 节点，目录已创建
- [ ] DTO 体系建立，按业务领域分目录
- [ ] 工具类：FileStorageUtil、AudioMetadataReader、CurrentUserUtil
- [ ] 5 个 CRUD Controller 完整实现（曲目、专辑、艺术家、歌单、标签）
- [ ] 文件上传接口能调用，能成功上传一个真实音频文件
- [ ] 流式播放接口能返回 ResourceRegion（用真实文件能播放）
- [ ] 收藏 / 播放历史接口
- [ ] 首页聚合接口
- [ ] 统计 5 个接口
- [ ] 转换任务骨架 4 个接口
- [ ] 歌词上传/获取
- [ ] 全局异常处理完善，参数校验生效
- [ ] 31 个测试接口在 Knife4j 全部能调通（除流式播放因占位路径预期失败）
- [ ] commit 完成

---

## 📩 反馈给架构师的内容

1. **Knife4j 接口列表截图**（左侧能看到完整接口树）
2. **任意 3 个接口的调用结果**：
   - `GET /api/dashboard` 返回首页聚合数据
   - `GET /api/stats/genres` 返回流派分布
   - `POST /api/favorites/3` 收藏后再 `GET /api/favorites/check/3` 应返回 true
3. **上传测试**：你准备一个小音频文件（任意 MP3 / FLAC），通过 `/api/tracks/upload` 上传，返回的 TrackResponse 里 metadata 是否正确读取
4. **流式播放测试**：用上传的真文件 ID 访问 `/api/stream/{id}`，浏览器是否能直接播放
5. **任何卡壳或不理解的地方**
6. **是否准备好进入 Phase 4（前端骨架）**

---

## ⚠️ 注意事项

- **不要修改种子数据**，所有测试用种子数据 + 用户上传的真文件
- **不要修改数据库 schema**，如发现字段不够用先报告
- **不要把 Service 写成 controller 的样子**（业务逻辑在 Service，Controller 只做接口入口）
- **MapStruct 不引入**，用手工转换
- **不实装 FFmpeg**，那是 Phase 5
- **不写鉴权 / 登录**，那是 Phase 4 末尾或后续考虑
- 联调过程中如发现 SQL 跑慢或字段不存在，**停下来分析**，不要乱改

---

**End of Phase 3 Document.**
