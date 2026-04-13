// Inclui a biblioteca padrão de entrada/saída
#include <stdio.h>
// Inclui o cabeçalho personalizado do projeto
#include "../ex4/psc_tp2.h"

// Declara um ponteiro global para o usuário atual, inicialmente nulo
User *current_user = NULL;
// Declara um ponteiro global para todos os produtos, inicialmente nulo
Products *all_products = NULL;
// Declara um ponteiro global para o carrinho atual, inicialmente nulo
Cart *current_cart = NULL;

// Função que lista os utilizadores
void list_users(Users *users) {
    // Verifica se a lista de usuários está vazia
    if (users == NULL || users->count == 0) {
        // Imprime mensagem se não houver usuários
        printf("Nenhum utilizador disponível.\n");
        // Retorna da função
        return;
    }
    // Imprime cabeçalho da lista de usuários
    printf("Lista de utilizadores:\n");
    // Loop através de todos os usuários
    for (size_t i = 0; i < users->count; i++) {
        // Imprime ID e nome de cada usuário
        printf("ID: %d\tNome: %s\n", users->users[i].id, users->users[i].name);
    }
}

// Função que verifica se a categoria listada existe
int category_exists(const char *category) {
    // Loop através de todos os produtos
    for (size_t i = 0; i < all_products->count; i++) {
        // Compara a categoria do produto com a categoria fornecida
        if (strcmp(all_products->products[i].category, category) == 0) {
            // Retorna 1 se a categoria for encontrada
            return 1; 
        }
    }
    // Retorna 0 se a categoria não for encontrada
    return 0; 
}

// Função que lista os produtos
void list_products(const char *category, const char *order) {
    // Verifica se os produtos foram carregados
    if (!all_products) {
        // Imprime mensagem se os produtos não foram carregados
        printf("Produtos ainda não carregados.\n");
        // Retorna da função
        return;
    }

    // Aloca memória para array de produtos filtrados
    Product *filtered_products = malloc(all_products->count * sizeof(Product));
    // Inicializa contador de produtos filtrados
    size_t filtered_count = 0;

    // Filtra produtos por categoria
    for (size_t i = 0; i < all_products->count; i++) {
        // Verifica se não há categoria especificada ou se a categoria corresponde
        if (!category || strcmp(all_products->products[i].category, category) == 0) {
            // Adiciona produto ao array filtrado
            filtered_products[filtered_count++] = all_products->products[i];
        }
    }

    // Ordena produtos pelo preço
    for (size_t i = 0; i < filtered_count - 1; i++) {
        for (size_t j = i + 1; j < filtered_count; j++) {
            // Determina se deve trocar os produtos baseado na ordem especificada
            bool swap = (strcmp(order, "<") == 0) ?
                        (filtered_products[i].price > filtered_products[j].price) :
                        (filtered_products[i].price < filtered_products[j].price);
            // Troca os produtos se necessário
            if (swap) {
                Product temp = filtered_products[i];
                filtered_products[i] = filtered_products[j];
                filtered_products[j] = temp;
            }
        }
    }

    // Imprime cabeçalho da lista de produtos
    printf("Produtos:\n");
    // Loop através dos produtos filtrados e ordenados
    for (size_t i = 0; i < filtered_count; i++) {
        // Imprime detalhes de cada produto
        printf("\tID: %d\n\tCategoria: %s\n\tPreço: %.2f\n\tDescrição: %s\n",
               filtered_products[i].id,
               filtered_products[i].category,
               filtered_products[i].price,
               filtered_products[i].description);
    }
    // Libera a memória alocada para os produtos filtrados
    free(filtered_products);
}

// Função que lista todos produtos com a sua categoria e ordenados pelo preço
void list_all_products(const char *order) {
    // Loop para ordenar todos os produtos
    for (size_t i = 0; i < all_products->count; i++) {
        for (size_t j = i + 1; j < all_products->count; j++) {
            // Determina se deve trocar os produtos baseado na ordem especificada
            bool swap = (strcmp(order, "<") == 0) ?
                        (all_products->products[i].price > all_products->products[j].price) :
                        (all_products->products[i].price < all_products->products[j].price);
            // Troca os produtos se necessário
            if (swap) {
                Product temp = all_products->products[i];
                all_products->products[i] = all_products->products[j];
                all_products->products[j] = temp;
            }
        }
    }
    // Loop para exibir todos os produtos ordenados
    for (size_t i = 0; i < all_products->count; i++) {
        // Imprime detalhes de cada produto
        printf("\tID: %d\n\tCategoria: %s\n\tPreço: %.2f\n\tDescrição: %s\n",
               all_products->products[i].id,
               all_products->products[i].category,
               all_products->products[i].price,
               all_products->products[i].description);
    }
}

// Função que calcula o preço total dos produtos no carrinho
float calculate_total(Cart *cart, Products *all_products) {
    // Inicializa o total
    float total = 0.0f;
    // Loop através dos produtos no carrinho
    for (size_t i = 0; i < cart->n_products; i++) {
        // Loop através de todos os produtos
        for (size_t j = 0; j < all_products->count; j++) {
            // Verifica se o ID do produto no carrinho corresponde ao produto atual
            if (cart->products[i].id == all_products->products[j].id) {
                // Adiciona o preço do produto multiplicado pela quantidade ao total
                total += all_products->products[j].price * cart->products[i].quantity;
                // Sai do loop interno
                break;
            }
        }
    }
    // Retorna o total calculado
    return total;
}

// Função que lista os produtos do carrinho, com as respetivas quantidades
void list_cart() {
    // Verifica se o carrinho está vazio
    if (!current_cart || current_cart->n_products == 0) {
        // Imprime mensagem se o carrinho estiver vazio
        printf("Carrinho está vazio.\n");
        // Retorna da função
        return;
    }

    // Imprime cabeçalho da lista do carrinho
    printf("Produtos no carrinho:\n");
    // Loop através dos produtos no carrinho
    for (size_t i = 0; i < current_cart->n_products; i++) {
        // Obtém o ID e a quantidade do produto atual
        int id = current_cart->products[i].id;
        size_t quantity = current_cart->products[i].quantity;
        // Imprime detalhes do produto no carrinho
        printf("\tID: %d\n\tQuantidade: %zu\n\tDescrição: %s\n\tPreço: %.2f\n", id, quantity, all_products->products[id-1].description, all_products->products[id-1].price);
    }
}

// Função que adiciona produtos ao carrinho
void add_to_cart(int product_id, size_t quantity) {
    // Loop para verificar se o produto já está no carrinho
    for (size_t i = 0; i < current_cart->n_products; i++) {
        // Se o produto já está no carrinho, aumenta a quantidade
        if (current_cart->products[i].id == product_id) {
            current_cart->products[i].quantity += quantity;
            // Imprime mensagem de confirmação
            printf("Produto adicionado ao carrinho.\n");
            // Retorna da função
            return;
        }
    }

    // Calcula o novo tamanho necessário para o carrinho
    size_t new_size = sizeof(Cart) + (current_cart->n_products + 1) * sizeof(*current_cart->products);
    // Realoca o carrinho com o novo tamanho
    Cart *temp = realloc(current_cart, new_size);
    // Verifica se a realocação foi bem-sucedida
    if (!temp) {
        // Imprime mensagem de erro se falhar
        printf("Erro ao adicionar produto ao carrinho: memória insuficiente.\n");
        // Retorna da função
        return;
    }

    // Atualiza o ponteiro do carrinho
    current_cart = temp;
    // Adiciona o novo produto ao carrinho
    current_cart->products[current_cart->n_products].id = product_id;
    current_cart->products[current_cart->n_products].quantity = quantity;
    // Incrementa o número de produtos no carrinho
    current_cart->n_products++;

    // Imprime mensagem de confirmação
    printf("Produto adicionado ao carrinho.\n");
}

// Função para finalizar a compra
void finalize_purchase() {
    // Verifica se há um usuário selecionado
    if (!current_user) {
        // Imprime mensagem de erro se não houver usuário selecionado
        printf("Nenhum utilizador selecionado, impossivel finalizar a compra. Use o comando 'utilizador <id>'.\n");
        // Retorna da função
        return;
    }
    
    // Verifica se o carrinho está vazio
    if (!current_cart || current_cart->n_products == 0) {
        // Imprime mensagem se o carrinho estiver vazio
        printf("Nenhum produto no carrinho.\n");
        // Retorna da função
        return;
    }
    
    // Calcula o total da compra
    float total = calculate_total(current_cart, all_products);
    // Imprime o total da compra
    printf("Total da compra: %.2f €\n", total);
    
    // Tenta finalizar a compra
    if (cart_put(current_cart)) {
        // Imprime mensagem de sucesso
        printf("Compra finalizada com sucesso!\n");  
        // Libera a memória do carrinho atual
        free(current_cart);
        // Aloca um novo carrinho vazio
        current_cart = malloc(sizeof(Cart) + sizeof(*current_cart->products));
        // Remove o usuário atual
        current_user = NULL;
        // Zera o número de produtos no novo carrinho
        current_cart->n_products = 0;
    } 
    else {
        // Imprime mensagem de erro se a finalização falhar
        printf("Falha ao finalizar a compra.\n");
    }
}
