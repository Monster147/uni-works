import numpy as np
import string
import math
import matplotlib.pyplot as plt  
import collections     
#------------------------------------------------------------------------------
# a) Implemente uma fonte de símbolos genérica, a qual gera ficheiros com N símbolos, de acordo com a Função Massa
# de Probabilidade (FMP) do alfabeto de M símbolos: p(x) = {p(x1), p(x2), . . . , p(xM)}. Apresente resultados que
# mostrem o funcionamento correto da fonte, na geração de sequências.
def gerar_sequencia(N, M, fmp):
    """
    Gera uma sequência de N símbolos de acordo com a FMP fornecida.

    :param N: Número de símbolos a serem gerados.
    :param M: Lista de símbolos da serem gerados.
    :param fmp: Lista de probabilidades associadas a cada símbolo do alfabeto.
    :return: Sequência de N símbolos.
    
    :Gera quantidades proporcionais aos valores da FMP usando round(prob * N) para cada símbolo.
    Se o tamanho total da sequência não for exatamente N, são adicionados ou removidos símbolos
    aleatoriamente de modo a garantir o comprimento correto. Por fim, a sequência é baralhada.
    """
    sequencia = []
    for simbolo, prob in zip(M, fmp):
        quantidade = int(round(prob * N))
        sequencia.extend([simbolo] * quantidade)
    
    # Corrigir se o tamanho não for exatamente N
    while len(sequencia) < N:
        sequencia.append(np.random.choice(M, p=fmp))
    while len(sequencia) > N:
        sequencia.pop()
    
    np.random.shuffle(sequencia)
    return sequencia

M = ['a', 'b', 'c', 'd']
fmp = [0.8, 0.1, 0.1, 0.0]

# Número de símbolos a serem gerados
N = 1000

# Gerar a sequência
sequencia = gerar_sequencia(N, M, fmp)
print(sequencia)

#------------------------------------------------------------------------------
# b)Recorra à implementação da fonte de símbolos, para a geração de sequências de símbolos nas seguintes condições:
#(i) Palavras-passe robustas (com diferentes tipos de caracteres), com dimensão entre 10 e 14 caracteres.
def password_creator():
    """
    Gera uma palavra-passe robusta com dimensão entre 10 e 14 caracteres.
    A sequência é gerada com distribuição uniforme usando a função gerar_sequencia().
    comprimento: Comprimento da palavra-passe.
    caracteres: Caracteres a serem utilizados.
    fmp: Probabilidade uniforme associada a cada carácter.
    passowrd: Palavra-passe gerada.
    ''.join -> Junta os caracteres gerados aleatoriamente.
    """
    comprimento = np.random.randint(10, 15)
    caracteres = string.ascii_letters + string.digits + string.punctuation
    fmp = [1/len(caracteres)] * len(caracteres)  
    password = ''.join(gerar_sequencia(comprimento, list(caracteres), fmp))
    return password

#(ii) Códigos idênticos a endereços IPv4.
def ipv4_creator():
    """
    Gera um endereço IPv4.
    octetos: Lista de valores possíveis (0 a 255), representados como strings.
    fmp: Probabilidade uniforme associada a cada octeto.
    ipv4: Endereço IPv4 gerado com 4 octetos separados por '.'.
    '.'.join -> Junta os octetos gerados aleatoriamente pela função gerar_sequencia().
    No IPv4, cada protocolo é composto por quatro grupos de dois dígitos hexadecimais, cada um com 32 bits e variando entre entre 0 e 255. 
    """
    octetos = list(map(str, range(256)))
    fmp = [1/256] * 256
    ipv4 = '.'.join(gerar_sequencia(4, octetos, fmp))
    return ipv4

#(iii) Códigos idênticos a endereços IPv6.
def ipv6_creator():
    """
    Gera um endereço IPv6.
    hex_chars: Caracteres hexadecimais permitidos (0-9, a-f).
    fmp: Probabilidade uniforme associada a cada carácter hexadecimal.
    ipv6: Endereço IPv6 gerado com 8 grupos de 4 caracteres separados por ':'.
    ':'.join -> Junta os grupos gerados aleatoriamente pela função gerar_sequencia().
    """
    hex_chars = list(string.digits + 'abcdef')
    fmp = [1/16] * 16
    ipv6 = ':'.join(''.join(gerar_sequencia(4, hex_chars, fmp)) for _ in range(8))
    return ipv6

#(iv) Tuplos hexadecimais com 8 elementos.
def tuplo_hexadecimal_creator():
    """
    Gera um tuplo hexadecimal com 8 elementos.
    hex_chars: Caracteres hexadecimais permitidos (0-9, A-F).
    fmp: Probabilidade uniforme associada a cada carácter hexadecimal.
    tuplo_hexadecimal: Tuplo hexadecimal gerado com 8 caracteres.
    ''.join -> Junta os caracteres gerados aleatoriamente pela função gerar_sequencia().
    """
    hex_chars = list(string.digits + 'ABCDEF')
    fmp = [1/16] * 16
    tuplo_hexadecimal = ''.join(gerar_sequencia(8, hex_chars, fmp))
    return tuplo_hexadecimal
#Apresente 10 resultados exemplificativos de cada uma das gerações anteriores

def gerar_exemplos():
    exemplos_password = [password_creator() for _ in range(10)]
    exemplos_ipv4 = [ipv4_creator() for _ in range(10)]
    exemplos_ipv6 = [ipv6_creator() for _ in range(10)]
    exemplos_tuplo_hexadecimal = [tuplo_hexadecimal_creator() for _ in range(10)]

    return exemplos_password, exemplos_ipv4, exemplos_ipv6, exemplos_tuplo_hexadecimal

exemplos_password, exemplos_ipv4, exemplos_ipv6, exemplos_tuplo_hexadecimal = gerar_exemplos()

print("Palavras-passe robustas:")
for exemplo in exemplos_password:
    print(exemplo)

print("\nEnderecos IPv4:")
for exemplo in exemplos_ipv4:
    print(exemplo)

print("\nEnderecos IPv6:")
for exemplo in exemplos_ipv6:
    print(exemplo)

print("\nTuplos hexadecimais com 8 elementos:")
for exemplo in exemplos_tuplo_hexadecimal:
    print(exemplo)
    
#------------------------------------------------------------------------------
# c)Recorra à implementação da fonte de símbolos, para a geração de três ficheiros, com alfabeto de símbolos à sua escolha,nas seguintes condições:
#(i) ficheiro 1, M = 4; N = 100; H(X) = 1.75
#(ii) ficheiro 2, M = 4; N = 1000; H(X) = 1.75
#(iii) ficheiro 3, M = 256; N = 10000; H(X) = 4.00


def save_to_file(filename, data):
    with open(filename, 'w') as f:
        for symbol in data:
            f.write(f"{symbol}\n")

def generate_file(filename, symbols, probabilities, N):
    data = gerar_sequencia(N, symbols, probabilities)
    save_to_file(filename, data)

symbols_M4 = ['A', 'B', 'C', 'D']
probabilities_M4 = [0.5, 0.25, 0.125, 0.125]

generate_file('file1.txt', symbols_M4, probabilities_M4, N=100)
generate_file('file2.txt', symbols_M4, probabilities_M4, N=1000)

symbols_M256 = list(range(256))
probabilities_M256 = [0.0625]*16 + [0.0]*(256-16)

generate_file('file3.txt', symbols_M256, probabilities_M256, N=10000)

print("Files generated successfully with fixed alphabets and corrected FMPs.")

#------------------------------------------------------------------------------
# d) Sobre os três ficheiros gerados na alínea anterior, apresente o histograma e o valor da entropia.

def entropias(contagem, simbolos):
    """
    Calcula a entropia de uma sequência de símbolos com base nas suas frequências.
    contagem: Dicionário com a contagem de cada símbolo.
    simbolos: Número total de símbolos na sequência.
    termos_entropia: Lista com os termos individuais da fórmula de entropia.
    """
    termos_entropia = []
    for freq in contagem.values():
        prob = freq / simbolos
        termos_entropia.append(prob * math.log2(prob))
    
    return -sum(termos_entropia)

def histograma(contagem, file_name, entropia, M):
    """
    Gera um histograma da frequência dos símbolos de um ficheiro.
    contagem: Dicionário com a contagem de cada símbolo.
    file_name: Nome do ficheiro (usado como título).
    entropia: Valor da entropia a ser exibido no gráfico.
    """
    
    ascii_vals = list(contagem.keys())  # Converte os caracteres para códigos ASCII
    frequencies = list(contagem.values())

    plt.figure(figsize=(10, 6))
    plt.bar(ascii_vals, frequencies, color='darkblue', width=1.0, alpha=0.7)
    plt.xlabel('Símbolos')
    plt.ylabel('Frequência')
    plt.title(file_name)
    plt.legend([f'H(X)= {entropia:.4f}'], loc='upper right', fontsize=12, facecolor='white')
    plt.grid(True, linestyle='--', alpha=0.5)
    plt.xlim(0, 256)
    plt.tight_layout()
    plt.show()

def analisar_ficheiro(nome_ficheiro, M):
    """
    Lê um ficheiro gerado, calcula a sua entropia e apresenta o histograma.
    nome_ficheiro: Caminho para o ficheiro a ser analisado.
    """
    with open(nome_ficheiro, 'r') as f:
        linhas = f.readlines()
    
    # Detetar se o conteúdo é letras ou números
    primeiro_valor = linhas[0].strip()
    if primeiro_valor.isalpha():  # Só letras? usa ord()
        dados = [ord(linha.strip()) for linha in linhas]
    else:  # É número
        dados = [int(linha.strip()) for linha in linhas]
    
    contagem = collections.Counter(dados)
    entropia = entropias(contagem, len(dados))
    histograma(contagem, nome_ficheiro, entropia, M)
    return entropia

# Análise dos três ficheiros
entropia1 = analisar_ficheiro('file1.txt', 4)
entropia2 = analisar_ficheiro('file2.txt', 4)
entropia3 = analisar_ficheiro('file3.txt', 256)

print(entropia1)
print(entropia2)
print(entropia3)