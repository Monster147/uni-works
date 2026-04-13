#!/bin/bash

MODE=${1:-private}

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
TVSCHAT_DIR="$(dirname "$SCRIPT_DIR")"
MAIN_DIR="$(dirname "$TVSCHAT_DIR")"

NGINX_AVAIL_DIR="/etc/nginx/sites-available"
NGINX_ENABLED_DIR="/etc/nginx/sites-enabled"

set_nginx_mode() {
    case $MODE in
        private)
            echo "Setting Nginx to private mode..."
            rm -f "$NGINX_ENABLED_DIR/tvschat"
            ;;
        prod)
            echo "Setting Nginx to production mode..."
            cp "$MAIN_DIR/nginx/tvschat-prod.conf" "$NGINX_AVAIL_DIR"
            ln -sf "$NGINX_AVAIL_DIR/tvschat-prod.conf" "$NGINX_ENABLED_DIR/tvschat"
            ;;
        dev)
            echo "Setting Nginx to development mode..."
            cp "$MAIN_DIR/nginx/tvschat-dev.conf" "$NGINX_AVAIL_DIR"
            ln -sf "$NGINX_AVAIL_DIR/tvschat-dev.conf" "$NGINX_ENABLED_DIR/tvschat"
            ;;
        *)
            echo "Invalid mode specified. Use 'prod', 'private' or 'dev' (default mode is private, when no args are specified)."
            exit 1
            ;;
    esac
    systemctl reload nginx
}

ensure_services_running() {
    echo "Starting TVSChat webapp service..."
    systemctl start tvswebapp.service
    systemctl start ollama.service
}

ensure_services_running
set_nginx_mode "$MODE"
