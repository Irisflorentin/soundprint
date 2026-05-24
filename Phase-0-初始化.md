# Phase 0：GitHub 仓库与项目初始化

> 这是 Soundprint 项目的第一个阶段文档。
> 把整份文档完整复制粘贴给 Claude Code，作为它的任务指令。

---

## 🎯 阶段目标

完成项目骨架的初始化：在本地建好目录结构、初始化 git 仓库、创建 GitHub 远程仓库、推送首次 commit。**不写任何业务代码，只搭骨架**。

---

## 📋 任务清单

请按顺序完成以下任务。每完成一项小任务，主动报告进度。

### 任务 1：核对当前位置

1. 通过 `pwd` 确认当前工作目录是 `D:\Claude_Playground\Soundprint`
2. 列出当前目录内容，确认 `CLAUDE.md` 已存在
3. 如果还没读过 `CLAUDE.md`，先完整读一遍——这是项目长期记忆，必须先理解
4. 同时读取 `docs/` 下的实验指导书和验收注意事项，确认你理解课程要求

### 任务 2：创建基础目录结构

在项目根目录创建以下空目录（暂时不创建里面的代码文件）：

```
Soundprint/
├── docs/                # 已有，存课程资料和 ER 图等
└── （backend、frontend 留到后续阶段再建）
```

确保 `docs/` 目录存在。如果用户上传的资料文件已经在 `docs/` 里，确认一下。如果在根目录，移动到 `docs/`。

### 任务 3：创建根目录关键文件

创建以下三个文件，内容详见后面的"⚙️ 技术细节"部分：

1. `.gitignore` ——按本文档提供的内容创建
2. `README.md` ——按本文档提供的模板创建，内容用中文
3. `dev-notes.md` ——按本文档提供的模板创建，空白结构即可

### 任务 4：初始化本地 git 仓库

1. `git init`
2. 设置默认分支为 `main`：`git branch -M main`
3. 配置 user.name 和 user.email（如果还没配过全局的话，问用户要）
4. 执行 `git status` 确认追踪状态

**不要立刻 commit，等任务 5、6 完成后再一起 commit。**

### 任务 5：核对环境（关键！）

执行以下命令，把所有版本号汇总输出给用户：

```bash
java -version
mvn -version
node -v
npm -v
mysql --version
docker --version
ffmpeg -version
git --version
```

**判断标准**（如果不达标，停下来报告给用户，不要继续）：
- Java ≥ 17
- Node ≥ 18
- MySQL ≥ 8.0
- Docker 能运行
- FFmpeg 存在
- Git 存在

### 任务 6：检查 MySQL 连接

询问用户 MySQL 的 root 密码，然后执行：

```bash
mysql -u root -p<password> -e "SHOW DATABASES;"
```

确认能连通即可，**不要现在就建数据库**（Phase 1 才建）。

### 任务 7：首次 commit

确认所有文件都创建完毕、`.gitignore` 内容正确（特别注意 `node_modules`、`target`、`*.flac`、`*.mp3` 等大文件类型已经排除），然后：

```bash
git add .
git commit -m "chore: 初始化项目结构与说明文档（Phase 0）"
```

### 任务 8：等待用户创建 GitHub 远程仓库

向用户提供以下指引（请用通俗中文输出给用户）：

> 请你现在去 GitHub 创建一个新仓库：
> 1. 登录 GitHub，点右上角"+" → "New repository"
> 2. 仓库名建议：`soundprint`（小写，跟项目名一致）
> 3. 描述可填：`A personal lossless music library with format conversion. 东北大学 Web 开发技术大作业`
> 4. 选择 Private 或 Public 都行（建议 Public，方便简历展示，但敏感配置别提交）
> 5. **不要勾选** "Initialize this repository with a README"（我们本地已经有了）
> 6. 创建完成后，把仓库的 SSH 或 HTTPS 地址发给我，例如：
>    - SSH: `git@github.com:你的用户名/soundprint.git`
>    - HTTPS: `https://github.com/你的用户名/soundprint.git`

等用户提供 URL 后，执行：

```bash
git remote add origin <用户提供的 URL>
git push -u origin main
```

如果推送失败（比如认证问题、SSH key 没配等），引导用户解决。

---

## ⚙️ 技术细节 / 文件内容

### `.gitignore` 内容

```gitignore
# ========== 操作系统 ==========
.DS_Store
Thumbs.db
desktop.ini

# ========== IDE ==========
.idea/
.vscode/
*.iml
*.suo
*.swp
*.swo

# ========== Node / 前端 ==========
node_modules/
dist/
.vite/
*.log
npm-debug.log*
yarn-debug.log*
yarn-error.log*
.env.local
.env.*.local

# ========== Java / 后端 ==========
target/
*.class
*.jar
*.war
*.ear
.mvn/
mvnw
mvnw.cmd
HELP.md
*.log

# ========== Spring Boot ==========
application-local.yml
application-local.properties

# ========== 数据库 / 敏感配置 ==========
*.sql.bak
.env
secret.yml
secrets/

# ========== 上传的音频文件 ==========
# 业务上传的音频不进仓库
uploads/
storage/
*.flac
*.mp3
*.wav
*.aac
*.m4a
*.ogg

# ========== Docker ==========
.docker/

# ========== 临时文件 ==========
tmp/
temp/
*.tmp
```

### `README.md` 模板

```markdown
# Soundprint 🎵

> 个人无损音乐库 + 在线播放器 + 音频格式转换工坊
> 东北大学软件工程专业 2026 春《Web 开发技术》课程大作业

## ✨ 项目简介

Soundprint 是一个面向音乐爱好者的个人音乐管理系统，支持：

- 上传本地音乐文件（FLAC / MP3 / WAV / AAC 等）
- 自动读取并管理 ID3 元数据
- 在线流式播放，带波形可视化
- 多种音频格式之间相互转换（基于 FFmpeg）
- 个人歌单管理、模糊搜索
- 听歌数据可视化（ECharts）

## 🛠 技术栈

**后端**
- Java 17 / Spring Boot 3.2 / MyBatis-Plus 3.5
- MySQL 8.0
- jaudiotagger（元数据） / FFmpeg（转码）

**前端**
- Vue 3 + TypeScript + Vite
- Element-Plus（业务组件） + Tailwind CSS（样式）
- vue-bits（动效组件库，用于视觉点缀）
- ECharts（数据可视化）
- wavesurfer.js（波形）

**部署**
- Docker + Docker Compose

## 🚀 快速开始

（等开发到 Phase 8 Docker 化后填写）

## 📁 项目结构

（等后续阶段填写）

## 🙏 引用与鸣谢

本项目使用以下开源资源，特此致谢：

- [Vue.js](https://vuejs.org/) - 渐进式 JavaScript 框架（MIT）
- [Element-Plus](https://element-plus.org/) - Vue 3 组件库（MIT）
- [vue-bits](https://github.com/DavidHDev/vue-bits) - Vue 动效组件库（MIT + Commons Clause）
- [Spring Boot](https://spring.io/projects/spring-boot) - Java 应用框架（Apache 2.0）
- [MyBatis-Plus](https://baomidou.com/) - MyBatis 增强工具（Apache 2.0）
- [FFmpeg](https://ffmpeg.org/) - 音视频处理工具（LGPL/GPL）
- [ECharts](https://echarts.apache.org/) - 数据可视化（Apache 2.0）
- [wavesurfer.js](https://wavesurfer.xyz/) - 音频波形可视化（BSD-3-Clause）

具体的 vue-bits 组件引用清单见 `docs/vue-bits-references.md`。

## 📝 开发者

姓名 · 班级 · 学号（提交前填写）

## 📄 License

本项目为课程作业，仅供学习交流。引用的第三方资源各自遵循其原协议。
```

### `dev-notes.md` 模板

```markdown
# 开发笔记 · Soundprint

> 本文件用于记录开发过程中的设计决策、技术难点、踩坑记录。
> 在 Phase 8 撰写实验报告时，这里的内容会被整理进报告"过程与分析"章节。

## 📅 时间线

- Phase 0（初始化）：YYYY-MM-DD 完成
- Phase 1（数据库设计）：待开始
- Phase 2（后端骨架）：待开始
- Phase 3（后端业务）：待开始
- Phase 4（前端业务）：待开始
- Phase 5（播放器+转换）：待开始
- Phase 6（统计可视化）：待开始
- Phase 7（视觉精修）：待开始
- Phase 8（部署+报告）：待开始

## 🎯 设计决策记录

### 为什么选 TypeScript？

（开发过程中遇到再填）

### 为什么 9 张表而不是 3 张？

（开发过程中遇到再填）

## 🐛 踩坑记录

（按时间倒序记录）

## 💡 技术亮点

（写报告时直接复制到"创新点"章节）

## ❓ 答辩可能被问到的问题与回答

（边开发边整理）
```

---

## 📚 边写边讲要求

在本阶段你需要给用户讲清楚的内容：

1. **`.gitignore` 为什么这么写**——尤其是为什么排除音频文件（仓库会爆炸 + 文件版权风险）
2. **`git init` 之后的工作流**——本地仓库、暂存区、远程仓库三者关系（这是答辩可能会被问的基础题）
3. **`git push -u origin main` 中 `-u` 是什么意思**——设置上游分支，后续可以直接 `git push`
4. **如果用户的 Node/Java 版本不达标，给出 nvm/SDKMAN 或换装的建议**——但不要擅自帮用户卸载重装，只提建议让用户决定

---

## ✅ 完成检查清单

向用户依次核对：

- [ ] `D:\Claude_Playground\Soundprint\` 下有 `CLAUDE.md`、`README.md`、`.gitignore`、`dev-notes.md`、`docs/`
- [ ] `docs/` 下有 3 个原始的课程资料 docx 文件
- [ ] `git status` 显示工作区干净，已 commit
- [ ] 环境核验全部通过（特别是 Java 17+、Node 18+、FFmpeg 存在）
- [ ] MySQL 可连接
- [ ] GitHub 远程仓库已创建并 push 成功
- [ ] 在 GitHub 网页上能看到 README.md 内容正常显示

---

## 🚀 Git Commit 指令

本阶段的 commit 已经在任务 7 完成。push 在任务 8 完成。

如果中途有补充修改，最后再补一次：

```bash
git add .
git commit -m "chore: Phase 0 补充修订"
git push
```

---

## 📩 反馈给架构师的内容

完成本阶段后，请把以下信息整理后反馈：

1. **环境核验输出**（全部命令的版本号）
2. **GitHub 仓库地址**
3. **任何报错或卡壳**（哪怕已经解决了也讲一下，方便复盘）
4. **Claude Code 给你的讲解你有没有听不懂的地方**——这是判断我们工作模式有没有跑通的关键
5. **是否准备好进入 Phase 1（数据库设计与建表）**

---

## ⚠️ 注意事项

- **本阶段不要写任何业务代码，不要 `npm install` 任何依赖，不要 `mvn` 任何东西**
- **不要建 backend/ 和 frontend/ 目录**，那是 Phase 2 和 Phase 4 的事
- **不要建数据库**，Phase 1 才做
- 如果用户的 `CLAUDE.md` 有任何与本阶段文档冲突的地方，**以 `CLAUDE.md` 为准**，并提醒用户
- 如果遇到本文档没说清楚的情况，**先停下来问用户**，不要自己猜

---

**End of Phase 0 Document.**
