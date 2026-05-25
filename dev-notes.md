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
- Phase 6（统计可视化）：待开始
- Phase 7（视觉精修）：待开始
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

- **2026-05-26 · 前端实际版本高于课程文档（Vite 8 / TS 6）**：`npm create vite@latest` 生成的是 Vite 8 + TypeScript 6 + Vue 3.5，而 CLAUDE.md/文档写的是 Vite 5 / TS 5。决定**保持不降级**——功能等价，且 **Tailwind 已锁 3.x**（vue-bits 兼容的关键项才是真正重要的）。代价：TS 6 对部分编译选项更严，`tsconfig.app.json` 加了 `"ignoreDeprecations": "6.0"` 才能过 `vue-tsc -b` 构建。答辩说辞："脚手架默认就是最新 Vite/TS，主版本号不影响技术栈本质，按需锁定的是 Tailwind 3 以兼容 vue-bits。"
- **2026-05-26 · 列表收藏状态需 JOIN 才能拿到**：`TrackResponse`（列表 DTO）原本没有收藏状态，音乐库无法显示心形。解决：`pageWithRelations` 加 `LEFT JOIN user_favorite uf ON uf.track_id=t.id AND uf.user_id=#{userId}`，SELECT `(uf.user_id IS NOT NULL) AS favorited`，前端据此渲染可切换心形。避免了"前端造假状态"。
- **2026-05-26 · @Async 不能在同一个类里自调用**：Phase 5 转换任务需要提交后立刻返回、后台跑 FFmpeg。直接在 `ConversionTaskServiceImpl` 里调用本类 `@Async` 方法会绕过 Spring 代理，异步不生效。解决：拆出 `ConversionExecutionService`，由提交服务调用另一个 Bean 的 `@Async executeConversion`，保证转换在线程池中执行。
- **2026-05-26 · FFmpeg 进度来自 stderr 而不是 stdout**：FFmpeg 的 `Duration` 和 `time=` 进度行输出在错误流。`FFmpegRunner` 用 `ProcessBuilder.redirectErrorStream(true)` 合并输出后按正则解析，总时长做分母、当前 `time=` 做分子，得到 0-100 进度。命令使用参数数组，不拼 shell 字符串，避免空格路径和命令注入。
- **2026-05-26 · wavesurfer 只画波形，不负责播放**：全局播放只允许 Pinia player store 里的单例 `HTMLAudioElement` 负责，wavesurfer 只 `load('/api/stream/{id}')` 做波形渲染和点击 seek。缺点是音频会被请求两次；好处是不会出现多个播放器同时播放、状态不同步的问题。
- **2026-05-26 · Phase 5 真实转换验证物证**：用 FFmpeg 生成 2 秒测试 MP3，经 `/api/tracks/upload` 上传为 trackId=32，再提交 `/api/conversions` 转 WAV，taskId=4 最终 `SUCCESS/100`，下载文件 352878 bytes，`ffprobe` 识别 `format_name=wav`、`duration=2.000000`。
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
