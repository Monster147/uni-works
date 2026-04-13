#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <curl/curl.h>
#include <jansson.h>
#include "../ex4/psc_tp2.h"

// Callback de escrita usado pela biblioteca cURL
size_t write_callback(void *ptr, size_t size, size_t nmemb, void *stream) {
    // Converte o stream genérico num write_buffer
    struct write_buffer *result = (struct write_buffer *)stream;

    // Certifica-se de que o buffer tem espaço suficiente para os novos dados
    while (result->current + size * nmemb >= result->max - 1) {
        result->buffer = realloc(result->buffer, result->max + BUFFER_CHUNK);
        if (NULL == result->buffer) {
            fprintf(stderr, "Out of memory\n"); // Caso não consiga alocar mais memória
            return 0;
        }
        result->max += BUFFER_CHUNK; // Atualiza o tamanho máximo do buffer
    }

    // Copia os dados recebidos para o buffer
    memcpy(result->buffer + result->current, ptr, size * nmemb);
    result->current += size * nmemb; // Atualiza a posição atual no buffer

    return size * nmemb; // Retorna o número de bytes processados
}

// Função para realizar um pedido HTTP GET e obter uma resposta JSON
json_t *http_get_json(const char *url) {
    // Inicializa a biblioteca cURL (deve ser chamada antes de qualquer uso de cURL)
    curl_global_init(CURL_GLOBAL_DEFAULT);
    CURL *hcurl = curl_easy_init(); // Cria um manipulador cURL
    if (hcurl == NULL) {
        fprintf(stderr, "Error in curl_easy_init function\n");
        goto error; // Se falhar, vai para a secção de tratamento de erro
    }

    char error_buf[CURL_ERROR_SIZE]; // Buffer para guardar mensagens de erro da cURL
    curl_easy_setopt(hcurl, CURLOPT_URL, url); // Define a URL do pedido
    curl_easy_setopt(hcurl, CURLOPT_ERRORBUFFER, error_buf); // Associa o buffer de erro
    curl_easy_setopt(hcurl, CURLOPT_WRITEFUNCTION, write_callback); // Define a função de callback para processar os dados recebidos

    // Aloca memória inicial para o buffer de resposta
    char *buffer = malloc(BUFFER_CHUNK);
    if (NULL == buffer)
        goto error;

    // Inicializa a estrutura write_buffer
    struct write_buffer write_result = {
        .buffer = buffer,
        .current = 0,
        .max = BUFFER_CHUNK
    };

    // Configura a cURL para enviar os dados para o write_buffer
    curl_easy_setopt(hcurl, CURLOPT_WRITEDATA, &write_result);
    curl_easy_setopt(hcurl, CURLOPT_VERBOSE, 0L); // Desativa logs detalhados (mudar para 3L para depuração)

    // Realiza o pedido HTTP
    int curl_res = curl_easy_perform(hcurl);

    // Limpa os recursos da cURL
    curl_easy_cleanup(hcurl);

    if (CURLE_OK != curl_res) {
        // Caso ocorra um erro no pedido HTTP, imprime a mensagem de erro
        printf("Curl error [#%X]: \"%s\"\n", curl_res, error_buf);
        goto error;
    }

    // Adiciona o terminador nulo ao final do buffer de resposta
    write_result.buffer[write_result.current] = '\0';

    // Converte o buffer de resposta JSON numa estrutura json_t da biblioteca Jansson
    json_error_t error;
    json_t *result = json_loadb(write_result.buffer, write_result.current, 0, &error);
    if (NULL == result) {
        // Caso ocorra um erro na análise do JSON, imprime a mensagem de erro
        fprintf(stderr, "error: on line %d: %s\n", error.line, error.text);
    }

    // Liberta o buffer de resposta e limpa os recursos globais da cURL
    free(write_result.buffer);
    curl_global_cleanup();
    return result;

error:
    // Limpeza de recursos em caso de erro
    curl_global_cleanup();
    return NULL;
}
/*
Incluímos bibliotecas padrão de C (stdio.h, stdlib.h, e string.h) para operações básicas.
Utilizamos curl/curl.h para pedidos HTTP e jansson.h para processamento de JSON.
Estrutura write_buffer:

Gerencia o armazenamento dos dados recebidos da resposta HTTP.
Função write_callback:

É chamada pela cURL sempre que recebe dados de um pedido HTTP.
Armazena os dados recebidos no buffer dinâmico write_buffer.
Função http_get_json:

Realiza um pedido HTTP GET para a URL fornecida.
Usa write_callback para armazenar a resposta no write_buffer.
Converte a resposta em formato JSON para uma estrutura utilizável pelo programa.
Trata erros relacionados com a inicialização da cURL, a alocação de memória e o processamento do JSON.
Tratamento de Erros:

São usadas verificações cuidadosas para lidar com alocação de memória, erros de cURL e erros no processamento de JSON.
Limpeza de Recursos:

A função garante que todos os recursos alocados são libertados em caso de sucesso ou falha, prevenindo fugas de memória.

*/
