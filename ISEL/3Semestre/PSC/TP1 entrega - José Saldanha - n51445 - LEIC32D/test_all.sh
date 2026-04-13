
echo ".............Teste do exercício 1"
cd exercicio1
./build.sh
./rotate_right_test

echo ".............Teste do exercício 2"
cd ../exercicio2
./build.sh
./my_memcmp_test

echo ".............Teste do exercício 3"
cd ../exercicio3
./build.sh
./get_val_ptr_test
./get_val_ptr_aditional_tests

echo ".............Teste do exercício 4"
cd ../exercicio4
./build.sh
./array_remove_cond_test
./array_remove_cond_test_student
