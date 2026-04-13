############################################
##
## ISEL - System Virtualization Techniques
## Autumn/Winter 2025/26
##
## Coursework Assignment #1
##

.PHONY: all clean

all: bin/libchuta.so

bin/libchuta.so: obj/chuta.o inc/chuta.h
	gcc -shared -g -o $@ obj/chuta.o

obj/chuta.o: src/chuta.c inc/chuta.h
	gcc -Wall -pedantic -g -fPIC -Iinc -c -o $@ src/chuta.c

clean:
	rm -f obj/*.o
	rm -f bin/libchuta.so
