import os  
import zipfile 
import collections  
import math   
import matplotlib.pyplot as plt  

def analisar_ficheiro_simbolos(file_path):
    with open(file_path, 'rb') as f: # Open file_path in read-binary mode
        conteudo = f.read()

    simbolos = len(conteudo)  # Calcula o número total de símbolos no conteúdo
    contagem = collections.Counter(conteudo)  # Conta a frequência de cada símbolo
    #print(f'Contagem de símbolos: {contagem}')  
    
    # Obtém o símbolo mais frequente e a sua frequência
    simbolo_mais_frequente, freq_max = contagem.most_common(1)[0]
    simbolo_mais_frequentes = chr(simbolo_mais_frequente)  # Converte o código ASCII para o caractere correspondente
    #print(f'Símbolo mais frequente: {simbolo_mais_frequente} (Frequência: {freq_max})')  # Imprime o símbolo mais frequente e a sua frequênciaW
    
    probabilidade = probabilidades(freq_max,simbolos)  # Calcula a probabilidade do símbolo mais frequente
    informacao_propria = informacao_proprias(probabilidade)  # Calcula a informação própria do símbolo mais frequente
    
    # Calcula a entropia do ficheiro
    entropia = entropias(contagem, simbolos)
    
    # Retorna a contagem de símbolos, o símbolo mais frequente, a probabilidade, a informação própria e a entropia
    return contagem, simbolo_mais_frequentes, probabilidade, informacao_propria, entropia

def probabilidades(freq, simbolos):
    return freq / simbolos

def informacao_proprias(probabilidade):
    return -math.log2(probabilidade)

def entropias(contagem, simbolos):
    termos_entropia = []
    for freq in contagem.values():
        prob = freq / simbolos
        termos_entropia.append(prob * math.log2(prob))
    
    return -sum(termos_entropia)

def histograma(contagem, file_name, entropia):
    plt.figure(figsize=(10, 6))  # Ajusta o tamanho da figura
    ascii_vals = list(contagem.keys())  # Converte os caracteres para códigos ASCII
    frequencies = list(contagem.values())

    plt.bar(ascii_vals, frequencies, color='darkblue', width=1.0, alpha=0.7)  # Define barras finas e semi-transparentes
    plt.xlabel('Código ASCII')  # Rótulo do eixo X
    plt.ylabel('Frequência')  # Rótulo do eixo Y
    plt.title(file_name)  # Define o título como o nome do ficheiro
    plt.xlim(0, 300)  # Define os limites do eixo X para melhor visualização

    # Adiciona a legenda com a entropia
    plt.legend([f'H(X)= {entropia:.4f}'], loc='upper right', fontsize=12, facecolor='white')

    plt.grid(True, linestyle='--', alpha=0.5)  # Adiciona uma grade leve ao gráfico
    plt.show()  # Exibe o histograma


def processar_zip(zip_path):
    # Abre o ficheiro ZIP e extrai o seu conteúdo para a pasta 'TestFilesCD'
    zip_ref = zipfile.ZipFile(zip_path)  # The class for reading and writing ZIP files
    zip_ref.extractall('TestFilesCD') #Extract all members from the archive to the current working directory
    #. path specifies a different directory to extract to. members is optional and must be a subset of the list returned by 
    #namelist(). pwd is the password used for encrypted files as a bytes object.
    
    # Percorre todos os ficheiros extraídos
    for root, _ ,files in os.walk('TestFilesCD'):
        #Generate the file names in a directory tree by walking the tree either top-down or bottom-up. 
        #For each directory in the tree rooted at directory top (including top itself), it yields a 3-tuple (dirpath, dirnames, filenames).
        for file in files:
            file_path = os.path.join(root, file)  # Obtém o caminho completo do ficheiro
            #This module implements some useful functions on pathnames. To read or write files see open(), and for accessing the filesystem 
            # see the os module. 
            # The path parameters can be passed as strings, or bytes, or any object implementing the os.PathLike protocol.
            print(f'Analisando {file}...')  # Imprime uma mensagem indicando que o ficheiro está a ser analisado
            # Analisa o ficheiro e obtém a contagem de símbolos, o símbolo mais frequente, a probabilidade, a informação própria e a entropia
            contagem, simbolo, prob, info, entropia = analisar_ficheiro_simbolos(file_path)
            # Imprime os resultados da análise
            print(f'Simbolo mais frequente: {repr(simbolo)} (Prob: {prob:.4f}, Info: {info:.4f} bits)')# repr() returns a string containing a printable representation of an object.
            print(f'Entropia do ficheiro: {entropia:.4f} bits/simbolo')
            print('-----------------------------------')
            histograma(contagem, file, entropia)  # Desenha o histograma da contagem de símbolos

# Chama a função processar_zip com o caminho do ficheiro ZIPz<

processar_zip('TestFilesCD.zip')