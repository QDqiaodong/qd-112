#!/bin/bash
# ======================================================
# 构建清理脚本
# 清理编译临时文件、Docker 悬空镜像
# ======================================================
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(cd "${SCRIPT_DIR}/.." && pwd)"

YELLOW='\033[1;33m'
GREEN='\033[0;32m'
NC='\033[0m'

cd "${PROJECT_ROOT}"

echo ""
echo "══════════════════════════════════════════════════════════════"
echo "  家用工具维护与使用台账 - 构建清理"
echo "══════════════════════════════════════════════════════════════"
echo ""

echo -e "${YELLOW}清理前端编译产物...${NC}"
rm -rf frontend/dist frontend/node_modules/.cache
echo -e "${GREEN}✅ 前端清理完成${NC}"

echo -e "${YELLOW}清理后端编译产物...${NC}"
rm -rf backend/target
echo -e "${GREEN}✅ 后端清理完成${NC}"

echo -e "${YELLOW}清理 Docker 悬空镜像...${NC}"
docker image prune -f 2>/dev/null || true
echo -e "${GREEN}✅ Docker 清理完成${NC}"

echo ""
echo "══════════════════════════════════════════════════════════════"
echo -e " ${GREEN}✅ 所有清理工作完成${NC} "
echo "══════════════════════════════════════════════════════════════"
echo ""
