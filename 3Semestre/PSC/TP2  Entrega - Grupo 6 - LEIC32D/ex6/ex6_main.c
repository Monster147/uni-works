#include <stdio.h> 
#include <dlfcn.h> //carregamento dinâmico de bibliotecas
#include "../ex4/psc_tp2.h" 
#include "../ex5/ex5.h"

#define END             "terminar" // Define uma constante para o comando de terminar do programa
#define USERS           "utilizadores" // Define uma constante para o comando para listar usuários
#define USER            "utilizador" // Define uma constante para o comando para selecionar um usuário
#define PRODUCTS        "produtos" // Define uma constante para o comando para listar produtos
#define CART            "carrinho" // Define uma constante para o comando para mostrar o carrinho
#define BUY             "comprar" // Define uma constante para o comando para comprar um produto
#define FINALIZE        "finalizar" // Define uma constante para o comando para finalizar a compra
#define CATEGORIES_CART "listar categorias" // Define uma constante para o comando para listar categorias no carrinho
#define HELP            "ajuda" // Define uma constante para o comando de ajuda

typedef void(*list_categories)(Cart *, Products *); // Define um tipo de função para listar categorias

typedef void(*helper)();// Define um tipo de função para exibir ajuda


int main() {// Função principal do programa
	//fgets e sscanf para prevenir buffer overflows,
    char command[COMMAND_BUFFER_SIZE];    // Declara um array para armazenar o comando do usuário
    Users *users = users_get();    // Obtém a lista de usuários
    all_products = products_get();     // Obtém a lista de todos os produtos
    current_cart = malloc(sizeof(Cart) + sizeof(*current_cart->products));     // Aloca memória para o carrinho atual
    
    if (!current_cart) {  // Verifica se a alocação de memória para o carrinho foi bem-sucedida
        fprintf(stderr, "Erro ao alocar memória para o carrinho.\n");  // Imprime uma mensagem de erro se a alocação falhar
        return 1;         // Retorna 1 para indicar erro
    }
    current_cart->user_id = 0; // Inicializa o ID do usuário do carrinho como 0
    current_cart->n_products = 0; // Inicializa o número de produtos no carrinho como 0

    while (true) {  // Inicia o loop principal do programa
        printf("Comando: ");         // Imprime o prompt para o usuário
        if (!fgets(command, COMMAND_BUFFER_SIZE, stdin)) {         // Lê o comando do usuário
            break; // Se a leitura falhar, sai do loop
        }
        command[strcspn(command, "\n")] = '\0';  // Remove o caractere de nova linha do final do comando
        
        if (strcasecmp(command, END) == 0) {  // Verifica se o comando é para terminar o programa
            break; // Se for, sai do loop
        } else if (strcasecmp(command, USERS) == 0) {  // Verifica se o comando é para listar usuários
            list_users(users);   // Chama a função para listar usuários
        } else if (strncasecmp(command, USER, 10) == 0) {  // Verifica se o comando é para selecionar um usuário
            int user_id;  // Declara uma variável para armazenar o ID do usuário
            int args = sscanf(command + strlen(USER), "%d", &user_id); // Tenta ler o ID do usuário do comando
            if (args != 1) { // Verifica se o ID do usuário foi fornecido corretamente
                if (strncasecmp(command, USER, 7) == 0) { // Verifica se o comando está correto, mas sem ID
                    printf("Comando inválido. Use 'utilizador <id>'.\n"); // Imprime uma mensagem de erro
                }
            } 
            else {
                if(user_id<1){ // Verifica se o ID do usuário é válido
                    printf("User_id inválido, por favor escolha um id maior que 0\n"); // Imprime uma mensagem de erro se o ID for inválido
                }
                else{
                    for (size_t i = 0; i < users->count; i++) { // Procura o usuário com o ID fornecido
                        if (users->users[i].id == user_id) {
                            current_user = &users->users[i];// Define o usuário atual
                            printf("Utilizador corrente: %s\n", current_user->name);  // Imprime o nome do usuário atual
                            current_cart->user_id = user_id;// Define o ID do usuário no carrinho      
                            break; // Sai do loop após encontrar o usuário
                        }
                    }
                }
            }
        } 
        else if (strncasecmp(command, PRODUCTS, 8) == 0) { // Verifica se o comando é para listar produtos
            char category[50], order[5]; // Declara variáveis para armazenar a categoria e a ordem
            int args = sscanf(command + strlen(PRODUCTS), "%49s %4s", category, order); // Tenta ler a categoria e a ordem do comando
            if(args == 2) {  // Verifica se a categoria e a ordem foram fornecidas
                if (strcmp(order, "<") != 0 && strcmp(order, ">") != 0) { // Verifica se a ordem é válida
                    printf("Maneira de ordenar desconhecida. Use '<' ou '>'\n");  // Imprime uma mensagem de erro se a ordem for inválida
                } 
                else if(category_exists(category)){ // Verifica se a categoria existe
                    list_products(category, order);  // Lista os produtos da categoria
                }
                else{
                    printf("Categoria desconhecida. Listando todos os produtos.\n");// Imprime uma mensagem se a categoria não existir
                    list_all_products(order);  // Lista todos os produtos
                } 
            }       
            else {
                printf("Comando inválido. Use 'Produtos <categoria> <ordem>'\n");   // Imprime uma mensagem de erro se o comando for inválido
            }
        } 
        else if (strcasecmp(command, CART) == 0) { // Verifica se o comando é para mostrar o carrinho
            list_cart();  // Chama a função para listar o carrinho
        } 
        else if (strncasecmp(command, BUY, 7) == 0) {  // Verifica se o comando é para comprar um produto
            int product_id;  // Declara variáveis para armazenar o ID do produto e a quantidade
            int quantity;	 // Declara variáveis para armazenar o ID do produto e a quantidade
            int args = sscanf(command + strlen(BUY), "%d %u", &product_id, &quantity);  // Tenta ler o ID do produto e a quantidade do comando
            if (args == 2) { // Verifica se o ID do produto e a quantidade foram fornecidos
                if(product_id < 1){  // Verifica se o ID do produto é válido
                    printf("Id de produto inválido\n"); // Imprime uma mensagem de erro se o ID for inválido
                }
                else if(quantity < 1){  // Verifica se a quantidade é válida
                    printf("Quantidade inválida\n");  // Imprime uma mensagem de erro se a quantidade for inválida
                }
                else{
                    add_to_cart(product_id, quantity);  // Adiciona o produto ao carrinho
                }
            }
            else {
                // Imprime uma mensagem de erro se o comando for inválido
                printf("Comando inválido. Use 'comprar <produto_id> <quantidade>'\n");
            }
        }
        else if (strcasecmp(command, FINALIZE) == 0) { // Verifica se o comando é para finalizar a compra
            finalize_purchase(); // Chama a função para finalizar a compra
        }
        //Esta abordagem oferece flexibilidade excepcional, permitindo-nos atualizar ou estender funcionalidades sem recompilar o programa principal. 
        else if (strcasecmp(command, CATEGORIES_CART) == 0) { // Verifica se o comando é para listar categorias no carrinho
            void *libHandler = dlopen("libplug_in.so", RTLD_LAZY); // Carrega a biblioteca dinâmica
            if(!libHandler) fprintf(stderr, "erro %s\n", dlerror());  // Verifica se houve erro ao carregar a biblioteca
            list_categories list_c = (list_categories) dlsym(libHandler,"list_categories"); // Obtém o ponteiro para a função list_categories
            /*
            A função dlsym é utilizada para obter o endereço de um símbolo (função) em uma biblioteca dinamica
            */
            list_c(current_cart, all_products);  // Chama a função list_categories
            dlclose(libHandler);// Fecha a biblioteca dinâmica
        }
        // Verifica se o comando é para exibir ajuda
        else if (strcasecmp(command, HELP) == 0) {
            void *libHandler = dlopen("libplug_in.so", RTLD_LAZY);  // Carrega a biblioteca dinâmica
            if(!libHandler) fprintf(stderr, "erro %s\n", dlerror());  // Verifica se houve erro ao carregar a biblioteca
            helper help = (helper) dlsym(libHandler,"helper");  // Obtém o ponteiro para a função helper
            /*
            A função dlsym é utilizada para obter o endereço de um símbolo (função) em uma biblioteca dinamica
            */
            help();  // Chama a função helper
            dlclose(libHandler); // Fecha a biblioteca dinâmica
        }
        else {
            printf("Comando desconhecido.\n"); // Imprime uma mensagem se o comando for desconhecido
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
/*

Os últimos dois comandos ("Listar categorias" e "Ajuda") chamam a biblioteca dinâmica, enquanto os outros não, devido à implementação específica dessas funcionalidades como plugins externos. 
Flexibilidade: As funções list_categories e helper são carregadas dinamicamente usando a biblioteca dlfcn.h1. 
Isso permite que essas funcionalidades sejam adicionadas ou modificadas sem recompilar o programa principal.

Modularidade: Ao separar essas funções em uma biblioteca dinâmica (libplug_in.so), o código principal se torna mais modular e fácil de manter.
Extensibilidade: Este design permite adicionar novas funcionalidades ao programa sem modificar o código principal, simplesmente adicionando novas funções à biblioteca dinâmica.
Demonstração de técnica: O uso de carregamento dinâmico para esses comandos específicos pode ser uma demonstração intencional da técnica de plugins em C.
Os outros comandos são implementados diretamente no código principal, provavelmente porque:
São considerados funcionalidades core do programa.
Podem requerer acesso direto a estruturas de dados e funções definidas no programa principal.
São menos propensos a mudanças ou extensões futuras.
Esta abordagem mista demonstra um equilíbrio entre funcionalidades integradas e extensíveis, permitindo que partes específicas do programa sejam facilmente atualizadas ou estendidas através de plugins.

A função strncasecmp() compara os caracteres de forma insensível a maiúsculas e minúsculas. Isso significa que ela trata caracteres maiúsculos e minúsculos como equivalentes durante a comparação

Priorizamos a segurança e robustez no tratamento de entradas do usuário. 
Utilizamos técnicas como fgets e sscanf para prevenir buffer overflows, e implementamos verificações rigorosas para garantir a validade dos dados inseridos

*/
