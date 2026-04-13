#include <stdio.h>

size_t int_to_string(unsigned value, int base, char buffer[], size_t buffer_size){
    if(base != 2 && base != 8 && base != 10 && base != 16){
        return 0; //base invalÃda
    }

    //Inicializa o indice do buffer
    size_t index = 0;
    
    //Converte para a base desejada
    do{
        int digit = value % base;
        value /= base;

        //Converte o digito retirado para um caracter
        if(digit < 10){
            buffer[index++] = '0' + digit;
        }
        else{
            buffer[index++] = 'a' + digit - 10;
        }
    } while (value > 0);

    //Decide o prefixo para base 2, 8, 16
    switch(base){
        case 2:    
            buffer[index++] = 'b';
            buffer[index++] = '0';
            break;
        case 8:
            buffer[index++] = '0';
            break;
        case 16:
            buffer[index++] = 'x';
            buffer[index++] = '0';
            break;
    }
    
    int lastindex = index - 1;
    int firstindex = 0;
    char tmp;
    
    while (lastindex > firstindex) {
        tmp = buffer[lastindex];
        buffer[lastindex] = buffer[firstindex];
        buffer[firstindex] = tmp;
        firstindex++;
        lastindex--;
    }
    

    buffer[index++] = '\0';

    if(index>buffer_size){
        return 0;
    }
    
    return index-1;
}
