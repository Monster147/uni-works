#include <stdio.h>
#include <stdarg.h>

size_t int_to_string(unsigned value, int base, char buffer[], size_t buffer_size);
size_t float_to_string(float value, char buffer[], size_t buffer_size);

size_t mini_snprintf(char *buffer, size_t buffer_size, const char *format, ...){
	va_list args;
	char *sval;
	const char *p;
	int ival, cwritten = 0;
	float fval;
	
	va_start(args, format);
	for (p = format; *p; p++){
		if(*p != '%'){
			if(cwritten < buffer_size - 1){
				buffer[cwritten++] = *p;
		}
		continue;
	}
		switch (*++p){
			case 'c':
				ival = va_arg(args, int);
				if(cwritten < buffer_size - 1){
					buffer[cwritten++] = ival;
				}
				break;
			case 's':
				for (sval = va_arg(args, char *); *sval; sval++){
					if(cwritten < buffer_size - 1){
						buffer[cwritten++] = *sval;
					}
				}
				break;
			case 'd':
				ival = va_arg(args, int);
				cwritten += int_to_string(ival, 10, &buffer[cwritten], buffer_size - cwritten);
				break;
			case 'x':
				ival = va_arg(args, int);
				cwritten += int_to_string(ival, 16, &buffer[cwritten], buffer_size - cwritten);
				break;
			case 'f':
				fval = va_arg(args, double);
				cwritten += float_to_string(fval, &buffer[cwritten], buffer_size - cwritten);
				break;
			default:
				if(cwritten < buffer_size - 1){
					buffer[cwritten++] = *p;
				}
				else return 0;
				break;
		}
	}
	
	if(cwritten < buffer_size){
		buffer[cwritten++] = *p;
	}
	else return 0;
	va_end(args); /* clean up when done */
	return cwritten-1;
 }
