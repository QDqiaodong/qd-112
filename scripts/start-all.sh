#!/bin/bash
set -e
ENV=${1:-dev}
echo "=== 启动家用工具台账系统 (${ENV}环境) ==="
PORTS=$(bash scripts/port-check.sh --export)
eval $PORTS
export FRONTEND_PORT BACKEND_PORT MYSQL_PORT REDIS_PORT
echo "前端端口: $FRONTEND_PORT"
echo "后端端口: $BACKEND_PORT"
echo "MySQL端口: $MYSQL_PORT"
echo "Redis端口: $REDIS_PORT"
docker compose -f docker-compose.${ENV}.yml --env-file .env.${ENV} up -d
echo "=== 服务启动完成 ==="
echo "访问地址: http://localhost:${FRONTEND_PORT}"
