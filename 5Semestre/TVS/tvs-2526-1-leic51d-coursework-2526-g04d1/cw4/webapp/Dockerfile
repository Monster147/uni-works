FROM node:alpine
WORKDIR /home/node/chat
COPY . .
RUN npm install && \
    chown -R node:node /home/node/
ENV NODE_PORT=54304
EXPOSE ${NODE_PORT}
USER node
CMD ["node", "tvschat.js"]