// crasha.c
#include <stdio.h>

int main() {
    int *ptr = NULL;
    printf("Trying to reach invalid memmory\n");
    int valor = *ptr;  
    printf("Cant reach this memory: %d\n", valor);
    return 0;
}
