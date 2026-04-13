FROM nginx:alpine

COPY ./tvschat-prod.conf /etc/nginx/nginx-prod.conf
COPY ./tvschat-dev.conf /etc/nginx/nginx-dev.conf
COPY ./set-mode.sh /usr/local/bin/set-mode

RUN chmod +x /usr/local/bin/set-mode && \
    ln -sf /etc/nginx/nginx-prod.conf /etc/nginx/conf.d/demoapp.conf