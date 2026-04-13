#!/bin/sh
/bin/ollama serve &

sleep 3

if ! /bin/ollama list | grep smollm2:135m; then
    echo "Model smollm2:135m not found, pulling..."
    /bin/ollama pull smollm2:135m
else
    echo "Model smollm2:135m already present"
fi

wait 