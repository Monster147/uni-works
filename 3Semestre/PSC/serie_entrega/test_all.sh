#!/bin/sh

echo
echo "Testes do exercício 1"

cd exercicio1
./build.sh
./int_to_string_test

echo
echo "Testes do exercício 2"

cd ../exercicio2
./build.sh
./float_to_string_test

echo
echo "Testes do exercício 3"

cd ../exercicio3
./build.sh
./mini_snprintf_test

echo
echo "Testes do exercício 4"

cd ../exercicio4
./build.sh
./time_to_string_test

echo
echo "Testes do exercício 5"

cd ../exercicio5
./build.sh
./test.sh

cd ..
