#Compressão de dados.
#Considere os ficheiros do conjunto Canterbury Corpus https://corpus.canterbury.ac.nz/descriptions/\#cantrbry
#Escolha uma ferramenta de compressão de uso comum (eg WinZip, WinRar, 7-Zip)

#(a) Para cada ficheiro do conjunto: determine e apresente o valor da entropia; realize a compressão com a ferramenta 
# escolhida; determine a razão de compressão.
import os  
import zipfile 
import collections  
import math   
import matplotlib.pyplot as plt

def analisar_ficheiro_simbolos(file_path):
    """Lê um ficheiro binário e calcula:
        Frequência de cada byte.
        Símbolo mais frequente.
        Probabilidade do símbolo mais frequente.
        Informação própria do símbolo.
        Entropia total do ficheiro.
        :param file_path: Caminho do ficheiro a ser analisado.
    """
    with open(file_path, 'rb') as f:
        conteudo = f.read()

    simbolos = len(conteudo)
    contagem = collections.Counter(conteudo)
    
    simbolo_mais_frequente, freq_max = contagem.most_common(1)[0]
    simbolo_mais_frequentes = chr(simbolo_mais_frequente)
    
    probabilidade = probabilidades(freq_max, simbolos)
    informacao_propria = informacao_proprias(probabilidade)
    entropia = entropias(contagem, simbolos)
    
    return contagem, simbolo_mais_frequentes, probabilidade, informacao_propria, entropia

    
def probabilidades(freq, simbolos):
    # Calcula a probabilidade de um símbolo.
    return freq / simbolos

def informacao_proprias(probabilidade):
    #Calcula a informação própria de um símbolo.
    return -math.log2(probabilidade)

def entropias(contagem, simbolos):
    # Calcula a entropia do ficheiro com base nas frequências.
    termos_entropia = []
    for freq in contagem.values():
        prob = freq / simbolos
        termos_entropia.append(prob * math.log2(prob))
    return -sum(termos_entropia)


def comprimir_ficheiro(ficheiro):
    #Comprime o ficheiro individualmente usando o formato ZIP.
    os.makedirs("filezip", exist_ok=True)
    zip_path = os.path.join("filezip", os.path.basename(ficheiro) + ".zip")
    with zipfile.ZipFile(zip_path, 'w', zipfile.ZIP_DEFLATED) as zipf:
        zipf.write(ficheiro, arcname=os.path.basename(ficheiro))
    return zip_path

def processar_zip(zip_path):
    #Executa a análise de entropia e compressão para todos os ficheiros do ZIP.
    zip_ref = zipfile.ZipFile(zip_path)
    zip_ref.extractall('cantrbry')

    for root, _ ,files in os.walk('cantrbry'):
        for file in files:
            file_path = os.path.join(root, file)
            print(f'Analisando {file}...')
            
            contagem, simbolo, prob, info, entropia = analisar_ficheiro_simbolos(file_path)
            print(f'Entropia do ficheiro: {entropia:.4f} bits/símbolo')

           
            # Tamanho original
            tamanho_original = os.path.getsize(file_path)

            # Comprimir e obter tamanho comprimido
            zip_individual = comprimir_ficheiro(file_path)
            tamanho_comprimido = os.path.getsize(zip_individual)

            # Calcular razão de compressão
            razao = (tamanho_comprimido / tamanho_original)*100 if tamanho_comprimido > 0 else 0
            print(f'Tamanho original: {tamanho_original} bytes')
            print(f'Tamanho comprimido: {tamanho_comprimido} bytes')
            print(f'Razão de compressão: {razao:.2f}%')
            print('-----------------------------------')


def unzip_file(zip_path, extract_to):
    with zipfile.ZipFile(zip_path, 'r') as zip_ref:
        zip_ref.extractall(extract_to)