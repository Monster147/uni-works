// Guarda de inclusão para evitar inclusões múltiplas do cabeçalho
#ifndef _EX5_H
#define _EX5_H

// Inclusão de bibliotecas padrão necessárias
#include <stdlib.h>  // Para funções como malloc, free, etc.
#include <stdio.h>   // Para funções de entrada/saída padrão
#include <string.h>  // Para manipulação de strings

// Declarações de variáveis globais externas
extern User *current_user;     // Ponteiro para o usuário atual
extern Products *all_products; // Ponteiro para todos os produtos
extern Cart *current_cart;     // Ponteiro para o carrinho atual

// Protótipo da função para listar usuários
void list_users(Users *users);

// Protótipo da função para verificar se uma categoria existe
int category_exists(const char *category);

// Protótipo da função para listar produtos de uma categoria específica
void list_products(const char *category, const char *order);

// Protótipo da função para listar todos os produtos
void list_all_products(const char *order);

// Protótipo da função para calcular o total do carrinho
float calculate_total(Cart *cart, Products *all_products);

// Protótipo da função para listar itens no carrinho
void list_cart();

// Protótipo da função para adicionar um produto ao carrinho
void add_to_cart(int product_id, size_t quantity);

// Protótipo da função para finalizar a compra
void finalize_purchase();

// Fim da guarda de inclusão
#endif/*_EX5_H*/
