# Flags do compilador:
# -Wall ativa todos os avisos do compilador
# -g adiciona informações de depuração
CFLAGS = -Wall -g

# Flags do linker:
# -L../ex4 especifica o caminho de pesquisa para bibliotecas
# -lpsc_tp2, -lcurl, -ljansson ligam as respetivas bibliotecas
LDFLAGS = -L../ex4 -lpsc_tp2 -lcurl -ljansson 

# Nome do executável final
TARGET = ex5_main

# Ficheiros fonte
SRCS = ex5_main.c ex5_auxiliar_functions.c

# Ficheiros objeto correspondentes aos ficheiros fonte
OBJS = ex5_main.o ex5_auxiliar_functions.o

# Caminho das bibliotecas a incluir no RPATH (runtime library search path)
LIBRARY_PATH = ../ex4

# Alvo padrão para construir o executável
all: $(TARGET)

# Regra para criar o executável
# $@: o nome do alvo (o executável final)
# $^: todas as dependências (os ficheiros objeto)
# -Wl,-rpath=$(LIBRARY_PATH): insere o caminho das bibliotecas para execução
$(TARGET): $(OBJS)
	gcc -o $@ $^ $(LDFLAGS) -Wl,-rpath=$(LIBRARY_PATH)

# Regra para compilar qualquer ficheiro .c num ficheiro .o
# $<: a primeira dependência (o ficheiro fonte)
# $@: o alvo (o ficheiro objeto)
%.o: %.c
	gcc -c $(CFLAGS) $< -o $@

# Regra para limpar os ficheiros gerados
# -f: força a remoção sem perguntar, mesmo que os ficheiros não existam
clean:
	rm -f $(TARGET) $(OBJS)
