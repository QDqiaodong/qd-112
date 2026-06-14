#!/bin/bash
# ======================================================
# 停止脚本
# ======================================================
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(cd "${SCRIPT_DIR}/.." && pwd)"

cd "${PROJECT_ROOT}"

echo ""
echo "══════════════════════════════════════════════════════════════"
echo "  家用工具维护与使用台账 - 停止服务"
echo "══════════════════════════════════════════════════════════════"
echo ""

docker compose --env-file .env down

echo ""
echo "✅ 服务已停止"
echo ""
