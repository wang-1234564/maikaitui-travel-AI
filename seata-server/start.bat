@echo off
chcp 65001 >nul
title Seata Server

REM ==========================================
REM Seata Server 一键启动 (双击即可)
REM 依赖: MySQL已启动, Nacos已启动 (:8848)
REM ==========================================

set SEATA_HOME=%~dp0
cd /d "%SEATA_HOME%"

REM --- Java check ---
java -version >nul 2>&1
if %ERRORLEVEL% NEQ 0 (
    echo [ERROR] Java not found in PATH!
    pause
    exit /b 1
)

REM --- Check MySQL driver ---
if not exist "%SEATA_HOME%lib\jdbc\mysql-connector-j-8.3.0.jar" (
    echo [WARN] MySQL driver not found in lib\jdbc\, please copy it there
)

echo ==========================================
echo   Seata Server Starting...
echo   RPC Port: 8091
echo   Config:   Nacos 127.0.0.1:8848
echo   Store:    DB (MySQL)
echo ==========================================
echo.

java -Xmx512m -Xms256m -server ^
  -Dloader.path=lib ^
  -Dspring.config.location=conf/application.yml ^
  -Dstore.db.datasource=hikari ^
  -Dstore.db.dbType=mysql ^
  -Dstore.db.driverClassName=com.mysql.cj.jdbc.Driver ^
  -Dstore.db.url="jdbc:mysql://127.0.0.1:3306/seata?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai" ^
  -Dstore.db.user=root ^
  -Dstore.db.password=123456 ^
  -jar target/seata-server.jar ^
  -p 8091 -h 127.0.0.1 -m db

pause
