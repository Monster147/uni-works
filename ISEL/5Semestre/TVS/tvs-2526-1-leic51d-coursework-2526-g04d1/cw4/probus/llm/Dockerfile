FROM ollama/ollama:0.6.5

COPY newEntrypoint.sh /entrypoint.sh
RUN chmod +x /entrypoint.sh

VOLUME ["/root/.ollama"]

ENTRYPOINT ["/entrypoint.sh"]
