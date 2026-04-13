#include <stdio.h>
#include <dlfcn.h> 
#include <string.h>
#include "../ex4/psc_tp2.h"

// plugins: demonstra uma visão de longo prazo, permite que o sistema evolua facilmente para atender a futuras necessidades.

// Função para listar as categorias dos produtos no carrinho
void list_categories(Cart *cart, Products *all_products) {

    // Verifica se o carrinho está vazio
    if (cart->n_products == 0) {
        printf("O carrinho está vazio.\n");
        return;
    }

    // Imprime o cabeçalho da lista de categorias
    printf("Categorias dos produtos no carrinho:\n");
    // Itera sobre todos os produtos no carrinho
    for (size_t i = 0; i < cart->n_products; i++) {
        // Obtém o ID do produto atual no carrinho
        int product_id = cart->products[i].id;
        // Procura o produto correspondente na lista de todos os produtos
        for (size_t j = 0; j < all_products->count; j++) {
            if (all_products->products[j].id == product_id) {
                // Imprime a categoria do produto encontrado
                printf("- %s\n", all_products->products[j].category);
                break;
            }
        }
    }
}

void helper(){// Função para exibir a ajuda com todos os comandos disponíveis
	 // Imprime a lista de comandos disponíveis com suas descrições
	printf("Comandos:\n"
		"\tUtilizadores -> Lista os utilizadores.\n"
		"\tUtilizador <id> -> Assumir o utilizador indicado como o utilizador corrente.\n"
		"\tProdutos <categoria> <critério> -> Lista os produtos da categoria indicada segundo o critério de ordenação indicado. Se a categoria não for reconhecida lista todos os produtos. Os critérios possíveis são “preço crescente” ou “preço decrescente”. Utilize os sinais < e > para indicar.\n"
		"\tComprar <produto> <quantidade> -> Acrescenta um produto ao carrinho de compras.\n"	
		"\tCarrinho -> Lista os produtos que estão no carrinho de compras (descrição, preço, quantidade).\n"
		"\tListar categorias -> Lista as categorias dos produtos que estão no carrinho.\n"
		"\tFinalizar -> Finalia a compra.\n"
		"\tTerminar -> Termina a execução do programa.\n");
}
/*

Modularidade: Permite adicionar ou modificar funcionalidades sem alterar o código principal
Flexibilidade: As funções podem ser atualizadas independentemente do programa
Extensibilidade: Facilita futuras expansões do sistema
Técnica de Carregamento Dinâmico: Utiliza dlopen e dlsym para carregar as funções em tempo de execução

*/
