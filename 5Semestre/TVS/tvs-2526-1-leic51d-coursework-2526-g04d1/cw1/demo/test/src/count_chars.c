#include <stdio.h>

long count_chars_in_stdin() {
    long count = 0;
    int c;

    while ((c = fgetc(stdin)) != EOF)
    {
        count++;
    }

    return count;
}

int main() {
    long num_chars = count_chars_in_stdin();
    if (num_chars >= 0)
    {
        printf("Number of characters is %ld\n", num_chars);
    }
    else
    {
        printf("Failed to read the stdin.\n");
    }

    return 0;
}