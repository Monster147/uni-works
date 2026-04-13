# 1. Na raiz do projeto, compilar o backend
`./gradlew build`

# 2. Compilar o frontend - entrar na pasta frontend antes
```bash
cd ./frontend/
npm install
npx vite build
cd ../
```

# 3. Construir todas as imagens docker e inciar os serviços
`./gradlew allUp`

## Notas
 - Garantir que antes de fazer o build do backend, o ficheiro `/repo-jdbc/docker/scripts/wait-for-postgres.sh`
   encontra-se no modo LF em vez de CRLF.
 - Se quiser limpar as imagems e containers docker criados, pode usar o comando:
 `./gradlew cleanImages`