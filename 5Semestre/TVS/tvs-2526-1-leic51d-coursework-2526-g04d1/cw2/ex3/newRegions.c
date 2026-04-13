#include <stdio.h>

int regiaoCodigo(){
    int x = 0;
    for (int i = 0; i < 1024; i++) x += i;
    return x;
}

char regiaoData[256*1024] = {1};