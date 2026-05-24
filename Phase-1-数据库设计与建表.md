# Phase 1：数据库设计与建表

> Soundprint 项目的第二个阶段文档。
> 把整份文档完整复制粘贴给 Claude Code，作为它的任务指令。
> 本阶段产出后将拿到课程评分中【进阶 2.2 · 数据库进阶】的 3 分。

---

## 🎯 阶段目标

1. 确定使用机器上的哪个 MySQL 实例（8.0 或 9.4），探测端口
2. 创建数据库 `soundprint`（utf8mb4 字符集）
3. 创建 11 张业务表，含外键、索引、注释、审计字段、软删除
4. 导入种子数据（约 10 个艺术家、15 张专辑、30 首曲目、5 个标签、3 个歌单、若干播放记录），保证开发期间页面"有内容可看"
5. 用 DataGrip 连接数据库、导出 ER 图存到 `docs/ER图.png`
6. 在 `dev-notes.md` 记录关键设计决策，方便答辩与报告写作

**重要**：本阶段只动数据库，不写 Java 代码、不建 backend/ 目录、不动前端。

---

## 📋 任务清单

### 任务 1：核对工作目录

1. 通过 `Get-Location` 确认当前在 `D:\Claude_Playground\Soundprint`
2. 用 `Get-ChildItem -Force` 列出当前目录，确认 `CLAUDE.md`、`README.md`、`.gitignore`、`dev-notes.md`、`docs/` 都在
3. 提示：本项目所有 shell 操作**默认走 PowerShell**，不要尝试 cmd / bash 语法（你上次有过一次失误）

### 任务 2：探测 MySQL 实例并选定

用户机器上同时存在 MySQL 8.0（服务名 `MySQL80`）和 MySQL 9.4（服务名 `MySQL94`）。**课程要求 MySQL 8.0**，所以必须用 `MySQL80`。

执行以下命令探测两个实例的端口：

```powershell
# 查所有 MySQL 服务和它们的可执行路径
Get-CimInstance Win32_Service -Filter "Name like 'MySQL%'" |
  Select-Object Name, State, StartMode, PathName | Format-List

# 查每个 my.ini 配置文件中的端口
$paths = @(
  "C:\ProgramData\MySQL\MySQL Server 8.0\my.ini",
  "C:\ProgramData\MySQL\MySQL Server 9.4\my.ini",
  "C:\Program Files\MySQL\MySQL Server 8.0\my.ini",
  "C:\Program Files\MySQL\MySQL Server 9.4\my.ini"
)
foreach ($p in $paths) {
  if (Test-Path $p) {
    Write-Output "=== $p ==="
    Select-String -Path $p -Pattern "^port\s*=" | ForEach-Object { $_.Line }
  }
}

# 看哪些端口在监听
Get-NetTCPConnection -LocalPort 3306,3307,3308 -ErrorAction SilentlyContinue |
  Select-Object LocalPort, State, OwningProcess | Format-Table
```

**判断规则**：
- 默认情况下，MySQL 8.0 在 3306，第二个实例 9.4 在 3307（或安装时自动避让的端口）
- 如果发现不是默认端口，**停下来报告给用户**，让用户选择/确认

把探测结果用一个清晰的小表格输出给用户：

```
┌───────────┬───────┬─────────┬─────────────┐
│ 实例      │ 端口  │ 状态    │ 我们用它吗  │
├───────────┼───────┼─────────┼─────────────┤
│ MySQL80   │ ???   │ Running │ ✅ 是       │
│ MySQL94   │ ???   │ Running │ ❌ 否       │
└───────────┴───────┴─────────┴─────────────┘
```

### 任务 3：找到 mysql.exe 路径并验证连接

`mysql` 命令不在 PATH 里，需要直接用绝对路径。常见位置：

```
C:\Program Files\MySQL\MySQL Server 8.0\bin\mysql.exe
```

如果不在那里，用：

```powershell
Get-ChildItem "C:\Program Files\MySQL" -Recurse -Filter "mysql.exe" -ErrorAction SilentlyContinue |
  Select-Object FullName
```

找到后，让用户输入 root 密码（**用 PowerShell 的 `Read-Host -AsSecureString` 安全读取，不要让密码出现在终端历史和命令参数里**）：

```powershell
$cred = Read-Host -AsSecureString "请输入 MySQL 8.0 的 root 密码"
$BSTR = [System.Runtime.InteropServices.Marshal]::SecureStringToBSTR($cred)
$plain = [System.Runtime.InteropServices.Marshal]::PtrToStringAuto($BSTR)
# 注意：$plain 仅在本次进程内存中，结束后即失效，不写入任何文件
```

验证连接（使用 MYSQL_PWD 环境变量传递密码，避免出现在命令行参数和进程列表里）：

```powershell
$env:MYSQL_PWD = $plain
& "C:\Program Files\MySQL\MySQL Server 8.0\bin\mysql.exe" -u root -P <端口号> -e "SELECT VERSION();"
```

**执行成功后立即清除变量**：
```powershell
Remove-Item Env:MYSQL_PWD
```

预期输出版本应该是 `8.0.x`。如果连接失败：
- 错误 1045（密码错）→ 让用户重新输入
- 错误 2003（连不上）→ 检查端口和服务状态
- 错误 1049（数据库不存在）→ 这是正常的，因为还没建数据库

### 任务 4：创建数据库

把以下 SQL 写到 `D:\Claude_Playground\Soundprint\docs\sql\01-create-database.sql`（先创建 `docs/sql/` 目录）：

```sql
-- ============================================================
-- Soundprint 数据库初始化
-- ============================================================
-- 字符集 utf8mb4 必须，因为：
--   1. 支持完整 Unicode，包括日文/韩文/俄文专辑名、emoji
--   2. utf8 在 MySQL 中是阉割版（最多 3 字节），utf8mb4 才是真正的 UTF-8
-- 排序规则 utf8mb4_unicode_ci：标准 Unicode 排序，对中文友好，大小写不敏感
-- ============================================================

DROP DATABASE IF EXISTS soundprint;
CREATE DATABASE soundprint
  DEFAULT CHARACTER SET utf8mb4
  DEFAULT COLLATE utf8mb4_unicode_ci;

USE soundprint;

-- 设置时区为东八区，使所有 DATETIME 字段反映北京时间
SET time_zone = '+08:00';
```

执行：
```powershell
$env:MYSQL_PWD = $plain
& "C:\Program Files\MySQL\MySQL Server 8.0\bin\mysql.exe" -u root -P <端口号> < D:\Claude_Playground\Soundprint\docs\sql\01-create-database.sql
Remove-Item Env:MYSQL_PWD
```

### 任务 5：创建 11 张业务表

把以下 SQL 写到 `D:\Claude_Playground\Soundprint\docs\sql\02-create-tables.sql`，内容见下方"⚙️ 技术细节"完整 DDL。

执行该脚本（继续用 `$env:MYSQL_PWD` 方式），命令前后注意环境变量的清理。

**重要约定**：所有 DDL **必须**包含：
- `COMMENT` 说明（中英文皆可，本项目用中文）
- `created_at` 和 `updated_at` 审计字段
- 业务实体表（user / artist / album / track / playlist）有 `is_deleted` 软删除字段
- 适当的索引（每张表 README 注释里说明索引设计的考虑）
- 合理的外键约束（部分跨表关系不加外键，仅靠应用层维护，原因见技术细节）

### 任务 6：导入种子数据

把以下 SQL 写到 `D:\Claude_Playground\Soundprint\docs\sql\03-seed-data.sql`，内容见下方"⚙️ 技术细节"完整种子。

执行后验证记录数：

```sql
SELECT 'user' AS tbl, COUNT(*) AS cnt FROM user UNION ALL
SELECT 'artist', COUNT(*) FROM artist UNION ALL
SELECT 'album', COUNT(*) FROM album UNION ALL
SELECT 'track', COUNT(*) FROM track UNION ALL
SELECT 'playlist', COUNT(*) FROM playlist UNION ALL
SELECT 'playlist_track', COUNT(*) FROM playlist_track UNION ALL
SELECT 'tag', COUNT(*) FROM tag UNION ALL
SELECT 'track_tag', COUNT(*) FROM track_tag UNION ALL
SELECT 'play_history', COUNT(*) FROM play_history UNION ALL
SELECT 'conversion_task', COUNT(*) FROM conversion_task UNION ALL
SELECT 'user_favorite', COUNT(*) FROM user_favorite;
```

预期所有表都有数据。

### 任务 7：跑一个验证查询（多表 JOIN，验证关系正确）

执行下面这个查询并把结果展示给用户。这是答辩演示时也可以用的"经典多表 JOIN"：

```sql
USE soundprint;

-- 查询：列出所有曲目及其专辑名、艺术家名、标签列表（多表 JOIN + GROUP_CONCAT）
SELECT
  t.id                                       AS track_id,
  t.title                                    AS track_title,
  ar.name                                    AS artist,
  al.title                                   AS album,
  t.duration_seconds                         AS duration,
  t.format,
  GROUP_CONCAT(tg.name SEPARATOR ', ')       AS tags
FROM track t
LEFT JOIN artist ar  ON t.artist_id = ar.id
LEFT JOIN album  al  ON t.album_id  = al.id
LEFT JOIN track_tag tt ON tt.track_id = t.id
LEFT JOIN tag    tg  ON tg.id = tt.tag_id
WHERE t.is_deleted = 0
GROUP BY t.id, t.title, ar.name, al.title, t.duration_seconds, t.format
ORDER BY t.id
LIMIT 20;
```

预期能看到曲目、艺术家、专辑、标签一一对应，**没有任何 NULL 或乱码**。如果有乱码，立刻报告——可能是字符集没设对。

### 任务 8：DataGrip 连接配置说明

向用户输出 DataGrip 连接配置说明（不要执行命令，只是引导用户操作）：

```
请打开 DataGrip 并按以下步骤连接：

1. File → New → Data Source → MySQL
2. 在配置面板填写：
   - Name: Soundprint (Local MySQL 8.0)
   - Host: localhost
   - Port: <探测到的 MySQL 8.0 端口>
   - User: root
   - Password: 你的 root 密码
   - Database: soundprint
3. 点 "Test Connection" 确认 ✅
4. 如果第一次用，会提示下载 MySQL 8 驱动，点同意下载
5. 连接成功后，左侧 schema 树展开 soundprint，应该能看到 11 张表

连接成功后请告诉我，我们进入下一步生成 ER 图。
```

### 任务 9：用 DataGrip 生成 ER 图

向用户输出操作指引：

```
在 DataGrip 中生成 ER 图的步骤：

1. 左侧 schema 树右键 soundprint 数据库
2. 选 "Diagrams" → "Show Visualisation"
3. DataGrip 会自动生成所有表的关系图
4. 调整布局：用鼠标拖动各张表卡片，让外键箭头清晰不交叉
5. 右键空白区域 → "Export to File" → 选 PNG 格式
6. 保存路径：D:\Claude_Playground\Soundprint\docs\ER图.png

完成后告诉我，我会验证文件存在并尺寸合理。
```

用户保存后，验证文件：
```powershell
Get-Item D:\Claude_Playground\Soundprint\docs\ER图.png |
  Select-Object FullName, Length, LastWriteTime
```

文件大小至少应该 > 50 KB（说明是真实的 PNG 不是空文件）。

### 任务 10：更新 dev-notes.md

把下面这一段（以三个反引号包围的"设计决策记录"内容）追加到 `dev-notes.md` 的相应章节下：

在【🎯 设计决策记录】下追加：

```
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
```

在【💡 技术亮点】下追加：

```
### Phase 1 完成的可讲技术点

1. utf8mb4 字符集选择（vs utf8）
2. 三范式遵循（消除冗余、消除传递依赖）
3. 软删除 + 审计字段（is_deleted、created_at、updated_at、deleted_at）
4. 多对多关系建模（playlist_track 含 order_index 排序字段、track_tag）
5. JSON 字段扩展性预留（track.extra_metadata 存非结构化元数据）
6. 行为表与实体表分离（play_history、conversion_task 与 track 解耦）
7. 索引设计：B+ 树索引在外键 + 业务高频查询字段
8. 时区统一为 +08:00
```

在【❓ 答辩可能被问到的问题与回答】下追加：

```
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
```

### 任务 11：commit 和 push

确认所有文件就位（4 个 SQL、1 个 PNG、更新后的 dev-notes.md），执行：

```powershell
cd D:\Claude_Playground\Soundprint
git add docs/sql/ docs/ER图.png dev-notes.md

# 用 -F 临时文件方式提交，避免 PowerShell 中文乱码
"feat: 完成数据库设计与建表（Phase 1）

- 创建 soundprint 数据库（utf8mb4）
- 建立 11 张业务表，含外键、索引、软删除、审计字段
- 导入种子数据（10 艺术家/15 专辑/30 曲目/5 标签/3 歌单）
- 导出 ER 图至 docs/ER图.png
- 更新 dev-notes.md 记录设计决策" | Out-File -FilePath "D:\Claude_Playground\_commitmsg.txt" -Encoding utf8

git commit -F "D:\Claude_Playground\_commitmsg.txt"
Remove-Item "D:\Claude_Playground\_commitmsg.txt"
git push
```

---

## ⚙️ 技术细节 / 文件内容

### 完整建表 SQL（`02-create-tables.sql`）

```sql
-- ============================================================
-- Soundprint 数据库建表脚本
-- 共 11 张表，遵循三范式 + 业务领域设计
-- ============================================================

USE soundprint;

-- ============================================================
-- 表 1：user 用户表
-- ============================================================
-- 设计说明：
-- 当前业务为单用户，但所有数据通过 user_id 关联，未来可扩展为多用户。
-- 密码字段长度 100，预留 BCrypt（60 字符）或其他哈希算法空间。
-- 当前阶段不要求加密，最小化用户系统直接明文或 SHA-256。
-- ============================================================
CREATE TABLE user (
  id          BIGINT       AUTO_INCREMENT PRIMARY KEY                COMMENT '主键',
  username    VARCHAR(50)  NOT NULL UNIQUE                            COMMENT '用户名（登录用，全局唯一）',
  password    VARCHAR(100) NOT NULL                                   COMMENT '密码（建议存哈希）',
  nickname    VARCHAR(50)                                             COMMENT '昵称（界面显示）',
  avatar_url  VARCHAR(500)                                            COMMENT '头像 URL（相对路径或外链）',
  bio         VARCHAR(255)                                            COMMENT '个人简介',
  is_deleted  TINYINT      NOT NULL DEFAULT 0                         COMMENT '软删除标志：0=正常，1=已删除',
  deleted_at  DATETIME                                                COMMENT '软删除时间',
  created_at  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP         COMMENT '创建时间',
  updated_at  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP
                           ON UPDATE CURRENT_TIMESTAMP                COMMENT '更新时间',
  INDEX idx_username (username)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- ============================================================
-- 表 2：artist 艺术家表
-- ============================================================
-- 设计说明：
-- artist 是共享实体（多个用户共用同一个艺术家信息），所以不带 user_id。
-- name 加唯一索引避免重复创建（如有两个不同的 "Beatles" 应该用编号区分而不是建两条）。
-- ============================================================
CREATE TABLE artist (
  id          BIGINT       AUTO_INCREMENT PRIMARY KEY                COMMENT '主键',
  name        VARCHAR(100) NOT NULL UNIQUE                            COMMENT '艺术家名称',
  bio         TEXT                                                    COMMENT '艺术家简介',
  avatar_url  VARCHAR(500)                                            COMMENT '艺术家头像 URL',
  country     VARCHAR(50)                                             COMMENT '国籍/地区',
  formed_year INT                                                     COMMENT '成立/出道年份',
  is_deleted  TINYINT      NOT NULL DEFAULT 0,
  deleted_at  DATETIME,
  created_at  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  INDEX idx_name (name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='艺术家表';

-- ============================================================
-- 表 3：album 专辑表
-- ============================================================
-- 设计说明：
-- album 多对一 artist。删除 artist 时不级联删 album，
-- 而是把 album.artist_id 置 NULL（artist 变成"未知艺术家"）。
-- ============================================================
CREATE TABLE album (
  id           BIGINT       AUTO_INCREMENT PRIMARY KEY               COMMENT '主键',
  title        VARCHAR(200) NOT NULL                                  COMMENT '专辑名称',
  artist_id    BIGINT                                                 COMMENT '所属艺术家 ID',
  cover_url    VARCHAR(500)                                           COMMENT '专辑封面 URL',
  release_year INT                                                    COMMENT '发行年份',
  genre        VARCHAR(50)                                            COMMENT '流派（Rock/Pop/Jazz/Classical 等）',
  description  TEXT                                                   COMMENT '专辑介绍',
  is_deleted   TINYINT      NOT NULL DEFAULT 0,
  deleted_at   DATETIME,
  created_at   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  INDEX idx_artist_id (artist_id),
  INDEX idx_title (title),
  INDEX idx_genre (genre),
  CONSTRAINT fk_album_artist FOREIGN KEY (artist_id) REFERENCES artist(id)
    ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='专辑表';

-- ============================================================
-- 表 4：track 曲目表（核心表）
-- ============================================================
-- 设计说明：
-- 核心业务实体。file_path 存物理文件路径（不是内容，文件本身在磁盘）。
-- duration_seconds 用 INT 而不是 TIME，方便计算和比较。
-- extra_metadata 用 JSON 字段做扩展性预留：
--   - DSD 标识、母带工程师、专辑画师等非结构化信息
--   - 未来想加新字段不用改表结构
-- lyrics 存歌词文本（LRC 格式或纯文本），最长 64KB（TEXT）足够。
-- ============================================================
CREATE TABLE track (
  id               BIGINT       AUTO_INCREMENT PRIMARY KEY            COMMENT '主键',
  title            VARCHAR(200) NOT NULL                              COMMENT '曲目标题',
  artist_id        BIGINT                                             COMMENT '艺术家 ID',
  album_id         BIGINT                                             COMMENT '专辑 ID',
  track_number     INT                                                COMMENT '在专辑中的曲目编号',
  file_path        VARCHAR(500) NOT NULL                              COMMENT '音频文件磁盘路径',
  cover_url        VARCHAR(500)                                       COMMENT '曲目封面（覆盖专辑封面）',
  format           VARCHAR(20)  NOT NULL                              COMMENT '格式：FLAC/MP3/WAV/AAC/OGG',
  duration_seconds INT          NOT NULL DEFAULT 0                    COMMENT '时长（秒）',
  bitrate_kbps     INT                                                COMMENT '比特率（kbps）',
  sample_rate_hz   INT                                                COMMENT '采样率（Hz）',
  channels         TINYINT                                            COMMENT '声道数（1=单声道，2=立体声）',
  file_size_bytes  BIGINT                                             COMMENT '文件大小（字节）',
  lyrics           TEXT                                               COMMENT '歌词（LRC 或纯文本）',
  extra_metadata   JSON                                               COMMENT '扩展元数据（JSON）',
  is_deleted       TINYINT      NOT NULL DEFAULT 0,
  deleted_at       DATETIME,
  created_at       DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP    COMMENT '上传时间',
  updated_at       DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  INDEX idx_artist_id (artist_id),
  INDEX idx_album_id (album_id),
  INDEX idx_title (title),
  INDEX idx_format (format),
  INDEX idx_created_at (created_at),
  CONSTRAINT fk_track_artist FOREIGN KEY (artist_id) REFERENCES artist(id)
    ON DELETE SET NULL ON UPDATE CASCADE,
  CONSTRAINT fk_track_album FOREIGN KEY (album_id) REFERENCES album(id)
    ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='曲目表';

-- ============================================================
-- 表 5：playlist 歌单表
-- ============================================================
-- 设计说明：
-- 歌单属于某个用户，所以有 user_id。
-- 删除歌单时其下所有 playlist_track 关系也应级联删除。
-- ============================================================
CREATE TABLE playlist (
  id          BIGINT       AUTO_INCREMENT PRIMARY KEY                COMMENT '主键',
  user_id     BIGINT       NOT NULL                                   COMMENT '所属用户 ID',
  name        VARCHAR(100) NOT NULL                                   COMMENT '歌单名称',
  description TEXT                                                    COMMENT '歌单描述',
  cover_url   VARCHAR(500)                                            COMMENT '歌单封面（可选）',
  is_deleted  TINYINT      NOT NULL DEFAULT 0,
  deleted_at  DATETIME,
  created_at  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  INDEX idx_user_id (user_id),
  INDEX idx_name (name),
  CONSTRAINT fk_playlist_user FOREIGN KEY (user_id) REFERENCES user(id)
    ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='歌单表';

-- ============================================================
-- 表 6：playlist_track 歌单-曲目关联表（多对多）
-- ============================================================
-- 设计说明：
-- order_index 字段实现歌单内自定义排序（拖拽功能依赖此字段）。
-- 复合主键 (playlist_id, track_id) 保证同一首歌在同一歌单不会重复。
-- 注意：order_index 不放在主键里，因为可能更新（排序时）。
-- ============================================================
CREATE TABLE playlist_track (
  playlist_id BIGINT   NOT NULL                                       COMMENT '歌单 ID',
  track_id    BIGINT   NOT NULL                                       COMMENT '曲目 ID',
  order_index INT      NOT NULL DEFAULT 0                             COMMENT '在歌单内的排序（小的在前）',
  added_at    DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP             COMMENT '加入时间',
  PRIMARY KEY (playlist_id, track_id),
  INDEX idx_track_id (track_id),
  INDEX idx_order (playlist_id, order_index),
  CONSTRAINT fk_pt_playlist FOREIGN KEY (playlist_id) REFERENCES playlist(id)
    ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT fk_pt_track FOREIGN KEY (track_id) REFERENCES track(id)
    ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='歌单-曲目关联表';

-- ============================================================
-- 表 7：tag 标签表
-- ============================================================
-- 设计说明：
-- 标签是用户自定义的元数据，可以贴在曲目上。
-- color 字段存十六进制色值（如 #FF5733），用于前端展示。
-- ============================================================
CREATE TABLE tag (
  id         BIGINT       AUTO_INCREMENT PRIMARY KEY                 COMMENT '主键',
  user_id    BIGINT       NOT NULL                                    COMMENT '创建标签的用户 ID',
  name       VARCHAR(50)  NOT NULL                                    COMMENT '标签名称',
  color      VARCHAR(20)                                              COMMENT '展示颜色（hex 如 #7C3AED）',
  created_at DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  UNIQUE KEY uk_user_name (user_id, name),
  INDEX idx_user_id (user_id),
  CONSTRAINT fk_tag_user FOREIGN KEY (user_id) REFERENCES user(id)
    ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='标签表';

-- ============================================================
-- 表 8：track_tag 曲目-标签关联表（多对多）
-- ============================================================
CREATE TABLE track_tag (
  track_id  BIGINT   NOT NULL                                        COMMENT '曲目 ID',
  tag_id    BIGINT   NOT NULL                                         COMMENT '标签 ID',
  tagged_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP               COMMENT '打标签时间',
  PRIMARY KEY (track_id, tag_id),
  INDEX idx_tag_id (tag_id),
  CONSTRAINT fk_tt_track FOREIGN KEY (track_id) REFERENCES track(id)
    ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT fk_tt_tag FOREIGN KEY (tag_id) REFERENCES tag(id)
    ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='曲目-标签关联表';

-- ============================================================
-- 表 9：play_history 播放历史表（行为表）
-- ============================================================
-- 设计说明：
-- 行为表数据量大，不加 FK 以提升插入性能。
-- played_at 加索引，因为统计查询（"最近 30 天"、"按月分组"）高频。
-- played_seconds 记录用户实际听了多少秒，用于"完整播放/跳过"分析。
-- ============================================================
CREATE TABLE play_history (
  id              BIGINT   AUTO_INCREMENT PRIMARY KEY                COMMENT '主键',
  user_id         BIGINT   NOT NULL                                   COMMENT '用户 ID',
  track_id        BIGINT   NOT NULL                                   COMMENT '曲目 ID（不加 FK，允许引用软删除曲目）',
  played_seconds  INT      NOT NULL DEFAULT 0                         COMMENT '实际播放秒数',
  played_at       DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP         COMMENT '播放时间',
  INDEX idx_user_id (user_id),
  INDEX idx_track_id (track_id),
  INDEX idx_played_at (played_at),
  INDEX idx_user_played (user_id, played_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='播放历史表';

-- ============================================================
-- 表 10：conversion_task 格式转换任务表（行为表）
-- ============================================================
-- 设计说明：
-- 转换是异步任务，必须持久化状态。
-- status 用 VARCHAR 而不是 ENUM，因为未来可能加新状态（如 CANCELED）。
-- progress 是 0-100 整数，前端轮询此字段刷新进度条。
-- ============================================================
CREATE TABLE conversion_task (
  id                BIGINT       AUTO_INCREMENT PRIMARY KEY          COMMENT '主键',
  user_id           BIGINT       NOT NULL                             COMMENT '发起用户 ID',
  source_track_id   BIGINT       NOT NULL                             COMMENT '源曲目 ID',
  source_format     VARCHAR(20)                                       COMMENT '源格式',
  target_format     VARCHAR(20)  NOT NULL                             COMMENT '目标格式：FLAC/MP3/WAV/AAC',
  target_bitrate    INT                                               COMMENT '目标比特率（kbps，无损时可为 NULL）',
  target_sample_rate INT                                              COMMENT '目标采样率（Hz）',
  status            VARCHAR(20)  NOT NULL DEFAULT 'PENDING'           COMMENT 'PENDING/RUNNING/SUCCESS/FAILED',
  progress          INT          NOT NULL DEFAULT 0                   COMMENT '进度 0-100',
  output_path       VARCHAR(500)                                      COMMENT '转换后文件路径',
  error_message     TEXT                                              COMMENT '失败时的错误信息',
  created_at        DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  started_at        DATETIME                                          COMMENT '开始执行时间',
  finished_at       DATETIME                                          COMMENT '完成/失败时间',
  INDEX idx_user_id (user_id),
  INDEX idx_status (status),
  INDEX idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='格式转换任务表';

-- ============================================================
-- 表 11：user_favorite 用户收藏表
-- ============================================================
-- 设计说明：
-- 独立成表而非用 tag 模拟，原因：
-- 1. "收藏"是核心业务概念，应该有专属表
-- 2. 与标签语义不同（标签是分类，收藏是偏好）
-- 3. 多用户场景下不同用户的收藏天然隔离
-- ============================================================
CREATE TABLE user_favorite (
  user_id      BIGINT   NOT NULL                                     COMMENT '用户 ID',
  track_id     BIGINT   NOT NULL                                      COMMENT '曲目 ID',
  favorited_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP            COMMENT '收藏时间',
  PRIMARY KEY (user_id, track_id),
  INDEX idx_track_id (track_id),
  INDEX idx_favorited_at (favorited_at),
  CONSTRAINT fk_uf_user FOREIGN KEY (user_id) REFERENCES user(id)
    ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT fk_uf_track FOREIGN KEY (track_id) REFERENCES track(id)
    ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户收藏表';

-- ============================================================
-- 建表完成，输出汇总
-- ============================================================
SELECT
  TABLE_NAME            AS '表名',
  TABLE_COMMENT         AS '说明',
  TABLE_ROWS            AS '行数（估算）'
FROM information_schema.TABLES
WHERE TABLE_SCHEMA = 'soundprint'
ORDER BY TABLE_NAME;
```

### 完整种子数据 SQL（`03-seed-data.sql`）

```sql
-- ============================================================
-- Soundprint 种子数据
-- 目的：开发期间页面"有内容可看"，答辩演示也可以直接用
-- 注意：种子数据中的 file_path 是占位路径，实际开发到 Phase 3 上传时
--      会被真实文件路径覆盖
-- ============================================================

USE soundprint;

-- 1. 用户（1 个默认用户）
INSERT INTO user (id, username, password, nickname, bio) VALUES
  (1, 'admin', 'admin123', 'Kaidi', '热爱无损音乐的开发者');

-- 2. 艺术家（10 个，覆盖多种语言和风格）
INSERT INTO artist (id, name, bio, country, formed_year) VALUES
  (1,  'Queen',           '英国摇滚乐队，传奇',                   'UK',     1970),
  (2,  'Eagles',          '美国摇滚乐队',                          'US',     1971),
  (3,  'Pink Floyd',      '英国前卫摇滚先驱',                       'UK',     1965),
  (4,  'Daft Punk',       '法国电子音乐二人组',                     'France', 1993),
  (5,  '坂本龙一',          '日本作曲家、钢琴家',                     'Japan',  1952),
  (6,  '周杰伦',           '华语流行天王',                          'Taiwan', 2000),
  (7,  'Norah Jones',     '美国爵士/流行歌手',                      'US',     2000),
  (8,  'Ludovico Einaudi','意大利新古典主义作曲家',                 'Italy',  1976),
  (9,  'Radiohead',       '英国另类摇滚乐队',                       'UK',     1985),
  (10, 'IU',              '韩国独立音乐人',                         'Korea',  2008);

-- 3. 专辑（15 张）
INSERT INTO album (id, title, artist_id, release_year, genre, description) VALUES
  (1,  'A Night at the Opera',  1, 1975, 'Rock',       '皇后乐队代表作'),
  (2,  'Hotel California',      2, 1976, 'Rock',       '老鹰乐队经典专辑'),
  (3,  'The Dark Side of the Moon', 3, 1973, 'Progressive Rock', '前卫摇滚里程碑'),
  (4,  'Discovery',             4, 2001, 'Electronic', 'Daft Punk 经典'),
  (5,  'Random Access Memories', 4, 2013, 'Electronic', 'Daft Punk 后期作'),
  (6,  'Async',                 5, 2017, 'Classical',  '坂本龙一晚期实验作'),
  (7,  '叶惠美',                 6, 2003, 'Pop',         '周杰伦代表专辑'),
  (8,  '范特西',                 6, 2001, 'Pop',         '周杰伦经典专辑'),
  (9,  'Come Away with Me',     7, 2002, 'Jazz',       'Norah Jones 出道神专'),
  (10, 'Una Mattina',           8, 2004, 'Neo-Classical', 'Einaudi 标志作'),
  (11, 'Nightbook',             8, 2009, 'Neo-Classical', NULL),
  (12, 'OK Computer',           9, 1997, 'Alternative Rock', 'Radiohead 转型之作'),
  (13, 'In Rainbows',           9, 2007, 'Alternative Rock', NULL),
  (14, 'Palette',              10, 2017, 'K-Pop',       'IU 第四张正规'),
  (15, '夜曲精选',                6, 2010, 'Pop',         '周杰伦精选辑（虚构）');

-- 4. 曲目（30 首，每张专辑 1-3 首示例）
-- file_path 这里是占位路径，开发到 Phase 3 上传真实文件时会被覆盖
INSERT INTO track (id, title, artist_id, album_id, track_number, file_path, format, duration_seconds, bitrate_kbps, sample_rate_hz, channels, file_size_bytes) VALUES
  -- A Night at the Opera
  (1,  'Bohemian Rhapsody',        1, 1, 1,  'storage/seed/bohemian.flac',   'FLAC', 355, 1411, 44100, 2, 50331648),
  (2,  'Love of My Life',          1, 1, 2,  'storage/seed/loveofmylife.flac','FLAC', 218, 1411, 44100, 2, 31457280),
  -- Hotel California
  (3,  'Hotel California',         2, 2, 1,  'storage/seed/hotelcalif.flac', 'FLAC', 390, 1411, 44100, 2, 55574528),
  (4,  'New Kid in Town',          2, 2, 2,  'storage/seed/newkid.flac',     'FLAC', 305, 1411, 44100, 2, 43450368),
  -- Dark Side of the Moon
  (5,  'Time',                     3, 3, 1,  'storage/seed/time.flac',       'FLAC', 421, 1411, 44100, 2, 60011008),
  (6,  'Money',                    3, 3, 2,  'storage/seed/money.flac',      'FLAC', 382, 1411, 44100, 2, 54395904),
  -- Discovery
  (7,  'One More Time',            4, 4, 1,  'storage/seed/onemoretime.mp3', 'MP3',  320,  320, 44100, 2, 12800000),
  (8,  'Harder, Better, Faster, Stronger', 4, 4, 2, 'storage/seed/hbfs.mp3', 'MP3',  225,  320, 44100, 2,  9000000),
  -- Random Access Memories
  (9,  'Get Lucky',                4, 5, 1,  'storage/seed/getlucky.flac',   'FLAC', 369, 1411, 44100, 2, 52428800),
  -- Async
  (10, 'andata',                   5, 6, 1,  'storage/seed/andata.flac',     'FLAC', 363, 1411, 44100, 2, 51380224),
  (11, 'solari',                   5, 6, 2,  'storage/seed/solari.flac',     'FLAC', 305, 1411, 44100, 2, 43450368),
  -- 叶惠美
  (12, '以父之名',                  6, 7, 1,  'storage/seed/yifuzhiming.flac','FLAC', 343, 1411, 44100, 2, 48758784),
  (13, '晴天',                      6, 7, 2,  'storage/seed/qingtian.flac',   'FLAC', 269, 1411, 44100, 2, 38273024),
  -- 范特西
  (14, '简单爱',                    6, 8, 1,  'storage/seed/jiandanai.flac',  'FLAC', 270, 1411, 44100, 2, 38535168),
  (15, '开不了口',                  6, 8, 2,  'storage/seed/kaibuliaokou.flac','FLAC',269, 1411, 44100, 2, 38273024),
  -- Come Away with Me
  (16, 'Don''t Know Why',          7, 9, 1,  'storage/seed/dontknowwhy.flac','FLAC', 186, 1411, 44100, 2, 26476544),
  (17, 'Come Away with Me',        7, 9, 2,  'storage/seed/comeaway.flac',   'FLAC', 198, 1411, 44100, 2, 28180480),
  -- Una Mattina
  (18, 'Una Mattina',              8, 10, 1, 'storage/seed/unamattina.flac', 'FLAC', 191, 1411, 44100, 2, 27197440),
  (19, 'Resta con me',             8, 10, 2, 'storage/seed/restaconme.flac', 'FLAC', 230, 1411, 44100, 2, 32731136),
  -- Nightbook
  (20, 'Nightbook',                8, 11, 1, 'storage/seed/nightbook.flac',  'FLAC', 296, 1411, 44100, 2, 42139648),
  -- OK Computer
  (21, 'Paranoid Android',         9, 12, 1, 'storage/seed/paranoid.flac',   'FLAC', 384, 1411, 44100, 2, 54657024),
  (22, 'Karma Police',             9, 12, 2, 'storage/seed/karmapolice.flac','FLAC', 261, 1411, 44100, 2, 37158912),
  (23, 'No Surprises',             9, 12, 3, 'storage/seed/nosurprises.flac','FLAC', 228, 1411, 44100, 2, 32440320),
  -- In Rainbows
  (24, '15 Step',                  9, 13, 1, 'storage/seed/15step.flac',     'FLAC', 237, 1411, 44100, 2, 33718272),
  (25, 'Nude',                     9, 13, 2, 'storage/seed/nude.flac',       'FLAC', 254, 1411, 44100, 2, 36118528),
  -- Palette
  (26, 'Palette',                 10, 14, 1, 'storage/seed/palette.flac',    'FLAC', 222, 1411, 44100, 2, 31588352),
  (27, '夜信',                     10, 14, 2, 'storage/seed/yexin.flac',      'FLAC', 209, 1411, 44100, 2, 29736960),
  -- 夜曲精选
  (28, '夜曲',                      6, 15, 1, 'storage/seed/yequ.flac',       'FLAC', 222, 1411, 44100, 2, 31588352),
  (29, '稻香',                      6, 15, 2, 'storage/seed/daoxiang.flac',   'FLAC', 223, 1411, 44100, 2, 31719424),
  (30, '青花瓷',                    6, 15, 3, 'storage/seed/qinghuaci.flac',  'FLAC', 238, 1411, 44100, 2, 33849344);

-- 5. 标签（5 个）
INSERT INTO tag (id, user_id, name, color) VALUES
  (1, 1, '深夜',     '#6366F1'),
  (2, 1, '通勤',     '#06B6D4'),
  (3, 1, '专注',     '#10B981'),
  (4, 1, '怀旧',     '#F59E0B'),
  (5, 1, 'Workout', '#EF4444');

-- 6. 曲目-标签关联（每首歌 1-2 个标签）
INSERT INTO track_tag (track_id, tag_id) VALUES
  (1, 4), (1, 1),       -- Bohemian Rhapsody: 怀旧 + 深夜
  (2, 1),               -- Love of My Life: 深夜
  (3, 4),               -- Hotel California: 怀旧
  (5, 1), (5, 3),       -- Time: 深夜 + 专注
  (6, 5),               -- Money: Workout
  (7, 5), (7, 2),       -- One More Time: Workout + 通勤
  (10, 3),              -- andata: 专注
  (11, 3),              -- solari: 专注
  (13, 1), (13, 4),     -- 晴天: 深夜 + 怀旧
  (14, 4),              -- 简单爱: 怀旧
  (18, 3), (18, 1),     -- Una Mattina: 专注 + 深夜
  (22, 2),              -- Karma Police: 通勤
  (26, 2),              -- Palette: 通勤
  (28, 1), (28, 4);     -- 夜曲: 深夜 + 怀旧

-- 7. 歌单（3 个）
INSERT INTO playlist (id, user_id, name, description) VALUES
  (1, 1, '深夜独处',     '一个人的夜晚，让音乐慢慢来'),
  (2, 1, '专注工作',     '不打扰的纯音乐，让代码飞起来'),
  (3, 1, '青葱岁月',     '回到过去的旋律');

-- 8. 歌单-曲目关联
INSERT INTO playlist_track (playlist_id, track_id, order_index) VALUES
  -- 深夜独处
  (1, 1, 1), (1, 2, 2), (1, 5, 3), (1, 11, 4), (1, 18, 5), (1, 28, 6),
  -- 专注工作
  (2, 10, 1), (2, 11, 2), (2, 18, 3), (2, 19, 4), (2, 20, 5),
  -- 青葱岁月
  (3, 13, 1), (3, 14, 2), (3, 15, 3), (3, 29, 4), (3, 30, 5);

-- 9. 收藏（5 首）
INSERT INTO user_favorite (user_id, track_id) VALUES
  (1, 1), (1, 3), (1, 13), (1, 18), (1, 28);

-- 10. 播放历史（30 条，分布在过去 30 天，演示统计页）
INSERT INTO play_history (user_id, track_id, played_seconds, played_at) VALUES
  (1, 1,  355, DATE_SUB(NOW(), INTERVAL 1 DAY)),
  (1, 2,  218, DATE_SUB(NOW(), INTERVAL 1 DAY)),
  (1, 13, 269, DATE_SUB(NOW(), INTERVAL 2 DAY)),
  (1, 5,  421, DATE_SUB(NOW(), INTERVAL 3 DAY)),
  (1, 18, 191, DATE_SUB(NOW(), INTERVAL 3 DAY)),
  (1, 11, 305, DATE_SUB(NOW(), INTERVAL 4 DAY)),
  (1, 10, 363, DATE_SUB(NOW(), INTERVAL 5 DAY)),
  (1, 28, 222, DATE_SUB(NOW(), INTERVAL 5 DAY)),
  (1, 3,  390, DATE_SUB(NOW(), INTERVAL 7 DAY)),
  (1, 14, 270, DATE_SUB(NOW(), INTERVAL 8 DAY)),
  (1, 22, 261, DATE_SUB(NOW(), INTERVAL 9 DAY)),
  (1, 1,  300, DATE_SUB(NOW(), INTERVAL 10 DAY)),
  (1, 18, 191, DATE_SUB(NOW(), INTERVAL 11 DAY)),
  (1, 26, 222, DATE_SUB(NOW(), INTERVAL 12 DAY)),
  (1, 5,  421, DATE_SUB(NOW(), INTERVAL 13 DAY)),
  (1, 13, 269, DATE_SUB(NOW(), INTERVAL 14 DAY)),
  (1, 30, 238, DATE_SUB(NOW(), INTERVAL 15 DAY)),
  (1, 11, 305, DATE_SUB(NOW(), INTERVAL 17 DAY)),
  (1, 29, 223, DATE_SUB(NOW(), INTERVAL 18 DAY)),
  (1, 1,  355, DATE_SUB(NOW(), INTERVAL 20 DAY)),
  (1, 2,  218, DATE_SUB(NOW(), INTERVAL 21 DAY)),
  (1, 10, 363, DATE_SUB(NOW(), INTERVAL 22 DAY)),
  (1, 18, 191, DATE_SUB(NOW(), INTERVAL 23 DAY)),
  (1, 3,  390, DATE_SUB(NOW(), INTERVAL 24 DAY)),
  (1, 13, 269, DATE_SUB(NOW(), INTERVAL 25 DAY)),
  (1, 28, 222, DATE_SUB(NOW(), INTERVAL 26 DAY)),
  (1, 22, 261, DATE_SUB(NOW(), INTERVAL 27 DAY)),
  (1, 11, 305, DATE_SUB(NOW(), INTERVAL 28 DAY)),
  (1, 14, 270, DATE_SUB(NOW(), INTERVAL 29 DAY)),
  (1, 5,  421, DATE_SUB(NOW(), INTERVAL 30 DAY));

-- 11. 转换任务（演示数据，1 个成功 + 1 个进行中）
INSERT INTO conversion_task (user_id, source_track_id, source_format, target_format, target_bitrate, status, progress, output_path, created_at, started_at, finished_at) VALUES
  (1, 1, 'FLAC', 'MP3', 320, 'SUCCESS', 100, 'storage/conversion/bohemian_320.mp3',
   DATE_SUB(NOW(), INTERVAL 2 DAY), DATE_SUB(NOW(), INTERVAL 2 DAY), DATE_SUB(NOW(), INTERVAL 2 DAY)),
  (1, 3, 'FLAC', 'AAC', 256, 'RUNNING', 45, NULL,
   DATE_SUB(NOW(), INTERVAL 1 MINUTE), DATE_SUB(NOW(), INTERVAL 1 MINUTE), NULL);

-- ============================================================
-- 完成
-- ============================================================
SELECT '种子数据导入完成' AS msg;
```

---

## 📚 边写边讲要求

Claude Code 在本阶段必须主动给用户讲解以下技术点：

### 1. utf8mb4 vs utf8（必讲）
两段对比 + 为什么音乐项目必须用 utf8mb4。**给出答辩话术**。

### 2. 三范式与本项目设计（必讲）
- 1NF：所有字段不可再分（OK，我们没有数组字段）
- 2NF：非主属性完全依赖主键（OK，复合主键的中间表都满足）
- 3NF：消除传递依赖（**重点讲**，举例：track 表不存 artist 名字，而是存 artist_id，因为"曲目→艺术家名字"是传递依赖）
- **给出答辩话术**

### 3. 软删除（必讲）
为什么不直接 DELETE。答辩话术："业务实体被引用，硬删会破坏外键和历史，软删的代价是查询加过滤，MyBatis-Plus 自动处理"。

### 4. 外键的取舍（必讲）
什么时候加 FK、什么时候不加 FK，本项目的设计权衡。

### 5. 索引设计（讲）
为什么这些字段加了索引：外键字段、高频查询字段、排序字段。

### 6. JSON 字段（讲）
`track.extra_metadata` 为什么用 JSON。扩展性优势 + 答辩话术。

### 7. order_index 不在主键里（讲）
为什么 `playlist_track` 的主键是 (playlist_id, track_id) 而不包括 order_index？因为 order_index 会被更新（排序时），主键应该是不变的标识。

讲解格式：每段代码完成后用一个"📚 给作者的讲解"小节展开。

---

## ✅ 完成检查清单

完成本阶段后，向用户依次核对：

- [ ] MySQL 8.0 端口已确认，连接验证成功
- [ ] `docs/sql/` 目录有 3 个 SQL 文件（01-create-database.sql、02-create-tables.sql、03-seed-data.sql）
- [ ] `soundprint` 数据库存在，字符集 utf8mb4
- [ ] 11 张表全部创建成功
- [ ] 种子数据导入成功（行数核对：user=1、artist=10、album=15、track=30、playlist=3、playlist_track=16、tag=5、track_tag=15、play_history=30、conversion_task=2、user_favorite=5）
- [ ] 多表 JOIN 验证查询能跑通，无乱码、无 NULL
- [ ] DataGrip 能连接数据库，11 张表都能看到
- [ ] `docs/ER图.png` 存在，大小 > 50 KB
- [ ] `dev-notes.md` 已追加设计决策、技术亮点、答辩问答三段
- [ ] 已 commit + push 到 GitHub

---

## 🚀 Git Commit 指令

在任务 11 中已经包含。如果中途有补充，最后再补一次：

```powershell
cd D:\Claude_Playground\Soundprint
git status
# 看看还有什么遗漏
git add .
"chore: Phase 1 补充修订" | Out-File "D:\Claude_Playground\_commitmsg.txt" -Encoding utf8
git commit -F "D:\Claude_Playground\_commitmsg.txt"
Remove-Item "D:\Claude_Playground\_commitmsg.txt"
git push
```

---

## 📩 反馈给架构师的内容

完成本阶段后，请把以下信息反馈给架构师（聊天另一端）：

1. **MySQL 端口** 是多少（MySQL80 实例）
2. **3 个 SQL 文件** 是否都跑成功
3. **种子数据计数**（11 张表的行数表格）
4. **多表 JOIN 验证查询**的前 5 行输出，确认是否正常
5. **ER 图截图** 让架构师看一下结构有没有问题
6. **GitHub 仓库** 是否看到 Phase 1 的 commit
7. **任何报错或卡壳**
8. **Claude Code 讲解你听不懂的地方**
9. **是否准备好进入 Phase 2（后端骨架）**

---

## ⚠️ 注意事项

- **本阶段不创建 backend/ 和 frontend/ 目录**
- **不安装任何 Maven/npm 依赖**
- **不写 Java/TypeScript 代码**
- 所有 shell 操作走 PowerShell，不要用 cmd 或 bash 语法
- 密码处理必须用 `Read-Host -AsSecureString` 和 `MYSQL_PWD` 环境变量，**密码绝不能进任何文件、日志、git history**
- 任何破坏性操作（DROP DATABASE 等）前先和用户确认
- 如果发现 SQL 跑错（约束冲突、字符集问题），停下来报告，**不要自己改 schema 蒙混过去**

---

**End of Phase 1 Document.**
