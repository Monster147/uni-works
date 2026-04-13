#include <stdio.h>

#define ARRAY_SIZE(a) (sizeof(a) / sizeof(a[0]))

const char *register_name[] = { "", "rbp", "rbx", "r15", "r14", "r13", "r12" };

int invoke_and_test(void *, void (*)(), int, ...);

void rotate_right(unsigned long value[], size_t n);

struct {
	unsigned nbits;
	unsigned long input[2];
	unsigned long output[2];
} values[] = {
	{4, {0xAFD5923475892300, 0xA629783EDF00B434},
		{0x4AFD592347589230, 0x0A629783EDF00B43} },
    
	{64, {0xAFD5923475892300, 0xA629783EDF00B434},
		 {0xA629783EDF00B434, 0xAFD5923475892300} },
     
	{68, {0xAFD5923475892300, 0xA629783EDF00B434},
		 {0x0A629783EDF00B43, 0x4AFD592347589230} },
};

int main() {
	for (size_t i = 0; i < ARRAY_SIZE(values); i++) {
		unsigned long value[] = {values[i].input[0], values[i].input[1]};

		size_t received_result;
		int result = invoke_and_test(&received_result, (void (*)())rotate_right,
													2, value, values[i].nbits);
		if (result != 0) {
			printf("Your function currupted %s, that is a calee saved register\n",
							register_name[result]);
			break;
		}
		if (value[0] != values[i].output[0] || value[1] != values[i].output[1]) {
			printf("[%zd] Before:   %016lx %016lx\n", i, values[i].input[1], values[i].input[0]);
			printf("    After:    %016lx %016lx\n", value[1], value[0]);
			printf("    Expected: %016lx %016lx\n", values[i].output[1], values[i].output[0]);
		}
//		else
//			printf("OK\n");
	}
}

