#include <stdio.h>     // Para funções de entrada/saída padrão
#include <stdlib.h>    // Para funções como malloc e free
#include <stdbool.h>   // Para usar o tipo booleano
#include "../ex4/psc_tp2.h"  // Cabeçalho personalizado com definições específicas do projeto

// Função principal para testar products_get, users_get e cart_put
int main() {
    // Obter produtos
    Products *products = products_get();
    if (!products) {
        fprintf(stderr, "Erro ao obter produtos\n");
        return 1;  // Retorna 1 para indicar erro
    }

    // Salvar produtos no ficheiro CSV
    save_products_to_csv("products.csv", products);

    // Obter utilizadores
    Users *users = users_get();
    if (!users) {
        fprintf(stderr, "Erro ao obter utilizadores\n");
        free(products);  // Libera a memória alocada para produtos antes de sair
        return 1;
    }

    // Salvar utilizadores no ficheiro CSV
    save_users_to_csv("users.csv", users);

    // Criar um carrinho de compras
    // Aloca memória para o carrinho com espaço para 2 produtos
    Cart *cart = malloc(sizeof(Cart) + 2 * sizeof(cart->products[0]));
    if (!cart) {
        perror("Erro ao alocar memória para o carrinho");
        free(products);
        free(users);
        return 1;
    }

    // Configura o carrinho
    cart->user_id = users->users[0].id;  // Associa o carrinho ao primeiro utilizador
    cart->n_products = 2;

    // Adiciona o primeiro produto ao carrinho
    cart->products[0].id = products->products[0].id;
    cart->products[0].quantity = 3;

    // Adiciona o segundo produto ao carrinho
    cart->products[1].id = products->products[1].id;
    cart->products[1].quantity = 5;

    // Enviar o carrinho para a API
    if (!cart_put(cart)) {
        fprintf(stderr, "Erro ao enviar carrinho de compras\n");
        free(cart);
        products_free(products);
        users_free(users);
        return 1;
    }

    printf("Operação concluída com sucesso! Ficheiros CSV criados e carrinho enviado.\n");

    // Liberta memória alocada
    free(cart);
    products_free(products);
    users_free(users);

    return 0;  // Retorna 0 para indicar sucesso
}
/*

Este código principal demonstra o uso das funções products_get(), users_get(), e cart_put(), além de funções auxiliares como save_products_to_csv() e save_users_to_csv(). Ele realiza as seguintes operações:
Obtém produtos e os salva em um arquivo CSV.
Obtém usuários e os salva em um arquivo CSV.
Cria um carrinho de compras com dois produtos.
Envia o carrinho para uma API.
Libera toda a memória alocada.
O código inclui verificações de erro em cada etapa e libera a memória apropriadamente em caso de falha. Isso demonstra boas práticas de programação em C, incluindo gerenciamento adequado de memória e tratamento de erros.

*/
