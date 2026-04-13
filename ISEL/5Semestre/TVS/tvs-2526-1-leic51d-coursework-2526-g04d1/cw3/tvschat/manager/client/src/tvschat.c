#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <unistd.h>
#include <sys/socket.h>
#include <sys/un.h>
#include <sys/types.h>
#include <sys/wait.h>
#include <sys/stat.h>
#include <fcntl.h>

#define BUFSIZE 1024

void error(char *msg) {
	perror(msg);
	exit(1);
}

int main(int argc, const char * argv[]) {
	// These must always be the first instructions. DON'T EDIT
	close(0); open("/dev/null", O_RDONLY);       // DON'T EDIT

	const char *SOCKET_PATH = "/run/isel/tvschat/request";
    char request[128] = {0};

	if (argc < 2) {
		fprintf(stderr, "usage: tvschat <command>\n");
		fprintf(stderr, "commands:\n");
		fprintf(stderr, "  run <private|prod|dev>\n");
		fprintf(stderr, "  stop [-llm]\n");
		fprintf(stderr, "  status\n");
		exit(1);
	}

	if (strcmp(argv[1], "run") == 0){
		if (argc == 3 && argv[2]) snprintf(request, sizeof(request), "run %s", argv[2]);
		else snprintf(request, sizeof(request), "run private");
	} else if(strcmp(argv[1], "stop") == 0) {
		if(argc == 3 && strcmp(argv[2], "-llm") == 0)
			snprintf(request, sizeof(request), "stop -llm");
		else 
			snprintf(request, sizeof(request), "stop");
	} else if(strcmp(argv[1], "status") == 0) {
		snprintf(request, sizeof(request), "status");
	}
	else {
		error("invalid command");
	}

	int conn_fd = socket(AF_UNIX, SOCK_STREAM, 0);
	if(conn_fd < 0 ) {
		error("ERROR creating socket");
	}

	struct sockaddr_un srv_addr;
	memset(&srv_addr, 0, sizeof(srv_addr));
	srv_addr.sun_family = AF_UNIX;
	strcpy(srv_addr.sun_path, SOCKET_PATH);

	if(connect(conn_fd, (struct sockaddr *)&srv_addr, sizeof(srv_addr)) < 0) {
		error("ERROR connecting socket");
	}

	if (write(conn_fd, request, strlen(request)) < 0)
        error("ERROR writing");

	char buf[BUFSIZE];
	int n;
	while ((n = read(conn_fd, buf, sizeof(buf)-1)) > 0) {
		buf[n] = '\0';
		printf("%s", buf);
	}

    close(conn_fd);
    return 0;
	
}