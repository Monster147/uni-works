Descompactar o pacote com os programas de teste
numa diretoria relativa à realização da série de exercicios.
Exemplo de estrutura de diretorias:

home
└── aluno
    └── psc
        └── 2425inv
              ├── serie
              ├── tp1
              └── tp2

$ cd ~/psc/2425inv/tp1
$ unzip ~/Downloads/tp1_entrega.zip

Depois da descompactação deve verificar o seguinte conteúdo:

$ tree serie2_entrega

serie2_entrega
├── exercicio1
│   ├── build.sh
|   ├── rotate_right.c
│   ├── rotate_right_asm.s     <-- a realizar pelo aluno
│   └── rotate_right_test.c
├── exercicio2
│   ├── build.sh
│   ├── my_memcmp_asm.s     <-- a realizar pelo aluno
│   └── my_memcmp_test.c
├── exercicio3
│   ├── build.sh
│   ├── get_val_ptr_asm.s     <-- a realizar pelo aluno
│   ├── get_val_ptr.c
│   └── get_val_ptr_test.c
├── exercicio4
│   ├── build.sh
│   ├── array_remove_cond_asm.s     <-- a realizar pelo aluno
│   ├── array_remove_cond.c
│   ├── array_remove_cond_test.c
│   └── less_than.s
├── invoke
│   └── invoke.s
├── readme.txt
└── test_all.sh


Os nomes dos ficheiros com o programa em assembly têm a forma xxx_asm.s,
em que xxx corresponde ao nome da função que contêm.
Os nomes dos ficheiros com o programa de teste têm a forma xxx_test.c,
em que xxx corresponde ao nome da função em teste.

A entrega deve ser realizada no Moodle através da submissão de um pacote zip
gerado da seguinte forma:

$ cd ~/psc/2425inv/tp1
$ zip -r tp1_entrega.zip tp1_entrega
