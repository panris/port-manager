#!/bin/bash

# Port Manager 启动脚本 (Mac)
# 双击此文件即可启动应用

# 切换到脚本所在目录
cd "$(dirname "$0")"

# 显示欢迎信息
echo "======================================"
echo "   Port Manager 端口管理工具"
echo "======================================"
echo ""

# 检测Java环境
if ! command -v java &> /dev/null; then
    echo "❌ 错误: 未检测到Java环境"
    echo ""
    echo "请先安装Java 11或更高版本:"
    echo "  方式1: brew install openjdk@11"
    echo "  方式2: 访问 https://adoptium.net/ 下载"
    echo ""
    read -p "按任意键退出..."
    exit 1
fi

# 显示Java版本
JAVA_VERSION=$(java -version 2>&1 | head -1 | cut -d'"' -f2)
echo "✓ Java版本: $JAVA_VERSION"
echo ""

# 检查端口9527是否被占用
if lsof -Pi :9527 -sTCP:LISTEN -t >/dev/null 2>&1; then
    echo "⚠️  警告: 端口9527已被占用"
    echo ""
    read -p "是否要强制关闭占用端口的进程? (y/n): " -n 1 -r
    echo ""
    if [[ $REPLY =~ ^[Yy]$ ]]; then
        PID=$(lsof -ti:9527)
        kill -9 $PID 2>/dev/null
        echo "✓ 已关闭进程 PID: $PID"
        sleep 1
    else
        echo "启动已取消"
        read -p "按任意键退出..."
        exit 0
    fi
fi

# 启动应用
echo "🚀 正在启动 Port Manager..."
echo "   访问地址: http://localhost:9527"
echo ""
echo "提示: 关闭此窗口将停止应用"
echo "======================================"
echo ""

# 启动JAR文件（后台运行）
java -jar port-manager-web.jar > /dev/null 2>&1 &
APP_PID=$!

# 等待应用启动
echo "⏳ 等待应用启动..."
sleep 3

# 检查应用是否成功启动
if ! kill -0 $APP_PID 2>/dev/null; then
    echo "❌ 应用启动失败，请查看错误日志"
    read -p "按任意键退出..."
    exit 1
fi

# 检查端口是否监听
RETRY_COUNT=0
MAX_RETRIES=10
while [ $RETRY_COUNT -lt $MAX_RETRIES ]; do
    if lsof -Pi :9527 -sTCP:LISTEN -t >/dev/null 2>&1; then
        echo "✓ 应用启动成功！"
        break
    fi
    sleep 1
    RETRY_COUNT=$((RETRY_COUNT + 1))
done

if [ $RETRY_COUNT -eq $MAX_RETRIES ]; then
    echo "❌ 应用启动超时"
    kill $APP_PID 2>/dev/null
    read -p "按任意键退出..."
    exit 1
fi

# 自动打开浏览器
echo "🌐 正在打开浏览器..."
open http://localhost:9527

echo ""
echo "======================================"
echo "✅ Port Manager 已启动"
echo "   访问地址: http://localhost:9527"
echo "   进程PID: $APP_PID"
echo ""
echo "按 Ctrl+C 或关闭此窗口以停止应用"
echo "======================================"

# 保持终端打开，等待用户中断
trap "echo ''; echo '🛑 正在停止应用...'; kill $APP_PID 2>/dev/null; echo '✓ 应用已停止'; sleep 2; exit 0" INT TERM

# 监控应用进程
while kill -0 $APP_PID 2>/dev/null; do
    sleep 2
done

echo ""
echo "⚠️  应用已意外停止"
read -p "按任意键退出..."