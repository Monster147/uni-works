#include <stdio.h>          // Biblioteca padrão para entrada e saída.
#include "../ex4/psc_tp2.h" // Cabeçalho que contém as definições das estruturas `Products` e `Users`.

#define ID_WIDTH            10  // Largura fixa para a coluna "ID".
#define PRICE_WIDTH         15  // Largura fixa para a coluna "Price".
#define CATEGORY_WIDTH      25  // Largura fixa para a coluna "Category".
#define DESCRIPTION_WIDTH   80  // Largura fixa para a coluna "Description".
#define NAME_WIDTH          20  // Largura fixa para a coluna "Name".

// Função que cria um ficheiro CSV a partir dos dados de produtos.
void save_products_to_csv(const char *filename, Products *products) {
    // Abre o ficheiro para escrita.
    FILE *file = fopen(filename, "w");
    if (!file) { // Verifica se o ficheiro foi aberto com sucesso.
        perror("Erro ao abrir ficheiro de produtos");
        return;
    }

    // Define larguras fixas para cada coluna.
    const int id_width = ID_WIDTH;
    const int price_width = PRICE_WIDTH;
    const int category_width = CATEGORY_WIDTH;
    const int description_width = DESCRIPTION_WIDTH;

    // Escreve o cabeçalho no ficheiro.
    fprintf(file, "%-*s %-*s %-*s %-*s\n",
            id_width, "ID",
            price_width, "Price",
            category_width, "Category",
            description_width, "Description");

    // Escreve os dados de cada produto.
    for (size_t i = 0; i < products->count; i++) {
        fprintf(file, "%-*d %-*.2f %-*s %-*s\n",
                id_width, products->products[i].id,                      // ID do produto.
                price_width, products->products[i].price,                // Preço do produto (com 2 casas decimais).
                category_width, products->products[i].category,          // Categoria do produto.
                description_width, products->products[i].description);   // Descrição do produto.
    }

    // Garante que os dados foram escritos corretamente.
    if (fflush(file) != 0 || ferror(file)) {
        fprintf(stderr, "Erro ao escrever no ficheiro: %s\n", filename);
    }

    // Fecha o ficheiro.
    fclose(file);
}

// Função que cria um ficheiro CSV a partir dos dados de utilizadores.
void save_users_to_csv(const char *filename, Users *users) {
    // Abre o ficheiro para escrita.
    FILE *file = fopen(filename, "w");
    if (!file) { // Verifica se o ficheiro foi aberto com sucesso.
        perror("Erro ao abrir ficheiro de utilizadores");
        return;
    }

    // Define larguras fixas para cada coluna.
    const int id_width = ID_WIDTH;
    const int name_width = NAME_WIDTH;

    // Escreve o cabeçalho no ficheiro.
    fprintf(file, "%-*s %-*s\n", id_width, "ID", name_width, "Name");

    // Escreve os dados de cada utilizador.
    for (size_t i = 0; i < users->count; i++) {
        fprintf(file, "%-*d %-*s\n",
                id_width, users->users[i].id,       // ID do utilizador.
                name_width, users->users[i].name); // Nome do utilizador.
    }

    // Garante que os dados foram escritos corretamente.
    if (fflush(file) != 0 || ferror(file)) {
        fprintf(stderr, "Erro ao escrever no ficheiro: %s\n", filename);
    }

    // Fecha o ficheiro.
    fclose(file);
}

/*

1. Estrutura das Funções
Ambas as funções seguem a mesma lógica básica:
Abrem um ficheiro em modo de escrita.
Escrevem um cabeçalho formatado para as colunas.
Iteram sobre os elementos (produtos ou utilizadores) e escrevem seus dados no ficheiro.
Garantem que os dados foram escritos corretamente usando fflush e verificando erros com ferror.
Fecham o ficheiro para liberar recursos.
2. Parâmetros de Formatação
%-*s: Imprime uma string com largura fixa, alinhada à esquerda.
%-*.2f: Imprime um número float com largura fixa e 2 casas decimais, alinhado à esquerda.
As larguras fixas das colunas são definidas pelas macros (ID_WIDTH, PRICE_WIDTH, etc.), garantindo consistência e boa formatação.
3. Cabeçalhos e Dados
Os cabeçalhos indicam claramente o tipo de informação em cada coluna (e.g., ID, Price, Category).
Cada linha contém os dados de um produto ou utilizador, formatados com base nos tipos e larguras definidos.
4. Tratamento de Erros
Se a abertura do ficheiro falhar, a função imprime uma mensagem de erro usando perror e retorna imediatamente.
Antes de fechar o ficheiro, fflush é chamado para forçar a escrita no disco, e ferror verifica se ocorreu algum problema.
5. Diferenças Entre Produtos e Utilizadores
A função para produtos lida com mais colunas (e.g., Price, Category, Description), enquanto a função para utilizadores lida apenas com ID e Name.
6. Flexibilidade
O uso de larguras definidas por macros permite ajustar facilmente o layout do CSV sem modificar o código principal.

*/
