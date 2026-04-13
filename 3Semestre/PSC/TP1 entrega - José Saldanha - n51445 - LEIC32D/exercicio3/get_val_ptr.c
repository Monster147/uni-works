#include <stddef.h>
#include <stdio.h>

#define ARRAY_SIZE(a) (sizeof(a) / sizeof(a[0]))

struct data { short flags:6; short length:10; short vals[]; };

struct info { double ref; struct data **data; int valid; };

short *get_val_ptr(struct info items[],
                      size_t item_idx, size_t data_idx, size_t val_idx, short mask);

short *_get_val_ptr(struct info items[],
                      size_t item_idx, size_t data_idx, size_t val_idx, short mask)
{
	return items[item_idx].valid
		&& val_idx < items[item_idx].data[data_idx]->length
		&& (items[item_idx].data[data_idx]->flags & mask)
			? &items[item_idx].data[data_idx]->vals[val_idx]
			: NULL;
}

struct data data0 = { .flags = 0b000001, .length = 5, .vals = {0x0000, 0x0000, 0x0000, 0x0000, 0x0000} };
struct data data1 = { .flags = 0b000011, .length = 5, .vals = {0x0000, 0x0000, 0x0000, 0x0000, 0x0000} };
struct data data2 = { .flags = 0b001111, .length = 5, .vals = {0x0000, 0x0000, 0x0000, 0x0000, 0x0000} };
struct data data3 = { .flags = 0b111111, .length = 5, .vals = {0x0000, 0x0000, 0x0000, 0x0000, 0x0000} };

struct data *datas[] = {&data0, &data1, &data2, &data3};

struct info items[] = {
	{ .ref = 3.5, .data = datas, .valid = 1 },
	{ .ref = 1.5, .data = datas, .valid = 1 },
	{ .ref = 3.5, .data = datas, .valid = 1 },
	{ .ref = 1.5, .data = datas, .valid = 1 },
};

struct {
	struct info *items;
	size_t item_idx, data_idx, val_idx;
	short mask;
	short *result;
} tests[] = {
	{ .items = items, .item_idx = 0, .data_idx = 3, .val_idx = 0, .mask = 0b010000 },
	{ .items = items, .item_idx = 1, .data_idx = 2, .val_idx = 2, .mask = 0b001000 },
	{ .items = items, .item_idx = 2, .data_idx = 1, .val_idx = 3, .mask = 0b100000 },
	{ .items = items, .item_idx = 3, .data_idx = 0, .val_idx = 4, .mask = 0b000001 },
	{ .items = items, .item_idx = 1, .data_idx = 2, .val_idx = 6, .mask = 0b001000 },
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
//	print(items, 4, 4, 5);
	for (int i = 0; i <  ARRAY_SIZE(tests); ++i) {
		tests[i].result = _get_val_ptr(tests[i].items,
			tests[i].item_idx, tests[i].data_idx, tests[i].val_idx,
			tests[i].mask);
	}
	for (int i = 0; i <  ARRAY_SIZE(tests); ++i) {
		short *result = get_val_ptr(tests[i].items,
			tests[i].item_idx, tests[i].data_idx, tests[i].val_idx,
			tests[i].mask);
		printf("Received result: %p\n", result);
		if (result != tests[i].result) {
			printf("[%d] received result: %p expected result: %p\n",
												i, result, tests[i].result);
		}
	}
}
