############################################
##
## ISEL - System Virtualization Techniques
## Autumn/Winter 2025/26
##
## Coursework Assignment #1
##

# Force evaluation of somecode.o and libchuta.so Makefiles at every use
.PHONY: all clean ../code/obj/somecode.o ../../chuta/bin/libchuta.so

all: bin/run_demo bin/crash bin/count_chars bin/math_operations

bin/run_demo: obj/run_tests.o obj/ftests.o ../code/obj/somecode.o ../../chuta/bin/libchuta.so
	gcc -g -L../../chuta/bin -o $@ obj/run_tests.o obj/ftests.o ../code/obj/somecode.o -lchuta '-Wl,-rpath,$$ORIGIN/../../../chuta/bin/'

bin/crash: src/crash.c
	gcc -Wall -pedantic -g -o $@ src/crash.c

bin/count_chars: src/count_chars.c
	gcc -Wall -pedantic -g -o $@ src/count_chars.c

bin/math_operations: src/math_operations.c
	gcc -Wall -pedantic -g -o $@ src/math_operations.c

obj/run_tests.o: src/run_tests.c src/ftests.h ../../chuta/inc/chuta.h
	gcc -Wall -pedantic -g -Isrc -I../../chuta/inc -c -o $@ src/run_tests.c

obj/ftests.o: src/ftests.c src/ftests.h ../code/inc/somecode.h ../../chuta/inc/chuta.h
	gcc -Wall -pedantic -g -Isrc -I../code/inc -I../../chuta/inc -c -o $@ src/ftests.c

../code/obj/somecode.o:
	make -C ../code obj/somecode.o

../../chuta/bin/libchuta.so:
	make -C ../../chuta bin/libchuta.so

clean:
	rm -f obj/*.o
	rm -f bin/run_demo bin/crash
	rm -f bin/run_demo bin/count_chars
