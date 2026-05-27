-- ============================================================
-- Soundprint Docker 最小演示用户
-- 只提供 current-user-id=1 所需的外键记录，不复制旧假曲库种子数据。
-- 前端 Phase 7.8 是本地演示登录，不读取这里的 password 字段。
-- ============================================================

USE soundprint;

INSERT INTO user (id, username, password, nickname, bio)
VALUES (1, 'admin', 'NOT_USED_FRONTEND_DEMO', 'Kaidi', 'Docker demo user')
ON DUPLICATE KEY UPDATE
  nickname = VALUES(nickname),
  bio = VALUES(bio);
