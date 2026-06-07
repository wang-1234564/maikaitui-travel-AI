#!/bin/bash
# ===========================================================
# 推送 Seata Server 配置到 Nacos
# 使用前确保 Nacos 已启动: http://127.0.0.1:8848/nacos
# ===========================================================

NACOS_ADDR="127.0.0.1:8848"
NACOS_USER="nacos"
NACOS_PASS="nacos"
DATA_ID="seataServer.properties"
GROUP="SEATA_GROUP"
NAMESPACE=""
CONFIG_FILE="$(dirname "$0")/seataServer.properties"

echo "=== Pushing Seata Server config to Nacos ==="
echo "Nacos:  $NACOS_ADDR"
echo "DataId: $DATA_ID"
echo "Group:  $GROUP"
echo ""

# URL encode the config content
CONFIG_CONTENT=$(cat "$CONFIG_FILE")

# Push via Nacos API
curl -X POST \
  "http://${NACOS_ADDR}/nacos/v1/cs/configs" \
  -d "dataId=${DATA_ID}" \
  -d "group=${GROUP}" \
  -d "namespace=${NAMESPACE}" \
  -d "content=${CONFIG_CONTENT}" \
  -d "type=properties" \
  -d "username=${NACOS_USER}" \
  -d "password=${NACOS_PASS}"

echo ""
echo "=== Done! ==="
echo "Verify at: http://${NACOS_ADDR}/nacos/v1/cs/configs?dataId=${DATA_ID}&group=${GROUP}"
