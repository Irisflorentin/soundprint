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
