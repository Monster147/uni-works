#include <stdio.h>        // Biblioteca padrão de entrada e saída.
#include <string.h>       // Biblioteca para manipulação de strings.
#include <stdbool.h>      // Biblioteca para usar o tipo booleano (true/false).
#include <jansson.h>      // Biblioteca para manipulação de JSON.
#include <curl/curl.h>    // Biblioteca para realizar requisições HTTP.
#include "../ex4/psc_tp2.h" // Cabeçalhos personalizados necessários, como `http_post_json`.

// Função para enviar (adicionar ou atualizar) um novo carrinho de compras.
bool cart_put(Cart *cart) {
    // Criação de um objeto JSON para representar o carrinho de compras.
    json_t *json_cart = json_object(); 
    if (!json_cart) { // Verifica se houve falha ao criar o objeto JSON.
        fprintf(stderr, "Erro ao criar objeto JSON para o carrinho.\n");
        return false; // Retorna false indicando falha.
    }

    // Criação de um array JSON para armazenar os produtos do carrinho.
    json_t *json_products = json_array(); 
    if (!json_products) { // Verifica se houve falha ao criar o array JSON.
        fprintf(stderr, "Erro ao criar array JSON para produtos.\n");
        json_decref(json_cart); // Libera o objeto `json_cart` criado anteriormente.
        return false;
    }

    // Itera sobre os produtos no carrinho para adicioná-los ao array JSON.
    for (size_t i = 0; i < cart->n_products; i++) {
        // Criação de um objeto JSON para representar cada produto.
        json_t *json_product = json_object();
        if (!json_product) { // Verifica se houve falha ao criar o objeto JSON do produto.
            fprintf(stderr, "Erro ao criar objeto JSON para produto.\n");
            json_decref(json_cart);
            json_decref(json_products);
            return false;
        }

        // Adiciona os campos "id" e "quantity" ao objeto JSON do produto.
        if (json_object_set_new(json_product, "id", json_integer(cart->products[i].id)) ||
            json_object_set_new(json_product, "quantity", json_integer(cart->products[i].quantity))) {
            fprintf(stderr, "Erro ao adicionar dados do produto ao JSON.\n");
            json_decref(json_product); // Libera o objeto `json_product`.
            json_decref(json_cart);
            json_decref(json_products);
            return false;
        }

        // Adiciona o objeto JSON do produto ao array JSON de produtos.
        if (json_array_append_new(json_products, json_product)) {
            fprintf(stderr, "Erro ao adicionar produto ao array JSON.\n");
            json_decref(json_product); // Libera o objeto `json_product`.
            json_decref(json_cart);
            json_decref(json_products);
            return false;
        }
    }

    // Adiciona os dados do carrinho ao objeto JSON principal.
    // Inclui o "userId" e o array "products".
    if (json_object_set_new(json_cart, "userId", json_integer(cart->user_id)) ||
        json_object_set_new(json_cart, "products", json_products)) {
        fprintf(stderr, "Erro ao adicionar dados do carrinho ao JSON.\n");
        json_decref(json_cart); // Libera o objeto `json_cart`.
        json_decref(json_products);
        return false;
    }

    // Envia a requisição HTTP POST com os dados do carrinho em formato JSON.
    bool result = http_post_json(CART_URL, json_cart);
    if (!result) { // Verifica se houve falha ao enviar o POST.
        fprintf(stderr, "Erro ao enviar o carrinho via HTTP POST.\n");
    }

    // Libera a memória alocada para o objeto JSON do carrinho.
    json_decref(json_cart);

    // Retorna true ou false dependendo do resultado do HTTP POST.
    return result;
}

/*

Criação do objeto JSON principal (json_cart):

Representa o carrinho de compras completo.
Contém campos como userId (ID do utilizador) e um array products.
Criação do array JSON (json_products):

Representa a lista de produtos no carrinho.
Cada produto é adicionado ao array como um objeto JSON separado.
Iteração sobre os produtos do carrinho:

Para cada produto, cria-se um objeto JSON (json_product) com dois campos:
"id": ID do produto.
"quantity": Quantidade do produto.
O objeto JSON do produto é então adicionado ao array json_products.
Adicionando dados ao carrinho:

Os dados do utilizador (userId) e o array de produtos (products) são adicionados ao objeto JSON principal (json_cart).
Envio do carrinho via HTTP POST:

A função http_post_json é utilizada para enviar o JSON do carrinho para o servidor.
O resultado da chamada (true para sucesso, false para falha) é armazenado na variável result.
Liberação de memória:

A função json_decref é usada para liberar a memória alocada para os objetos JSON, evitando fugas de memória.
Tratamento de erros:

Caso alguma etapa falhe, o programa imprime uma mensagem de erro e retorna false.

*/
