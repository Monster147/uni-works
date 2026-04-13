### Exercise 2

**a) List and explain the sequence of calls performed by the program in x86-64/prog.s**

```s
movq $257, %rax
syscall
```

openat - abre um ficheiro utilizando o file descriptor específicado.

`openat(AT_FDCWD, "/etc/os-release", O_RDONLY) = 3`

O commando está a abrir o ficheiro `/etc/os-release` na diretoria atual (`AT_FCWD`), em
modo de leitura apenas.

```s
movq $8, %rax
syscall
```

lseek - altera o offset do ficheiro, permitindo ler e/ou escrever numa nova posição.

`lseek(3, 0, SEEK_END)`  

Move o cursor até ao fim, determinando o tamanho total do ficheiro.

```s
movq $9, %rax
syscall
```

mmap - cria um novo mapping entre o espaço de endereços do processo e um ficheiro de UNIX.

`mmap(NULL, 400, PROT_READ, MAP_PRIVATE, 3, 0) = 0x7b1d86365000`

Cria o mappng entre o ficheiro `/etc/os-release` e o espaço de endereços do processo.
O mapeamento é feito em modo de leitura (`PROT_READ`) e privado (`MAP_PRIVATE`).

```s
movq $1, %rax
syscall
```

write - escreve bytes de um buufer para um file descriptor.

```
write(1, "PRETTY_NAME=\"Ubuntu 24.04.1 LTS\""..., 400PRETTY_NAME="Ubuntu 24.04.1 LTS"NAME="Ubuntu"
VERSION_ID="24.04"
VERSION="24.04.1 LTS (Noble Numbat)"
VERSION_CODENAME=noble
ID=ubuntu
ID_LIKE=debian
HOME_URL="https://www.ubuntu.com/"
SUPPORT_URL="https://help.ubuntu.com/"
BUG_REPORT_URL="https://bugs.launchpad.net/ubuntu/"
PRIVACY_POLICY_URL="https://www.ubuntu.com/legal/terms-and-policies/privacy-policy"
UBUNTU_CODENAME=noble
LOGO=ubuntu-logo
) = 400
```
Escreve para o stdout os bytes mapeados do ficheiro.

```s
movq $231, %rax
syscall
```

exit_group - termina todas a threads no grupo de threads do processo.

`exit_group(0)`

Encerra o processo de forma limpa, terminando todas as threads e
devolvendo o código de sáida `0`.