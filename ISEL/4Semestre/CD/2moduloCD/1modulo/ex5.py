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
        content = infile.read().decode('utf-8', errors='ignore')
    encrypted_content = encrypt(content, key)
    with open(output_file, 'wb') as outfile:
        outfile.write(encrypted_content.encode('utf-8'))

def decrypt_file(input_file, output_file, key):
    """
        Lê um ficheiro cifrado.
        Aplica `decrypt`.
        Escreve o texto decifrado num novo ficheiro.
    """
    with open(input_file, 'rb') as infile:
        content = infile.read().decode('utf-8', errors='ignore')
    decrypted_content = decrypt(content, key)
    with open(output_file, 'wb') as outfile:
        outfile.write(decrypted_content.encode('utf-8'))

encrypt_file('teste.txt','ENCRYPT.txt','KEY')
decrypt_file('ENCRYPT.txt','DECRYPT.txt','KEY')

#(b) Para dois ficheiros do conjunto Canterbury Corpus realize a sua cifra e decifra. Mostre o correto funcionamento das
#operações cifra e decifra. Apresente os histogramas e os valores das entropias dos ficheiros em claro, cifrado e decifrado.


def calculate_entropy(text):
    """
        Calcula a entropia do texto com base na frequência dos símbolos (ASCII).
    """
    simbolos = len(text)
    conteudo = collections.Counter(text)
    termos_entropia = []
    for freq in conteudo.values():
        prob = freq / simbolos
        termos_entropia.append(prob * math.log2(prob))
    
    return -sum(termos_entropia)

def plot_histogram(text, title, entropy):
    """ 
        Gera um gráfico de barras com as frequências dos códigos ASCII no texto fornecido.
    """
    conteudo = collections.Counter(text)
    ascii_values = list(conteudo.keys())  # Converte os caracteres para seus códigos ASCII
    frequency = list(conteudo.values())
    plt.figure(figsize=(10, 6))
    plt.bar(ascii_values, frequency, color='darkblue', alpha=0.7)
    plt.title(title)
    plt.xlabel('Código ASCII')
    plt.ylabel('Frequência')
    plt.xlim(0, 260)
    plt.legend([f'H(X)= {entropy:.4f}'], loc='upper right', fontsize=12, facecolor='white')
    plt.grid(True, linestyle='--', alpha=0.5)
    plt.tight_layout()
    plt.show()

# Encrypt and decrypt the file
encrypt_file('plain_text/alice29.txt', 'encrypt/ENCRYPTalicie29.txt', 'KEY')
decrypt_file('encrypt/ENCRYPTalicie29.txt', 'decrypt/DECRYPTalice29.txt', 'KEY')

# Read the contents of the files
with open('plain_text/alice29.txt', 'rb') as f:
    original_text = f.read()
with open('encrypt/ENCRYPTalicie29.txt', 'rb') as f:
    encrypted_text = f.read()
with open('decrypt/DECRYPTalice29.txt', 'rb') as f:
    decrypted_text = f.read()

# Calculate and display entropy
entropyOGAlice29 = calculate_entropy(original_text)
entropyEncryptedAlice29 = calculate_entropy(encrypted_text)
entropyDecryptedAlice29 = calculate_entropy(decrypted_text)
print(f'Entropy for alice29.txt (original): {entropyOGAlice29:.4f}')
print(f'Entropy for ENCRYPTalicie29.txt (encrypted): {entropyEncryptedAlice29:.4f}')
print(f'Entropy for DECRYPTalice29.txt (decrypted): {entropyDecryptedAlice29:.4f}')

# Plot histograms
plot_histogram(original_text, "Histogram for alice29.txt (Original)", entropyOGAlice29)
plot_histogram(encrypted_text, "Histogram for EXNCRYPTalice29.txt (Encrypted)", entropyEncryptedAlice29)
plot_histogram(decrypted_text, "Histogram for DECRYPTalice29.txt (Decrypted)", entropyDecryptedAlice29)

print("---------------------------------------------------------------------------------")

encrypt_file('plain_text/asyoulik.txt', 'encrypt/ENCRYPTasyoulik.txt', 'KEYS')
decrypt_file('encrypt/ENCRYPTasyoulik.txt', 'decrypt/DECRYPTasyoulik.txt', 'KEYS')

with open('plain_text/asyoulik.txt', 'rb') as f:
    original_text = f.read()
with open('encrypt/ENCRYPTasyoulik.txt', 'rb') as f:
    encrypted_text = f.read()
with open('decrypt/DECRYPTasyoulik.txt', 'rb') as f:
    decrypted_text = f.read()

entropyOGAsyoulik = calculate_entropy(original_text)
entropyEncryptedAsyoulik = calculate_entropy(encrypted_text)
entropyDecryptedAsyoulik = calculate_entropy(decrypted_text)
print(f'Entropy for asyoulik.txt (original): {entropyOGAsyoulik:.4f}')
print(f'Entropy for ENCRYPTasyoulik.txt (encrypted): {entropyEncryptedAsyoulik:.4f}')
print(f'Entropy for DECRYPTasyoulik.txt (decrypted): {entropyDecryptedAsyoulik:.4f}')

# Plot histograms
plot_histogram(original_text, "Histogram for asyoulik.txt (Original)", entropyOGAsyoulik)
plot_histogram(encrypted_text, "Histogram for ENCRYPTasyoulik.txt (Encrypted)", entropyEncryptedAsyoulik)
plot_histogram(decrypted_text, "Histogram for DECRYPTasyoulik.txt (Decrypted)", entropyDecryptedAsyoulik)
    