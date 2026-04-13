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
    os.makedirs("cantrbrycomozip", exist_ok=True)
    zip_path = os.path.join("cantrbrycomozip", os.path.basename(ficheiro) + ".zip")
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

# Executar
processar_zip('cantrbry.zip')


# (b) Sobre os resultados da alínea anterior, apresente o gráfico que relaciona a entropia do ficheiro (eixo do xx) e a razão de
# compressão (eixo dos yy). Comente sobre o formato do gráfico.

def grafico(entropias_lista, compressoes_lista, nomes_ficheiros):
    # Gera gráfico de dispersão Entropia vs Razão de Compressão.
    plt.figure(figsize=(12, 7))
    plt.scatter(entropias_lista, compressoes_lista, color='blue', marker='o')

    for i, nome in enumerate(nomes_ficheiros):
        if nome == "alice29.txt":
            offset = (-45, 5)
        else:
            offset = (5, 5)
        plt.annotate(
            nome,
            (entropias_lista[i], compressoes_lista[i]),
            textcoords="offset points",
            xytext=offset,  # deslocamento leve
            ha='left',
            fontsize=8,
            alpha=0.7
        )

    plt.xlabel('Entropia (bits/símbolo)')
    plt.ylabel('Razão de compressão (%)')
    plt.title('Entropia vs Razão de Compressão')
    plt.grid(True, linestyle='--', alpha=0.6)
    plt.tight_layout()
    plt.show()


def processar_zip_com_grafico(zip_path):
    # Executa a análise como `processar_zip`, mas acumula os dados para o gráfico.
    zip_ref = zipfile.ZipFile(zip_path)
    zip_ref.extractall('cantrbry')

    entropias_lista = []
    compressoes_lista = []
    nomes_ficheiros = []

    for root, _ ,files in os.walk('cantrbry'):
        for file in files:
            file_path = os.path.join(root, file)
            print(f'Analisando {file}...')
            
            contagem, simbolo, prob, info, entropia = analisar_ficheiro_simbolos(file_path)

            # Tamanho original
            tamanho_original = os.path.getsize(file_path)

            # Comprimir e obter tamanho comprimido
            zip_individual = comprimir_ficheiro(file_path)
            tamanho_comprimido = os.path.getsize(zip_individual)

            # Calcular razão de compressão
            razao = (tamanho_comprimido / tamanho_original)*100 if tamanho_comprimido > 0 else 0

            nomes_ficheiros.append(file)
            entropias_lista.append(entropia)
            compressoes_lista.append(razao)
    
    grafico(entropias_lista, compressoes_lista, nomes_ficheiros)

processar_zip_com_grafico('cantrbry.zip')