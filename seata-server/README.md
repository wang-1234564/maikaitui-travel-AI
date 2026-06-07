# Seata Server 部署说明

## 版本信息
- Seata Server: **2.1.0**
- 配置中心: **Nacos** (127.0.0.1:8848)
- 存储模式: **db** (MySQL)
- 端口: **7091** (控制台), **8091** (RPC)

## 部署前检查

1. ✅ MySQL 已启动，已创建 `seata` 数据库
2. ✅ Nacos 已启动 (127.0.0.1:8848)
3. ✅ Java 17+ 环境

## 快速启动 (3 步)

### Step 1: 推送配置到 Nacos

```bash
# Git Bash / WSL 环境
bash push-config-to-nacos.sh
```

或手动在 Nacos 控制台创建：
- DataId: `seataServer.properties`
- Group: `SEATA_GROUP`
- 内容: 复制 `seataServer.properties` 文件内容

### Step 2: 下载并启动 Seata Server

```cmd
# Windows CMD (双击运行)
start.bat
```

首次运行会自动下载 Seata Server 2.1.0。

### Step 3: 验证

浏览器访问: http://127.0.0.1:7091

## 手动操作

### 手动下载 Seata Server

从 [GitHub Releases](https://github.com/apache/incubator-seata/releases/tag/v2.1.0) 下载 `seata-server-2.1.0.zip`，解压到当前目录。

### 手动启动

```bash
cd seata-server-2.1.0
# Linux/Mac
bin/seata-server.sh -p 8091 -h 127.0.0.1
```

```cmd
REM Windows
cd seata-server-2.1.0
bin\seata-server.bat -p 8091 -h 127.0.0.1
```

## Docker 方式 (备选)

```bash
docker run -d \
  --name seata-server \
  -p 7091:7091 \
  -p 8091:8091 \
  -v ./application.yml:/seata-server/resources/application.yml \
  seataio/seata-server:2.1.0
```

## 配置文件说明

| 文件 | 用途 |
|------|------|
| `application.yml` | Seata Server 主配置 |
| `seataServer.properties` | Nacos 远端配置 |
| `start.bat` | Windows 一键启动脚本 |
| `push-config-to-nacos.sh` | 推送配置到 Nacos |
