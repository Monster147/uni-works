from bsc import bsc
from BERSER import bit_error_rate, symbol_error_rate

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