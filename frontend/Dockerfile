# ============================================================
# Stage 1: 编译阶段 - Node 编译 Vue 静态资源
# ============================================================
FROM node:20-alpine AS builder

WORKDIR /build

COPY package*.json ./
RUN npm config set registry https://registry.npmmirror.com && npm ci

COPY . .

ARG VITE_API_BASE=/api
ENV VITE_API_BASE=$VITE_API_BASE
RUN npm run build

# ============================================================
# Stage 2: 运行阶段 - Nginx 托管静态资源并反向代理后端
# ============================================================
FROM nginx:1.27-alpine

RUN rm -rf /etc/nginx/conf.d/default.conf /usr/share/nginx/html/*

COPY nginx.conf /etc/nginx/conf.d/default.conf
COPY --from=builder /build/dist /usr/share/nginx/html

EXPOSE 80

HEALTHCHECK --interval=10s --timeout=3s --retries=3 \
    CMD wget -q --spider http://localhost/health || exit 1

CMD ["nginx", "-g", "daemon off;"]
