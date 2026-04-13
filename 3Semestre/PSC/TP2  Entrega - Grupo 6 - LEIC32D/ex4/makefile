# Define o diretório de inclusão
INCLUDE_DIR = ./include

# Define o diretório de código fonte
SRC_DIR = ../ex

# Nome da biblioteca a ser criada
LIBRARY = psc_tp2

# Nome do executável principal
TARGET = ex4_main

# Lista de arquivos fonte para o executável principal
SRCS = ex4_main.c 

# Gera lista de objetos a partir dos arquivos fonte
OBJS = $(patsubst %.c, %.o, $(SRCS))

# Flags de compilação
CFLAGS = -Wall -g -I$(INCLUDE_DIR)

# Flags de ligação
LDFLAGS = -L. -l$(LIBRARY) -ljansson -lcurl

# Regra padrão: cria a biblioteca compartilhada e o executável
all: lib$(LIBRARY).so $(TARGET)

# Regra para criar a biblioteca compartilhada
lib$(LIBRARY).so: $(SRC_DIR)3/products_get.c $(SRC_DIR)3/users_get.c $(SRC_DIR)3/cart_put.c $(SRC_DIR)3/csv_files_creator.c $(SRC_DIR)2/http_post_json.c $(SRC_DIR)1/http_get_json.c
	gcc -Wall -Og -fPIC -shared -o $@ $^

# Regra para criar o executável principal
$(TARGET): $(OBJS) lib$(LIBRARY).so
	gcc -o $@ $^ $(LDFLAGS) -Wl,-rpath=.

# Regra genérica para compilar arquivos .c em .o
%.o: %.c
	gcc $(CFLAGS) -c $< -o $@

# Regra para limpar os arquivos gerados
clean:
	rm -f $(TARGET) $(OBJS) $/lib$(LIBRARY).so


#Este Makefile define as regras para compilar uma biblioteca compartilhada e um executável principal. Ele inclui:
#Definição de diretórios e nomes de arquivos.
#Configuração de flags de compilação e ligação.
#Regras para criar a biblioteca compartilhada e o executável.
#Uma regra genérica para compilar arquivos .c em .o.
#Uma regra de limpeza para remover arquivos gerados.
