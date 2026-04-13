#include <stddef.h>
#include <stdio.h>

#define ARRAY_SIZE(a) (sizeof(a) / sizeof(a[0]))

const char *register_name[] = { "", "rbp", "rbx", "r15", "r14", "r13", "r12" };

int invoke_and_test(void *, void (*)(), int, ...);

struct data { short flags:6; short length:10; short vals[]; };

struct info { double ref; struct data **data; int valid; };

short *get_val_ptr(struct info items[],
                      size_t item_idx, size_t data_idx, size_t val_idx, short mask);

static short *_get_val_ptr(struct info items[],
                      size_t item_idx, size_t data_idx, size_t val_idx, short mask)
{
	return items[item_idx].valid
		&& val_idx < items[item_idx].data[data_idx]->length
		&& (items[item_idx].data[data_idx]->flags & mask)
			? &items[item_idx].data[data_idx]->vals[val_idx]
			: NULL;
}


static struct data dataA = { .flags = 0b000010, .length = 3, .vals = {0x1000, 0x2000, 0x3000} };
static struct data dataB = { .flags = 0b000101, .length = 4, .vals = {0x4000, 0x5000, 0x6000, 0x7000} };
static struct data dataC = { .flags = 0b111000, .length = 2, .vals = {0x8000, 0x9000} };
static struct data dataD = { .flags = 0b000001, .length = 6, .vals = {0xA000, 0xB000, 0xC000, 0xD000, 0xE000, 0xF000} };

static struct data *datas[] = {&dataA, &dataB, &dataC, &dataD};

static struct info items[] = {
	{ .ref = 4.0, .data = datas, .valid = 1 },
	{ .ref = 2.5, .data = datas, .valid = 1 },
	{ .ref = 0.5, .data = datas, .valid = 0 },
	{ .ref = 1.0, .data = datas, .valid = 1 },
};

static struct {
	struct info *items;
	size_t item_idx, data_idx, val_idx;
	short mask;
	short *result;
} tests[] = {
	
	// Testes adicionais - Casos Válidos
	{ .items = items, .item_idx = 0, .data_idx = 1, .val_idx = 2, .mask = 0b000101, .result = &dataB.vals[2] }, 
	{ .items = items, .item_idx = 1, .data_idx = 3, .val_idx = 4, .mask = 0b000001, .result = &dataD.vals[4] }, 
	{ .items = items, .item_idx = 0, .data_idx = 0, .val_idx = 1, .mask = 0b000010, .result = &dataA.vals[1] },

	// Testes adicionais - Casos inválidos devido a valid = 0
	{ .items = items, .item_idx = 2, .data_idx = 1, .val_idx = 1, .mask = 0b000101, .result = NULL }, 

	// Testes adicionais - Casos com val_idx fora do limite
	{ .items = items, .item_idx = 0, .data_idx = 1, .val_idx = 5, .mask = 0b000101, .result = NULL }, 
	{ .items = items, .item_idx = 3, .data_idx = 2, .val_idx = 2, .mask = 0b111000, .result = NULL }, 

	// Testes adicionais - Casos com máscara incompatível
	{ .items = items, .item_idx = 1, .data_idx = 3, .val_idx = 2, .mask = 0b010000, .result = NULL }, 
	{ .items = items, .item_idx = 0, .data_idx = 2, .val_idx = 0, .mask = 0b000100, .result = NULL }, 
};

void print(struct info items[],
                      size_t item_size, size_t data_size, size_t val_size) {
	for (int i = 0; i < item_size; ++i) {
		puts("--------------------------------------------");
		for (int d = 0; d < data_size; d++) {
			for (int v = 0; v < val_size; v++)
				printf("%p ", &items[i].data[d]->vals[v]);
			putchar('\n');
		}
	}
}

int main() {
    for (int i = 0; i < ARRAY_SIZE(tests); ++i) {
		short *result = _get_val_ptr(tests[i].items, tests[i].item_idx, tests[i].data_idx, tests[i].val_idx, tests[i].mask);
        int invoke_result = invoke_and_test(&result, (void (*)())get_val_ptr,
                                             5, tests[i].items, tests[i].item_idx, tests[i].data_idx,
                                             tests[i].val_idx, tests[i].mask);

        if (invoke_result != 0) {
            printf("Your function corrupted %s, which is a callee-saved register\n", register_name[invoke_result]);
            break;
        }

        printf("Test %d: Expected result: %p, Received result: %p\n", i, tests[i].result, result);
        if (result != tests[i].result) {
            printf("[%d] Test failed. Expected: %p, Got: %p\n", i, tests[i].result, result);
        } else {
            printf("[%d] Test passed.\n", i);
        }
    }
    return 0;
}
