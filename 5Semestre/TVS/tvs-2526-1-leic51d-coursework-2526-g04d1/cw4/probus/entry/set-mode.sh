#!/bin/sh

MODE="$1"

if [ -z "$MODE" ]; then
  echo "[entry:set-mode] Error: No mode specified."
  exit 1
fi

if [ "$MODE" != "prod" ] && [ "$MODE" != "dev" ]; then
  echo "[entry:set-mode] Error: Invalid mode specified. Valid modes are 'prod', 'dev'."
  echo "[entry:set-mode] Usage: set-mode {prod|dev}"
  exit 1
fi

if [ "$MODE" = "prod" ]; then
  ln -sf /etc/nginx/nginx-prod.conf /etc/nginx/conf.d/demoapp.conf
  echo "[entry:set-mode] Switched to production mode."
else
  ln -sf /etc/nginx/nginx-dev.conf /etc/nginx/conf.d/demoapp.conf
  echo "[entry:set-mode] Switched to development mode."
fi

nginx -s reload