#!/bin/sh

MODE="$1"

if [ -z "$MODE" ]; then
  echo "[tvschat-set-mode.sh] Error: No mode specified."
  exit 1
fi

if [ "$MODE" != "prod" ] && [ "$MODE" != "dev" ]; then
  echo "[tvschat-set-mode.sh] Error: Invalid mode specified. Valid modes are 'prod', 'dev'."
  echo "[tvschat-set-mode.sh] Usage: tvschat-set-mode.sh {prod|dev}"
  exit 1
fi

if [ "$MODE" = "dev" ]; then
  docker network connect tvschat-probus_devnet tvschat-probus-entry-1
  echo "[tvschat-set-mode.sh] Switched to development mode."
else
  docker network disconnect tvschat-probus_devnet tvschat-probus-entry-1
  echo "[tvschat-set-mode.sh] Switched to production mode."
fi

docker compose exec entry set-mode "$MODE"