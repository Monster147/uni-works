CFLAGS = -Wall -g
LDFLAGS = -lcurl -ljansson

TARGET = http_get_json_test
SRCS = http_get_json_test.c http_get_json.c

all: $(TARGET)

$(TARGET): $(SRCS)
	gcc $(CFLAGS) -o $@ $^ $(LDFLAGS)

clean:
	rm -f $(TARGET)

# Makefile é usado para compilar um programa em C que utiliza bibliotecas externas (libcurl e jansson) para trabalhar com HTTP e JSON.

# CFLAGS contém as opções que serão passadas ao compilador (gcc) na fase de compilação:
#-Wall: Ativa todos os avisos possíveis para ajudar a identificar problemas no código.
#-g: Gera informações de depuração no executável, permitindo o uso de ferramentas como gdb.

#LDFLAGS contém as opções para a fase de linkagem do programa:
#-lcurl: Informa ao linker que o programa depende da biblioteca libcurl para realizar operações HTTP.
#-ljansson: Informa ao linker que o programa usa a biblioteca jansson para manipulação de JSON.

#TARGET é o nome do executável final que será gerado após a compilação.

#SRCS lista todos os ficheiros .c (código-fonte em C) necessários para compilar o programa.
#Neste caso, o programa depende de dois ficheiros:
#http_get_json_test.c: Contém a função main para testar a funcionalidade.
#http_get_json.c: Implementa a lógica para realizar pedidos HTTP e processar JSON.

#Este é o alvo padrão. Quando se executa make, ele compila o programa especificado no alvo $(TARGET) (neste caso, http_get_json_test).

#Esta regra especifica como gerar o executável:
#$@: Representa o nome do alvo (neste caso, http_get_json_test).
#$^: Lista todos os ficheiros de entrada (neste caso, http_get_json_test.c e http_get_json.c).
#gcc: O compilador GNU para C.
#Combina as opções:
#$(CFLAGS) para definir avisos e depuração.
#$(LDFLAGS) para incluir bibliotecas externas.
#O resultado final é o executável chamado http_get_json_test.

#Remove o ficheiro executável gerado:
#rm -f: Remove o ficheiro sem perguntar e não lança erros se o ficheiro não existir.
#$(TARGET): Alvo que será removido (neste caso, http_get_json_test).
#Objetivo: Garantir que o diretório de trabalho fique limpo, útil antes de recompilar tudo.
