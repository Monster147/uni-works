# Compilador a utilizar
CC = gcc

# Flags do compilador:
# -Wall ativa todos os avisos do compilador
# -g adiciona informações de depuração
# -I./include especifica o diretório de inclusão para ficheiros de cabeçalho
CFLAGS = -Wall -g -I./include

# Flags do linker:
# -L../ex4 e -L. especificam os caminhos de pesquisa para bibliotecas
# -lpsc_tp2, -lplug_in, -lcurl, -ljansson ligam as respetivas bibliotecas
LDFLAGS = -L../ex4 -lpsc_tp2 -L. -lplug_in -lcurl -ljansson

# Nome da biblioteca partilhada a criar
LIBRARY = plug_in

# Nome do executável final
TARGET = ex6_main

# Ficheiros fonte
SRCS = ex6_main.c plug_in.c ../ex5/ex5_auxiliar_functions.c

# Ficheiros objeto correspondentes aos ficheiros fonte
OBJS = ex6_main.o plug_in.o ../ex5/ex5_auxiliar_functions.o

# Caminhos incluídos no RPATH (runtime library search path)
LIBRARY_PATHS = ../ex4:.

# Alvo padrão para construir a biblioteca partilhada e o executável
all: lib$(LIBRARY).so $(TARGET)

# Regra para criar a biblioteca partilhada a partir de plug_in.c
# -Wall: ativa todos os avisos do compilador
# -Og: otimizações específicas para depuração
# -fPIC: gera código independente da posição para bibliotecas partilhadas
# -shared: cria uma biblioteca partilhada
# $@: nome do alvo (lib$(LIBRARY).so)
# $^: todas as dependências (plug_in.c)
lib$(LIBRARY).so: plug_in.c
	$(CC) -Wall -Og -fPIC -shared -o $@ $^

# Regra para criar o executável principal
# -Wl,-rpath=$(LIBRARY_PATHS): insere os caminhos das bibliotecas no executável para execução
$(TARGET): $(OBJS) lib$(LIBRARY).so
	$(CC) $(CFLAGS) -o $@ $(OBJS) $(LDFLAGS) -Wl,-rpath=$(LIBRARY_PATHS)

# Regra para compilar qualquer ficheiro .c num ficheiro .o
# $<: a primeira dependência (o ficheiro fonte)
# $@: o alvo (o ficheiro objeto)
%.o: %.c
	$(CC) $(CFLAGS) -c $< -o $@

# Regra específica para criar o ficheiro objeto de ex5_auxiliar_functions.c
../ex5/ex5_auxiliar_functions.o: ../ex5/ex5_auxiliar_functions.c
	$(CC) $(CFLAGS) -c $< -o $@

# Regra para limpar os ficheiros gerados
# -f: força a remoção sem perguntar, mesmo que os ficheiros não existam
clean: 
	rm -f $(TARGET) lib$(LIBRARY).so $(OBJS)
