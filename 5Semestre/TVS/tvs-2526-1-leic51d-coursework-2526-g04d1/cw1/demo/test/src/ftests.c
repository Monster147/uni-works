////////////////////////////////////////////
//
// ISEL - System Virtualization Techniques
// Autumn/Winter 2025/26
//
// Coursework Assignment #1
//

#include "ftests.h"

#include "chuta.h"
#include "stdio.h"
#include "somecode.h"

void test_add() {
	
	int res = add(2, 2);
	
	//CH_ASSERT(res == 4);
	CH_ASSERT_MSG(res == 4, "Soma mal feita");
}

void test_sub() {
	
	int res = sub(5, 2);

	//CH_ASSERT(res == 3);
	CH_ASSERT_MSG(res == 3, "Subtração mal feita");
}

void test_mul() {
	int res = mul(15, 5);
	CH_ASSERT_MSG(res == 75, "Multiplicação mal feita");
}

void test_div() {
	float res = division(10, 5); 				
	CH_ASSERT_MSG(res == 2, "Divisão mal feita");
}

void test_div_with_0() {
	float res = division(0, 5); 				//will crash
	CH_ASSERT_MSG(res == 0, "Divisão mal feita");
}

void test_is_prime() {
	int res = isPrime(1193);
	CH_ASSERT_MSG(res == 1, "É suposto ser primo");
}

void test_factorial() {
	int res = factorial(5);
	CH_ASSERT_MSG(res == 120, "Mau cálculo do fatorial");
}

void test_factorial_res_1() {
	int res = factorial(0);
	CH_ASSERT_MSG(res == 1, "Fatorial de um número menor ou igual a 1 é 1");
}

void test_power() {
	size_t res = power(3, 3);
	CH_ASSERT_MSG(res == 27, "Mau cálculo da potência");
}

void test_power_exp_0() {
	size_t res = power(3, 0);
	CH_ASSERT_MSG(res == 1, "Mau cálculo da potência");
}

void test_strsize() {
	
	size_t size = strsize("ISEL");
	
	//CH_ASSERT(size == 4);
	CH_ASSERT_MSG(size == 4, "Tamanho mal calculado");
}

void test_rev() {
	char str[] = "ISEL";
	size_t invertions = rev(str);
	CH_ASSERT_MSG(invertions == 2, "String invertida incorretamente");
}
