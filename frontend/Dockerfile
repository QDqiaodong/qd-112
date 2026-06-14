# ======================================================
# 前端 Dockerfile - 依赖层与源码层分离
# 首次构建全量下载，后续变更仅重编译
# 严禁使用 # syntax=docker/dockerfile:* 语法
# 所有基础镜像使用全局 DOCKER_REGISTRY 变量
# ======================================================

ARG DOCKER_REGISTRY
ARG NODE_IMAGE_VERSION=18-alpine
ARG NGINX_IMAGE_VERSION=1.27-alpine

# ------------------------------
# 构建阶段：依赖层（独立缓存）
# package.json / package-lock.json 无变更则复用缓存
# ------------------------------
FROM ${DOCKER_REGISTRY}library/node:${NODE_IMAGE_VERSION} AS deps
WORKDIR /app
COPY package.json package-lock.json* .npmrc ./
RUN npm ci

# ------------------------------
# 构建阶段：源码编译层
# 仅源码变更时触发，不重新下载依赖
# ------------------------------
FROM ${DOCKER_REGISTRY}library/node:${NODE_IMAGE_VERSION} AS builder
WORKDIR /app
COPY --from=deps /app/node_modules ./node_modules
COPY package.json package-lock.json* .npmrc ./
COPY tsconfig.json tsconfig.node.json vite.config.ts postcss.config.js tailwind.config.js index.html ./
COPY src ./src
COPY public ./public
RUN npm run build

# ------------------------------
# 运行阶段：Nginx 托管静态资源
# ------------------------------
FROM ${DOCKER_REGISTRY}library/nginx:${NGINX_IMAGE_VERSION} AS runtime
LABEL org.opencontainers.image.title="home-tools-frontend"
LABEL org.opencontainers.image.description="家用工具维护与使用台账 - 前端"
LABEL org.opencontainers.image.authors="home-tools"

COPY --from=builder /app/dist /usr/share/nginx/html

EXPOSE 80
STOPSIGNAL SIGQUIT
CMD ["nginx", "-g", "daemon off;"]
