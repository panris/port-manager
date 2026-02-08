@echo off
chcp 65001 >nul
title Port Manager 端口管理工具

:: 切换到脚本所在目录
cd /d "%~dp0"

echo ======================================
echo    Port Manager 端口管理工具
echo ======================================
echo.

:: 检测Java环境
where java >nul 2>nul
if %errorlevel% neq 0 (
    echo ❌ 错误: 未检测到Java环境
    echo.
    echo 请先安装Java 11或更高版本:
    echo   访问 https://adoptium.net/ 下载
    echo.
    pause
    exit /b 1
)

:: 显示Java版本
for /f "tokens=3" %%g in ('java -version 2^>^&1 ^| findstr /i "version"') do (
    set JAVA_VERSION=%%g
)
set JAVA_VERSION=%JAVA_VERSION:"=%
echo ✓ Java版本: %JAVA_VERSION%
echo.

:: 检查端口9527是否被占用
netstat -ano | findstr :9527 | findstr LISTENING >nul 2>nul
if %errorlevel% equ 0 (
    echo ⚠️  警告: 端口9527已被占用
    echo.
    set /p REPLY="是否要强制关闭占用端口的进程? (y/n): "
    if /i "%REPLY%"=="y" (
        for /f "tokens=5" %%a in ('netstat -ano ^| findstr :9527 ^| findstr LISTENING') do (
            taskkill /F /PID %%a >nul 2>nul
            echo ✓ 已关闭进程 PID: %%a
        )
        timeout /t 1 /nobreak >nul
    ) else (
        echo 启动已取消
        pause
        exit /b 0
    )
)

:: 启动应用
echo 🚀 正在启动 Port Manager...
echo    访问地址: http://localhost:9527
echo.
echo 提示: 关闭此窗口将停止应用
echo ======================================
echo.

:: 启动JAR文件（后台运行）
start /b javaw -jar port-manager-web.jar

:: 等待应用启动
echo ⏳ 等待应用启动...
timeout /t 3 /nobreak >nul

:: 检查端口是否监听
set RETRY_COUNT=0
set MAX_RETRIES=10

:check_port
netstat -ano | findstr :9527 | findstr LISTENING >nul 2>nul
if %errorlevel% equ 0 (
    echo ✓ 应用启动成功！
    goto start_browser
)

set /a RETRY_COUNT+=1
if %RETRY_COUNT% geq %MAX_RETRIES% (
    echo ❌ 应用启动超时
    taskkill /F /IM javaw.exe /FI "WINDOWTITLE eq port-manager-web.jar" >nul 2>nul
    pause
    exit /b 1
)

timeout /t 1 /nobreak >nul
goto check_port

:start_browser
:: 自动打开浏览器
echo 🌐 正在打开浏览器...
start http://localhost:9527

echo.
echo ======================================
echo ✅ Port Manager 已启动
echo    访问地址: http://localhost:9527
echo.
echo 按任意键或关闭此窗口以停止应用
echo ======================================
echo.

:: 保持窗口打开
pause >nul

:: 停止应用
echo.
echo 🛑 正在停止应用...
for /f "tokens=5" %%a in ('netstat -ano ^| findstr :9527 ^| findstr LISTENING') do (
    taskkill /F /PID %%a >nul 2>nul
)
echo ✓ 应用已停止

timeout /t 2 /nobreak >nul
exit /b 0