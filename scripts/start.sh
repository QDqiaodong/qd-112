#!/bin/bash
# ======================================================
# 一键启动脚本
# 端口检查 -> 构建启动 -> 完成提示
# ======================================================
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(cd "${SCRIPT_DIR}/.." && pwd)"

# 颜色输出
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BOLD='\033[1m'
NC='\033[0m'

cd "${PROJECT_ROOT}"

echo ""
echo "══════════════════════════════════════════════════════════════"
echo -e " ${BOLD}家用工具维护与使用台账 - 一键启动${NC} "
echo "══════════════════════════════════════════════════════════════"
echo ""

# 步骤1: 端口检查
echo -e "${YELLOW}📌 步骤 1/3: 端口冲突检查${NC}"
if ! bash "${SCRIPT_DIR}/port-check.sh"; then
    echo -e "${RED}❌ 端口检查未通过，启动终止。${NC}"
    exit 1
fi
eval "$(bash "${SCRIPT_DIR}/port-check.sh" --export)"
echo ""

# 步骤2: Docker Compose 构建启动
echo -e "${YELLOW}📌 步骤 2/3: Docker 构建与启动${NC}"
echo "  依赖层使用缓存，仅变更文件重新构建..."
echo ""

docker compose \
    --env-file .env \
    up \
    --build \
    -d \
    --remove-orphans

echo ""
echo -e "${GREEN}✅ 容器启动完成，等待服务就绪...${NC}"
echo ""

# 步骤3: 构建完成提示与验证
echo -e "${YELLOW}📌 步骤 3/3: 服务验证${NC}"
bash "${SCRIPT_DIR}/build-complete.sh"

exit 0
