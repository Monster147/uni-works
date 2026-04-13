from bsc import bsc
from BERSER import bit_error_rate, symbol_error_rate
import os

def open_file(file_path):
    with open(file_path, 'rb') as f:
        return f.read()
 
def write_file(file_path, conteudo):
    with open(file_path, 'wb') as f:
        f.write(conteudo)

# ----- Código de repetição (3,1) -----
def repeat_encode(input_file, output_file):
    conteudo = open_file(input_file)
    bits = ''.join(f"{b:08b}" for b in conteudo)
    enconded_bits_list = []
    for bit in bits:
        enconded_bits_list.append(bit * 3)
    enconded_bits = ''.join(enconded_bits_list)
    ebits_array = bytearray(int(enconded_bits[i:i+8], 2) for i in range(0, len(enconded_bits), 8))
    write_file(output_file, ebits_array)
    return ebits_array

def repeat_decode(input_file, output_file):
    conteudo = open_file(input_file)
    bits = ''.join(f"{b:08b}" for b in conteudo)
    decoded_bits_list = []
    for i in range(0, len(bits), 3):
        block = bits[i:i+3]
        if len(block) < 3:
            break
        decoded_bits_list.append('1' if block.count('1') > 1 else '0')
    decoded_bits = ''.join(decoded_bits_list)
    dbits_array = bytearray(int(decoded_bits[i:i+8], 2) for i in range(0, len(decoded_bits), 8))
    write_file(output_file, dbits_array)
    return dbits_array

# ----- Código de Hamming (7,4) -----
def hamming_encode(input_file, output_file):
    conteudo = open_file(input_file)
    bits = ''.join(f"{b:08b}" for b in conteudo)
    encoded_bits_list = []
    for i in range(0, len(bits), 4):
        block = bits[i:i+4].ljust(4, '0')
        m = len(block)  
        p = calculate_parity_bits(m)
        positioned_data = position_parity_bits(block, p)
        hamming_code = calculate_parity_values(positioned_data, p)
        encoded_bits_list += hamming_code
    encoded_bits = ''.join(encoded_bits_list)
    encoded_bits = encoded_bits.ljust((len(encoded_bits) + 7) // 8 * 8, '0') 
    ebits_array = bytearray(
        int(encoded_bits[i:i+8], 2)
        for i in range(0, len(encoded_bits), 8)
    )
    write_file(output_file, ebits_array)
    return ebits_array

def hamming_decode(input_file, output_file):
    conteudo = open_file(input_file)
    bits = ''.join(f"{b:08b}" for b in conteudo)
    decoded_bits_list = []
    for i in range(0, len(bits), 7):
        block = bits[i:i+7]
        if len(block) < 7:
            break
        p = calculate_parity_bits(4)
        corrected_block = detect_correct_error(block, p)
        decoded_bits_list.append(''.join(corrected_block[i] for i in [2, 4, 5, 6]))
    decoded_bits = ''.join(decoded_bits_list)
    decoded_bytes = bytearray(
        int(decoded_bits[i:i+8], 2)
        for i in range(0, len(decoded_bits) - len(decoded_bits) % 8, 8)
    )
    write_file(output_file, decoded_bytes)
    return decoded_bytes


def calculate_parity_bits(m):
    p = 0
    while(2 ** p) < m + p + 1:
        p += 1
    return p

def position_parity_bits(data, p):
    result = []
    j = 0
    k = 0
    m = len(data)
    total_length = m + p
    for i in range(1, total_length + 1):
        if i == 2 ** j:
            result.append('0') 
            j += 1
        else:
            result.append(data[k])
            k += 1
    return ''.join(result)

def calculate_parity_values(code, p):
    code = list(code)
    n = len(code)
    for i in range(p):
        parity = 0
        for j in range(1, n + 1):
            if j & (2 ** i):
                parity ^= int(code[j - 1])
        code[(2 ** i) - 1] = str(parity)
    return ''.join(code)

def detect_correct_error(code, p):
    code = list(code)
    n = len(code)
    error_pos = 0
    for i in range(p):
        parity = 0
        for j in range(1, n + 1):
            if j & (2 ** i):
                parity ^= int(code[j - 1])
        if parity:
            error_pos += 2 ** i
    if error_pos:
            code[error_pos - 1] = str(1 - int(code[error_pos - 1]))
    return ''.join(code)

# ----- Exercício 1 c) ------

def testar_codigos(input_file, p):
    folder = "exercicio_1_created_files"
    os.makedirs(folder, exist_ok=True)

    print(f"----------------Teste no {input_file}----------------")
    original_bits = os.path.getsize(input_file) * 8
# ---------- Sem códigos de controlo de erros ----------
    no_code_file = os.path.join(folder, f"{input_file}_output_no_error_code.txt")
    bsc(input_file, no_code_file, p)
    transmitted_bits = os.path.getsize(no_code_file) * 8
    ber = bit_error_rate(input_file, no_code_file)
    ser = symbol_error_rate(input_file, no_code_file)
    print(f"(i) Sem códigos de controlo de erros: BER={ber}, SER={ser}")
    print(f"(i) Ficheiro de saída: {no_code_file}")
    print(f"(i) Bits transmitidos: {transmitted_bits}")
    print(f"(i) Bits de informação: {original_bits}")
    # ---------- Com Repetição (3,1) ----------
    encoded_rep = os.path.join(folder, f"{input_file}_encoded_rep.txt")
    output_rep = os.path.join(folder, f"{input_file}_output_rep.txt")
    decoded_rep = os.path.join(folder, f"{input_file}_decoded_rep.txt")
    encoded = repeat_encode(input_file, encoded_rep)
    write_file(encoded_rep, encoded)
    bsc(encoded_rep, output_rep, p)
    repeat_decode(output_rep, decoded_rep)
    transmitted_bits = os.path.getsize(output_rep) * 8
    ber = bit_error_rate(input_file, decoded_rep)
    ser = symbol_error_rate(input_file, decoded_rep)
    print(f"(ii) Repetição (3,1): BER={ber}, SER={ser}")
    print(f"(ii) Ficheiro de saída: {output_rep}")
    print(f"(ii) Bits transmitidos: {transmitted_bits}")
    print(f"(ii) Bits de informação: {original_bits}")
    
    # ---------- Com Hamming (7,4) ----------
    # ---------- Com Hamming (7,4) ----------
    encoded_ham = os.path.join(folder, f"{input_file}_encoded_ham.txt")
    output_ham = os.path.join(folder, f"{input_file}_output_ham.txt")
    decoded_ham = os.path.join(folder, f"{input_file}_decoded_ham.txt")
    hamming_encode(input_file, encoded_ham)
    bsc(encoded_ham, output_ham, p)
    hamming_decode(output_ham, decoded_ham)
    transmitted_bits = os.path.getsize(output_ham) * 8
    ber = bit_error_rate(input_file, decoded_ham)
    ser = symbol_error_rate(input_file, decoded_ham)
    print(f"(iii) Hamming (7,4): BER={ber}, SER={ser}")
    print(f"(iii) Ficheiro de saída: {output_ham}")
    print(f"(iii) Bits transmitidos: {transmitted_bits}")
    print(f"(iii) Bits de informação: {original_bits}")

# ---------- Para p = 0 ----------
print("--------------------- Para p = 0 ---------------------")
testar_codigos('inputFile.txt', 0)
testar_codigos('ababa', 0)
testar_codigos('comeralgo', 0)
testar_codigos('mangacomabacaxi', 0)
testar_codigos('alice29', 0)
testar_codigos('asyoulik', 0)

# ---------- Para p = 0.01 ----------
print("--------------------- Para p = 0.01 ---------------------")
testar_codigos('inputFile.txt', 0.01)
testar_codigos('ababa', 0.01)
testar_codigos('comeralgo', 0.01)
testar_codigos('mangacomabacaxi', 0.01)
testar_codigos('alice29', 0.01)
testar_codigos('asyoulik', 0.01)

# ---------- Para p = 0.1 ----------
print("--------------------- Para p = 0.1 ---------------------")
testar_codigos('inputFile.txt', 0.1)
testar_codigos('ababa', 0.1)
testar_codigos('comeralgo', 0.1)
testar_codigos('mangacomabacaxi', 0.1)
testar_codigos('alice29', 0.1)
testar_codigos('asyoulik', 0.1)

# ---------- Para p = 0.5 ----------
print("--------------------- Para p = 0.5 ---------------------")
testar_codigos('inputFile.txt', 0.5)
testar_codigos('ababa', 0.5)
testar_codigos('comeralgo', 0.5)
testar_codigos('mangacomabacaxi', 0.5)
testar_codigos('alice29', 0.5)
testar_codigos('asyoulik', 0.5)
