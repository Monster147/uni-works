// Guarda de inclusão para evitar inclusões múltiplas do cabeçalho
#ifndef _PSC_TP2_H
#define _PSC_TP2_H

#include <stdlib.h>  // Para funções como malloc, free, etc.
#include <stdio.h>   // Para funções de entrada/saída padrão
#include <string.h>  // Para manipulação de strings
#include <stdbool.h> // Para usar o tipo booleano
#include <curl/curl.h> // Para fazer requisições HTTP
#include <jansson.h>   // Para parsing de JSON

// Definição de constantes
#define BUFFER_CHUNK           (4 * 1024)  // Tamanho do chunk do buffer (4KB)
#define COMMAND_BUFFER_SIZE    256         // Tamanho do buffer de comando
#define PRODUCTS_URL           "https://dummyjson.com/products"  // URL da API de produtos -- ?skip=30
#define USERS_URL              "https://dummyjson.com/users"     // URL da API de usuários -- ?skip=30
#define CART_URL               "https://dummyjson.com/carts/add" // URL da API de carrinho

// Estrutura para buffer de escrita
struct write_buffer {
    char *buffer;  // Ponteiro para o buffer
    int current,   // Posição atual no buffer
        max;       // Tamanho máximo do buffer
};

// Estrutura para buffer de leitura
struct read_buffer {
    char *buffer;  // Ponteiro para o buffer
    int current,   // Posição atual no buffer
        max;       // Tamanho máximo do buffer
};

// Estrutura para representar um produto
typedef struct {
    int id;                 // ID do produto
    float price;            // Preço do produto
    const char *description;// Descrição do produto
    const char *category;   // Categoria do produto
} Product;

// Estrutura para armazenar múltiplos produtos
typedef struct {
    Product *products;  // Array de produtos
    size_t count;       // Número de produtos
} Products;

// Estrutura para representar um usuário
typedef struct {
    int id;             // ID do usuário
    const char *name;   // Nome do usuário
} User;

// Estrutura para armazenar múltiplos usuários
typedef struct {
    User *users;    // Array de usuários
    size_t count;   // Número de usuários
} Users;

// Estrutura para representar um carrinho de compras
typedef struct {
    int user_id;        // ID do usuário dono do carrinho
    size_t n_products;  // Número de produtos no carrinho
    struct {
        int id;         // ID do produto
        size_t quantity;// Quantidade do produto
    } products[];       // Array flexível de produtos no carrinho
} Cart;

// Declaração da função de callback para escrita
size_t write_callback(void *ptr, size_t size, size_t nmemb, void *stream);

// Declaração da função para obter JSON via HTTP GET
json_t *http_get_json(const char *url);

// Declaração da função de callback para leitura
size_t read_callback(char *dest, size_t size, size_t nmemb, void *userp);

// Declaração da função para enviar JSON via HTTP POST
bool http_post_json(const char *url, json_t *data);

// Declaração da função para obter produtos
Products *products_get();

// Declaração da função para obter usuários
Users *users_get();

// Declaração da função para enviar carrinho
bool cart_put(Cart *cart);

// Declaração da função para liberar memória dos produtos
void products_free(Products *products);

// Declaração da função para liberar memória dos usuários
void users_free(Users *users);

// Declaração da função para salvar produtos em CSV
void save_products_to_csv(const char *filename, Products *products);

// Declaração da função para salvar usuários em CSV
void save_users_to_csv(const char *filename, Users *users);

// Fim da guarda de inclusão
#endif/*_PCS_TP2_H*/


