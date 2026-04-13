import matplotlib.pyplot as plt  

def histograma(contagem, file_name, entropia):
    plt.figure(figsize=(10, 6))  # Ajusta o tamanho da figura
    ascii_vals = list(contagem.keys())  # Converte os caracteres para códigos ASCII
    frequencies = list(contagem.values())

    plt.bar(ascii_vals, frequencies, color='darkblue', width=1.0, alpha=0.7)  # Define barras finas e semi-transparentes
    plt.xlabel('Código ASCII')  # Rótulo do eixo X
    plt.ylabel('Frequência')  # Rótulo do eixo Y
    plt.title(file_name)  # Define o título como o nome do ficheiro
    plt.xlim(0, 300)  # Define os limites do eixo X para melhor visualização

    # Adiciona a legenda com a entropia
    plt.legend([f'H(X)= {entropia:.4f}'], loc='upper right', fontsize=12, facecolor='white')

    plt.grid(True, linestyle='--', alpha=0.5)  # Adiciona uma grade leve ao gráfico
    plt.show()  # Exibe o histograma