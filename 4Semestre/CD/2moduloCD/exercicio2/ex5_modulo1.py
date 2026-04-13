import collections
import math
import matplotlib.pyplot as plt
import random

#Cifra de ficheiros.
#Considere a cifra de Vigenère, https://pt.wikipedia.org/wiki/Cifra_de_Vigenère.
#(a) Implemente as funções de cifra e decifra sobre ficheiros

#Lista de caracteres especiais usados para substituir espaços (' ') na cifra.
special_chars = ['¤', '¢']

def encrypt(text, key):
    """
     Realiza a cifra do texto usando a cifra de Vigenère.
        Letras são cifradas normalmente.
        Dígitos também são deslocados com base na chave.
        Espaços são substituídos aleatoriamente por símbolos especiais.
        Caracteres não alfabéticos são mantidos.
    """
    encrypted = []
    key = key.upper()
    key_length = len(key)
    key_index = 0

    for char in text:
        if char.isalpha():
            shift = ord(key[key_index]) - ord('A')
            if char.islower():
                encrypted.append(chr((ord(char) - ord('a') + shift) % 26 + ord('a')))
            else:
                encrypted.append(chr((ord(char) - ord('A') + shift) % 26 + ord('A')))
            key_index = (key_index + 1) % key_length
        elif char.isdigit():
            shift = ord(key[key_index]) - ord('4')
            encrypted.append(chr((ord(char) - ord('0') + shift) % 10 + ord('0')))
        elif char == ' ':
            encrypted.append(random.choice(special_chars))
        else:
            encrypted.append(char)

    return ''.join(encrypted)

def decrypt(text, key):
    """ 
        Inverte a operação feita em `encrypt`.
        Caracteres especiais voltam a ser espaços.
        Letras e dígitos são decifrados com base na mesma chave.
    """
    decrypted = []
    key = key.upper()
    key_length = len(key)
    key_index = 0

    for char in text:
        if char.isalpha():
            shift = ord(key[key_index]) - ord('A')
            if char.islower():
                decrypted.append(chr((ord(char) - ord('a') - shift) % 26 + ord('a')))
            else:
                decrypted.append(chr((ord(char) - ord('A') - shift) % 26 + ord('A')))
            key_index = (key_index + 1) % key_length
        elif char.isdigit():
            shift = ord(key[key_index]) - ord('4')
            decrypted.append(chr((ord(char) - ord('0') - shift) % 10 + ord('0')))
        elif char in special_chars:
             decrypted.append(' ')
        else:
            decrypted.append(char)

    return ''.join(decrypted)

def encrypt_file(input_file, output_file, key):
    """
    Lê o conteúdo de um ficheiro de entrada.
        Aplica `encrypt`.
        Escreve o resultado num novo ficheiro.

    """
    with open(input_file, 'rb') as infile:
        content = infile.read()
    key_bytes = key.encode('utf-8')
    key_len = len(key_bytes)
    encrypted = bytearray(
        (b ^ key_bytes[i % key_len]) for i, b in enumerate(content)
    )
    with open(output_file, 'wb') as outfile:
        outfile.write(encrypted)

def decrypt_file(input_file, output_file, key):
    """
        Lê um ficheiro cifrado.
        Aplica `decrypt`.
        Escreve o texto decifrado num novo ficheiro.
    """
    with open(input_file, 'rb') as infile:
        content = infile.read()
    key_bytes = key.encode('utf-8')
    key_len = len(key_bytes)
    decrypted = bytearray(
        (b ^ key_bytes[i % key_len]) for i, b in enumerate(content)
    )
    with open(output_file, 'wb') as outfile:
        outfile.write(decrypted)