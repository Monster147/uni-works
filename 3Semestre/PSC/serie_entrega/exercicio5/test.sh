#!/bin/sh

#echo "------------------------------------------------------------------------"
#echo " - Testes com erros nos argumentos do programa"
#echo

./csv_show 2> /dev/null
if [ $? -ne 0 ]; then
	echo "[00] - OK"
else
	echo "[00] - Error"
	exit 1
fi

./csv_show -o output1.csv 2> /dev/null
if [ $? -ne 0 ]; then
	echo "[01] - OK"
else
	echo "[01] - Error"
	exit 1
fi

./csv_show -i input.csv -o output1.csv 2> /dev/null
if [ $? -ne 0 ]; then
	echo "[02] - OK"
else
	echo "[02] - Error"
	exit 1
fi

#echo "------------------------------------------------------------------------"
#echo " - Testes com redirecionamento"
#echo

./csv_show_ref input.csv > output1.ref.csv 2> /dev/null
./csv_show input.csv > output1.csv 2> /dev/null
cmp output1.ref.csv output1.csv

if [ $? -eq 0 ]; then
	echo "[10] - OK"
else
	echo "[10] - Error"
fi

rm output1.ref.csv output1.csv

#echo "------------------------------------------------------------------------"
#echo " - Testes com ficheiros na diretoria corrente"
#echo

./csv_show_ref input.csv -o output1.ref.csv 2> /dev/null
./csv_show input.csv -o output1.csv 2> /dev/null
cmp output1.ref.csv output1.csv

if [ $? -eq 0 ]; then
	echo "[20] - OK"
else
	echo "[20] - Error"
fi

rm output1.ref.csv output1.csv

./csv_show_ref input.csv -o output1.ref.csv -ar 2> /dev/null
./csv_show input.csv -o output1.csv -ar 2> /dev/null
cmp output1.ref.csv output1.csv

if [ $? -eq 0 ]; then
	echo "[21] - OK"
else
	echo "[21] - Error"
fi

rm output1.ref.csv output1.csv

./csv_show_ref input.csv -o output1.ref.csv -al 2> /dev/null
./csv_show input.csv -o output1.csv -al 2> /dev/null
cmp output1.ref.csv output1.csv > /dev/null

if [ $? -eq 0 ]; then
	echo "[22] - OK"
else
	echo "[22] - Error"
fi

rm output1.ref.csv output1.csv

#echo "------------------------------------------------------------------------"
#echo " - Testes com variável de ambiente"
#echo

mkdir -p subdir
cp input.csv subdir/inputx.csv
export CSV_FILE_PATH=./subdir

./csv_show_ref inputx.csv -o output1.ref.csv 2> /dev/null
./csv_show inputx.csv -o output1.csv 2> /dev/null
cmp output1.ref.csv output1.csv
if [ $? -eq 0 ]; then
	echo "[30] - OK"
else
	echo "[30] - Error"
fi

rm output1.ref.csv output1.csv

unset CSV_FILE_PATH
./csv_show inputx.csv -o output1.csv 2> /dev/null
if [ $? -ne 0 ]; then
	echo "[31] - OK"
else
	echo "[31] - Error"
fi

rm -rf subdir

