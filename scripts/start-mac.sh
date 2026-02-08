#!/bin/bash

# Port Manager Web - Mac启动脚本

echo "========================================="
echo "Port Manager Web - Starting..."
echo "========================================="

# 获取脚本所在目录
SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
PROJECT_DIR="$( cd "$SCRIPT_DIR/.." && pwd )"

cd "$PROJECT_DIR"

# 检查是否已构建
if [ ! -f "target/port-manager-web.jar" ]; then
    echo "JAR file not found. Building project..."
    mvn clean package -DskipTests

    if [ $? -ne 0 ]; then
        echo "Build failed!"
        exit 1
    fi
fi

# 启动应用
echo ""
echo "Starting Port Manager Web..."
echo "Access at: http://127.0.0.1:9527"
echo ""
echo "Press Ctrl+C to stop the application"
echo ""

java -jar target/port-manager-web.jar