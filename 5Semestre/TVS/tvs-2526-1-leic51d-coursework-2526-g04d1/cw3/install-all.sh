#!/bin/bash

# Create users and groups

# Copy files from the repository directory into system directories like:
#    /etc/nginx/sites-available/
#    /etc/systemd/system/
#    /opt/isel/tvs/

# Set proper permissions

# Leave the system in a well known state

NGINX_AVAIL_DIR="/etc/nginx/sites-available"
NGINX_ENABLED_DIR="/etc/nginx/sites-enabled"
TARGET_BASE="/opt/isel/tvs/"
SYSTEMD_DIR="/etc/systemd/system/"
SOCKET_PATH="/run/isel/tvschat/"

CW3_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

##########################
# 1 - create users/groups
##########################

groupadd -f tvsgrp

USER="${1:-isel}"

if ! id -u "$USER" >/dev/null; then
    echo "A criar utilizador $USER"
    useradd "$USER"
else
    echo "Utilizador $USER já existe - a continuar..."
fi

usermod -a -G tvsgrp "$USER"

#######################
# 2 - make directories
#######################
echo "A criar diretório base em $TARGET_BASE"
mkdir -p "$TARGET_BASE"
echo "A criar diretório base em $SOCKET_PATH"
mkdir -p "$SOCKET_PATH"

####################################################################
# 3 - installing webapp dependencies and compiling daemon and client
####################################################################
echo "A instalar as dependências da webapp"
cd ./tvschat/webapp
npm install --silent
cd "$CW3_DIR"

echo "A compilar o daemon e client"
make

###########################################
# 4 - copy tvschat folder to opt/isel/tvs/
###########################################
echo "A copiar ficheiros para $TARGET_BASE"
cp -r "$CW3_DIR/tvschat" "$TARGET_BASE"
cp -r "$CW3_DIR/nginx" "$TARGET_BASE"

###############################
# 5 - install systemd files
###############################
echo "A instalar ficheiros systemd em $SYSTEMD_DIR"
cp "$CW3_DIR/systemd/tvswebapp.service" "$SYSTEMD_DIR"
cp "$CW3_DIR/systemd/tvschatd.socket" "$SYSTEMD_DIR"

cp "$CW3_DIR/systemd/tvschatd.service" "$SYSTEMD_DIR"

systemctl daemon-reload

############################
# 6 - configure permissions
############################
echo "A configurar permissões..."

chmod -R 750 "$CW3_DIR/tvschat/scripts/tvschat-run.sh"
chmod -R 750 "$CW3_DIR/tvschat/scripts/tvschat-status.sh"
chmod -R 750 "$CW3_DIR/tvschat/scripts/tvschat-stop.sh"
chmod -R 770 "$CW3_DIR/tvschat/manager/"


#############################################
# 7 - leave the system in a well known state
#############################################
echo "A deixar o sistema num estado conhecido (private)..."
systemctl start tvswebapp.service
systemctl enable tvschatd.socket
systemctl start tvschatd.socket
systemctl enable tvschatd.service
systemctl enable ollama.service
systemctl start ollama.service

rm -f "$NGINX_ENABLED_DIR/tvschat"

systemctl reload nginx

#############################################
# 8 - end
#############################################
echo "Instalação concluída com sucesso."
echo "Agora pode executar: "
echo "  - /opt/isel/tvs/tvschat/manager/client/bin/tvschat run [private|dev|prod] "
echo "  - /opt/isel/tvs/tvschat/manager/client/bin/tvschat stop [-llm] "
echo "  - /opt/isel/tvs/tvschat/manager/client/bin/tvschat status"
