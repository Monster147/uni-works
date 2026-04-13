#include <stdio.h>
#include <stddef.h>
#include <string.h>

#define ARRAY_SIZE(a) (sizeof(a) / sizeof(a[0]))

const char *register_name[] = { "", "rbp", "rbx", "r15", "r14", "r13", "r12" };

int invoke_and_test(void *, void (*)(), int, ...);

size_t array_remove_cond(void **array, size_t size,
                    int (*eval)(const void *, const void *), void *context);
                    
void *memmove(void *dest_str, const void *src_str, size_t numBytes);

struct student {
    const char *name;
    int number;
};

int student_number_above(const void *student_ptr, const void *threshold_ptr) {
    const struct student *student = (const struct student *)student_ptr;
    const int *threshold = (const int *)threshold_ptr;
    return student->number > *threshold;
}

static struct student s1 = {"Alice", 1001};
static struct student s2 = {"Bob", 2002};
static struct student s3 = {"Charlie", 1500};
static struct student s4 = {"Diana", 4004};

static struct student *students[] = {&s1, &s2, &s3, &s4};
static struct student *students1[ARRAY_SIZE(students)];
static struct student *students2[ARRAY_SIZE(students)];

static struct {
    struct student **origin, **array1, **array2;
    size_t size;
    int (*compare)(const void *, const void *);
    void *context;
} tests[] = {
    { .origin = students, .size = ARRAY_SIZE(students), .array1 = students1, .array2 = students2,
    .compare = student_number_above, .context = (void *) &(int){1500} },  // Teste com limite 1500
    { .origin = students, .size = ARRAY_SIZE(students), .array1 = students1, .array2 = students2,
      .compare = student_number_above, .context = (void *) &(int){3000} },  // Teste com limite 3000
    { .origin = students, .size = ARRAY_SIZE(students), .array1 = students1, .array2 = students2,
      .compare = student_number_above, .context = (void *) &(int){10000} }, // Teste com limite acima de todos
    { .origin = students, .size = ARRAY_SIZE(students), .array1 = students1, .array2 = students2,
      .compare = student_number_above, .context = (void *) &(int){0} },      // Teste com limite abaixo de todos
    { .origin = students, .size = ARRAY_SIZE(students), .array1 = students1, .array2 = students2,
      .compare = student_number_above, .context = (void *) &(int){2000} },   // Teste com limite 2000
};

int main() {
    for (int i = 0; i < ARRAY_SIZE(tests); ++i) {
        memcpy(tests[i].array2, tests[i].origin, tests[i].size * sizeof tests[i].origin[0]);
        size_t size2 = array_remove_cond((void **)tests[i].array2, tests[i].size, tests[i].compare, tests[i].context);
		
        memcpy(tests[i].array1, tests[i].origin, tests[i].size * sizeof tests[i].origin[0]);
        size_t size1;
        int invoke_result = invoke_and_test(&size1, (void (*)())array_remove_cond,
                4, tests[i].array1, tests[i].size, tests[i].compare, tests[i].context);

        if (invoke_result != 0) {
            printf("Your function corrupted %s, that is a callee saved register\n",
                            register_name[invoke_result]);
            break;
        }

        if (size1 == size2) {
            int mismatch_found = 0;
            for (size_t j = 0; j < size1; ++j) {
                if (tests[i].array1[j] != tests[i].array2[j]) {
                    printf("array[%d] index %zd: expected: %p, got: %p\n", i, j, tests[i].array2[j], tests[i].array1[j]);
                    mismatch_found = 1;
                }
            }
            if (!mismatch_found) {
                printf("Test[%d] passed: Arrays match, expected and received results are the same.\n", i);
            }
        }
        else {
            printf("test[%d] returned size: %zd, expected: %zd\n", i, size1, size2);
        }
	}
}
