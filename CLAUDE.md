# Soundprint - 项目说明（Claude Code 长期记忆）

## 项目背景

这是东北大学软件工程专业大二《Web 开发技术》课程的大作业，**单人开发**，DDL 约 1-2 周。

项目名：**Soundprint（声纹）**
定位：个人无损音乐库 + 在线播放器 + 音频格式转换工坊

用户（开发者）背景：
- 软工大二学生
- 写过 Vue/React 小项目，前端基础语法没问题
- 后端 Spring 框架是这门课新学的，**需要重点讲解**
- TypeScript 是首次系统使用，需要在使用时给出类型相关解释
- 项目完成后要参加 5 分钟现场答辩（3 分钟演示 + 2 分钟回答），所以**每个技术点都必须搞懂**

## 评分目标：满分（60/60）

- 基础功能 30 分（5 个核心 CRUD，每个 6 分）
- 设计与答辩 10 分
- 创新 10 分（进阶任选 2 个 + 拓展任选 1 个）
- 实验报告 10 分

## 强制技术栈（课程要求，不能换）

**后端：**
- Java 17
- Spring Boot 3.2.x
- MyBatis-Plus 3.5.x
- MySQL 8.0
- Maven

**前端：**
- Vue 3.4+（Composition API + `<script setup>`）
- **TypeScript**（5.x）——为了无缝集成 vue-bits 组件库
- Vite 5.x
- Element-Plus 2.x（业务组件主力）
- **Tailwind CSS 3.x**（自定义样式 + 配合 vue-bits）
- **vue-bits**（开源动效组件库，用于视觉点缀）
- Pinia（状态管理）
- Vue Router 4
- Axios
- ECharts 5.x
- wavesurfer.js（波形可视化）

**部署 / 拓展：**
- Docker + Docker Compose（拓展 3.3 容器化部署）

**音频处理：**
- jaudiotagger（读取 ID3 标签）
- FFmpeg（系统命令调用进行格式转换）

## 📦 第三方资源使用声明（重要）

**本项目使用以下开源资源：**

1. **vue-bits**（https://github.com/DavidHDev/vue-bits）
   - License: MIT + Commons Clause
   - 使用方式：通过 jsrepo CLI 复制所需组件到项目源码中
   - 使用范围：仅用于视觉动效（首页 Hero、过渡动画、装饰背景），**不用于业务功能**
   - 必须保留：原始版权注释；在 README 和实验报告创新点章节明确标注引用

2. **Element-Plus、Vue、其他 npm 依赖**
   - 标准的 MIT/Apache 2.0 等开源协议
   - 按常规方式引用即可

**学术诚信原则：**
- 任何第三方代码进入项目，必须在 README 中"鸣谢"或"引用"章节列出
- 不在代码注释中假装原创
- 答辩时如被问到，必须诚实承认引用并能解释引用部分的工作原理及自己做的集成工作

## 项目结构

```
Soundprint/
├── CLAUDE.md                       # 本文件
├── README.md                       # 项目说明（含第三方引用列表）
├── docker-compose.yml              # 一键部署
├── docs/                           # 课程资料、ER 图等
├── backend/                        # 后端
│   ├── pom.xml
│   ├── Dockerfile
│   └── src/main/java/com/soundprint/
│       ├── SoundprintApplication.java
│       ├── controller/
│       ├── service/
│       ├── mapper/
│       ├── entity/
│       ├── dto/
│       ├── config/
│       └── util/
└── frontend/                       # 前端
    ├── package.json
    ├── tsconfig.json
    ├── vite.config.ts
    ├── tailwind.config.ts
    ├── Dockerfile
    └── src/
        ├── main.ts
        ├── App.vue
        ├── router/
        ├── stores/
        ├── api/
        ├── views/                  # 业务页面（自己写）
        ├── components/
        │   ├── business/           # 业务组件（自己写）
        │   └── vue-bits/           # 引入的 vue-bits 组件（保留版权头）
        └── assets/
```

## 核心功能（必须完成）

1. **音乐库**：上传、列表、搜索、编辑元数据、删除
2. **专辑 / 艺术家管理**：CRUD + 关联关系
3. **歌单**：创建、添加/移除歌曲、排序
4. **在线播放**：流式播放（HTTP Range Request），波形可视化
5. **格式转换工坊**：FLAC ↔ MP3 ↔ WAV ↔ AAC，可调比特率
6. **听歌统计**：ECharts 图表（流派分布、月度趋势、Top 艺术家、热力图）
7. **模糊搜索**：按歌曲名/艺术家/专辑

## 数据库设计（核心表 7-9 张）

- `user`（用户）
- `artist`（艺术家）
- `album`（专辑）
- `track`（曲目，核心表）
- `playlist`（歌单）
- `playlist_track`（歌单-曲目，多对多中间表）
- `tag`（标签）
- `track_tag`（曲目-标签，多对多中间表）
- `play_history`（播放历史）

关键关系：
- track 多对一 artist
- track 多对一 album
- album 多对一 artist
- track 多对多 playlist（中间表 playlist_track）
- track 多对多 tag（中间表 track_tag）
- user 一对多 play_history

## 🎨 vue-bits 使用边界（防止滥用）

**允许使用的位置：**
- ✅ 登录页 / 注册页：背景动效 + 标题文字动效
- ✅ 首页 Hero 区：粒子背景、品牌标语动效
- ✅ 加载页 / 路由过渡动画
- ✅ 转换工坊：转换进行中的视觉反馈
- ✅ 统计页：标题入场动画
- ✅ 空状态提示（无数据时的装饰图）

**禁止使用的位置：**
- ❌ 业务表格、表单（用 Element-Plus）
- ❌ 数据密集页面（库列表、歌单详情）
- ❌ 播放器核心 UI（用 wavesurfer.js + 自定义）
- ❌ 模态框、确认框、Toast（用 Element-Plus）

**性能约束：**
- 同一页面同时启用 vue-bits 动效组件不超过 2 个
- 重型组件（粒子背景、WebGL 类）只在首页/登录页使用
- 移动端要降级或关闭动画

**引用规范：**
- 每个从 vue-bits 复制进来的组件，文件顶部加注释：
  ```
  // 引用自 vue-bits (https://github.com/DavidHDev/vue-bits)
  // License: MIT + Commons Clause
  // 本项目对该组件做了以下修改：xxx
  ```

## 编码规范

**通用：**
- 注释和文档**统一用中文**，符合答辩场景
- 命名清晰，宁可长一点也不要缩写到看不懂
- 每个新模块完成后必须 commit 一次（小步快跑）

**后端：**
- 包名小写：`com.soundprint.controller`
- 类名大驼峰：`TrackController`、`TrackService`
- 方法名小驼峰：`getTrackById`
- 数据库字段用下划线：`created_at`，Java 字段用小驼峰：`createdAt`，MyBatis-Plus 配置自动映射
- Controller 返回统一用 `Result<T>` 包装类（成功 code 200，失败 400/500，附 message）
- 异常用全局异常处理器 `@RestControllerAdvice` 统一捕获
- DTO 与 Entity 分离：Controller 接收/返回 DTO，Service 内部用 Entity

**前端：**
- 组件名大驼峰：`TrackList.vue`、`PlayerBar.vue`
- TypeScript：interface 定义放在 `src/types/` 下按模块分文件
- 用 Composition API + `<script setup lang="ts">`，不要用 Options API
- API 请求统一封装在 `src/api/` 下，按模块分文件，每个 API 函数有明确的入参/返回类型
- 全局状态用 Pinia（带类型），组件局部状态用 ref/reactive
- 样式：业务组件用 SCSS + Element-Plus 主题变量；装饰组件用 Tailwind
- Tailwind 不要和 SCSS 混在一个 `<style>` 块里，分清楚

## 必须遵守的"作业规则"

1. **绝对不要装无关依赖**。课程要求的栈不能换、不能简化（比如不许用 JPA 替代 MyBatis-Plus）
2. **数据库字段至少 6 个**（track 表远不止这个数，但要确认每张表都满足）
3. **界面至少 10 个**（前端路由清单要核对）
4. **提交时只交 src 文件夹**，所以 src 内部结构要清晰自包含
5. **不许使用任何破解、解密、绕过 DRM 的代码**。本项目处理的是用户合法持有的 FLAC/MP3 等通用格式
6. **第三方引用必须显式标注**（见上面的资源声明章节）

## 🌟 核心工作模式：边写边讲

这是这个项目最重要的规则，**优先级最高**。

**用户是答辩制学生，不是甩手掌柜**。每完成一个有技术含量的模块（不是改个 CSS 颜色这种），必须主动用通俗中文给用户讲清楚：

1. **这段代码在做什么**（功能层面）
2. **关键技术点是什么**（原理层面）
3. **答辩时如果老师问"这里你为什么这么做"，应该怎么回答**（答辩话术）

讲解时机：
- 后端写完一个 Controller + Service 链路 → 讲
- 实现流式播放接口 → 必须讲 HTTP Range Request 原理
- 集成 FFmpeg → 必须讲 ProcessBuilder、异步任务、子进程通信
- 写多表 JOIN 查询 → 讲为什么这么 JOIN、有没有 N+1 问题
- Docker 化 → 必须讲容器编排、网络、数据卷
- 引入新依赖 → 讲这个依赖解决什么问题，不引入会怎样
- 引入 vue-bits 组件 → 讲该组件大致原理（CSS 动画？WebGL？Canvas？），方便答辩时回答
- 写 TypeScript 复杂类型 → 讲为什么这么定义类型

讲解格式：写完代码后**附一个"📚 给作者的讲解"小节**，简明但实质。
不需要每行都讲，但每个技术决策点都要讲。

## 任务推进规则

1. **任何破坏性操作前先确认**：删除文件、重置数据库、改 schema、强推 git 都要先问用户
2. **小步快跑**：每完成一个可验证的小功能就 commit，commit message 用中文，格式如 "feat: 完成 track 表的基础 CRUD"
3. **遇到歧义先问再做**，不要自己猜需求
4. **报错优先看完整 stack trace**，不要只看第一行就开始猜
5. **写完代码主动建议测试方法**（curl 命令、postman、前端点击路径）
6. **引入 vue-bits 组件前先问用户哪个页面**，确认不违反使用边界

## 开发节奏（10 天计划）

- Day 1：环境 + 数据库 + 双端骨架（前端含 TS + Tailwind + Element-Plus 三件套配通）
- Day 2：后端 entity/mapper/service/controller 基础 CRUD
- Day 3：文件上传 + 元数据读取 + 流式播放接口
- Day 4：前端布局 + 路由 + 库/专辑/艺术家页面（用 Element-Plus）
- Day 5：播放器底栏（wavesurfer.js）+ 歌单
- Day 6：FFmpeg 格式转换
- Day 7：ECharts 统计页 + 首页（首页开始引入 vue-bits 装饰）
- Day 8：UI 精修 + vue-bits 视觉点缀（登录页、转场、Hero 区）
- Day 9：Docker 化 + 联调
- Day 10：实验报告 + 答辩排练

## 实验报告

最后要按学校模板写报告，模板在 `docs/` 下。报告需要包含：
- 实验目的、内容、环境
- 过程与分析（**重点**，要有截图和代码片段）
- 创新点（**必须列出 vue-bits、TypeScript、Tailwind、Docker、FFmpeg 等技术选型理由**）
- 引用与鸣谢（vue-bits 等第三方资源）
- 总结

开发过程中遇到值得记录的设计决策、技术难点、解决方案，**主动建议用户记录到一个 `dev-notes.md` 文件里**，最后整理进报告会很轻松。

## 禁止事项

- ❌ 不要主动修改 `CLAUDE.md` 本文件，除非用户明确要求
- ❌ 不要把 `node_modules`、`target`、音频文件、`.idea` 等提交进 git
- ❌ 不要在代码里硬编码密码、密钥（用 `application.yml` 或环境变量）
- ❌ 不要使用任何破解版工具或盗版资源
- ❌ 不要在没说清楚的情况下大规模重构已有代码
- ❌ 不要在业务核心组件里塞 vue-bits 动效（用错地方）
- ❌ 不要去掉 vue-bits 组件文件顶部的版权注释
