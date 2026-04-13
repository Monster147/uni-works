#include <stdio.h>

int add(int a, int b) {
	return a + b;
}

int sub(int a, int b) {
	return b - a;                 
}

int mul(int a, int b) {
	return a * b;
}

int division(int a, int b) {
	return a / b;				   
}

int main(){
    int a, b;
    int res_add, res_sub, res_mul, res_div;
    scanf("%d %d", &a, &b);
    res_add = add(a, b);
    res_sub = sub(a, b);
    res_mul = mul(a, b);
    res_div = division(a, b);
    printf("Soma: %d\n", res_add);
    printf("Subtração: %d\n", res_sub);
    printf("Multiplicação: %d\n", res_mul);
    printf("Divisão: %d\n", res_div);

    return 0;
}