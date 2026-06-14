#!/bin/bash
echo "=== 清理前端构建临时文件 ==="
rm -rf frontend/dist frontend/node_modules/.vite
echo "=== 清理后端编译临时文件 ==="
rm -rf backend/target
echo "=== 清理Docker构建缓存 ==="
docker builder prune -f 2>/dev/null || true
echo "=== 构建清理完成 ==="
