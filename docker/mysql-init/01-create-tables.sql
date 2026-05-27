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
