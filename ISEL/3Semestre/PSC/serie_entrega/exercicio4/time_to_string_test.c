/*
 *	Programação em Sistemas Computacionais
 *	Semestre de inverno de 2024/2025
 *	Série de exercícios
 *
 *	Programa de teste do 4º exercício
 *
 *	Utilização:
 *
 *	$ gcc string_to_time_test.c string_to_time.c -o string_to_time_test -g -Wall -pedantic
 *
 *	$ ./time_to_stringtest
 */

#include <stdio.h>
#include <string.h>
#include <time.h>

#define ARRAY_SIZE(a)	(sizeof(a) / sizeof(a[0]))

struct {
	char buffer[100];
	size_t buffer_size;
	struct tm tm;
	size_t result_size;
	char *result_string;
} test_array[] = {
	{
		.buffer_size = 100,
		.tm = {.tm_wday = 3, .tm_mday = 16, .tm_mon = 8, .tm_year = 124, .tm_hour = 16, .tm_min = 43, .tm_sec = 45 },
		.result_size = 34,
		.result_string = "quarta-feira, 16-09-2024, 16:43:45", 
	},
	{
		.buffer_size = 35,
		.tm = {.tm_wday = 2, .tm_mday = 24, .tm_mon = 8, .tm_year = 124, .tm_hour = 16, .tm_min = 43, .tm_sec = 45 },
		.result_size = 34,
		.result_string = "terça-feira, 24-09-2024, 16:43:45", 
	},
	{
		.buffer_size = 100,
		.result_size = 29,
		.result_string = "domingo, 00-01-1900, 00:00:00", 
	},
	{
		.buffer_size = 34,
		.tm = {.tm_wday = 2, .tm_mday = 24, .tm_mon = 8, .tm_year = 124, .tm_hour = 16, .tm_min = 43, .tm_sec = 45 },
		.result_size = 0,
	},
	{
		.buffer_size = 0,
		.result_size = 0,
	}
};

int error;

void print_result(int i, size_t result_size, char *result_string) {
	if (result_size != test_array[i].result_size) {
		printf("[%d] - Error, expected size = %zd, returned size = %zd, returned string = %s\n",
			i, test_array[i].result_size, result_size, result_string);
		
		error = 1;
	}
	else if (result_size != 0 && strcmp(result_string, test_array[i].result_string) != 0) {
			printf("[%d] - Error, expected string = %s, returned string %s\n",
				i, test_array[i].result_string, result_string);
			error = 1;
		}
}

size_t time_to_string(struct tm *tm, char *buffer, size_t buffer_size);

int main() {
	for (int i = 0; i < ARRAY_SIZE(test_array); ++i) {
		size_t result_size = time_to_string(&test_array[i].tm, test_array[i].buffer, test_array[i].buffer_size);
		print_result(i, result_size, test_array[i].buffer);
	}
	return error;
}

