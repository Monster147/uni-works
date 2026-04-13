Este README, tem como propósito de informar a como procedi com as alíneas B
do exercício 3 e 4.

Nas pastas referentes ao exercício 3 e 4 criei um novo ficheiro de testes,
com os testes pedidos no enunciado.

Para o exercício 3, o ficheiro get_val_ptr_aditional_tests.c, inclui os novos testes.
Para o exercício 4, o ficheiro array_remove_cond_test_student.c, inclui os novos testes
com o struct student.

Também para ambos os exercícios, alterei o build.sh de cada um.
Adicionei um comando gcc novo, que, para além, de compilarem para os testes fornecidos,
agora também compilam para os novos testes.

Assim, o test_all.sh, também foi alterado para correrem o executável dos novos testes.
Cada executável segue o nome do ficheiro de testes, ou seja, para o get_val_ptr_aditional_tests.c,
temos o executável com o nome get_val_ptr_aditional_tests e para o array_remove_cond_test_student.c,
temos o executável com o nome array_remove_cond_test_student

Trabalho realizado por: José Saldanha, nº 51445, LEIC32D
