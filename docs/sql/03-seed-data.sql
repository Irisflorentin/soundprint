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
