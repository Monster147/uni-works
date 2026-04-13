////////////////////////////////////////////
//
// ISEL - System Virtualization Techniques
// Autumn/Winter 2025/26
//
// Coursework Assignment #1
//

#include "somecode.h"

int add(int a, int b) {
	return a + b;
}

int sub(int a, int b) {
	return b - a;                  // or is it (a - b) ?
}

int mul(int a, int b) {
	return a * b;
}

int division(int a, int b) {
	return b / a;				   // a and b swapped
}

int isPrime(int a) {
	if (a <= 1) 
		return 0;
	for (size_t i = 2; i < a; i++) {
		if (a % i == 0)
			return 0;
	}
	return 1;
}

size_t factorial(int a) {
	if (a < 1) {
		abort();				   //something fun to happen
	}
	if (a == 1) {
		return 1;
	}
	return a + factorial(a - 1);   //should be a times, not a +
}

size_t power(int a, int b) {
	size_t temp;
	if (b == 0)
		return 1;
	temp = power(a, b / 2);
	if ((b % 2) == 0)
        return temp * temp;
    else
        return a * temp * temp;
}

size_t strsize(const char * str) {
	char * ustr = (char *)str;     // a clumsy cast
	size_t size = 0;
	for (; (*ustr++ = 0); ++size); // find the terminator '\0' (in a buggy way)
	return size;
}

size_t rev(char * str) {
	int l = 0;
	int r = strlen(str) - 1;
	char temp;
	while(l < r) {
		temp = str[l];
		str[l] = str[r];
		str[r] = temp;
		l++;
		r--;
	}
	return l; //return the number of inversions made to revert the string completely
}
