// Inclusão de bibliotecas padrão
#include <stdio.h>    // Para funções de entrada/saída
#include <string.h>   // Para manipulação de strings
#include <stdbool.h>  // Para usar o tipo booleano

// Inclusão de bibliotecas externas
#include <jansson.h>  // Para parsing de JSON
#include <curl/curl.h> // Para fazer requisições HTTP

// Inclusão de cabeçalho personalizado
#include "../ex4/psc_tp2.h"  // Provavelmente contém definições de estruturas e constantes

// Função que retorna a informação sobre todos os produtos
Products *products_get() {
    // Inicializa a biblioteca cURL
    CURL *curl = curl_easy_init();
    if (!curl) {
        // Se falhar, imprime erro e retorna NULL
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
    curl_easy_setopt(curl, CURLOPT_URL, PRODUCTS_URL);  // Define a URL
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

    // Obtém o array de produtos do JSON
    json_t *products_array = json_object_get(root, "products");
    if (!json_is_array(products_array)) {
        fprintf(stderr, "Invalid JSON structure\n");
        json_decref(root);
        return NULL;
    }

    // Obtém o número de produtos
    size_t count = json_array_size(products_array);
    
    // Aloca memória para a estrutura Products
    Products *products = malloc(sizeof(Products));
    if (!products) {
        fprintf(stderr, "Failed to allocate Products structure\n");
        json_decref(root);
        return NULL;
    }

    // Aloca memória para o array de produtos
    products->products = malloc(count * sizeof(Product));
    if (!products->products) {
        fprintf(stderr, "Failed to allocate products array\n");
        free(products);
        json_decref(root);
        return NULL;
    }

    // Define o número de produtos
    products->count = count;

    // Itera sobre os produtos no array JSON
    for (size_t i = 0; i < count; i++) {
        // Obtém o objeto JSON para o produto atual
        json_t *product_json = json_array_get(products_array, i);
        if (!json_is_object(product_json)) {
            continue;  // Pula para o próximo se não for um objeto válido
        }

        // Extrai os campos do produto do JSON
        if (json_unpack(product_json, "{s:i, s:s, s:s}",
                    "id", &products->products[i].id,
                    "description", &products->products[i].description,
                    "category", &products->products[i].category) != 0) {
            fprintf(stderr, "Failed to unpack JSON object for product\n");
            continue;  // Pula para o próximo em caso de erro
        }

        // Extrai o preço do produto
        json_t *price_json = json_object_get(product_json, "price");
        if (!json_is_number(price_json)) {
            fprintf(stderr, "Invalid or missing price for product\n");
            continue;  // Pula para o próximo se o preço for inválido
        }
        products->products[i].price = json_number_value(price_json);

        // Duplica as strings para evitar problemas de memória
        products->products[i].description = strdup(products->products[i].description);
        products->products[i].category = strdup(products->products[i].category);
    }

    // Libera a memória do JSON
    json_decref(root);
    
    // Finaliza o cURL globalmente
    curl_global_cleanup();
    
    // Retorna a estrutura de produtos preenchida
    return products;
}

// Função que liberta toda a memória alocada para os produtos
void products_free(Products *products) {
    if (products) {  // Verifica se products não é NULL
        for (size_t i = 0; i < products->count; i++) {
            // Libera a memória das strings duplicadas
            free((void *)products->products[i].description);
            free((void *)products->products[i].category);
        }
        // Libera o array de produtos
        free(products->products);
        // Libera a estrutura Products
        free(products);
    }
}


/*

Bibliotecas utilizadas:
stdio.h: Para operações de entrada/saída padrão.
string.h: Para manipulação de strings.
stdbool.h: Para usar o tipo booleano.
jansson.h: Biblioteca para parsing de JSON.
curl/curl.h: Biblioteca para fazer requisições HTTP.
"../ex4/psc_tp2.h": Arquivo de cabeçalho personalizado (não fornecido no código).
Função products_get():
Inicializa o cURL para fazer a requisição HTTP.
Aloca um buffer para armazenar a resposta da API.
Configura e executa a requisição HTTP usando cURL.
Faz o parsing do JSON recebido usando a biblioteca Jansson.
Aloca memória para a estrutura Products e para o array de Product.
Itera sobre os produtos no JSON, extraindo as informações e armazenando-as na estrutura Products.
Usa strdup() para criar cópias das strings (descrição e categoria) para evitar problemas de memória.
Função products_free():
Libera toda a memória alocada para a estrutura Products, incluindo as strings duplicadas e o array de produtos.
Tratamento de erros:
O código inclui várias verificações de erro, como falhas na inicialização do cURL, alocação de memória, parsing de JSON e extração de dados.
Erros são reportados usando fprintf(stderr, ...).
Manipulação de JSON:
Usa funções da biblioteca Jansson como json_loads(), json_object_get(), json_array_get(), e json_unpack() para trabalhar com o JSON.
Gerenciamento de memória:
Aloca memória dinamicamente para o buffer, estrutura Products, e array de Product.
Libera a memória alocada em caso de erro ou ao finalizar o uso dos dados.
Este código é uma implementação robusta para recuperar dados de produtos de uma API web, parseá-los e armazená-los em uma estrutura de dados em C. 
Ele demonstra boas práticas de programação, incluindo tratamento de erros, gerenciamento cuidadoso de memória e uso de bibliotecas externas para tarefas específicas como requisições HTTP e parsing de JSON.

*/

