#(b) Apresente resultados que comprovem o correto funcionamento da função bsc, com valores de p e ficheiros à sua escolha.
#Apresente os respetivos valores de Bit Error Rate (BER) e Symbol Error Rate (SER).
def bit_error_rate(original, received):
    conteudo_original = open_file(original)
    bits_original = []
    for b in conteudo_original:
        bits_original.append(f"{b:08b}")
    #print("Original bits:", bits_original) 
    
    conteudo_received = open_file(received)
    bits_received = []
    for b in conteudo_received:
        bits_received.append(f"{b:08b}")
        
    total_bits = len(bits_original) * 8
    #print("Total bits:", total_bits)
    error_bits = 0
    for i in range(len(bits_original)):
        for j in range(8):
            #print(f"Comparing {bits_original[i][j]} with {bits_received[i][j]}")
            if bits_original[i][j] != bits_received[i][j]:
                error_bits += 1
    #print("Error bits:", error_bits)
    return error_bits / total_bits

def symbol_error_rate(original, received):
    conteudo_original = open_file(original)
    bits_original = []
    for b in conteudo_original:
        bits_original.append(f"{b:08b}")
    total_symbols = len(bits_original)
    
    conteudo_received = open_file(received)
    bits_received = []
    for b in conteudo_received:
        bits_received.append(f"{b:08b}")    
        
    error_symbols = 0
    
    for i in range(len(bits_original)):
            print(f"Comparing {bits_original[i]} with {bits_received[i]}")
            if bits_original[i] != bits_received[i]:
                error_symbols += 1
    
    return error_symbols / total_symbols

def open_file(file_path):
 with open(file_path, 'rb') as f:
        return f.read()
 
#print("BER:", bit_error_rate('inputFile.txt', 'outputFile.txt'))
#print("SER:", symbol_error_rate('inputFile.txt', 'outputFile.txt'))