## i.

Ao ligaro browser ao servidor HTTPS, o Security Overview apresenta os seguintes parâmetros
de segurança utilizados na ligação:
- Protocolo: TLS 1.3
- Algoritmo de troca de chaves: X25519
- Cifra: AES_256_GCM

Com base nestas informações:

### Confidencialidade das mensagens

A confidencialidade é garantida pelo algoritmo AES-256 em modo GCM.
O modo GCM é um esquema AEAD (Authenticated Encryption with Associated Data), que cifra os dados e impede que um
atacante os consiga obter sem conhecer as chaves de sessão derivadas durante o handshake TLS.

### Autenticidade e integridade das mensagens

A autenticidade das mensagens do Record Protocol é assegurada pelo próprio modo GCM, que inclui um authentication tag
que deteta qualquer modificação nos dados. 

Em TLS 1.3 não existe a combinação de HMAC com cifra, toda a integridade
é fornecida pelo mecanismo AEAD.

### Aunteticação e estabelicimento de chaves

A troca de chave utiliza X25519, uma variante eficiente de ECDHE.

Isto garante PFS (Perfect Forward Secrecy), ou seja, mesmo que a chave privada do servidor seja comprometida, um
atacante não consegue decifrar comunicações passadas porque cada ligação gera chaves efémeras independentes.

### Autenticação dos participantes

No cenário sem autenticação de cliente, apenas o servidor é autenticado através do seu certificado X.509.

No cenários com autenticação de cliente, o browser apresentou o certificado Alice_2, que o servidor validou,
autenticando o cliente.

## ii.

### Problema relacionado com a troca de chaves (RSA estático):

Esta cipher suite utiliza RSA estático para realizar a troca de chaves. Neste método, o cliente gera o pre-master secret
e envia-o cifrado com a cahve pública RSA do servidor, O servidor usa a sua chave pricada pra o decifrar.

O prblema é que este esquema não fornece PFS (Perfect Forward Secrecy). Isto significa que a segurança de todas as
chaves de sessão depende diretamente da proteção da chave privada RSA do servidor.

### Como um atacante pode explorar este problema:

Um atancante pode comprometer a confidencialidade das comunicações da seguinte forma:

1. Captura o tráfego cifrado entre o browser e o servidor (por exemplo, usando um sniffer).
2. Mais tarde, obtém a chave privada RSA do servidor, seja por roubo, fuga de informaç~oes, má configuração, etc.
3. Com a chave privada, o atacante decifra o pre-master secret que o cliente enviou no handshake.
4. A partir do pre-master secret, recalcula o master secret e todas as chaves simétricas derivadas
5. Finalmente, consegue decifrar todas as mensagens TLS capturadas, mesmo meses ou anos após a comunicação original.

Isto é possível porque o pre-master secret é sempre cifrado com a mesma chave pública RSA, e não com
chaves efémeras como acontece em ECDHE.