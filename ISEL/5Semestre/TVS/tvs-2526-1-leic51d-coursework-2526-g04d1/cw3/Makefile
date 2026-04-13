.PHONY: all clean

# Compiler
CC := gcc
CFLAGS := -Wall -pedantic -g

# Directories
CLIENT_SRC_DIR := tvschat/manager/client/src
CLIENT_BIN_DIR := tvschat/manager/client/bin

DAEMON_SRC_DIR := tvschat/manager/daemon/src
DAEMON_BIN_DIR := tvschat/manager/daemon/bin

# Targets
ALL_TARGETS := $(CLIENT_BIN_DIR)/tvschat $(DAEMON_BIN_DIR)/tvschatd

all: $(ALL_TARGETS)

# Client
$(CLIENT_BIN_DIR)/tvschat: $(CLIENT_SRC_DIR)/tvschat.c
	@mkdir -p $(CLIENT_BIN_DIR)
	$(CC) $(CFLAGS) -o $@ $<

# Daemon
$(DAEMON_BIN_DIR)/tvschatd: $(DAEMON_SRC_DIR)/tvschatd.c
	@mkdir -p $(DAEMON_BIN_DIR)
	$(CC) $(CFLAGS) -o $@ $< -lsystemd

# Clean
clean:
	rm -f $(CLIENT_BIN_DIR)/tvschat
	rm -f $(DAEMON_BIN_DIR)/tvschatd