#include <stdio.h>
#include <stddef.h>
#include <string.h>

#define ARRAY_SIZE(a) (sizeof(a) / sizeof(a[0]))

const char *register_name[] = { "", "rbp", "rbx", "r15", "r14", "r13", "r12" };

int invoke_and_test(void *, void (*)(), int, ...);

static size_t _array_remove_cond(void **array, size_t size,
                    int (*eval)(const void *, const void *), void *context) {
	for (void **current = array, **last = array + size; current < last; ) {
		if (eval(*current, context)) {
			memmove(current, current + 1, (last - current - 1) * sizeof(void *));
			size -= 1;
			last -= 1;
		}
		else {
			current += 1;
		}
	}
	return size;
}

size_t array_remove_cond(void **array, size_t size,
                    int (*eval)(const void *, const void *), void *context);
                    
void *memmove(void *dest_str, const void *src_str, size_t numBytes);
                    
static int numbers[] = {10, 100, 20, 200};
static int *pnumbers[] = {&numbers[0], &numbers[1], &numbers[2], &numbers[3]};
static int *pnumbers1[ARRAY_SIZE(pnumbers)];
static int *pnumbers2[ARRAY_SIZE(pnumbers)];

int less_than(const void *a, const void *b);

int greater_than(const void *a, const void *b) {
	return *(int *)a > (long)b;
}

int equal_to(const void *a, const void *b) {
	return *(int *)a == (long)b;
}

static struct {
	int **origin, **array1, **array2;
	size_t size;
	int (*compare)(const void *, const void *);
	void *context;
} tests[] = {
	{ .origin = pnumbers, .size = ARRAY_SIZE(pnumbers), .array1 = pnumbers1, .array2 = pnumbers2,
	.compare = less_than, .context = (void *) 100 },
	{ .origin = pnumbers, .size = ARRAY_SIZE(pnumbers), .array1 = pnumbers1, .array2 = pnumbers2,
	.compare = greater_than, .context = (void *) 100 },
	{ .origin = pnumbers, .size = ARRAY_SIZE(pnumbers), .array1 = pnumbers1, .array2 = pnumbers2,
	.compare = equal_to, .context = (void *) 1 },
	{ .origin = pnumbers, .size = 1, .array1 = pnumbers1, .array2 = pnumbers2,
	.compare = equal_to, .context = (void *) 10 }
};

int main() {
	for (int i = 0; i < ARRAY_SIZE(tests); ++i) {
		memcpy(tests[i].array2, tests[i].origin, tests[i].size * sizeof tests[i].origin[0]);
		size_t size2 = _array_remove_cond((void **)tests[i].array2, tests[i].size, tests[i].compare, tests[i].context);

		memcpy(tests[i].array1, tests[i].origin, tests[i].size * sizeof tests[i].origin[0]);
		size_t size1;
		int invoke_result = invoke_and_test(&size1, (void (*)())array_remove_cond,
				4, tests[i].array1, tests[i].size, tests[i].compare, tests[i].context);
		if (invoke_result != 0) {
			printf("Your function currupted %s, that is a calee saved register\n",
							register_name[invoke_result]);
			break;
		}
//		printf("test[%d] returned size: %zd, expected: %zd\n", i, size1, size2);
		if (size1 == size2) {
			for (size_t j = 0; j < size1; ++j)
				if (tests[i].array1[j] != tests[i].array2[j])
					printf("array[%d]: %p, expected: %p\n", i, tests[i].array1[j], tests[i].array2[j]);
		}
		else {
			printf("test[%d] returned size: %zd, expected: %zd\n", i, size1, size2);
		}
	}
}
