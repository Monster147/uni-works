#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <getopt.h>

#define MAX_LINE_LENGTH 1024
#define MAX_FIELD_LENGTH 256

void print_grid(char ***data, int rows, int cols, char alignment) {
    int *col_widths = (int *)malloc(cols * sizeof(int));
    for (int j = 0; j < cols; j++) {
        col_widths[j] = 0;
        for (int i = 0; i < rows; i++) {
            int len = strlen(data[i][j]);
            if (len > col_widths[j]) {
                col_widths[j] = len;
            }
        }
    }

    for (int i = 0; i < rows; i++) {
        for (int j = 0; j < cols; j++) {
            printf("+");
            for (int k = 0; k < col_widths[j] + 2; k++) {
                printf("-");
            }
        }
        printf("+\n");

        for (int j = 0; j < cols; j++) {
            printf("| ");
            int len = strlen(data[i][j]);
            if (alignment == 'l') {
                printf("%-*s ", col_widths[j], data[i][j]);
            } else {
                printf("%*s ", col_widths[j], data[i][j]);
            }
        }
        printf("|\n");
    }

    for (int j = 0; j < cols; j++) {
        printf("+");
        for (int k = 0; k < col_widths[j] + 2; k++) {
            printf("-");
        }
    }
    printf("+\n");

    free(col_widths);
}

int main(int argc, char *argv[]) {
    int opt;
    char *output_filename = NULL;
    char alignment = 'r';
    FILE *output_file = stdout;

    while ((opt = getopt(argc, argv, "o:a:")) != -1) {
        switch (opt) {
            case 'o':
                output_filename = optarg;
                break;
            case 'a':
                alignment = optarg[0];
                break;
            default:
                fprintf(stderr, "Usage: %s [-o outputfile] [-a alignment] filename\n", argv[0]);
                exit(EXIT_FAILURE);
        }
    }

    if (optind >= argc) {
        fprintf(stderr, "Expected argument after options\n");
        exit(EXIT_FAILURE);
    }

    char *filename = argv[optind];
    char *csv_file_path = getenv("CSV_FILE_PATH");
    char filepath[MAX_LINE_LENGTH];

    if (csv_file_path) {
        snprintf(filepath, sizeof(filepath), "%s/%s", csv_file_path, filename);
    } else {
        snprintf(filepath, sizeof(filepath), "./%s", filename);
    }

    FILE *file = fopen(filepath, "r");
    if (!file) {
        perror("Error opening file");
        exit(EXIT_FAILURE);
    }

    if (output_filename) {
        output_file = fopen(output_filename, "w");
        if (!output_file) {
            perror("Error opening output file");
            exit(EXIT_FAILURE);
        }
    }

    char line[MAX_LINE_LENGTH];
    char ***data = NULL;
    int rows = 0, cols = 0;

    while (fgets(line, sizeof(line), file)) {
        data = (char ***)realloc(data, (rows + 1) * sizeof(char **));
        data[rows] = (char **)malloc(MAX_FIELD_LENGTH * sizeof(char *));
        char *token = strtok(line, ",");
        int col = 0;
        while (token) {
            data[rows][col] = (char *)malloc((strlen(token) + 1) * sizeof(char));
            strcpy(data[rows][col], token);
            token = strtok(NULL, ",");
            col++;
        }
        if (col > cols) {
            cols = col;
        }
        rows++;
    }

    fclose(file);

    print_grid(data, rows, cols, alignment);

    for (int i = 0; i < rows; i++) {
        for (int j = 0; j < cols; j++) {
            free(data[i][j]);
        }
        free(data[i]);
    }
    free(data);

    if (output_file != stdout) {
        fclose(output_file);
    }

    return 0;
}
