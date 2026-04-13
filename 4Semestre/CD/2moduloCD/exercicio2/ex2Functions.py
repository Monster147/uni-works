import sys
from ex4_modulo1 import (
    analisar_ficheiro_simbolos,
    comprimir_ficheiro,
    processar_zip,
    unzip_file
)
from ex5_modulo1 import (
    encrypt,
    decrypt,
    encrypt_file,
    decrypt_file,
)
from bsc import bsc
from transmitSimulation import (
    open_file,
    write_file,
    repeat_encode,
    repeat_decode,
)

def pipeline(input_file, output_prefix, p_bsc, key):
    # 1. Ler o ficheiro de entrada (A)
    open_file(input_file)

    # 2. Codificação de fonte (A -> B)
    compressed_file = comprimir_ficheiro(input_file)

    # 3. Cifra (B -> C)
    cifra_file = f'{output_prefix}_encrypt.txt'
    encrypt_file(compressed_file, cifra_file, key)

    # 4. Codificação de canal (C -> D)
    canal_codificado_file = f'{output_prefix}_encoded_rep.txt'
    canal_codificado = repeat_encode(cifra_file, canal_codificado_file)
    write_file(canal_codificado_file, canal_codificado)

    # 5. BSC
    bsc_file = f'{output_prefix}_bsc.txt'
    bsc(canal_codificado_file, bsc_file, p_bsc)
    
    # 6. Decodificação de canal
    canal_decodificado_file = f'{output_prefix}_decoded.txt'
    repeat_decode(bsc_file, canal_decodificado_file)
    
    # 7. Decifra
    decifra_file = f'{output_prefix}_decrypt.txt'
    decrypt_file(canal_decodificado_file, decifra_file, key)
    
    # 8. Codificação de fonte (D -> E)
    unzip_file(decifra_file, f'{output_prefix}_final')

    print("Processo concluído.")