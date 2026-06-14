#!/bin/bash
# ======================================================
# 构建完成提示脚本
# 构建成功后自动打印前端访问地址并验证服务
# ======================================================
set -euo pipefail

# 加载环境变量
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
ENV_FILE="${SCRIPT_DIR}/../.env"

if [ -f "${ENV_FILE}" ]; then
    while IFS= read -r line; do
        [[ -z "${line}" || "${line}" == \#* ]] && continue
        key="${line%%=*}"
        if [ -z "${!key+x}" ]; then
            export "${line?}"
        fi
    done < "${ENV_FILE}"
fi

# 颜色输出
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
CYAN='\033[0;36m'
BOLD='\033[1m'
NC='\033[0m'

# 边框字符
HLINE="══════════════════════════════════════════════════════════════"

echo ""
echo "${HLINE}"
echo -e " ${BOLD}${GREEN}🎉 家用工具维护与使用台账 - 构建完成${NC} "
echo "${HLINE}"
echo ""

# 等待服务就绪
echo -e "${YELLOW}⏳ 等待前端服务就绪...${NC}"
MAX_RETRIES=30
RETRY=0
while [ ${RETRY} -lt ${MAX_RETRIES} ]; do
    if curl -sSf "http://127.0.0.1:${FRONTEND_PORT}" > /dev/null 2>&1; then
        break
    fi
    RETRY=$((RETRY + 1))
    sleep 2
done

if [ ${RETRY} -eq ${MAX_RETRIES} ]; then
    echo -e "${RED}❌ 前端服务启动超时，请检查容器日志。${NC}"
    docker logs "${PROJECT_NAME}-frontend" --tail=50
    exit 1
fi

echo -e "${GREEN}✅ 前端服务就绪${NC}"
echo ""

# 服务信息展示
echo -e "${BOLD}📋 服务信息${NC}"
echo "──────────────────────────────────────────────────────────────"
echo ""
echo -e "  ${CYAN}🌐 前端访问地址:${NC}"
echo -e "     ${BOLD}${BLUE}http://localhost:${FRONTEND_PORT}${NC}"
echo -e "     ${BOLD}${BLUE}http://127.0.0.1:${FRONTEND_PORT}${NC}"
echo ""
echo -e "  ${CYAN}🔧 后端 API 地址:${NC}"
echo -e "     http://127.0.0.1:${BACKEND_PORT}/api"
echo -e "     Swagger文档: http://127.0.0.1:${BACKEND_PORT}/swagger-ui.html"
echo ""
echo -e "  ${CYAN}🗄️  数据库服务:${NC}"
echo -e "     MySQL: 127.0.0.1:${MYSQL_PORT}"
echo -e "     数据库名: ${MYSQL_DATABASE}"
echo -e "     用户名: root / ${MYSQL_USER}"
echo ""
echo -e "  ${CYAN}⚡ 缓存服务:${NC}"
echo -e "     Redis: 127.0.0.1:${REDIS_PORT}"
echo ""

# 验证 127.0.0.1 和 localhost 一致性
echo "──────────────────────────────────────────────────────────────"
echo -e "${BOLD}🔍 访问一致性验证${NC}"
echo ""

TITLE1=$(curl -sS "http://127.0.0.1:${FRONTEND_PORT}" 2>/dev/null | grep -o '<title>[^<]*</title>' | head -1 || echo "N/A")
TITLE2=$(curl -sS "http://localhost:${FRONTEND_PORT}" 2>/dev/null | grep -o '<title>[^<]*</title>' | head -1 || echo "N/A")

echo -e "  127.0.0.1 页面标题: ${TITLE1}"
echo -e "  localhost 页面标题: ${TITLE2}"

if [ "${TITLE1}" = "${TITLE2}" ] && [ -n "${TITLE1}" ]; then
    echo -e "  ${GREEN}✅ 访问一致性验证通过${NC}"
else
    echo -e "  ${RED}❌ 访问一致性验证失败！${NC}"
    echo -e "  请检查 /etc/hosts 中 localhost 是否指向 127.0.0.1"
fi

echo ""
echo "──────────────────────────────────────────────────────────────"
echo -e "${BOLD}📦 容器状态${NC}"
echo ""
docker ps --format "table {{.Names}}\t{{.Status}}\t{{.Ports}}" | grep "${PROJECT_NAME}" || true
echo ""
echo "${HLINE}"
echo -e " ${BOLD}${GREEN}🎉 项目启动成功！请在浏览器访问:${NC} "
echo -e " ${BOLD}${BLUE}   http://localhost:${FRONTEND_PORT}${NC} "
echo "${HLINE}"
echo ""
