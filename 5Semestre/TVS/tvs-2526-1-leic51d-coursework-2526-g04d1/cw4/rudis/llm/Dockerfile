FROM ollama/ollama:0.6.5 AS builder

RUN ollama serve & \
    sleep 3 && \
    ollama pull smollm2:135m

FROM ollama/ollama:0.6.5

COPY --from=builder /root/.ollama /root/.ollama

EXPOSE 11434

CMD ["serve"]