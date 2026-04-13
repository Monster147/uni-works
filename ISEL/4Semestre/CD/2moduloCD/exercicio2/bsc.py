import random
#(a) Escreva a função bsc, a qual realiza o modelo BSC sobre um ficheiro de entrada e produz o respetivo ficheiro de saída.
#A troca de bits é realizada com probabilidade p, passada como parâmetro.
def open_file(file_path):
 with open(file_path, 'rb') as f:
        return f.read()
        
def write_file(file_path, conteudo):
    with open(file_path, 'wb') as f:
        f.write(conteudo)

def bsc(input_file, output_file, p):
    conteudo = open_file(input_file)
    output_bytes = bytearray()
    for byte in conteudo:
        new_byte = byte
        for i in range(8):
            if random.random() <= p:
                mask = 1 << (7 - i)
                new_byte ^= mask
        output_bytes.append(new_byte)
    write_file(output_file, output_bytes)

bsc('inputFile.txt', 'outputFile.txt', 0.1)