#!/bin/bash

report_status(){
    local name="$1";
    local service="$2";

    echo "Status report for $name:";

    if systemctl is-active --quiet "$service"; then
        echo "$name is running."
    else
        echo "$name is not running."
    fi
}

report_status "TVSChat Webapp" "tvswebapp.service"
report_status "Ollama LLM" "ollama.service"

if systemctl is-active --quiet nginx; then
    echo "Nginx: running"
else
    echo "Nginx: stopped"
fi