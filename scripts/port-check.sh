#!/bin/bash
# ======================================================
# 端口冲突检查脚本
# 检查端口是否被占用，若占用则自动向后顺延
# 支持 --export 模式，将最终端口导出给 docker compose 使用
# ======================================================
set -euo pipefail

# 加载环境变量
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
ENV_FILE="${SCRIPT_DIR}/../.env"

if [ -f "${ENV_FILE}" ]; then
    while IFS= read -r line; do
        [[ -z "${line}" || "${line}" == \#* ]] && continue
        export "${line?}"
    done < "${ENV_FILE}"
fi

# 颜色输出
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m'

EXPORT_ONLY=0
if [ "${1:-}" = "--export" ]; then
    EXPORT_ONLY=1
fi

is_port_occupied() {
    local port=$1
    lsof -nP -iTCP:"${port}" -sTCP:LISTEN >/dev/null 2>&1
}

resolve_port() {
    local var_name=$1
    local name=$2
    local start_port=${!var_name}
    local port=${start_port}
    local max_port=$((start_port + 200))

    while is_port_occupied "${port}"; do
        if [ ${EXPORT_ONLY} -eq 0 ]; then
            echo -e "  ${YELLOW}⚠️  ${name} 端口 ${port} 已占用，尝试 $((port + 1))${NC}"
        fi
        port=$((port + 1))
        if [ "${port}" -gt "${max_port}" ]; then
            if [ ${EXPORT_ONLY} -eq 0 ]; then
                echo -e "  ${RED}❌ ${name} 从 ${start_port} 起连续 200 个端口均不可用${NC}"
            fi
            return 1
        fi
    done

    export "${var_name}=${port}"
    if [ ${EXPORT_ONLY} -eq 1 ]; then
        echo "export ${var_name}=${port}"
    elif [ "${port}" != "${start_port}" ]; then
        echo -e "  ${GREEN}✅ ${name} 已自动顺延为 ${port}${NC}"
    else
        echo -e "  ${GREEN}✅ ${name} 端口 ${port} 可用${NC}"
    fi
    return 0
}

if [ ${EXPORT_ONLY} -eq 0 ]; then
    echo "=========================================="
    echo "  家用工具台账 - 端口检查（自动顺延）"
    echo "=========================================="
    echo ""
fi

resolve_port FRONTEND_PORT "前端 Nginx"
resolve_port BACKEND_PORT "后端 SpringBoot"
resolve_port MYSQL_PORT "MySQL"
resolve_port REDIS_PORT "Redis"

if [ ${EXPORT_ONLY} -eq 0 ]; then
    echo ""
    echo "=========================================="
    echo -e "${GREEN}✅ 端口检查完成。${NC}"
    echo "=========================================="
fi
