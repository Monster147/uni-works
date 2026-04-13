#include <stdio.h>

struct tm {
	int tm_sec;
	int tm_min;
	int tm_hour;
	int tm_mday;
	int tm_mon;
	int tm_year;
	int tm_wday;
	int tm_yday;
	int tm_isdst;
};

const char *weekdays[] = {"domingo", "segunda-feira", "terça-feira", "quarta-feira", 
							"quinta-feira", "sexta-feira", "sábado"};
							
size_t time_to_string(struct tm *tm, char *buffer, size_t buffer_size){
	if(buffer == NULL || buffer_size==0){
		return 0;
	}
	
	int time = snprintf(buffer, buffer_size, "%s, %02d-%02d-%04d, %02d:%02d:%02d",
						weekdays[tm -> tm_wday],
						tm -> tm_mday,
						tm -> tm_mon + 1, // mês começa em 0
						tm -> tm_year + 1900, //ano começa em 1900
						tm -> tm_hour,
						tm -> tm_min,
						tm -> tm_sec);
	if (time >= buffer_size){
		return 0;
	}
	return time;					
}
