#include <stdio.h>
#include <stdint.h>

// União para representar o float
typedef union {
    struct {
        unsigned int m: 23; // Mantissa
        unsigned int e: 8;  // Expoente
        unsigned int s: 1;  // Sinal
    };
    float f;
} Float_BF;

size_t int_to_string(unsigned value, int base, char buffer[], size_t buffer_size);

size_t float_to_string(float value, char buffer[], size_t buffer_size){
	if(buffer_size <= 8) return 0;
	
    Float_BF bf; 
    bf.f = value;
    
    int sign = bf.s;
    int exp = bf.e - 127; 
    int mantissa = bf.m | (1<<23);
    int sign_size = 0;
    
    char *buf = buffer; 
    
    switch(sign){
		case 1 :
			*buf++ = '-';
			sign_size = 1;
			break;
	}
    
    int int_part = 0;
    
    switch(exp>=0){
		case 1:
			int_part = mantissa >> (23-exp);
			break;
	}
    
    uint32_t frac_part = mantissa & ((1<<(23-exp))-1); 
    uint64_t frac = ((uint64_t)frac_part * 1000000) >> (23-exp); 
    
    char buffer_int[32];
    char int_str = int_to_string(int_part, 10, buffer_int, sizeof(buffer_int));
    
    // Formata a parte fracionária manualmente
    char buffer_frac[7]; // 6 digitos + null-terminator
    for (int i = 5; i >= 0; i--) {
        buffer_frac[i] = '0' + (frac % 10);
        frac /= 10;
    }
    buffer_frac[6] = '\0'; // Ensure null-termination
    
    int frac_len = 7;
    int size = sign_size + int_str + frac_len + 1; // +1 por causa de '.' caracter 
    if (size > buffer_size) return 0; 
    
    int length = sprintf(buf, "%s.%6s", buffer_int, buffer_frac); 

    return length + (buf - buffer); 
}
