////////////////////////////////////////////
//
// ISEL - System Virtualization Techniques
// Autumn/Winter 2025/26
//
// Coursework Assignment #1
//
#include "chuta.h"
#include "ftests.h"
///////////////////////////////
//
// DECLARE ALL FUNCTION TESTS
//
/*
test_function all_function_tests[] = {
    test_add,
    test_sub,
    test_strsize,
};*/
chuta all_function_tests[] = {
    {"test_add", test_add},
    {"test_sub", test_sub},
    {"test_mul", test_mul},
    {"test_div", test_div},
    {"test_div_with_0", test_div_with_0},
    {"test_is_prime", test_is_prime},
    {"test_factorial", test_factorial},
    {"test_factorial_res_1", test_factorial_res_1},
    {"test_power", test_power},
    {"test_power_exp_0", test_power_exp_0},
    {"test_strsize", test_strsize},
    {"test_rev", test_rev}
};

const char* args1[] = {"cat", NULL};
const char* args2[] = {"./crash", NULL};
const char* args3[] = {"grep", "Hello", NULL};
const char* args4[] = {"sort", NULL};
const char* args5[] = {"cut", "-d,", "-f2", NULL};
const char* args6[] = {"./count_chars", NULL};
const char* args7[] = {"./math_operations", NULL};

chuta_prog all_program_tests[] = {
    { "Cat Test", "cat", args1, "demo/test/src/dummy1.txt", "demo/test/src/expect.txt" },
	{ "Cat Invalid Test", "cat", args1, "demo/test/src/dummy2.txt", "demo/test/src/expect.txt" },
	{ "Crash Test", "demo/test/bin/crash", args2, "demo/test/src/dummy3.txt", "demo/test/src/crash.txt" },
    { "Grep Test", "grep", args3, "demo/test/src/grep.txt", "demo/test/src/grepExpect.txt" },
    { "Grep Invalid Test ", "grep", args3, "demo/test/src/dummy1.txt", "demo/test/src/grepExpect.txt" },
    { "Sort Test", "sort", args4, "demo/test/src/unsorted.txt", "demo/test/src/sorted.txt" },
    { "Sort Invalid Test ", "sort", args4, "demo/test/src/dummy1.txt", "demo/test/src/sorted.txt" },
    { "Cut Test", "cut", args5, "demo/test/src/uncut.txt", "demo/test/src/cut.txt" },
	{ "Cut Invalid Test", "cut", args5, "demo/test/src/dummy2.txt", "demo/test/src/cut.txt" },
    { "Count Chars Test", "demo/test/bin/count_chars", args6, "demo/test/src/unsorted.txt", "demo/test/src/expected_n_char.txt"},
    { "Count Chars Invalid Test", "demo/test/bin/count_chars", args6, "demo/test/src/uncut.txt", "demo/test/src/expected_n_char.txt"},
    { "Math ops Test", "demo/test/bin/math_operations", args7, "demo/test/src/ok_numbers.txt", "demo/test/src/ok_n_expect.txt"},
    { "Math ops Invalid Test", "demo/test/bin/math_operations", args7, "demo/test/src/invalid_numbers.txt", "demo/test/src/ok_n_expect.txt"},
    { "Math ops Crash Test", "demo/test/bin/math_operations", args7, "demo/test/src/crash_numbers.txt", "demo/test/src/crash_n_expect.txt"}
};
//////////////////////////////
//
// DECLARE ALL PROGRAM TESTS
//
// prog_function all_program_tests[] = {
//    /* ... */
//};
////////////////////////
//
// RUN ALL TESTS
//
// (do not modify, except to add the call to run_stdio_program_tests)
//
#define ARRLEN(arr) (sizeof(arr) / sizeof(arr[0]))
const size_t num_function_tests = ARRLEN(all_function_tests);
const size_t num_program_tests = ARRLEN(all_program_tests);
int main()
{
    run_function_tests(all_function_tests, num_function_tests, 0);
    run_stdio_program_tests(all_program_tests, num_program_tests);
    return 0;
}