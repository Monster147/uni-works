import serial
import time
import random
import csv

# CRC-8 com polinómio 0x07
def crc8(data):
    crc = 0
    for b in data:
        crc ^= b
        for _ in range(8):
            if crc & 0x80:
                crc = (crc << 1) ^ 0x07
            else:
                crc <<= 1
            crc &= 0xFF
    return crc

def introduzir_erro_isolado(data):
    data = bytearray(data)
    i = random.randint(0, 3)
    bit = 1 << random.randint(0, 7)
    data[i] ^= bit
    return bytes(data)

def introduzir_rajada(data):
    data = bytearray(data)
    bit_start = random.randint(0, 28)
    for b in range(4):
        pos = bit_start + b
        byte_idx = pos // 8
        bit_pos = pos % 8
        data[byte_idx] ^= (1 << bit_pos)
    return bytes(data)

# ---------------- CONFIGURAÇÕES ----------------
PORTA = 'COM4'
BAUD = 9600
N = 20
use_crc = True # False = exercício (a), True = exercício (b)

erros = {
    3: 'isolado',
    7: 'rajada',
    12: 'isolado',
    18: 'rajada'
}

sequencia = []
relatorio = []

with serial.Serial(PORTA, BAUD, timeout=2) as ser:
    time.sleep(2)

    for i in range(N):
        data = ser.read(4)
        if len(data) < 4:
            print(f"[{i}] Erro: dados incompletos.")
            continue

        valor_original = int.from_bytes(data, 'big')

        if not use_crc:
            sequencia.append(valor_original)
            print(f"[{i}] Valor recebido: {valor_original}")
            continue

        crc_byte = ser.read(1)
        if len(crc_byte) < 1:
            print(f"[{i}] Erro: byte de CRC em falta.")
            continue
        crc_recebido = crc_byte[0]

        tipo_erro = erros.get(i, 'nenhum')
        data_mod = data

        if tipo_erro == 'isolado':
            data_mod = introduzir_erro_isolado(data)
        elif tipo_erro == 'rajada':
            data_mod = introduzir_rajada(data)

        crc_calculado = crc8(data_mod)
        resultado = 'OK' if crc_calculado == crc_recebido else 'ERRO'
        valor_recebido = int.from_bytes(data_mod, 'big')

        sequencia.append(valor_recebido)

        relatorio.append({
            'Índice': i,
            'ValorOriginal': valor_original,
            'ValorRecebido': valor_recebido,
            'Bytes': ' '.join(f"{b:02X}" for b in data_mod),
            'Erro': tipo_erro,
            'CRC_esperado': f"{crc_calculado:02X}",
            'CRC_recebido': f"{crc_recebido:02X}",
            'Resultado': resultado
        })

# ---------------- FICHEIROS ----------------

if use_crc:
    seq_file = "fibonacci_com_crc.txt"
    relatorio_file = "relatorio_crc.csv"
else:
    seq_file = "fibonacci_sem_crc.txt"
    relatorio_file = None

# Guardar sequência recebida
with open(seq_file, "w", encoding="utf-8") as f:
    for v in sequencia:
        f.write(f"{v}\n")

print(f"\nSequência guardada em '{seq_file}'")

# Guardar relatório CRC se aplicável
if use_crc and relatorio:
    with open(relatorio_file, "w", newline='', encoding="utf-8") as f:
        writer = csv.DictWriter(f, fieldnames=relatorio[0].keys())
        writer.writeheader()
        writer.writerows(relatorio)

    print(f"Relatório guardado em '{relatorio_file}'")

    print(f"\n{'Idx':>3} {'Valor':>6} {'Erro':>8} {'CRC Exp':>8} {'CRC Rec':>8} {'Status':>7}")
    for linha in relatorio:
        print(f"{linha['Índice']:>3} {linha['ValorRecebido']:>6} {linha['Erro']:>8} "
              f"{linha['CRC_esperado']:>8} {linha['CRC_recebido']:>8} {linha['Resultado']:>7}")
