############################################
##
## ISEL - System Virtualization Techniques
## Autumn/Winter 2025/26
##
## Coursework Assignment #1
##

.PHONY: all clean

all: obj/somecode.o

obj/somecode.o: src/somecode.c inc/somecode.h
	gcc -Wall -pedantic -g -Iinc -c -o $@ src/somecode.c

clean:
	rm -f obj/*.o
