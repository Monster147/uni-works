#include <stdio.h>
#include <unistd.h>
#include <fcntl.h>
#include <sys/mman.h>

#define DATA_SIZE 16*1024*1024

char info[DATA_SIZE];
char data[DATA_SIZE] = {1};

void * create_regions(size_t code, size_t data){
	code *= 1024;
	data *= 1024;
	void * code_region = mmap(NULL, code, PROT_READ | PROT_EXEC, MAP_PRIVATE | MAP_ANONYMOUS, -1, 0);
	void * data_region = mmap(NULL, data, PROT_READ | PROT_WRITE, MAP_SHARED | MAP_ANONYMOUS, -1, 0);
	printf("Created 4KB code region at %p and 256KB data region at %p\n", code_region, data_region);
	return data_region;
}

int main() {
	printf("PID: %u\n", getpid());

	const long PAGE_SIZE = sysconf(_SC_PAGE_SIZE);
	printf("PAGE_SIZE: %ld\n", PAGE_SIZE);

	printf("#1 (press ENTER to continue)"); getchar();

	// a

	const size_t two_MB = 2 * 1024 * 1024;
	size_t pages = two_MB / (size_t)PAGE_SIZE;
	for (size_t i = 0; i < pages; ++i) {
		info[i * PAGE_SIZE] = 1;
	}

	printf("Touched %zu pages (~%zu bytes) in .bss\n", pages, pages * (size_t)PAGE_SIZE);
		
	printf("#2 (press ENTER to continue)"); getchar();

	// b

	char read = 0;
	const size_t start_offset = 14 * PAGE_SIZE;
    for (size_t i = 0; i < 128; i++) {
        size_t offset = i * (size_t)PAGE_SIZE + start_offset;
        read += data[offset];
    }

	printf("Read 128 bytes from .data (should increase Private_Clean)\n");

	printf("#3 (press ENTER to continue)"); getchar();

	// c
	
	pid_t pid = fork();
	if (pid == 0) { // Child process
		printf("Child process (PID: %u) keeping .bss shared for 30s...\n", getpid());
		sleep(1);
		_exit(0);
	} 
	else if (pid > 0) { // Parent
		printf("Parent process waiting 30s (check smaps for lower Pss, same Rss)...\n");
		sleep(1);
		printf("Child done, Pss will go back up now.\n");
	} 
	else {
		perror("fork");
	}

	printf("#4 (press ENTER to continue)"); getchar();

	// d

	const size_t region_KB = 512 * 1024;
	void* new_region = mmap(NULL, region_KB, PROT_READ | PROT_WRITE, MAP_PRIVATE | MAP_ANONYMOUS, -1, 0);
	printf("Created 512KB code region at %p\n", new_region);

	printf("#5 (press ENTER to continue)"); getchar();

	// e

	const size_t some_KB = 256 * 1024;
	size_t secondpages = some_KB / PAGE_SIZE;
	char *p = (char*)new_region;

	for (size_t i = 0; i < secondpages; ++i) {
		p[i * PAGE_SIZE] = 1;
	}

	printf("Touched %zu pages (~%zu bytes) in new .data region\n", secondpages, secondpages * (size_t)PAGE_SIZE);

	printf("#6 (press ENTER to continue)"); getchar();

	// f

	void *data_region = create_regions(4, 256);

	printf("#7 (press ENTER to continue)"); getchar();

	// g

	const size_t new_KB = 128 * 1024;
	size_t thirdpages = new_KB / PAGE_SIZE;
	char *pointer = (char*)data_region;

	for (size_t i = 0; i < thirdpages; i++) {
		*(pointer + i * PAGE_SIZE)= 1;
	}

	printf("Touched %zu pages (~%zu bytes) in mmap'd data region (now dirty)\n",
           pages, pages * (size_t)PAGE_SIZE);

	printf("END (press ENTER to continue)"); getchar();

	return 0;
}