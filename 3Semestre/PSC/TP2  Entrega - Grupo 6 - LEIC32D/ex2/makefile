# Flags do compilador:
# -Wall ativa todos os avisos do compilador
# -g adiciona informações de depuração
CFLAGS = -Wall -g

# Flags do linker:
# -lcurl e -ljansson ligam as bibliotecas libcurl e libjansson, respetivamente
LDFLAGS = -lcurl -ljansson

# Nome do executável final
TARGET = http_post_json_test

# Ficheiros fonte utilizados no projeto
SRCS = http_post_json_test.c http_post_json.c ../ex1/http_get_json.c

# Alvo padrão para construir o executável
all: $(TARGET)

# Regra para criar o executável
# $@: o nome do alvo (o executável final)
# $^: todas as dependências (os ficheiros fonte)
# Compila os ficheiros fonte e liga as bibliotecas especificadas em $(LDFLAGS)
$(TARGET): $(SRCS)
	gcc $(CFLAGS) -o $@ $^ $(LDFLAGS)

# Regra para limpar os ficheiros gerados
# -f: força a remoção sem perguntar, mesmo que os ficheiros não existam
clean:
	rm -f $(TARGET)
