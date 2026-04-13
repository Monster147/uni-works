#include <stdio.h>       // Biblioteca padrão de entrada e saída.
#include <stdbool.h>     // Biblioteca para o uso do tipo booleano (true/false).
#include <jansson.h>     // Biblioteca para manipulação de objetos JSON.
#include "../ex4/psc_tp2.h" // Inclui cabeçalhos personalizados necessários, como http_post_json.

// Definição da URL para adicionar itens ao carrinho de compras.
#define CARTS_ADD_URL "https://dummyjson.com/carts/add"

int main() {
    // Criação de um objeto JSON para um produto com ID 14 e quantidade 4.
    json_t *product = json_pack("{s:i, s:i}", "id", 14, "quantity", 4);
    if (!product) { // Verifica se a criação do JSON falhou.
        fprintf(stderr, "Erro ao criar JSON para produto.\n");
        return 1; // Termina o programa com erro.
    }

    // Criação de um array JSON para conter a lista de produtos.
    json_t *products = json_array();
    if (!products || json_array_append_new(products, product) != 0) {
        // Verifica se o array JSON foi criado com sucesso e se o produto foi adicionado.
        fprintf(stderr, "Erro ao criar JSON para lista de produtos.\n");
        json_decref(product); // Libera a memória alocada para o produto.
        return 1;
    }

    // Criação do objeto JSON principal representando o carrinho de compras.
    // Contém um ID de utilizador (userId) e a lista de produtos.
    json_t *cart = json_pack("{s:i, s:o}", "userId", 1, "products", products);
    if (!cart) { // Verifica se a criação do JSON do carrinho falhou.
        fprintf(stderr, "Erro ao criar JSON para carrinho de compras.\n");
        json_decref(products); // Libera a memória alocada para os produtos.
        return 1;
    }

    // Envia o carrinho como JSON usando a função `http_post_json`.
    // A função devolve `true` se o pedido HTTP POST for bem-sucedido.
    bool cart_added = http_post_json(CARTS_ADD_URL, cart);
    printf("Resultado da chamada do HTTP POST: %b\n", cart_added); // Imprime o resultado (true/false).

    if (!cart_added) { // Verifica se o pedido falhou.
        fprintf(stderr, "Falha no HTTP POST.\n");
        json_decref(cart); // Libera a memória alocada para o carrinho.
        return 1; // Termina o programa com erro.
    }

    // Indica que o pedido HTTP POST foi bem-sucedido.
    printf("Pedido HTTP POST realizado com sucesso.\n");

    // Libera a memória alocada para o carrinho de compras.
    json_decref(cart);
    return 0; // Indica que o programa terminou com sucesso.
}

/*

Criação do JSON para o produto:

json_pack é usado para criar um objeto JSON com dois campos: "id" e "quantity".
Verifica-se se o objeto foi criado com sucesso. Caso contrário, o programa termina com erro.
Criação do array JSON de produtos:

json_array cria um array JSON vazio.
O produto é adicionado ao array usando json_array_append_new.
Criação do JSON do carrinho de compras:

Contém o "userId" e o array "products".
json_pack é usado novamente para criar o objeto JSON principal.
Envio do JSON via HTTP POST:

A função http_post_json é utilizada para enviar o JSON para a URL definida.
Retorna um valor booleano indicando o sucesso (true) ou falha (false) do pedido.
Libertação de memória:

json_decref é usado para libertar a memória alocada para os objetos JSON. É fundamental para evitar fugas de memória.
Mensagens de erro e sucesso:

O programa imprime mensagens no terminal para indicar erros ou sucesso no processo.

*/
