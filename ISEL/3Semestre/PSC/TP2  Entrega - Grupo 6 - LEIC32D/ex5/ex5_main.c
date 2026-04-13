// Inclui a biblioteca padrão de entrada/saída
#include <stdio.h>
// Inclui o cabeçalho personalizado do projeto
#include "../ex4/psc_tp2.h"
// Inclui o cabeçalho específico para este exercício
#include "ex5.h"

// Define uma constante para o comando de terminar o programa
#define END         "terminar"
// Define uma constante para o comando de listar usuários
#define USERS       "utilizadores"
// Define uma constante para o comando de selecionar um usuário
#define USER        "utilizador"
// Define uma constante para o comando de listar produtos
#define PRODUCTS    "produtos"
// Define uma constante para o comando de mostrar o carrinho
#define CART        "carrinho"
// Define uma constante para o comando de comprar um produto
#define BUY         "comprar"
// Define uma constante para o comando de finalizar a compra
#define FINALIZE    "finalizar"

// Função principal do programa
int main() {
    // Declara um array para armazenar o comando do usuário
    char command[COMMAND_BUFFER_SIZE];
    // Obtém a lista de usuários
    Users *users = users_get();
    // Obtém a lista de todos os produtos
    all_products = products_get();
    // Aloca memória para o carrinho atual
    current_cart = malloc(sizeof(Cart) + sizeof(*current_cart->products));
    // Verifica se a alocação de memória para o carrinho foi bem-sucedida
    if (!current_cart) {
        // Imprime uma mensagem de erro se a alocação falhar
        fprintf(stderr, "Erro ao alocar memória para o carrinho.\n");
        // Retorna 1 para indicar erro
        return 1;
    }
    // Inicializa o ID do usuário do carrinho como 0
    current_cart->user_id = 0;
    // Inicializa o número de produtos no carrinho como 0
    current_cart->n_products = 0;

    // Inicia o loop principal do programa
    while (true) {
        // Imprime o prompt para o usuário
        printf("Comando: ");
        // Lê o comando do usuário
        if (!fgets(command, COMMAND_BUFFER_SIZE, stdin)) {
            // Se a leitura falhar, sai do loop
            break;
        }
        // Remove o caractere de nova linha do final do comando
        command[strcspn(command, "\n")] = '\0'; 

        // Verifica se o comando é para terminar o programa
        if (strcasecmp(command, END) == 0) {
            // Se for, sai do loop
            break;
        // Verifica se o comando é para listar usuários
        } else if (strcasecmp(command, USERS) == 0) {
            // Chama a função para listar usuários
            list_users(users);
        // Verifica se o comando é para selecionar um usuário
        } else if (strncasecmp(command, USER, 10) == 0) {
            // Declara uma variável para armazenar o ID do usuário
            int user_id;
            // Tenta ler o ID do usuário do comando
            int args = sscanf(command + strlen(USER), "%d", &user_id);
            // Verifica se o ID do usuário foi fornecido corretamente
            if (args != 1) {
                // Verifica se o comando está correto, mas sem ID
                if (strncasecmp(command, USER, 7) == 0) {
                    // Imprime uma mensagem de erro
                    printf("Comando inválido. Use 'utilizador <id>'.\n");
                }
            } 
            else {
                // Verifica se o ID do usuário é válido
                if(user_id<1){
                    // Imprime uma mensagem de erro se o ID for inválido
                    printf("User_id inválido, por favor escolha um id maior que 0\n");
                }
                else{
                    // Procura o usuário com o ID fornecido
                    for (size_t i = 0; i < users->count; i++) {
                        if (users->users[i].id == user_id) {
                            // Define o usuário atual
                            current_user = &users->users[i];
                            // Imprime o nome do usuário atual
                            printf("Utilizador corrente: %s\n", current_user->name);
                            // Define o ID do usuário no carrinho
                            current_cart->user_id = user_id;
                            // Sai do loop após encontrar o usuário
                            break;
                        }
                    }
                }
            }
        } 
        // Verifica se o comando é para listar produtos
        else if (strncasecmp(command, PRODUCTS, 8) == 0) {
            // Declara variáveis para armazenar a categoria e a ordem
            char category[50], order[5];
            // Tenta ler a categoria e a ordem do comando
            int args = sscanf(command + strlen(PRODUCTS), "%49s %4s", category, order);
            // Verifica se a categoria e a ordem foram fornecidas
            if(args == 2) {
                // Verifica se a ordem é válida
                if (strcmp(order, "<") != 0 && strcmp(order, ">") != 0) {
                    // Imprime uma mensagem de erro se a ordem for inválida
                    printf("Maneira de ordenar desconhecida. Use '<' ou '>'\n");
                } 
                // Verifica se a categoria existe
                else if(category_exists(category)){
                    // Lista os produtos da categoria
                    list_products(category, order);
                }
                else{
                    // Imprime uma mensagem se a categoria não existir
                    printf("Categoria desconhecida. Listando todos os produtos.\n");
                    // Lista todos os produtos
                    list_all_products(order);
                } 
            }   
            else {
                // Imprime uma mensagem de erro se o comando for inválido
                printf("Comando inválido. Use 'Produtos <categoria> <ordem>'\n");
            }
        } 
        // Verifica se o comando é para mostrar o carrinho
        else if (strcasecmp(command, CART) == 0) {
            // Chama a função para listar o carrinho
            list_cart();
        } 
        // Verifica se o comando é para comprar um produto
        else if (strncasecmp(command, BUY, 7) == 0) {
            // Declara variáveis para armazenar o ID do produto e a quantidade
            int product_id;
            int quantity;
            // Tenta ler o ID do produto e a quantidade do comando
            int args = sscanf(command + strlen(BUY), "%d %u", &product_id, &quantity);
            // Verifica se o ID do produto e a quantidade foram fornecidos
            if (args == 2) {
                // Verifica se o ID do produto é válido
                if(product_id < 1){
                    // Imprime uma mensagem de erro se o ID for inválido
                    printf("Id de produto inválido\n");
                }
                // Verifica se a quantidade é válida
                else if(quantity < 1){
                    // Imprime uma mensagem de erro se a quantidade for inválida
                    printf("Quantidade inválida\n");
                }
                else{
                    // Adiciona o produto ao carrinho
                    add_to_cart(product_id, quantity);
                }
            }
            else {
                // Imprime uma mensagem de erro se o comando for inválido
                printf("Comando inválido. Use 'comprar <produto_id> <quantidade>'\n");
            }
        } 
        // Verifica se o comando é para finalizar a compra
        else if (strcasecmp(command, FINALIZE) == 0) {
            // Chama a função para finalizar a compra
            finalize_purchase();
        } 
        else {
            // Imprime uma mensagem se o comando for desconhecido
            printf("Comando desconhecido.\n");
        }
    }
    
    // Libera a memória alocada para os usuários
    users_free(users);
    // Libera a memória alocada para os produtos
    products_free(all_products);
    // Libera a memória alocada para o carrinho
    free(current_cart);

    // Retorna 0 para indicar que o programa terminou com sucesso
    return 0;
}
