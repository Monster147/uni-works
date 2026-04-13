#include <stdio.h>
#include <string.h>

#define ARRAY_SIZE(a) (sizeof(a) / sizeof(a[0]))

const char *register_name[] = { "", "rbp", "rbx", "r15", "r14", "r13", "r12" };

int invoke_and_test(void *, void (*)(), int, ...);

int my_memcmp(const void *ptr1, const void *ptr2, size_t num);

int array1[] = {5463453, 6456456, 56456, 456, 45645, 645};
int array2[] = {5463453, 6456456, 56456, 456, 45645, 645};

char array3[] = {0, 2, 1, 3, 4, 5, 6, 7};
char array4[] = {0, 1, 2, 3, 4, 5, 6, 7};

char array5[] = {0, 1, 2, 3, 4, 5, 6, 7, 0, 1, 2, 3, 4, 5, 6, 7};
char array6[] = {0, 1, 2, 3, 4, 5, 6, 7, 0, 1, 2, 3, 5, 5, 6, 7};

struct {
	void *ptr1, *ptr2;
	size_t size;
	int result;
} tests[] = {
	{.ptr1 = array1, .ptr2 = array2, .size = sizeof array1, .result = 0},
	{.ptr1 = array3, .ptr2 = array4, .size = sizeof array3, .result = +1}, //1
	{.ptr1 = array5 + 1, .ptr2 = array6 + 1, .size = sizeof array5 - 1, .result = -1}, //2
	{.ptr1 = array5 + 3, .ptr2 = array6 + 3, .size = 8, .result = 0},
	{.ptr1 = array6, .ptr2 = array5, .size = sizeof array5 - 3, .result = +1}, //4
};

int main() {
	for (int i = 0; i < ARRAY_SIZE(tests); ++i) {
		long received_result;	// temm que ser um valor a 64 bits
		int invoke_result = invoke_and_test(&received_result, (void (*)())my_memcmp,
									3, tests[i].ptr1, tests[i].ptr2, tests[i].size);
		if (invoke_result != 0) {
			printf("Your function currupted %s, that is a calee saved register\n",
							register_name[invoke_result]);
			break;
		}
		int result = received_result;
		//printf("Received result: %d\n", result);
		if (   (result == 0 && tests[i].result != 0)
			|| (result <  0 && tests[i].result >= 0)
			|| (result >  0 && tests[i].result <= 0) )
				printf("[%d] received result: %d expected result: %d\n",
												i, result, tests[i].result);
	}
}
