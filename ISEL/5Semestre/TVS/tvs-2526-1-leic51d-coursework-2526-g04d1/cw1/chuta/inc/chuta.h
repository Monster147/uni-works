////////////////////////////////////////////
//
// ISEL - System Virtualization Techniques
// Autumn/Winter 2025/26
//
// Coursework Assignment #1
//

#ifndef CHUTA_H
#define CHUTA_H

#include <stdlib.h>
#include <unistd.h>
#include <stdbool.h>
#include <string.h>

#define CH_ASSERT(test_expression) \
	do { if (!(test_expression)) { \
		exit(EXIT_FAILURE); \
		} } while (0)

#define CH_ASSERT_MSG(test_expression, error_msg) \
	do { if (!(test_expression)) { \
		 char msg[256]; \
            snprintf(msg, sizeof(msg), \
                     "Assertion failed: %s, Message: %s, File: %s, Line: %d\n", \
                     #test_expression, error_msg, __FILE__, __LINE__); \
		write(2, msg, strlen(msg)); \
		exit(EXIT_FAILURE); \
	} } while (0)

typedef void (*test_function)();

typedef struct {
	const char* func_name;
	test_function tfunc;
} chuta;

typedef struct {
	const char* test_name;
	const char* command;
	const char* const* args;
	const char* std_input_text_file_path;
	const char* expected_output_text_file_path;
} chuta_prog;


void run_function_tests(chuta tests[], size_t num_tests, bool stop_at_first_failure);

void run_stdio_program_tests(chuta_prog programs[], size_t num_programs);

#endif // CHUTA_H
