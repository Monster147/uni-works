////////////////////////////////////////////
//
// ISEL - System Virtualization Techniques
// Autumn/Winter 2025/26
//
// Coursework Assignment #1
//

#include "chuta.h"

#include <stdio.h>
#include <stdio.h>
#include <unistd.h>
#include <sys/types.h>
#include <sys/wait.h>
#include <stdlib.h>
#include <sys/stat.h>
#include <fcntl.h>

static int run_test(test_function tfunc, char *errbuf, size_t errbuf_size);
static int run_program(chuta_prog ptest);
static int compare_files(const char *file1, const char *file2);

void run_function_tests(chuta tests[], size_t num_tests, bool stop_at_first_failure) {
	size_t tests_executed = 0, tests_succeeded = 0;

	for (size_t i = 0; i < num_tests; i++) {
		
		char errbuf[1024];

		int res = run_test(tests[i].tfunc, errbuf, sizeof(errbuf));

		if(WIFEXITED(res)){
			if(WEXITSTATUS(res) == EXIT_SUCCESS){
				printf("Test %lu (%s) passed\n", i + 1, tests[i].func_name);
				tests_succeeded++;
			} else {
				printf("Test %lu (%s) failed\n", i + 1, tests[i].func_name);
				
				printf("\t%s", errbuf);
				
				if (stop_at_first_failure) {
					tests_executed++;
					break;
				}

			};
		} else if (WIFSIGNALED(res)) {
			printf("Test %lu (%s) crashed with signal %d\n", i + 1, tests[i].func_name ,WTERMSIG(res));
			if (stop_at_first_failure) {
				tests_executed++;
				break;
			};
		};
		printf("===============================================\n");
		tests_executed++;
	}

	printf("Summary: Total tests: %lu, Executed: %lu, Succeeded: %lu, Failed: %lu\n",
		num_tests, tests_executed, tests_succeeded, tests_executed - tests_succeeded);
	printf("===============================================\n");
}

static int run_test(test_function tfunc, char *errbuf, size_t errbuf_size) {
	int pipefd[2];

	pipe(pipefd);

	pid_t pid = fork();
	if (pid == 0) {
		dup2(pipefd[1], 2);
		close(pipefd[0]);
		close(pipefd[1]);

		tfunc();
		exit(EXIT_SUCCESS);
	} else {
		close(pipefd[1]);
		ssize_t n = read(pipefd[0], errbuf, errbuf_size - 1);
		if (n >= 0) {
			errbuf[n] = '\0';
		} else {
			errbuf[0] = '\0';
		}
		close(pipefd[0]);
		int res = 0;
		waitpid(pid, &res, 0);
		return res;
	}
}

void run_stdio_program_tests(chuta_prog programs[], size_t num_programs) {
    size_t num_programs_executed = 0, programs_succeded = 0;

    for(size_t i = 0; i < num_programs; i++) {
        int res = run_program(programs[i]);

		if(res == 0) {
			printf("Test %lu (%s) passed\n", i + 1, programs[i].test_name);
			programs_succeded++;
		} else if (res == 1) {
			printf("Test %lu (%s) failed\n", i + 1, programs[i].test_name);
		} else if (res == -2) {
			printf("Test %lu (%s) crashed with signal %d\n", i + 1, programs[i].test_name, WTERMSIG(res));
			
		} 
		printf("===============================================\n");
		num_programs_executed++;
    }
    printf("Summary: Total tests: %lu, Executed: %lu, Succeeded: %lu, Failed: %lu\n",
		num_programs,  num_programs_executed , programs_succeded, num_programs_executed - programs_succeded);
}

static int run_program(chuta_prog ptest) {
    pid_t pid = fork();
    if (pid == 0) {
		fprintf(stderr, "Test source: %s\n", ptest.std_input_text_file_path);
		int fd_in = open(ptest.std_input_text_file_path, O_RDONLY);
		dup2(fd_in, 0);
		close(fd_in);

		int fd_out = open("tmp_output.txt", O_WRONLY | O_CREAT , 0644);
		dup2(fd_out, 1);
		close(fd_out);

        execvp(ptest.command, (char * const *)ptest.args);

		perror("execvp failed");
        exit(EXIT_FAILURE);
    } else {
        int res;
        waitpid(pid, &res, 0);

		int cmp = compare_files("tmp_output.txt", ptest.expected_output_text_file_path);

		remove("tmp_output.txt");

		if (WIFSIGNALED(res)) {
			return -2;
		} else if(cmp == 0 && WIFEXITED(res) && WEXITSTATUS(res) == EXIT_SUCCESS) {
			return 0;
		} else {
			return 1;
		}
    }
}

static int compare_files(const char *file1, const char *file2) {
	FILE *f1 = fopen(file1, "r");
	FILE *f2 = fopen(file2, "r");

	if (f1 == NULL || f2 == NULL) {
		if (f1) fclose(f1);
		if (f2) fclose(f2);
		return -1;
	}

	int ch1, ch2;
	do {
		ch1 = fgetc(f1);
		ch2 = fgetc(f2);
		if (ch1 != ch2) {
			fclose(f1);
			fclose(f2);
			return 1;
		}
	} while (ch1 != EOF && ch2 != EOF);

	fclose(f1);
	fclose(f2);

	return (ch1 == EOF && ch2 == EOF) ? 0 : 1; 
}