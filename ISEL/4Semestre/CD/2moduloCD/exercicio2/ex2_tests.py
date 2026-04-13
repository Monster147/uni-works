from ex2Functions import pipeline
from ex2_modulo1 import histograma
import filecmp
import zipfile
import os
from ex4_modulo1 import analisar_ficheiro_simbolos, comprimir_ficheiro

def testar_pipeline(input_file, output_prefix, p_bsc, key):
    pipeline(input_file, output_prefix, p_bsc, key)
    original = open(input_file, 'rb').read()
    final_path = os.path.join(f'{output_prefix}_final', os.path.basename(input_file))
    final = open(final_path, 'rb').read()   
    if original == final:
        print(f"SUCESSO: {input_file} == {output_prefix}")
    else:
        print(f"ERRO: {input_file} != {output_prefix}")

# Exemplos de teste com diferentes ficheiros
testar_pipeline('ababa', 'ababa', 0, 'key')
testar_pipeline('comeralgo', 'comeralgo', 0, 'key')
testar_pipeline('mangacomabacaxi', 'mangacomabacaxi', 0, 'key')
testar_pipeline('asyoulik', 'asyoulik', 0, 'key')
testar_pipeline('alice29', 'alice29', 0, 'key')


def mostrar_info_ficheiro(label, file_path):
    contagem, simbolo, prob, info, entropia = analisar_ficheiro_simbolos(file_path)
    tamanho = os.path.getsize(file_path)
    print(f"{label}: {file_path}")
    print(f"  Tamanho: {tamanho} bytes")
    print(f"  Entropia: {entropia:.4f} bits/símbolo")
    print(f"  Histograma (10 mais frequentes): {contagem.most_common(10)}")
    histograma(contagem, file_path, entropia)
    print('-'*100)
    return tamanho, entropia, contagem

def analisar_pipeline(input_file, output_prefix):
    # A: original
    A = input_file
    # B: ZIP
    B = os.path.join("filezip", os.path.basename(input_file) + ".zip")
    # C: cifrado
    C = f"{output_prefix}_encrypt.txt"
    # D: codificado canal
    D = f"{output_prefix}_encoded_rep.txt"
    # E: decifrado final (ZIP extraído)
    
    E_dir = f"{output_prefix}_final"
    E = os.path.join(E_dir, os.path.basename(input_file))

    print("Análise dos ficheiros do pipeline:")
    tamanho_A, entropia_A, hist_A = mostrar_info_ficheiro("A (original)", A)
    tamanho_B, entropia_B, hist_B = mostrar_info_ficheiro("B (ZIP)", B)
    tamanho_C, entropia_C, hist_C = mostrar_info_ficheiro("C (cifrado)", C)
    tamanho_D, entropia_D, hist_D = mostrar_info_ficheiro("D (canal)", D)
    tamanho_E, entropia_E, hist_E = mostrar_info_ficheiro("E (final extraído)", E)

    # Razão de compressão
    razao = tamanho_B / tamanho_A if tamanho_A > 0 else 0
    print(f"Razão de compressão (B/A): {razao:.4f}")
    print('-'*100)

    
analisar_pipeline('ababa', 'ababa')
analisar_pipeline('comeralgo', 'comeralgo')
analisar_pipeline('mangacomabacaxi', 'mangacomabacaxi')
analisar_pipeline('asyoulik', 'asyoulik')
analisar_pipeline('alice29', 'alice29')
