#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <unistd.h>
#include <sys/socket.h>
#include <sys/un.h>
#include <errno.h>
#include <sys/types.h>
#include <sys/stat.h>
#include <sys/wait.h>
#include <systemd/sd-daemon.h>

#define BUFSIZE 1024
#define SOCKET_PATH "/run/isel/tvschat/request"

const char *script_run = "/opt/isel/tvs/tvschat/scripts/tvschat-run.sh";
const char *script_stop = "/opt/isel/tvs/tvschat/scripts/tvschat-stop.sh";
const char *script_status = "/opt/isel/tvs/tvschat/scripts/tvschat-status.sh";

static void die_perror(const char *msg) {
    perror(msg);
    exit(EXIT_FAILURE);
}

int run_script(const char *script, char *const argv[], char *output) {
    int pipefd[2];
	pipe(pipefd);

    pid_t pid = fork();
    if (pid == 0) {
        close(pipefd[0]);
        dup2(pipefd[1], 1);
        close(pipefd[1]);
        execv(script, argv);
        die_perror("execv failed");
    }
    close(pipefd[1]);
    int total = 0, n;
    while ((n = read(pipefd[0], output + total, BUFSIZE - 1 - total)) > 0) {
        total += n;
        if (total >= BUFSIZE - 1) break;
    }
    if (n < 0) die_perror("read pipe");
	output[total] = '\0';
	close(pipefd[0]);
    waitpid(pid, NULL, 0);
    return total;
}

char *strremove(char *str, const char *sub) {
    char *p, *q, *r;
    if (*sub && (q = r = strstr(str, sub)) != NULL) {
        size_t len = strlen(sub);
        while ((r = strstr(p = r + len, sub)) != NULL) {
            while (p < r)
                *q++ = *p++;
        }
        while ((*q++ = *p++) != '\0')
            continue;
    }
    return str;
}

int main() {
	int fds = sd_listen_fds(0);
	if (fds != 1) die_perror("expected 1 fd");

	int sock = SD_LISTEN_FDS_START;
	struct sockaddr_un addr;
	socklen_t len = sizeof(addr);

	for (;;) {
		int conn = accept(sock, (struct sockaddr*)&addr, &len);
		if (conn < 0) die_perror("accept");
		char buf[BUFSIZE];
		int n;

		n = read(conn, buf, BUFSIZE - 1);
		if (n <= 0) { 
			close(conn); 
			continue; 
		}
		buf[n] = '\0';
		if (n > 0 && buf[n-1] == '\n') buf[n-1] = '\0';
		
		char output[BUFSIZE];
		char script_path[256];
		
		if (strncmp(buf, "run", 3) == 0) {
			strcpy(script_path, script_run);
			strremove(buf, "run ");
			char *mode = buf;
			char *args[] = { script_path, mode[0] ? mode : NULL, NULL };
			run_script(script_path, args, output);

		} else if (strcmp(buf, "status") == 0) {
			strcpy(script_path, script_status);

			char *args[] = { script_path, NULL };
			run_script(script_path, args, output);

		} else if (strncmp(buf, "stop", 4) == 0) {
			strcpy(script_path, script_stop);
			if (buf[4] == ' ') strremove(buf, "stop ");
			char *flag = buf;
			char *args[] = { script_path, flag[0] ? flag : NULL, NULL };
			run_script(script_path, args, output);

		} else {
			strcpy(output, "Unknown command\n");
		}

		int total = strlen(output);
		int sent = 0;
		while (sent < total) {
			int w = write(conn, output + sent, total - sent);
			if (w < 0) {
				perror("write");
				break;
			}
			sent += w;
		}
		close(conn);
	}
	return 0;
}