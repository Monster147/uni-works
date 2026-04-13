#include <stdio.h>    // Para funções de entrada/saída
#include <string.h>   // Para manipulação de strings
#include <stdbool.h>  // Para usar o tipo booleano
#include <jansson.h>  // Para parsing de JSON
#include <curl/curl.h> // Para fazer requisições HTTP
#include "../ex4/psc_tp2.h"  // Provavelmente contém definições de estruturas e constantes

// Função para obter informação sobre todos os utilizadores (id e username)
Users *users_get() {
    // Inicializa a biblioteca cURL
    CURL *curl = curl_easy_init();
    if (!curl) {
        fprintf(stderr, "Failed to initialize cURL\n");
        return NULL;
    }

    // Aloca e inicializa um buffer para armazenar a resposta da API
    struct write_buffer buffer = {
        .buffer = malloc(BUFFER_CHUNK),
        .current = 0,
        .max = BUFFER_CHUNK
    };

    // Verifica se a alocação do buffer foi bem-sucedida
    if (!buffer.buffer) {
        fprintf(stderr, "Failed to allocate buffer\n");
        curl_easy_cleanup(curl);
        return NULL;
    }

    // Configura as opções do cURL
    curl_easy_setopt(curl, CURLOPT_URL, USERS_URL);  // Define a URL
    curl_easy_setopt(curl, CURLOPT_WRITEFUNCTION, write_callback);  // Define a função de callback
    curl_easy_setopt(curl, CURLOPT_WRITEDATA, &buffer);  // Define o buffer para armazenar os dados

    // Executa a requisição HTTP
    CURLcode res = curl_easy_perform(curl);
    curl_easy_cleanup(curl);  // Limpa os recursos do cURL

    // Verifica se a requisição foi bem-sucedida
    if (res != CURLE_OK) {
        fprintf(stderr, "cURL error: %s\n", curl_easy_strerror(res));
        free(buffer.buffer);
        return NULL;
    }

    // Adiciona um terminador nulo ao final do buffer
    buffer.buffer[buffer.current] = '\0';

    // Faz o parsing do JSON recebido
    json_error_t error;
    json_t *root = json_loads(buffer.buffer, 0, &error);
    free(buffer.buffer);  // Libera o buffer, pois já não é mais necessário

    // Verifica se o parsing do JSON foi bem-sucedido
    if (!root) {
        fprintf(stderr, "JSON error: on line %d: %s\n", error.line, error.text);
        return NULL;
    }

    // Obtém o array de usuários do JSON
    json_t *users_array = json_object_get(root, "users");
    if (!json_is_array(users_array)) {
        fprintf(stderr, "Invalid JSON structure\n");
        json_decref(root);
        return NULL;
    }

    // Obtém o número de usuários
    size_t count = json_array_size(users_array);
    
    // Aloca memória para a estrutura Users
    Users *users = malloc(sizeof(Users));
    if (!users) {
        fprintf(stderr, "Failed to allocate Users structure\n");
        json_decref(root);
        return NULL;
    }

    // Aloca memória para o array de usuários
    users->users = malloc(count * sizeof(User));
    if (!users->users) {
        fprintf(stderr, "Failed to allocate users array\n");
        free(users);
        json_decref(root);
        return NULL;
    }

    // Define o número de usuários
    users->count = count;

    // Itera sobre os usuários no array JSON
    for (size_t i = 0; i < count; i++) {
        // Obtém o objeto JSON para o usuário atual
        json_t *user_json = json_array_get(users_array, i);
        if (!json_is_object(user_json)) {
            continue;  // Pula para o próximo se não for um objeto válido
        }

        // Extrai os campos do usuário do JSON
        if (json_unpack(user_json, "{s:i, s:s}",
                        "id", &users->users[i].id,
                        "username", &users->users[i].name) != 0) {
            fprintf(stderr, "Failed to unpack JSON object for user\n");
            continue;  // Pula para o próximo em caso de erro
        }

        // Duplica a string do nome para evitar problemas de memória
        users->users[i].name = strdup(users->users[i].name);
    }

    // Libera a memória do JSON
    json_decref(root);
    
    // Finaliza o cURL globalmente
    curl_global_cleanup();
    
    // Retorna a estrutura de usuários preenchida
    return users;
}

// Função para liberar a memória alocada para os usuários
void users_free(Users *users) {
    if (users) {  // Verifica se users não é NULL
        for (size_t i = 0; i < users->count; i++) {
            // Libera a memória das strings duplicadas
            free((void *)users->users[i].name);
        }
        // Libera o array de usuários
        free(users->users);
        // Libera a estrutura Users
        free(users);
    }
}

/*

Faz uma requisição HTTP para obter dados de usuários.
Processa a resposta JSON.
Armazena os dados em uma estrutura de dados personalizada.
Fornece uma função para liberar a memória alocada.

*/
