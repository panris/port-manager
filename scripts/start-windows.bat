@echo off
REM Port Manager Web - Windows启动脚本

echo =========================================
echo Port Manager Web - Starting...
echo =========================================

REM 获取脚本所在目录
set SCRIPT_DIR=%~dp0
set PROJECT_DIR=%SCRIPT_DIR%..

cd /d "%PROJECT_DIR%"

REM 检查是否已构建
if not exist "target\port-manager-web.jar" (
    echo JAR file not found. Building project...
    call mvn clean package -DskipTests

    if errorlevel 1 (
        echo Build failed!
        pause
        exit /b 1
    )
)

REM 启动应用
echo.
echo Starting Port Manager Web...
echo Access at: http://127.0.0.1:9527
echo.
echo Press Ctrl+C to stop the application
echo.

java -jar target\port-manager-web.jar

pause