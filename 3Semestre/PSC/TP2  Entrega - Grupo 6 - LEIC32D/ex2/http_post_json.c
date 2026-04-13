#include <curl/curl.h>
#include <jansson.h>
#include <string.h>
#include <stdbool.h>
#include "../ex4/psc_tp2.h" // Header personalizado com funções auxiliares, como write_callback e definições de constantes.

// Callback para leitura de dados do buffer, usado para preencher o corpo do POST.
size_t read_callback(char *dest, size_t size, size_t nmemb, void *userp) {
    struct read_buffer *rd = (struct read_buffer *)userp; // Converte o ponteiro genérico para a estrutura de buffer.
    size_t buffer_size = size * nmemb; // Determina o tamanho máximo de dados que podem ser copiados.

    if (rd->current < rd->max) { // Verifica se ainda há dados no buffer para serem enviados.
        size_t copy_this_much = rd->max - rd->current; // Calcula o tamanho restante de dados no buffer.
        if (copy_this_much > buffer_size) // Limita o tamanho ao máximo permitido pela chamada.
            copy_this_much = buffer_size;
        memcpy(dest, rd->buffer + rd->current, copy_this_much); // Copia os dados para o destino.
        rd->current += copy_this_much; // Atualiza o índice do buffer.
        return copy_this_much; // Retorna o número de bytes copiados.
    }

    return 0; // Se não houver mais dados, retorna 0 para indicar o fim.
}

// Função para realizar um pedido HTTP POST com JSON.
bool http_post_json(const char *url, json_t *data) {
    curl_global_init(CURL_GLOBAL_DEFAULT); // Inicializa o ambiente global do cURL.
    CURL *curl = curl_easy_init(); // Cria uma instância de cURL para realizar o pedido.
    struct curl_slist *list = NULL; // Inicializa a lista de cabeçalhos HTTP.

    if (curl != NULL) {
        // Adiciona o cabeçalho "Content-Type: application/json".
        list = curl_slist_append(list, "Content-Type: application/json");
        if (!list) // Verifica se houve erro ao criar a lista.
            goto error;

        curl_easy_setopt(curl, CURLOPT_HTTPHEADER, list); // Define os cabeçalhos HTTP.
        curl_easy_setopt(curl, CURLOPT_URL, url); // Define o URL do pedido.
        curl_easy_setopt(curl, CURLOPT_POST, 1L); // Configura o método HTTP para POST.
        curl_easy_setopt(curl, CURLOPT_READFUNCTION, read_callback); // Define o callback para leitura de dados do buffer.

        char *buffer = malloc(BUFFER_CHUNK); // Aloca memória para o buffer de leitura.
        if (NULL == buffer) // Verifica falha de alocação.
            goto error;

        // Inicializa a estrutura de buffer de leitura.
        struct read_buffer read_data = {
            .buffer = buffer,
            .current = 0,
            .max = BUFFER_CHUNK
        };

        // Serializa o objeto JSON para o buffer.
        read_data.max = json_dumpb(data, read_data.buffer, read_data.max, 0);
        curl_easy_setopt(curl, CURLOPT_READDATA, &read_data); // Configura os dados para o callback de leitura.
        curl_easy_setopt(curl, CURLOPT_VERBOSE, 0L); // Define o nível de detalhe do log (0L = silencioso, 3L = detalhado).
        curl_easy_setopt(curl, CURLOPT_POSTFIELDSIZE, (long)read_data.max); // Informa o tamanho do corpo do POST.
        curl_easy_setopt(curl, CURLOPT_WRITEFUNCTION, write_callback); // Callback para processar a resposta do servidor.

        buffer = malloc(BUFFER_CHUNK); // Aloca memória para o buffer de escrita.
        if (NULL == buffer) // Verifica falha de alocação.
            goto error;

        // Inicializa a estrutura de buffer de escrita.
        struct write_buffer write_result = {
            .buffer = buffer,
            .current = 0,
            .max = BUFFER_CHUNK
        };

        curl_easy_setopt(curl, CURLOPT_WRITEDATA, &write_result); // Configura o buffer de escrita para armazenar a resposta.

        // Executa o pedido HTTP.
        CURLcode curl_result = curl_easy_perform(curl);
        free(read_data.buffer); // Libera o buffer de leitura após o uso.

        if (CURLE_OK != curl_result) { // Verifica se houve erro no pedido.
            fprintf(stderr, "curl told us %d\n", curl_result); // Log de erro.
            free(read_data.buffer); // Libera o buffer antes de sair.
            goto error;
        }

        // Adiciona o terminador de string ao buffer da resposta.
        write_result.buffer[write_result.current] = '\0';

        // Processa a resposta como JSON.
        json_error_t error;
        json_t *result = json_loadb(write_result.buffer, write_result.current, 0, &error);
        free(write_result.buffer); // Libera o buffer de escrita após o uso.
        if (NULL == result) { // Verifica erro ao carregar JSON.
            fprintf(stderr, "error: on line %d: %s\n", error.line, error.text); // Log de erro.
        }

        json_decref(result); // Libera o objeto JSON processado.
        curl_slist_free_all(list); // Libera a lista de cabeçalhos.
        curl_easy_cleanup(curl); // Limpa a instância de cURL.
        curl_global_cleanup(); // Finaliza o ambiente global de cURL.
        return true; // Indica sucesso.
    }
    
error:
    curl_global_cleanup(); // Garante que os recursos de cURL sejam limpos em caso de erro.
    return false; // Indica falha.
}


/*

Estrutura do Código:

A função principal é http_post_json, que realiza um pedido POST e envia dados JSON.
Utiliza as funções read_callback (para enviar dados) e write_callback (para processar a resposta).
Etapas Importantes:

Inicialização do cURL (curl_easy_init e curl_global_init).
Configuração do URL, método HTTP, cabeçalhos e callbacks.
Serialização do JSON e envio no corpo do pedido.
Receção da resposta e tentativa de processar como JSON.
Tratamento de Erros:

Sempre que ocorre um erro, o código garante que os recursos alocados são limpos antes de sair.
Comentários no Código:

Incluem explicações sobre cada linha relevante, parâmetros e lógica subjacente.

*/
