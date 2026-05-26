# 开发笔记 · Soundprint

> 本文件用于记录开发过程中的设计决策、技术难点、踩坑记录。
> 在 Phase 8 撰写实验报告时，这里的内容会被整理进报告"过程与分析"章节。

## 📅 时间线

- Phase 0（初始化）：2026-05-24 完成
- Phase 1（数据库设计）：2026-05-24 完成
- Phase 2（后端骨架）：2026-05-25 完成
- Phase 3（后端业务）：2026-05-25 完成
- Phase 4（前端骨架）：2026-05-26 完成（Codex 接力实现，Claude 审计 + 收藏补漏）
- Phase 5（播放器+转换）：2026-05-26 完成（Codex 实现，待 Claude 审计）
- Phase 6（统计可视化）：2026-05-26 完成（Codex 实现，待 Claude 审计）
- Phase 7（视觉精修）：2026-05-26 完成（Codex 实现，待 Claude 审计）
- Phase 8（部署+报告）：待开始

## 🎯 设计决策记录

### 为什么选 TypeScript？

（开发过程中遇到再填）

### 为什么 11 张表而不是 3 张？

课程要求"至少 1 张表 6 个字段"，但单表设计有严重缺陷：
1. 一首歌有 artist、album、tag 等多重属性，单表存储会出现大量重复
   （比如 Beatles 的 100 首歌会让 "The Beatles" 这个字符串重复 100 次）
2. 多对多关系（曲目↔歌单、曲目↔标签）无法用单表表达
3. 无法做有意义的 JOIN 查询，进阶分拿不到

11 张表的设计基于三范式 + 业务领域分析：
- 5 张实体表：user、artist、album、track、playlist、tag
- 3 张关系表：playlist_track（多对多 + 排序）、track_tag、user_favorite
- 3 张行为表：play_history（播放记录）、conversion_task（转换任务记录）
- 满足第二范式（非主属性完全依赖主键）和第三范式（消除传递依赖）

### 为什么用 utf8mb4 而不是 utf8？

MySQL 的 utf8 实际只支持 3 字节编码，无法存储 4 字节字符
（如部分 emoji、生僻汉字、罕见日韩字符）。无损音乐元数据国际化程度高，
日文专辑、韩文歌手名、emoji 标签都可能出现，必须用真正的 utf8mb4。

### 为什么有 is_deleted 软删除？

业务实体（曲目、专辑、艺术家、歌单）一旦被引用（如出现在播放历史、
歌单中），物理删除会导致级联问题：
- 历史播放记录里的 track_id 会指向不存在的曲目
- 用户误删后无法恢复
- 审计/统计需求需要看到"删除前的数据"

软删除将 is_deleted 设为 1 + 记录 deleted_at 时间，查询时统一过滤
WHERE is_deleted = 0。物理删除（包括磁盘文件清理）由后台任务异步执行。

### 为什么部分跨表关系不加 FOREIGN KEY？

权衡结果：
- 主体业务关系（track→artist、track→album）加 FK，保证强一致性
- 行为表关系（play_history.track_id、conversion_task.source_track_id）不加 FK
  理由：行为表数据量大，FK 检查会显著影响插入性能；且业务上允许引用
  软删除/已清理的曲目（统计仍需要这条记录）
- 应用层用 MyBatis-Plus 的关联查询 + 软删除过滤保证一致性

## 🐛 踩坑记录

（按时间倒序记录）

- **2026-05-26 · vue-bits 源码不直接改，包装层做工程化集成**：Phase 7 文档建议在第三方组件源码顶部加版权注释，但本阶段用户明确要求 4 个 vue-bits 源码文件"严格不修改"。处理方式：源码原样保留在 `frontend/src/components/vue-bits/`，新增 `docs/vue-bits-references.md` 记录来源、协议和定制点，所有配色、尺寸、资源清理都放在 `Soundprint*` 包装组件里。
- **2026-05-26 · 同屏 WebGL 上下文必须控量**：浏览器单页 WebGL 上下文通常有 8-16 个上限，超过后会丢失旧上下文。本项目只在当前路由局部挂载 1 个 WebGL 组件：登录/首页 Galaxy、列表页 Circular Gallery、NowPlaying Antigravity、Studio RUNNING Balatro。包装层在 `onBeforeUnmount` 里额外调用 `WEBGL_lose_context`，路由切走主动释放。
- **2026-05-26 · 127.0.0.1 与 localhost 的 CORS 差异**：转换工坊 POST 在 `127.0.0.1:5173` 下曾被后端 CORS 预检拦成 403，因为开发放行地址只写了 `localhost:5173`。解决：`CorsConfig` 同步放行 `127.0.0.1` 的 5173/5174/3000，避免同一台机器不同访问写法导致 POST 失败。
- **2026-05-26 · 转换任务刷新恢复要避开陈旧 RUNNING 数据**：本地库里有历史遗留 RUNNING 任务，直接恢复"最新活跃任务"会误显示旧任务。StudioView 改为只恢复 30 分钟内创建的 PENDING/RUNNING 任务，既支持刷新后继续看进度，也避免历史脏数据污染 UI。
- **2026-05-26 · 自动化下 GPU memory 不可读**：Phase 7 要做 Performance Monitor 验证。headless 自动化能读到 canvas 数和 `performance.memory.usedJSHeapSize`，但拿不到 Chrome DevTools Performance Monitor 的 GPU memory 列。实测路由切换：login 1 canvas / 18.99MB，dashboard 1 / 16.71MB，albums 1 / 16.93MB，now-playing 1 / 25.58MB，关闭 NowPlaying 后 0 canvas，回首页 1 canvas；说明 canvas 没累积，JS heap 增长主要来自懒加载 Three/wavesurfer 代码块和未立即 GC。
- **2026-05-26 · 前端实际版本高于课程文档（Vite 8 / TS 6）**：`npm create vite@latest` 生成的是 Vite 8 + TypeScript 6 + Vue 3.5，而 CLAUDE.md/文档写的是 Vite 5 / TS 5。决定**保持不降级**——功能等价，且 **Tailwind 已锁 3.x**（vue-bits 兼容的关键项才是真正重要的）。代价：TS 6 对部分编译选项更严，`tsconfig.app.json` 加了 `"ignoreDeprecations": "6.0"` 才能过 `vue-tsc -b` 构建。答辩说辞："脚手架默认就是最新 Vite/TS，主版本号不影响技术栈本质，按需锁定的是 Tailwind 3 以兼容 vue-bits。"
- **2026-05-26 · 列表收藏状态需 JOIN 才能拿到**：`TrackResponse`（列表 DTO）原本没有收藏状态，音乐库无法显示心形。解决：`pageWithRelations` 加 `LEFT JOIN user_favorite uf ON uf.track_id=t.id AND uf.user_id=#{userId}`，SELECT `(uf.user_id IS NOT NULL) AS favorited`，前端据此渲染可切换心形。避免了"前端造假状态"。
- **2026-05-26 · @Async 不能在同一个类里自调用**：Phase 5 转换任务需要提交后立刻返回、后台跑 FFmpeg。直接在 `ConversionTaskServiceImpl` 里调用本类 `@Async` 方法会绕过 Spring 代理，异步不生效。解决：拆出 `ConversionExecutionService`，由提交服务调用另一个 Bean 的 `@Async executeConversion`，保证转换在线程池中执行。
- **2026-05-26 · FFmpeg 进度来自 stderr 而不是 stdout**：FFmpeg 的 `Duration` 和 `time=` 进度行输出在错误流。`FFmpegRunner` 用 `ProcessBuilder.redirectErrorStream(true)` 合并输出后按正则解析，总时长做分母、当前 `time=` 做分子，得到 0-100 进度。命令使用参数数组，不拼 shell 字符串，避免空格路径和命令注入。
- **2026-05-26 · wavesurfer 只画波形，不负责播放**：全局播放只允许 Pinia player store 里的单例 `HTMLAudioElement` 负责，wavesurfer 只 `load('/api/stream/{id}')` 做波形渲染和点击 seek。缺点是音频会被请求两次；好处是不会出现多个播放器同时播放、状态不同步的问题。
- **2026-05-26 · Phase 5 真实转换验证物证**：用 FFmpeg 生成 2 秒测试 MP3，经 `/api/tracks/upload` 上传为 trackId=32，再提交 `/api/conversions` 转 WAV，taskId=4 最终 `SUCCESS/100`，下载文件 352878 bytes，`ffprobe` 识别 `format_name=wav`、`duration=2.000000`。
- **2026-05-26 · ECharts 配色不能散落在每个图表里**：Phase 6 新增 `echarts-theme.ts` 统一注册 `soundprint-dark` 主题，并导出 `SOUNDPRINT_CHART_COLORS`。图表组件只描述业务 option，轴线、tooltip、文字等公共样式走主题；必须单独配置的渐变/热力图颜色也引用同一个主题色常量，避免每个图表各写一套颜色。
- **2026-05-26 · wavesurfer 大文件波形性能隐患**：Phase 5 的波形会让浏览器下载并解码完整音频，大 FLAC 可能慢且占内存。Phase 6 新增 `/api/tracks/{id}/peaks`，后端用 FFmpeg 输出单声道 8kHz `f32le` PCM，Java 解码后等距取绝对峰值，前端用 `wavesurfer.load(url, [peaks], duration)` 直接画波形。实测 trackId=31 首次生成约 750ms，缓存命中约 133ms，JSON 约 7.7KB。
- **2026-05-26 · peaks 缓存必须防止重复跑 FFmpeg**：第一次生成后写入 `D:/soundprint-storage/peaks/{trackId}.json`，后续同 sampleCount + duration 请求直接读缓存。缓存读失败或参数不匹配时重新生成，不影响主流程。
- **2026-05-26 · ECharts option 更新用 notMerge + lazyUpdate**：`BaseChart` 中 `setOption(option, { notMerge: true, lazyUpdate: true })`。`notMerge` 避免旧 series/axis 残留导致图表数据串场；`lazyUpdate` 让 ECharts 把渲染合并到下一帧，降低连续响应式更新时的抖动。
- **2026-05-25 · @TableField(fill) 不配 MetaObjectHandler 等于没填**：实体上标了 `@TableField(fill = FieldFill.INSERT)` 的 `created_at`，插入时仍报 `Column 'created_at' cannot be null`。原因：fill 注解只是声明意图，MyBatis-Plus 真正写值要实现一个 `MetaObjectHandler` Bean，代码生成器只加了注解没生成 handler。解决：新增 `MybatisMetaObjectHandler`，在 `insertFill/updateFill` 里用 `strictInsertFill/strictUpdateFill` 填 createdAt/updatedAt。坑的隐蔽点：中间表（playlist_track 等）的时间字段是手动 set 的，不依赖 fill，所以前面测试一直没暴露，直到第一个走 fill 的 insert（转换任务）才炸。
- **2026-05-25 · MySQL only_full_group_by 严格模式**：热力图 SQL `SELECT DATE_FORMAT(played_at,'%Y-%m-%d') ... GROUP BY DATE(played_at)` 报错 `not functionally dependent ... incompatible with sql_mode=only_full_group_by`。原因：MySQL 8 默认开启 only_full_group_by，SELECT 里的非聚合列必须能被 GROUP BY 列推导出来，而 `DATE_FORMAT(...)` 和 `DATE(...)` 被视为不同表达式。解决：GROUP BY 改用 SELECT 的别名（`GROUP BY date`），与月度趋势查询保持一致写法。
- **2026-05-25 · 中间表复合主键炸了 MyBatis-Plus**：`playlist_track`、`track_tag`、`user_favorite` 三张中间表用的是联合主键（两列），代码生成器给两列都打了 `@TableId`，启动时报 `@TableId can't more than one in Class`。MP 的 `BaseMapper`（getById/updateById 等）只支持单主键。解决：每张表只保留一个 `@TableId(type = IdType.INPUT)`（INPUT = 值由业务插入，非自增），另一列改 `@TableField`。不动数据库的联合主键，中间表的增删改用自定义查询处理。
- **2026-05-25 · JDBC characterEncoding 填错**：连接串里写了 `characterEncoding=utf8mb4`，启动报 `Unsupported character encoding 'utf8mb4'`。原因：该参数要的是 **Java 字符集名**（`UTF-8`），`utf8mb4` 是 MySQL 服务端字符集名，两者不能混。改成 `UTF-8` 后，配合服务器的 utf8mb4 即可正确存 4 字节字符。
- **2026-05-25 · Maven 注解处理器路径缺版本号**：pom 里给 Lombok 配 `annotationProcessorPaths` 但没写 `<version>`，Maven 3.9.16 + compiler-plugin 3.11.0 不会自动从父 POM 继承，报 `version can neither be null...`。解决：补 `<version>${lombok.version}</version>`（引用 Spring Boot 父 POM 已定义的属性，不自己挑版本）。
- **2026-05-25 · PowerShell 粘贴长命令被断行**：`mvn ... -Dexec.mainClass=...` 多 token 长命令粘进 PowerShell 被自动拆成多行执行，导致参数丢失。解决：把命令写进 `.ps1` 脚本文件，用一条短命令运行，彻底避开粘贴断行。
- **2026-05-24 · MySQL 端口非默认**：开发机装了 MySQL 8.0（服务 MySQL80）和 9.4（服务 MySQL94）两个实例。8.0 实例端口被设为 **3307**（默认 3306 被 9.4 占用）。后端 application.yml 连接务必用 3307。
- **2026-05-24 · 环境备忘**：Maven 3.6.1 偏低，Spring Boot 3.2 需 ≥3.6.3，Phase 2 前需升级至 3.9.x。

## 💡 技术亮点

（写报告时直接复制到"创新点"章节）

### Phase 1 完成的可讲技术点

1. utf8mb4 字符集选择（vs utf8）
2. 三范式遵循（消除冗余、消除传递依赖）
3. 软删除 + 审计字段（is_deleted、created_at、updated_at、deleted_at）
4. 多对多关系建模（playlist_track 含 order_index 排序字段、track_tag）
5. JSON 字段扩展性预留（track.extra_metadata 存非结构化元数据）
6. 行为表与实体表分离（play_history、conversion_task 与 track 解耦）
7. 索引设计：B+ 树索引在外键 + 业务高频查询字段
8. 时区统一为 +08:00

### Phase 2 完成的可讲技术点

1. Spring Boot 自动配置 + 起步依赖（约定大于配置）
2. 分层架构 Controller → Service → Mapper → DB（单一职责、可测试）
3. MyBatis-Plus 代码生成器：11 张表一次性生成 Entity/Mapper/Service
4. 统一响应封装 Result<T>：前后端契约一致
5. 全局异常处理 @RestControllerAdvice（AOP 横切，用户永远拿到友好提示）
6. MyBatis-Plus 分页插件 PaginationInnerInterceptor（物理分页，非内存分页）
7. CORS 跨域配置（解决 5173→8080 同源策略限制）
8. Knife4j 在线 API 文档（前端没写也能测后端）
9. Lombok 编译期注解处理 + 构造器注入（@RequiredArgsConstructor）
10. 多环境配置隔离（profiles dev/prod）+ HikariCP 连接池
11. 软删除全局生效（logic-delete-field 配置 + 实体 @TableLogic，查询自动加 is_deleted=0）

### Phase 4 完成的可讲技术点（前端骨架）

1. Vite 代理 `/api → :8080` 解决开发期跨域（生产用 Nginx 同理转发）
2. Axios 响应拦截器自动剥 `Result<T>` 壳 + 统一 ElMessage 报错（调用方直接拿 data）
3. Tailwind 与 Element-Plus 共存：`preflight:false` 关掉 Tailwind reset 防冲突
4. 设计令牌系统：颜色/间距/圆角/阴影集中管理（tokens.scss + tailwind.config），Element-Plus 主色对齐品牌紫
5. CSS Grid 三段式布局（grid-template-areas：侧栏/主区/底栏）
6. Pinia（Composition 风格 + TS）分 user/player/library store
7. Vue Router `meta.hideChrome` 控制登录页不套主框架 + 路由守卫
8. TS 类型严格对齐后端 DTO（不为 UI 造假字段；列表收藏状态靠 SQL JOIN 真实返回）
9. 磨砂玻璃 `backdrop-filter: blur` 统一封装进 GlassCard 组件
10. 工程协作：Claude 搭骨架(段1-2) → Codex 接力完成页面 → Claude 审计补漏，体现可交接性

### Phase 5 完成的可讲技术点（播放器 + 转换）

1. 静态文件服务：`WebMvcConfig` 将 `D:/soundprint-storage/` 映射到 `/files/**`，封面图由 `SmartCover + fileUrl()` 统一访问并失败回退。
2. 全局单例播放器：Pinia player store 持有唯一 `HTMLAudioElement`，统一播放状态、队列、进度、音量和历史上报。
3. 播放队列：列表页调用 `playTrack(track, queue, index)`，底栏上一首/下一首/单曲循环/列表循环都基于同一队列。
4. 音量持久化：`localStorage` 保存音量偏好，刷新页面后不重置。
5. wavesurfer.js：只负责波形渲染和点击定位，不作为第二个播放器。
6. LRC 歌词同步：正则解析 `[mm:ss.xx]歌词`，按当前播放秒数计算高亮行并自动滚动。
7. FFmpeg 集成：`ProcessBuilder` 参数数组调用系统 FFmpeg，解析 `Duration` 与 `time=` 进度。
8. 异步转换：`@EnableAsync + @Async` 让耗时转换在后台线程执行，Controller 立即返回任务 id。
9. 轮询进度：前端转换工坊每 0.8 秒查询任务状态，短任务足够简单稳定，暂不引入 WebSocket/SSE。
10. 歌单拖拽排序：`vuedraggable@next` 管 UI 顺序，释放后调用后端 `PUT /api/playlists/{id}/reorder` 持久化。

### Phase 6 完成的可讲技术点（统计页 + 图表 + 波形优化）

1. 设计令牌闭环：Phase 5 临时硬编码的 success/warning/danger/info 状态色，在 Phase 6 同步补进 `tailwind.config.js` 和 `tokens.scss`，转换工坊改用 CSS 变量，视觉不变但技术债清零。
2. ECharts 统一主题：`registerSoundprintTheme()` 在 `main.ts` 启动时注册，所有图表通过 `BaseChart` 使用 `soundprint-dark`，避免白底闪烁和重复配置。
3. `BaseChart` 抽象：唯一持有 `echarts.init/dispose/resize` 的组件，具体图表只传 `option`，降低内存泄漏和重复代码风险。
4. `shallowRef` 管 ECharts 实例：实例是第三方复杂对象，不需要 Vue 深度代理；用 `shallowRef` 只追踪引用变化，性能更稳。
5. 环形饼图：`radius: ['48%', '72%']` 形成 donut，中心留白，适合展示流派占比；数据来自后端聚合，不在前端伪造。
6. GitHub 风格热力图：ECharts `calendar + heatmap` 把 365 天播放行为映射成日历格子，适合展示长期习惯，视觉密度高。
7. 横向 Top 艺术家条形图：横向条形更适合艺术家名称这种长文本，`grid.containLabel` 保证标签不被裁剪。
8. KPI 翻牌动画：`requestAnimationFrame` + ease-out 插值，不用定时器；浏览器按刷新率调度，动画平滑且不阻塞。
9. PCM f32le 解码：后端不直接解析 FLAC/MP3，而让 FFmpeg 统一转成 raw PCM 32-bit float little-endian，Java 用位运算把 4 字节还原成 float，再按桶取绝对峰值。
10. 波形优化成本/收益：第一次 peaks 要跑 FFmpeg，耗时约 1 秒内；之后读文件缓存是毫秒级。前端只拿几 KB JSON，而不是为画波形下载几十 MB FLAC。
11. `notMerge/lazyUpdate`：`notMerge` 防止旧图表配置残留，`lazyUpdate` 把渲染延迟到下一帧批处理，适合 Vue 响应式数据更新。
12. ProcessBuilder 安全调用：peaks 与转换都使用参数数组调用 FFmpeg，不拼接 shell 字符串，文件名有空格也不会出错，同时规避命令注入。

### Phase 7 完成的可讲技术点（vue-bits 视觉爆点）

1. WebGL 与 Canvas 2D 的区别：Canvas 2D 是 CPU 按绘图命令逐步绘制，WebGL 把顶点和片段计算交给 GPU 并行执行；Galaxy/Balatro 这种大量像素级动效更适合 WebGL。
2. Fragment shader：Galaxy 和 Balatro 的核心视觉来自片段着色器，每个像素根据时间、鼠标、噪声和 hue 参数计算颜色；`uHueShift` 让默认青绿/红蓝配色统一转成 Soundprint 的紫青体系。
3. OGL vs Three：OGL 用在 Galaxy/Circular Gallery/Balatro，封装薄、适合 full-screen shader 和纹理画廊；Three 用在 Antigravity，利用 InstancedMesh 批量渲染粒子，开发成本更低。
4. WebGL 资源释放：组件只挂在具体路由内，包装层 `onBeforeUnmount` 调用 `WEBGL_lose_context`，避免路由反复切换后 WebGL context 累积。
5. 全屏三角形 shader：Galaxy/Balatro 用单个覆盖屏幕的大三角形跑 fragment shader，比两个三角形组成的矩形少一条边界和一次顶点处理，适合纯背景特效。
6. Circular Gallery 弧形数学：画廊通过 `R = (H^2 + B^2) / (2B)` 把平面卡片投到圆弧上，`bend` 控制弯曲强度；业务层只提供精选 12-15 条，避免纹理过多。
7. backdrop-filter 与 WebGL 叠层：登录页、首页 Hero、NowPlaying、Studio 都让 WebGL 做底层氛围，业务文字和控件放在磨砂浮层上，保证视觉冲击不牺牲可读性。
8. scoped CSS 的 `:deep`：Balatro 源码 canvas 有内部 z-index，父组件 scoped CSS 默认打不到子组件内部 DOM，所以包装层用 `:deep(canvas) { z-index: 0 !important; }` 把背景压到底层。
9. Circular Gallery 图片跨域：组件用原生 `Image` 加载纹理，封面 URL 必须经 `fileUrl()` 归一，并拼成同源 `/files/...`，配合 Vite 代理和后端 ResourceHandler，避免纹理加载失败。
10. 开源合规：核心 shader/动效来自 vue-bits，源码原样放入项目，README 与 `docs/vue-bits-references.md` 记录来源和协议；答辩时明确说明"引用开源核心，自己做配色、数据适配、生命周期和性能集成"。
11. 不加路由转场动画：Phase 7 只集成 Galaxy/Circular Gallery/Antigravity/Balatro 四件套，不额外增加路由转场，避免同时存在多个 WebGL 组件和动画栈。
12. Phase 8 可选低性能模式：如果答辩机性能差，优先把 Antigravity 粒子数从 200 降到 100，再把 Galaxy density 从 1.2 降到 0.8；本阶段先不做开关。

## ❓ 答辩可能被问到的问题与回答

（边开发边整理）

### Q: 你的数据库设计为什么这样分表？

A: 基于三范式和业务领域分析。把不同领域概念分开：用户、艺术家、专辑、
曲目、歌单是实体；标签和它们的关联用关系表；播放和转换是行为，单独记录。
这样的好处：消除数据冗余（一个艺术家信息只存一份）、支持多对多关系
（一首歌可以在多个歌单里）、便于做有意义的统计查询（按艺术家聚合）。

### Q: 为什么有 11 张表，会不会过度设计？

A: 不会。每张表对应一个明确的业务概念，缺一不可：
- 没有 artist/album 单独表，曲目元数据会有大量字符串重复
- 没有 playlist_track 中间表，无法实现"一首歌在多个歌单"
- 没有 play_history，无法做听歌统计
- 没有 conversion_task，转换进度无法持久化（页面刷新就丢）
反过来，每张表都能找到至少一个业务场景必须用到它。

### Q: 软删除和硬删除如何选择？

A: 业务实体用软删除，关系表用硬删除。理由：
- 实体被引用，硬删会破坏外键、丢失历史
- 关系表（如 playlist_track）本身就是中间记录，硬删等于解除关系
  不影响两端的实体
- 软删除的代价是查询要带 is_deleted = 0 过滤，MyBatis-Plus 自动处理

### Q: utf8 和 utf8mb4 有什么区别？为什么选 utf8mb4？

A: MySQL 的 utf8 实际上是 utf8mb3，最多 3 字节，无法表示完整 Unicode
（如 4 字节的 emoji 和部分生僻字）。utf8mb4 是真正的 UTF-8，4 字节完整。
音乐元数据国际化程度高，日文歌、希腊语标题都要支持，必须用 utf8mb4。

### Q: @SpringBootApplication 这一个注解做了什么？

A: 它是三个注解的合体：@Configuration（本身是配置类）、
@EnableAutoConfiguration（自动配置：发现 classpath 有 spring-web 就自动配
Tomcat，有 mysql 驱动就自动配数据源）、@ComponentScan（扫描本包及子包的
@Controller/@Service/@Component 并注册成 Bean）。体现 Spring Boot
"约定大于配置"的核心思想，省去传统 Spring 的大量 XML。

### Q: 为什么要分 Controller / Service / Mapper 三层？能不能在 Controller 里直接写 SQL？

A: 不能（不应该）。分层是单一职责：Controller 只管 HTTP 收发和参数校验，
Service 管业务逻辑和事务，Mapper 管数据访问。好处是逻辑可复用（一个业务方法
多个接口共用）、可单独测试、改一层不影响其他层。Controller 直接写 SQL 会
导致业务逻辑散落、无法复用、难维护。

### Q: 项目用 MyBatis-Plus，它和原生 MyBatis、JPA 区别在哪？为什么选它？

A: MP 是 MyBatis 的增强。原生 MyBatis 连单表 CRUD 都要手写 SQL；JPA 把 SQL
完全藏起来（面向对象，但复杂查询难控）。MP 折中：单表增删改查由内置 BaseMapper
零 SQL 完成，复杂查询仍可写 XML/注解 SQL。课程要求"会写 SQL"，MP 既省样板
又保留手写 SQL 的能力，比 JPA 更合适。

### Q: 前端调后端为什么会跨域？怎么解决的？

A: 浏览器的同源策略：协议/域名/端口任一不同就算跨域，默认拦截。前端 Vite 跑在
localhost:5173，后端在 localhost:8080，端口不同 = 跨域。解决：后端用
WebMvcConfigurer 配 CORS，显式允许 5173 等来源的请求，浏览器收到响应头里的
Access-Control-Allow-Origin 才放行。

### Q: 中间表是联合主键，MyBatis-Plus 怎么处理的？

A: MP 的 BaseMapper 假设单一主键，多个 @TableId 会直接报错。我的处理是每张
中间表只标一个 @TableId(type=IdType.INPUT)（INPUT 表示主键值由业务插入、
非自增），另一个主键列标 @TableField。数据库层面联合主键不变，中间表的
插入/删除走自定义查询（按两个外键定位），不依赖 getById 这类单主键方法。

### Q: Phase 7 的酷炫动效是你自己写的吗？

A: 核心 WebGL shader 和 Three.js 粒子组件引用自 vue-bits 开源项目，已在
README 和 `docs/vue-bits-references.md` 标明来源和协议。我负责的是工程集成：
把默认配色统一成 Soundprint 紫青体系，把业务数据适配成 Circular Gallery 的
纹理输入，处理封面 URL/CORS、z-index、路由卸载和 WebGL context 释放。

### Q: 为什么不把所有页面都加 WebGL 背景？

A: WebGL context 是浏览器稀缺资源，单页通常只有 8-16 个上下文上限。项目原则是
"同屏最多 1 个 WebGL 组件"：登录/首页用 Galaxy，列表页用 Circular Gallery，
播放详情用 Antigravity，转换 RUNNING 才用 Balatro。这样视觉足够强，同时不会
因为上下文累积导致黑屏或 context lost。
