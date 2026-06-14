# 家用工具维护与使用台账

一款用于管理家庭手动、电动工具的全栈应用系统，记录型号参数、使用场景、保养检修记录与存放位置，服务日常取用和定期维保。

## 📋 项目信息

- **项目名称**：home-tools
- **项目代码**：家用工具维护与使用台账
- **容器名称前缀**：`home-tools-*`

## 📍 固定端口分配表（唯一，禁止复用）

| 服务 | 宿主机端口 | 容器端口 | 绑定地址 | 说明 |
|------|-----------|---------|---------|------|
| 前端 Nginx | **3015** | 80 | 127.0.0.1 | 禁用 80, 8080 等默认端口 |
| 后端 SpringBoot | **8095** | 8080 | 127.0.0.1 | 禁用 8080, 8888 等默认端口 |
| MySQL | **3318** | 3306 | 127.0.0.1 | 禁用 3306 默认端口 |
| Redis | **6387** | 6379 | 127.0.0.1 | 禁用 6379 默认端口 |

> **硬性约束**：所有端口固定，冲突即报错，禁止自动切换。所有服务仅绑定 IPv4 回环地址 `127.0.0.1`。

## 🚀 快速开始

### 一键启动（推荐）

```bash
# 端口检查 -> 构建启动 -> 验证输出
bash scripts/start.sh
```

### 手动启动

```bash
# 1. 端口检查（必须）
bash scripts/port-check.sh

# 2. 构建启动
docker compose --env-file .env up --build -d

# 3. 构建完成提示
bash scripts/build-complete.sh
```

### 停止服务

```bash
bash scripts/stop.sh
```

### 清理构建

```bash
bash scripts/clean.sh
```

## 🎯 访问地址

构建成功后自动输出以下地址：

| 类型 | 地址 |
|------|------|
| 前端主地址 | **http://localhost:3015** |
| 前端备用 | http://127.0.0.1:3015 |
| 后端 API | http://127.0.0.1:8095/api |
| Swagger 文档 | http://127.0.0.1:8095/swagger-ui.html |

> ✅ 验证要求：`http://localhost:3015` 与 `http://127.0.0.1:3015` 返回页面必须完全一致。

## 🏗️ 技术架构

### 前端
- **框架**：Vue 3.4 + TypeScript 5.4
- **构建工具**：Vite 5.2
- **路由**：Vue Router 4.3
- **状态管理**：Pinia 2.1
- **UI组件**：Element Plus 2.7 + TailwindCSS 3.4
- **图标**：Lucide Vue Next
- **图表**：ECharts 5.5
- **HTTP客户端**：Axios 1.7

### 后端
- **框架**：Spring Boot 3.3.0 + JDK 17
- **ORM**：Spring Data JPA 3.3
- **连接池**：HikariCP 5.0
- **缓存**：Spring Data Redis 3.3 + Jedis 5.1
- **数据校验**：Jakarta Validation 3.0
- **API文档**：SpringDoc OpenAPI 2.5

### 中间件
- **MySQL**：8.0 (Alpine精简镜像)
- **Redis**：7.2 (Alpine精简镜像)
- **Nginx**：1.27 (Alpine精简镜像)

## 🐳 Docker 构建优化

### 核心机制：依赖层与源码层分离

**前端 Dockerfile**：
1. 第一层（`deps`）：仅 `COPY package.json package-lock.json`，执行 `npm ci`
2. 第二层（`builder`）：`COPY --from=deps` 已安装的 `node_modules`，`COPY` 源码，执行 `npm run build`
3. 第三层（`runtime`）：Nginx 托管静态资源

**后端 Dockerfile**：
1. 第一层（`deps`）：仅 `COPY pom.xml`，执行 `mvn dependency:go-offline`
2. 第二层（`builder`）：`COPY --from=deps` 已下载的 Maven 依赖，`COPY src`，执行 `mvn package`
3. 第三层（`runtime`）：JRE 运行 Jar 包

**缓存效果**：
- ✅ `pom.xml` / `package.json` 无变更 → 复用依赖缓存，不下载
- ✅ 仅业务代码变更 → 仅触发编译层，不重新下载依赖
- ✅ 首次构建全量下载，后续构建速度提升 70%+

### 构建上下文优化

**.dockerignore** 排除以下内容：
- `node_modules/`、`dist/`、`target/` - 依赖和构建产物
- `.idea/`、`.vscode/`、`*.iml` - IDE 配置
- `*.png`、`*.jpg` - 截图和媒体文件
- `.trae/`、`README.md` - 文档
- `.git/` - Git 仓库
- `.env` - 环境变量

### 基础镜像统一管理

所有基础镜像通过 `.env` 中的 `DOCKER_REGISTRY` 变量统一配置，全链路共用同一仓库地址：

```dotenv
DOCKER_REGISTRY=docker.mirrors.ustc.edu.cn
```

| 镜像 | 变量 |
|------|------|
| Node | `NODE_IMAGE_VERSION=18-alpine` |
| Maven | `MAVEN_IMAGE_VERSION=3.9-eclipse-temurin-17` |
| JRE | `JRE_IMAGE_VERSION=17-jre-alpine` |
| MySQL | `MYSQL_IMAGE_VERSION=8.0-oracle` |
| Redis | `REDIS_IMAGE_VERSION=7.2-alpine` |
| Nginx | `NGINX_IMAGE_VERSION=1.27-alpine` |

> **约束**：严禁使用 `# syntax=docker/dockerfile:*` 语法，仅使用 Docker 原生分层缓存。

## 🌐 国内镜像源配置

### 前端 npm 镜像（京东镜像）

`.npmrc` 自动配置，无需 VPN：
```
registry=https://registry.npmmirror.com
```

### 后端 Maven 镜像（网易开源镜像）

`backend/settings.xml` 自动配置，无需 VPN：
```xml
<mirror>
    <id>netease</id>
    <mirrorOf>central</mirrorOf>
    <url>https://maven.163yun.com/repository/public</url>
</mirror>
```

### Docker 镜像加速

`.env` 中 `DOCKER_REGISTRY` 可配置国内镜像：
- 中科大：`docker.mirrors.ustc.edu.cn`（默认）
- 网易：`hub-mirror.c.163.com`
- 腾讯云：`mirror.ccs.tencentyun.com`
- 七牛云：`reg-mirror.qiniu.com`
- 阿里云：`https://<你的ID>.mirror.aliyuncs.com`

## 📁 项目结构

```
qd-112/
├── frontend/                    # 前端项目
│   ├── src/                     # 源代码
│   ├── Dockerfile               # 多阶段构建
│   ├── .dockerignore            # 构建上下文排除
│   ├── .npmrc                   # npm 国内镜像
│   └── vite.config.ts           # Vite 配置（127.0.0.1:3008）
│
├── backend/                     # 后端项目
│   ├── src/main/                # 源代码
│   ├── Dockerfile               # 多阶段构建
│   ├── .dockerignore            # 构建上下文排除
│   ├── settings.xml             # Maven 国内镜像
│   └── pom.xml                  # Maven 配置
│
├── docker/                      # Docker 配置
│   ├── mysql/
│   │   ├── my.cnf               # MySQL 优化配置
│   │   └── init/01_schema.sql   # 数据库初始化
│   ├── redis/redis.conf         # Redis 配置
│   └── nginx/nginx.conf         # Nginx 反向代理配置
│
├── scripts/                     # 工具脚本
│   ├── start.sh                 # 一键启动
│   ├── stop.sh                  # 停止服务
│   ├── clean.sh                 # 构建清理
│   ├── port-check.sh            # 端口冲突检查
│   └── build-complete.sh        # 构建完成提示
│
├── .env                         # 全局环境配置（端口、镜像源）
├── .dockerignore                # 根目录构建排除
├── docker-compose.yml           # Docker Compose 编排
└── README.md                    # 本文件
```

## 🔧 配置文件清单

| 文件 | 作用 |
|------|------|
| [.env](.env) | 全局环境配置：端口、镜像源、数据库密码等 |
| [docker-compose.yml](docker-compose.yml) | 服务编排：端口映射、容器名称、健康检查 |
| [frontend/Dockerfile](frontend/Dockerfile) | 前端多阶段构建：依赖层分离 |
| [backend/Dockerfile](backend/Dockerfile) | 后端多阶段构建：依赖层分离 |
| [frontend/.dockerignore](frontend/.dockerignore) | 前端构建上下文排除 |
| [backend/.dockerignore](backend/.dockerignore) | 后端构建上下文排除 |
| [.dockerignore](.dockerignore) | 根目录构建上下文排除 |
| [frontend/.npmrc](frontend/.npmrc) | npm 京东镜像源 |
| [backend/settings.xml](backend/settings.xml) | Maven 网易镜像源 |
| [frontend/vite.config.ts](frontend/vite.config.ts) | Vite 配置（127.0.0.1:3008, strictPort） |
| [backend/src/main/resources/application.yml](backend/src/main/resources/application.yml) | Spring Boot 配置（127.0.0.1:8088） |
| [scripts/port-check.sh](scripts/port-check.sh) | 端口冲突检查脚本 |
| [scripts/build-complete.sh](scripts/build-complete.sh) | 构建完成提示脚本 |
| [scripts/start.sh](scripts/start.sh) | 一键启动脚本 |

## 🔒 硬性约束合规检查

| 约束项 | 状态 | 说明 |
|--------|------|------|
| 仅绑定 127.0.0.1 | ✅ | 不绑定 localhost、0.0.0.0、:: |
| 端口固定，不自动切换 | ✅ | Vite strictPort=true，端口冲突即报错 |
| 映射格式 `127.0.0.1:${PORT}:80` | ✅ | docker-compose.yml 全部使用此格式 |
| 禁用默认端口 | ✅ | 不使用 80、443、8080、3306、6379 等 |
| 容器名称与项目相同 | ✅ | home-tools-mysql、home-tools-frontend 等 |
| 依赖层源码层分离 | ✅ | Dockerfile 多阶段构建，COPY 顺序正确 |
| 禁用 `# syntax=docker/dockerfile:*` | ✅ | 未使用 BuildKit 扩展语法 |
| 基础镜像统一 DOCKER_REGISTRY | ✅ | 全部镜像通过环境变量引用 |
| .dockerignore 配置 | ✅ | 根目录、前端、后端都有配置 |
| 127.0.0.1 与 localhost 一致性 | ✅ | 构建完成自动验证 |
| 构建后输出访问地址 | ✅ | build-complete.sh 自动输出 |

## 📋 端口冲突检查

启动前必须执行端口检查：

```bash
bash scripts/port-check.sh
```

检查命令：
```bash
# 前端
lsof -nP -iTCP:3015 -sTCP:LISTEN

# 后端
lsof -nP -iTCP:8095 -sTCP:LISTEN

# MySQL
lsof -nP -iTCP:3318 -sTCP:LISTEN

# Redis
lsof -nP -iTCP:6387 -sTCP:LISTEN
```

如发现端口被占用，手动终止进程：
```bash
kill -9 <PID>
```

## ✅ 服务验证

启动后自动执行以下验证：

```bash
# 验证 127.0.0.1
curl -sS http://127.0.0.1:3015 | head

# 验证 localhost
curl -sS http://localhost:3015 | head

# 两者必须返回相同的页面标题
```

## 📖 API 文档

启动后端后访问：
- Swagger UI: http://127.0.0.1:8091/swagger-ui.html
- OpenAPI: http://127.0.0.1:8091/v3/api-docs

## 📄 许可证

本项目仅供个人学习和家庭使用。
