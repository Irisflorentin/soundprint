# Phase 8 - Part 1：Docker 化部署

> Soundprint 项目的终章首段——容器化部署。
> 完成后拿满拓展项 4 分。
> 目标：在任何装了 Docker Desktop 的电脑上 `docker compose up` 一键起整个项目。

---

## 🎯 阶段目标

把项目变成 **3 容器编排**：

```
┌─────────────────────────────────────────────────────────┐
│ Docker Network: soundprint-network                      │
│                                                         │
│  ┌──────────────┐    ┌──────────────┐   ┌────────────┐ │
│  │ mysql:8.0    │←───│ backend      │←──│ frontend   │ │
│  │ port 3307    │    │ Spring Boot  │   │ Nginx      │ │
│  │              │    │ port 8080    │   │ port 80    │ │
│  └──────────────┘    └──────────────┘   └────────────┘ │
│       ▲                    ▲                            │
│       │                    │                            │
│   mysql-data           soundprint-storage               │
│   volume               volume                           │
└─────────────────────────────────────────────────────────┘
                 ▲
            外部访问: http://localhost
```

**关键设计**：

- **3 个容器**：mysql / backend / frontend(nginx)
- **2 个 named volume**：mysql-data（数据库持久化）+ soundprint-storage（音频/封面/转码文件持久化）
- **1 个网络**：内部通信走 service name（backend 访问数据库写 `mysql:3306` 而不是 `localhost:3307`）
- **健康检查**：backend 等 mysql ready 才启动，nginx 等 backend ready 才启动
- **环境变量统一管理**：`.env` 文件（不进 git）
- **演示一键起**：`docker compose up -d` 然后浏览器访问 `http://localhost`

---

## ⚠️ 关键约束

- **不动业务代码**——只在项目根新增 docker 相关文件
- **不动 application-dev.yml**——新增一份 `application-docker.yml` 用于容器内
- **不动数据库已有数据** —— mysql 启动时如果发现已初始化就不再跑 init SQL
- **Phase 0-7.8 已有功能保持工作**

---

## 📋 任务清单

### 任务 0：在项目根创建 Docker 目录结构

最终项目结构（新增项标 ⭐）：

```
Soundprint/
├── backend/
│   ├── src/
│   ├── pom.xml
│   ├── Dockerfile              ⭐ 新增
│   └── src/main/resources/
│       ├── application.yml
│       ├── application-dev.yml
│       └── application-docker.yml   ⭐ 新增
│
├── frontend/
│   ├── src/
│   ├── package.json
│   ├── Dockerfile              ⭐ 新增
│   └── nginx.conf              ⭐ 新增（Nginx 配置）
│
├── docker/                     ⭐ 新增整个目录
│   └── mysql-init/
│       ├── 01-create-tables.sql   (从 docs/sql/ 复制过来)
│       └── 02-seed-data.sql       (从 docs/sql/ 复制过来，可选 — 见任务 6)
│
├── docker-compose.yml          ⭐ 新增（项目根）
├── .env                        ⭐ 新增（不进 git）
├── .env.example                ⭐ 新增（进 git，模板）
├── .dockerignore               ⭐ 新增
└── README.md                   （更新 Docker 启动说明）
```

---

### 任务 1：后端 Dockerfile

**关键决策**：用**多阶段构建**——build 阶段用 maven 镜像编译，运行阶段用更小的 jre-only 镜像。

**最终镜像体积**：约 200-250 MB（不算 FFmpeg 的话 ~150 MB）

#### 1.1 创建 `backend/Dockerfile`

```dockerfile
# ============================================================
# Stage 1: 编译阶段 — 用 Maven 镜像编译出 jar
# ============================================================
FROM maven:3.9.6-eclipse-temurin-17 AS builder

WORKDIR /build

# 先复制 pom.xml 利用 Docker 缓存层（如果 pom 没变就不重新下载依赖）
COPY pom.xml .
RUN mvn dependency:go-offline -B

# 再复制源码编译
COPY src ./src
RUN mvn clean package -DskipTests -B

# ============================================================
# Stage 2: 运行阶段 — JRE 17 + FFmpeg + 中文字体
# ============================================================
FROM eclipse-temurin:17-jre-jammy

WORKDIR /app

# 安装运行时依赖
# - ffmpeg: 音频转码必需
# - fonts-noto-cjk: 中文文件名/标签显示支持
# - tzdata: 时区
RUN apt-get update && apt-get install -y --no-install-recommends \
        ffmpeg \
        fonts-noto-cjk \
        tzdata \
        ca-certificates \
        curl \
    && rm -rf /var/lib/apt/lists/*

# 设置时区为东八区
ENV TZ=Asia/Shanghai
RUN ln -snf /usr/share/zoneinfo/$TZ /etc/localtime && echo $TZ > /etc/timezone

# 从 builder 阶段拷贝 jar
COPY --from=builder /build/target/*.jar app.jar

# 暴露端口
EXPOSE 8080

# 健康检查（用于 docker-compose depends_on 等待）
HEALTHCHECK --interval=10s --timeout=3s --start-period=40s --retries=5 \
    CMD curl -f http://localhost:8080/actuator/health || exit 1

# 启动 Spring Boot
# 使用 dockerfile 的 profile，从 application-docker.yml 读配置
ENTRYPOINT ["java", \
    "-Dspring.profiles.active=docker", \
    "-Xms256m", \
    "-Xmx512m", \
    "-jar", \
    "/app/app.jar"]
```

#### 1.2 后端要支持 actuator health 端点

`backend/pom.xml` 检查有没有 actuator 依赖。如果没有，加：

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-actuator</artifactId>
</dependency>
```

`application.yml` 加 health 端点暴露：

```yaml
management:
  endpoints:
    web:
      exposure:
        include: health
  endpoint:
    health:
      show-details: never
```

---

### 任务 2：后端 application-docker.yml

容器内的配置文件——**MySQL 主机名不是 localhost 而是 `mysql`**（Docker 网络内的 service name）。

#### 2.1 创建 `backend/src/main/resources/application-docker.yml`

```yaml
server:
  port: 8080

spring:
  datasource:
    # 关键：容器间通信用 service name，不是 localhost
    url: jdbc:mysql://mysql:3306/${MYSQL_DATABASE:soundprint}?useUnicode=true&characterEncoding=UTF-8&useSSL=false&serverTimezone=Asia/Shanghai&allowPublicKeyRetrieval=true
    username: ${MYSQL_USER:soundprint}
    password: ${MYSQL_PASSWORD:soundprint123}
    driver-class-name: com.mysql.cj.jdbc.Driver
    hikari:
      maximum-pool-size: 10
      minimum-idle: 5
      connection-timeout: 30000

  servlet:
    multipart:
      max-file-size: 200MB
      max-request-size: 200MB

# Soundprint 自定义配置
soundprint:
  storage:
    # 关键：容器内路径，由 docker-compose 挂载到宿主机
    root: /data/storage
    audio: /data/storage/audio
    cover: /data/storage/cover
    avatar: /data/storage/avatar
    conversion: /data/storage/conversion
    temp: /data/storage/temp
    peaks: /data/storage/peaks
  ffmpeg:
    # 容器内 FFmpeg 在 PATH 里直接调
    path: ffmpeg

mybatis-plus:
  configuration:
    map-underscore-to-camel-case: true
    log-impl: org.apache.ibatis.logging.nologging.NoLoggingImpl
  global-config:
    db-config:
      logic-delete-field: isDeleted
      logic-delete-value: 1
      logic-not-delete-value: 0

# 日志：容器化部署降低日志级别
logging:
  level:
    root: INFO
    com.soundprint: INFO
  pattern:
    console: "%d{yyyy-MM-dd HH:mm:ss} [%thread] %-5level %logger{36} - %msg%n"

management:
  endpoints:
    web:
      exposure:
        include: health
```

**关键点**：
- 数据库地址用 `mysql:3306`（Docker DNS 自动解析到 mysql 容器）
- 存储路径用 `/data/storage`（容器内绝对路径，由 docker-compose 挂载到宿主机的 named volume）
- 凭据从环境变量读取，默认值用于本地测试

---

### 任务 3：前端 Dockerfile + Nginx 配置

**关键决策**：**多阶段构建**——Node 镜像编译出静态文件，Nginx 镜像只负责 serve。**最终镜像约 50 MB**（对比纯 node 镜像 1 GB+ 是质的提升）。

#### 3.1 创建 `frontend/Dockerfile`

```dockerfile
# ============================================================
# Stage 1: 编译阶段 — Node 编译出 dist
# ============================================================
FROM node:20-alpine AS builder

WORKDIR /build

# 先复制 package.json 利用 Docker 缓存层
COPY package*.json ./
RUN npm config set registry https://registry.npmmirror.com && \
    npm ci

# 复制源码编译
COPY . .

# 构建生产版本（VITE_API_BASE 通过构建参数传入）
ARG VITE_API_BASE=/api
ENV VITE_API_BASE=$VITE_API_BASE
RUN npm run build

# ============================================================
# Stage 2: 运行阶段 — Nginx
# ============================================================
FROM nginx:1.27-alpine

# 清理 nginx 默认配置和默认 html
RUN rm -rf /etc/nginx/conf.d/default.conf /usr/share/nginx/html/*

# 复制自定义 nginx 配置
COPY nginx.conf /etc/nginx/conf.d/default.conf

# 从 builder 拷贝编译产物
COPY --from=builder /build/dist /usr/share/nginx/html

EXPOSE 80

HEALTHCHECK --interval=10s --timeout=3s --retries=3 \
    CMD wget -q --spider http://localhost/ || exit 1

CMD ["nginx", "-g", "daemon off;"]
```

#### 3.2 创建 `frontend/nginx.conf`

**关键设计**：
- 前端静态文件直接 serve
- `/api/*` 反向代理到后端 `backend:8080`（Docker 网络内 service name）
- `/files/*` 也反向代理到后端（静态文件服务）
- SPA 路由 fallback 到 index.html
- Gzip 压缩

```nginx
server {
    listen 80;
    listen [::]:80;
    server_name _;

    # 设置最大上传大小（FLAC/WAV 文件可能很大）
    client_max_body_size 200M;
    client_body_buffer_size 1M;

    # 字符集
    charset utf-8;
    source_charset utf-8;

    # ============== Gzip 压缩 ==============
    gzip on;
    gzip_vary on;
    gzip_min_length 1024;
    gzip_proxied any;
    gzip_comp_level 6;
    gzip_types
        text/plain
        text/css
        text/xml
        text/javascript
        application/json
        application/javascript
        application/xml+rss
        application/atom+xml
        image/svg+xml;

    # ============== 前端静态资源 ==============
    root /usr/share/nginx/html;
    index index.html;

    # 长期缓存（带 hash 的资源）
    location ~* \.(js|css|png|jpg|jpeg|gif|ico|svg|woff|woff2|ttf|eot)$ {
        expires 30d;
        add_header Cache-Control "public, immutable";
        try_files $uri =404;
    }

    # ============== /api 反向代理 ==============
    location /api/ {
        proxy_pass http://backend:8080/api/;
        proxy_http_version 1.1;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;

        # 大文件上传（音频/封面）超时延长
        proxy_connect_timeout 60s;
        proxy_send_timeout 300s;
        proxy_read_timeout 300s;
        proxy_request_buffering off;
    }

    # ============== /files 静态文件代理 ==============
    # 后端 /files/* 提供音频流和封面图，需要支持 Range Request
    location /files/ {
        proxy_pass http://backend:8080/files/;
        proxy_http_version 1.1;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;

        # 关键：支持 HTTP Range Request（流式音频播放需要）
        proxy_set_header Range $http_range;
        proxy_set_header If-Range $http_if_range;
        proxy_cache_bypass $http_range $http_if_range;

        # 大文件传输配置
        proxy_buffering off;
        proxy_read_timeout 300s;
    }

    # ============== SPA 路由 fallback ==============
    location / {
        try_files $uri $uri/ /index.html;
        # 不缓存 index.html，保证更新及时
        add_header Cache-Control "no-store, no-cache, must-revalidate";
    }

    # ============== 健康检查端点 ==============
    location /health {
        access_log off;
        return 200 "OK\n";
        add_header Content-Type text/plain;
    }
}
```

**关键技术点**（答辩讲点）：
- **HTTP Range Request 支持**：流式播放音频必需（`Range`、`If-Range` header 透传）
- **`proxy_buffering off`**：音频/封面文件大，关闭缓冲减少延迟
- **`client_max_body_size 200M`**：上传 FLAC 文件可能 50-100MB
- **SPA fallback**：Vue Router 的 history 模式刷新页面会 404，必须 fallback 到 index.html

---

### 任务 4：docker-compose.yml

#### 4.1 创建项目根 `docker-compose.yml`

```yaml
services:
  # ============== MySQL 数据库 ==============
  mysql:
    image: mysql:8.0
    container_name: soundprint-mysql
    restart: unless-stopped
    environment:
      MYSQL_ROOT_PASSWORD: ${MYSQL_ROOT_PASSWORD:-rootpass123}
      MYSQL_DATABASE: ${MYSQL_DATABASE:-soundprint}
      MYSQL_USER: ${MYSQL_USER:-soundprint}
      MYSQL_PASSWORD: ${MYSQL_PASSWORD:-soundprint123}
      TZ: Asia/Shanghai
    command:
      - --character-set-server=utf8mb4
      - --collation-server=utf8mb4_unicode_ci
      - --default-authentication-plugin=mysql_native_password
    volumes:
      - mysql-data:/var/lib/mysql
      - ./docker/mysql-init:/docker-entrypoint-initdb.d:ro
    ports:
      # 仅本机映射（避免暴露到公网），3308 防止与本机 MySQL 端口冲突
      - "127.0.0.1:3308:3306"
    networks:
      - soundprint-network
    healthcheck:
      test: ["CMD", "mysqladmin", "ping", "-h", "localhost", "-u", "root", "-p${MYSQL_ROOT_PASSWORD:-rootpass123}"]
      interval: 10s
      timeout: 5s
      retries: 10
      start_period: 30s

  # ============== 后端 Spring Boot ==============
  backend:
    build:
      context: ./backend
      dockerfile: Dockerfile
    container_name: soundprint-backend
    restart: unless-stopped
    environment:
      SPRING_PROFILES_ACTIVE: docker
      MYSQL_DATABASE: ${MYSQL_DATABASE:-soundprint}
      MYSQL_USER: ${MYSQL_USER:-soundprint}
      MYSQL_PASSWORD: ${MYSQL_PASSWORD:-soundprint123}
      TZ: Asia/Shanghai
    volumes:
      - soundprint-storage:/data/storage
    networks:
      - soundprint-network
    depends_on:
      mysql:
        condition: service_healthy
    healthcheck:
      test: ["CMD", "curl", "-f", "http://localhost:8080/actuator/health"]
      interval: 10s
      timeout: 5s
      retries: 5
      start_period: 60s

  # ============== 前端 Nginx ==============
  frontend:
    build:
      context: ./frontend
      dockerfile: Dockerfile
      args:
        VITE_API_BASE: /api
    container_name: soundprint-frontend
    restart: unless-stopped
    ports:
      # 主入口
      - "80:80"
    networks:
      - soundprint-network
    depends_on:
      backend:
        condition: service_healthy
    healthcheck:
      test: ["CMD", "wget", "-q", "--spider", "http://localhost/health"]
      interval: 10s
      timeout: 3s
      retries: 3

# ============== Volume ==============
volumes:
  mysql-data:
    name: soundprint-mysql-data
  soundprint-storage:
    name: soundprint-storage

# ============== Network ==============
networks:
  soundprint-network:
    name: soundprint-network
    driver: bridge
```

**关键设计**（答辩讲点）：

1. **`depends_on` + healthcheck 启动顺序**：mysql ready → backend ready → frontend 启动，**不会出现"后端连不上数据库"的崩溃**
2. **named volume vs bind mount**：用 named volume `mysql-data`，**不依赖宿主机路径**，跨机器迁移友好
3. **`restart: unless-stopped`**：宿主机重启后容器自动恢复，**演示后第二天还能用**
4. **MySQL 端口映射到 3308**：避免与你本机 MySQL 80（3307 端口）冲突。**只演示无需暴露**也可以删除
5. **环境变量分层**：默认值 + `.env` 覆盖，**演示也能用、生产也能用**

---

### 任务 5：环境变量管理

#### 5.1 创建 `.env.example`（进 git，作为模板）

```bash
# ============================================================
# Soundprint Docker Compose 环境变量
# 复制为 .env 后修改实际值使用
# ============================================================

# MySQL 配置
MYSQL_ROOT_PASSWORD=rootpass123
MYSQL_DATABASE=soundprint
MYSQL_USER=soundprint
MYSQL_PASSWORD=soundprint123

# 时区
TZ=Asia/Shanghai
```

#### 5.2 创建 `.env`（不进 git）

```bash
cp .env.example .env
# 然后编辑 .env，把密码改成强密码（演示场景可以保留默认值）
```

#### 5.3 更新 `.gitignore`

```gitignore
# 已有内容...

# Docker
.env
```

---

### 任务 6：数据库初始化 SQL

#### 6.1 复制建表 SQL 到 docker/mysql-init/

```powershell
mkdir docker\mysql-init
Copy-Item docs\sql\02-create-tables.sql docker\mysql-init\01-create-tables.sql
```

**注意**：
- MySQL 容器**首次启动**会执行 `/docker-entrypoint-initdb.d/` 下的 .sql 文件
- 文件按字典序执行，所以前缀 `01-`、`02-` 控制顺序
- **只执行一次** —— 如果 mysql-data volume 已经有数据，下次启动不会重复执行

#### 6.2 关于种子数据：**不复制**

Phase 7.6 你清理过假数据，**种子 SQL（`03-seed-data.sql`）不要再复制进 docker**，否则演示时容器里会重新出现假数据。

如果你想给 demo 一些初始数据，**用 mysqldump 导出当前的真实数据**：

```powershell
# 导出当前数据库为 seed.sql（演示用初始数据）
& "C:\Program Files\MySQL\MySQL Server 8.0\bin\mysqldump.exe" --port=3307 -u root -p `
  --no-create-info --skip-triggers `
  --databases soundprint `
  > docker/mysql-init/02-seed-current-data.sql

# 编辑 02-seed-current-data.sql，确保首行有 USE soundprint;
```

**但注意**：你的真实音频文件**不会进容器**——只有数据库记录会。这样演示时打开应用看到"55 首歌"但点播放会 404。**所以演示前要先手动上传几首歌**到容器（见任务 8）。

---

### 任务 7：.dockerignore（避免无关文件进镜像）

#### 7.1 创建 `backend/.dockerignore`

```
target/
.idea/
*.iml
.git/
.gitignore
README.md
*.log
.env
application-dev.yml
```

#### 7.2 创建 `frontend/.dockerignore`

```
node_modules/
dist/
.vite/
.idea/
.git/
.gitignore
README.md
*.log
.env
.env.local
```

#### 7.3 项目根 `.dockerignore`（可选）

```
.git/
docs/
*.md
!README.md
.env
```

---

### 任务 8：演示前的数据准备

由于音频文件在宿主机的 `D:/soundprint-storage/audio/`，**默认不会自动进容器**。两种解决方案：

#### 方案 A（演示推荐）：每次启动前手动复制几首歌到 volume

```powershell
# 找到 docker volume 的实际路径
docker volume inspect soundprint-storage --format '{{.Mountpoint}}'
# 输出类似：C:\ProgramData\Docker\volumes\soundprint-storage\_data

# 复制几个真实的 audio 文件进去（不复制全部，避免演示等太久）
Copy-Item D:\soundprint-storage\audio\*.flac `
    (docker volume inspect soundprint-storage --format '{{.Mountpoint}}')\audio\ -Force
Copy-Item D:\soundprint-storage\cover\*.jpg `
    (docker volume inspect soundprint-storage --format '{{.Mountpoint}}')\cover\ -Force
```

#### 方案 B（更工程化）：用 bind mount 直接挂宿主机目录

修改 `docker-compose.yml` 的 backend 服务：

```yaml
volumes:
  # 不再用 named volume，直接挂宿主机目录
  - D:/soundprint-storage:/data/storage
```

**优点**：宿主机文件改了容器立刻可见。**缺点**：不便于跨机器迁移。

**演示场景推荐方案 B**——所有真实数据都在你的电脑上，挂载使用最方便。

---

### 任务 9：启动 + 验证 + 排错

#### 9.1 启动命令

```powershell
cd D:\Claude_Playground\Soundprint

# 复制环境变量模板（仅首次）
Copy-Item .env.example .env

# 启动（首次会拉取镜像 + 编译，需要 5-10 分钟）
docker compose up -d

# 看日志（验证启动）
docker compose logs -f
```

#### 9.2 验证清单

```powershell
# 1. 三个容器都在运行
docker compose ps
# 预期：mysql / backend / frontend 全部 healthy

# 2. 后端 health 检查
curl http://localhost/api/health
# 或访问 actuator
docker compose exec backend curl http://localhost:8080/actuator/health
# 预期：{"status":"UP"}

# 3. MySQL 能连
docker compose exec mysql mysql -u soundprint -psoundprint123 -e "SHOW TABLES IN soundprint;"
# 预期：列出 11 张表

# 4. 前端能访问
# 浏览器：http://localhost
# 预期：登录页 Galaxy 星空
```

#### 9.3 常见错误排查

| 现象 | 原因 | 解决 |
|---|---|---|
| `docker compose up` 卡在 build 阶段几十分钟 | 镜像下载慢 | 配置镜像加速器（`{"registry-mirrors":["https://docker.mirrors.ustc.edu.cn"]}`）|
| 前端访问 502 Bad Gateway | backend 还没启动好 | 等 1 分钟，看 `docker compose logs backend` |
| 后端日志 `Communications link failure` | mysql 没 ready 后端就启动了 | depends_on healthcheck 配置好，或重启 `docker compose restart backend` |
| MySQL 启动报错 `Can't init init_file` | seed sql 有语法错误 | 检查 `docker/mysql-init/*.sql`，可以注释掉重启 |
| 上传文件 413 Request Entity Too Large | Nginx 限制 | 检查 nginx.conf 的 client_max_body_size |
| 上传文件后下载播放 404 | volume 挂载没生效 | `docker volume inspect soundprint-storage`，确认 mount point |
| FFmpeg 转码失败 | 容器内没装 FFmpeg | 检查 backend Dockerfile 是否 RUN apt-get install ffmpeg |

#### 9.4 清理命令（如果出问题想重来）

```powershell
# 停止所有容器
docker compose down

# 停止 + 删除 volume（注意：会丢数据）
docker compose down -v

# 完全清理（连镜像也删）
docker compose down -v --rmi all
docker system prune -a
```

---

### 任务 10：commit + push

```powershell
cd D:\Claude_Playground\Soundprint
git add backend/Dockerfile backend/src/main/resources/application-docker.yml
git add frontend/Dockerfile frontend/nginx.conf
git add docker-compose.yml .env.example .dockerignore
git add backend/.dockerignore frontend/.dockerignore
git add docker/

git status   # 确认 .env 不在列表里

@"
feat: Phase 8 Part 1 - Docker 化部署

新增容器:
- mysql:8.0 (3308 端口本机映射)
- backend (Spring Boot + FFmpeg + 中文字体)
- frontend (Nginx 反向代理 /api 和 /files)

关键设计:
- 多阶段构建: backend 镜像 ~250MB, frontend ~50MB
- 健康检查 + depends_on: mysql ready 才启 backend, backend ready 才启 frontend
- Named volume: mysql-data + soundprint-storage 持久化
- 环境变量分层: .env.example 模板 + .env 实际值（不进 git）
- Nginx 支持 HTTP Range Request（流播放必需）
- SPA 路由 fallback 到 index.html
- 时区统一 Asia/Shanghai

启动命令:
  cp .env.example .env
  docker compose up -d

访问: http://localhost
"@ | Out-File "D:\Claude_Playground\_msg.txt" -Encoding utf8NoBOM

git commit -F "D:\Claude_Playground\_msg.txt"
Remove-Item "D:\Claude_Playground\_msg.txt"
git push
```

---

## 📚 答辩讲点（必讲技术点）

Phase 8 Part 1 涉及的高频答辩问题：

### 1. **为什么用 Docker 而不是直接部署？**（必讲）
- 环境一致性：开发/测试/生产用同一份镜像，避免 "It works on my machine"
- 一键启动：演示老师只需要 `docker compose up`，不用配 Java/MySQL/FFmpeg
- 隔离性：容器间隔离，互不影响

### 2. **多阶段构建有什么用？**（必讲）
- 前端镜像从 1GB → 50MB（不包含 Node 编译工具，只包含 Nginx + 静态文件）
- 后端从 800MB → 250MB（不包含 Maven，只包含 JRE）
- 答辩讲："构建工具只在 build 阶段需要，运行时不需要——多阶段构建分离这两个关注点"

### 3. **`depends_on` + healthcheck 解决什么问题？**（必讲）
- 没有 healthcheck：mysql 容器**进程启动了**但 SQL 还没 ready，后端连上来就报错
- 有 healthcheck：mysql 真正能响应 SQL 才标记为 healthy，后端才会启动
- 这是 **distributed systems** 的经典启动顺序问题

### 4. **named volume vs bind mount？**（讲）
- **named volume**：Docker 管理的存储，跨机器迁移容易，**演示生产推荐**
- **bind mount**：直接挂宿主机目录，**开发调试方便**（改了立刻见效）
- 我们的项目：mysql 用 named volume（不动）、storage 用 bind mount（方便看文件）

### 5. **Nginx 反向代理 + Range Request**（必讲，亮点）
- 前端访问 `/api/*` → Nginx 转发到 `backend:8080`
- 流播放音频时浏览器发 `Range: bytes=0-65535` → Nginx 透传 → 后端返回 `206 Partial Content`
- **没有 `proxy_set_header Range $http_range`** 就播放不了——这是个容易踩坑的地方

### 6. **service name 通信 vs localhost**（讲）
- 容器内 `localhost` 指容器自己，不是宿主机
- 容器间通信用 Docker 自动注册的 DNS：service name → IP
- 后端连 mysql 写 `jdbc:mysql://mysql:3306/...`，不是 `localhost:3307`

### 7. **环境变量管理**（讲）
- `.env.example` 进 git 作为模板
- `.env` 不进 git（含密码）
- docker-compose 自动读 `.env`，用 `${VAR:-default}` 语法兜底
- 这是 12-factor app 标准做法

---

## ✅ 完成检查清单

- [ ] 任务 0：项目根创建 docker/ 目录
- [ ] 任务 1：backend/Dockerfile（多阶段 + FFmpeg + 中文字体 + 健康检查）
- [ ] 任务 2：application-docker.yml（数据库 host = mysql, 存储路径 = /data/storage）
- [ ] 任务 3：frontend/Dockerfile + nginx.conf（多阶段 + 反向代理 + Range Request 支持）
- [ ] 任务 4：docker-compose.yml（3 容器 + 2 volume + 1 network + healthcheck）
- [ ] 任务 5：.env.example + .env（不进 git）+ .gitignore
- [ ] 任务 6：docker/mysql-init/01-create-tables.sql（不放假种子）
- [ ] 任务 7：3 个 .dockerignore
- [ ] 任务 8：决定数据挂载策略（推荐 bind mount D:/soundprint-storage）
- [ ] 任务 9：`docker compose up -d` 启动成功，浏览器访问 http://localhost 正常
- [ ] 任务 9.2：登录 / 浏览 / 播放 / 上传 / 转换 / 统计全部正常
- [ ] 任务 10：commit + push

---

## 📩 反馈给架构师

完成后给我看：

1. **`docker compose ps` 截图**（3 个容器 healthy）
2. **浏览器访问 http://localhost 截图**（登录页 Galaxy）
3. **上传一首歌成功 + 播放正常 截图**（验证 volume 挂载 + Range Request）
4. **转换 FLAC → MP3 截图**（验证容器内 FFmpeg）
5. **commit hash**

---

## ⚠️ 注意事项

- **不动业务代码**——除了加 actuator 依赖外，源码零修改
- **不动 application-dev.yml**——本地开发仍用原配置
- **`.env` 不能进 git**——commit 前 `git status` 检查
- **首次构建会慢**（5-10 分钟）——后续 cache 会快
- **mysql-init 只执行一次**——如果想重新初始化，`docker compose down -v` 删除 volume
- **演示前确保 volume 里有真实音频**——否则点播放 404
- **Windows 环境 Docker Desktop 必须装 + 启动**

---

**End of Phase 8 Part 1 Document.**
