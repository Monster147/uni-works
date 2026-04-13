#include <limits.h>
#include <stddef.h>

void rotate_right(unsigned long value[], size_t n) {
	size_t nbits = sizeof value[0] * CHAR_BIT;
	n %= nbits * 2;
	if (n == 0)
		return;
	if (n >= nbits) {
		unsigned long tmp = value[0];
		value[0] = value[1];
		value[1] = tmp;
	}
	if (n == nbits)
		return;
	unsigned long tmp = value[0];
	value[0] = (value[0] >> n) + (value[1] << (nbits - n));
	value[1] = (value[1] >> n) + (tmp << (nbits - n));  
}
