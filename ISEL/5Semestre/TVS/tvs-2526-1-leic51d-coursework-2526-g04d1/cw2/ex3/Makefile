CC = gcc
CFLAGS = -Wall -fPIC

LIB_SRCS = newRegions.c
LIB_OBJS = $(LIB_SRCS:.c=.o)
LIB_TARGET = libnewRegions.so

PROG_SRCS = prog.c
PROG_OBJS = $(PROG_SRCS:.c=.o)
PROG_TARGET = prog

all: $(LIB_TARGET) $(PROG_TARGET)

$(LIB_TARGET): $(LIB_OBJS)
	$(CC) -shared -o $@ $(LIB_OBJS)

$(PROG_TARGET): $(PROG_OBJS)
	$(CC) $(CFLAGS) -o $@ $(PROG_OBJS) -ldl

%.o: %.c
	$(CC) $(CFLAGS) -c $< -o $@

clean:
	rm -f $(LIB_OBJS) $(PROG_OBJS) $(LIB_TARGET) $(PROG_TARGET)

.PHONY: all clean