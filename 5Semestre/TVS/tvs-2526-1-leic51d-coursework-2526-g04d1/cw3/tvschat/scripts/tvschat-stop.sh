#!/bin/bash

STOP_LLM=false
NGINX_ENABLED_DIR="/etc/nginx/sites-enabled"

if [ "$1" == "-llm" ]; then
    STOP_LLM=true
fi

echo "Stopping TVSChat webapp service..."
systemctl stop tvswebapp.service
rm -f "$NGINX_ENABLED_DIR/tvschat"

if $STOP_LLM; then
    echo "Stopping TVSChat LLM service..."
    systemctl stop ollama.service
fi

systemctl reload nginx


