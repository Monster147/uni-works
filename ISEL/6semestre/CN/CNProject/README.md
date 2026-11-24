# CN2026Labels

Sistema de deteção e tradução de características em imagens, desenvolvido no âmbito da unidade curricular **Computação na Nuvem** (LEIC), verão 2025/2026.

---

## Visão Geral

O CN2026Labels permite submeter ficheiros de imagem (JPG, PNG, etc.) para análise automática via Google Cloud Vision API, traduzindo as *labels* detetadas de inglês para português através da Cloud Translation API. O sistema é elástico — o número de instâncias de servidores gRPC e de workers de processamento pode ser ajustado dinamicamente.

---

## Estrutura do Projeto

```
CN2026Labels/
├── CN2026LabelsContract/          # Contrato protobuf gRPC partilhado
│   └── src/main/proto/
│       └── CN2026LabelsContract.proto
│
├── CN2026LabelsClientApp/         # Aplicação cliente (linha de comandos)
│   └── src/main/java/grpcclientapp/
│       └── Client.java
│
├── CN2026LabelsServerApp/         # Servidor gRPC (executa em Compute Engine)
│   └── src/main/java/grpcserverapp/
│       ├── CN2026LabelsServiceImpl.java
│       ├── ConfigManager.java
│       ├── FirestoreService.java
│       ├── GrpcServer.java
│       ├── StorageService.java
│       ├── PubSubPublisher.java
│       └── ...
│
├── CN2026LabelsTranslatorApp/     # Worker de processamento de imagens (Labels App)
│   └── src/main/java/grpcservices/
│       ├── AppMain.java
│       ├── ImageProcessor.java
│       ├── PubSubSubscriber.java
│       ├── WorkerMain.java
│       └── ...
│
├── CN2026LabelsLookupFunc/        # Cloud Run Function HTTP (IP lookup)
│   └── src/main/java/cn/
│       ├── ListVMIP.java
│       └── ConfigManager.java
│
└── CN2026LabelsPubSubCloud/       # Cloud Run Function Pub/Sub (logging)
    └── src/main/java/cn/
        ├── Entrypoint.java
        ├── PSMessage.java
        └── ConfigManager.java
```

---


## Pré-requisitos

- **Java 25**
- Conta GCP com os seguintes serviços habilitados:
    - Cloud Storage
    - Firestore
    - Pub/Sub
    - Compute Engine
    - Cloud Run Functions
    - Cloud Vision API
    - Cloud Translation API
- `gcloud` CLI configurado
- Variável de ambiente `GOOGLE_APPLICATION_CREDENTIALS` apontada para a service account com permissões adequadas

---

## Configuração GCP

### Variáveis de ambiente (ConfigManager)

Cada módulo aceita configuração por variáveis de ambiente, com os seguintes valores predefinidos:

| Variável de Ambiente     | Valor Padrão                        | Descrição                            |
|--------------------------|-------------------------------------|--------------------------------------|
| `GCP_PROJECT_ID`         | `cn2526-t1-g07`                     | ID do projeto GCP                    |
| `GCP_ZONE`               | `europe-west1-b`                    | Zona dos recursos Compute Engine     |
| `GCP_BUCKET_NAME`        | `cn2026labels-g07-bucket`           | Bucket Cloud Storage para imagens    |
| `FIRESTORE_COLLECTION`   | `cn2026labels-g07-collection`       | Coleção Firestore                    |
| `FIRESTORE_DATABASE_ID`  | `cn2026labels-g07-firestore`        | ID da base de dados Firestore        |
| `PUBSUB_TOPIC`           | `labels-topic`                      | Tópico Pub/Sub principal             |
| `GRPC_SERVERS_GROUP`     | `cn2026-labels-server-group`        | Instance group dos servidores gRPC   |
| `WORKERS_GROUP`          | `cn2026-labels-worker-group`        | Instance group dos workers           |

### Recursos GCP a criar previamente

1. **Bucket Cloud Storage** — para armazenamento das imagens
2. **Base de dados Firestore** — para persistência dos resultados
3. **Tópico Pub/Sub** com duas subscrições:
    - Uma partilhada pelos workers (work-queue pattern)
    - Uma para a Cloud Function de logging
4. **Instance Groups** para servidores gRPC e workers (Managed Instance Groups)
5. **Cloud Run Functions** — Lookup (HTTP) e Logging (Pub/Sub trigger)

---

## Compilação

Cada módulo é compilado individualmente com Maven. Para o `CN2026LabelsContract` (geração dos stubs gRPC) compilar primeiro:

```bash
# 1. Compilar o contrato (stubs gRPC gerados automaticamente via protoc)
cd CN2026LabelsContract
mvn clean install

# 2. Compilar o servidor gRPC
cd ../CN2026LabelsServerApp
mvn clean package

# 3. Compilar o cliente
cd ../CN2026LabelsClientApp
mvn clean package

# 4. Compilar o worker (Labels App)
cd ../CN2026LabelsTranslatorApp
mvn clean package

# 5. Compilar a Lookup Function
cd ../CN2026LabelsLookupFunc
mvn clean package -DskipTests

# 6. Compilar a PubSub Cloud Function (logging)
cd ../CN2026LabelsPubSubCloud
mvn clean package -DskipTests
```

Os JARs executáveis são gerados em `target/` com todas as dependências incluídas (assembly fat-jar).

---

## Execução

### Servidor gRPC (`CN2026LabelsServerApp`)

Na ciração do template da VM para os servidores gRPC, configurar o startup script para executar o servidor automaticamente. O script deve definir as variáveis de ambiente necessárias e iniciar o servidor:

```bash
cd <diretório do servidor>
export GOOGLE_APPLICATION_CREDENTIALS=/path/to/service-account.json
java -jar CN2026LabelsServerApp-1.0-jar-with-dependencies.jar
```

### Worker de Processamento (`CN2026LabelsTranslatorApp`)

Na criação do template da VM para os workers, configurar o startup script para iniciar o worker automaticamente. O script deve definir as variáveis de ambiente necessárias e iniciar o worker:

```bash
cd <diretório do worker>
export GOOGLE_APPLICATION_CREDENTIALS=/path/to/service-account.json
java -jar CN2026LabelsTranslatorApp-1.0-jar-with-dependencies.jar
```

O worker inicia automaticamente a subscrição Pub/Sub e processa imagens em loop contínuo.

### Cliente (`CN2026LabelsClientApp`)

```bash
java -jar CN2026LabelsClientApp-1.0-jar-with-dependencies.jar
```

O cliente apresenta um menu interativo para submeter imagens, consultar resultados e gerir a infraestrutura. Antes de poder submeter imagens, é necessário listar os IPs disponíveis dos servidores gRPC (opção 6) e escolher um para se conectar. O menu principal é o seguinte:

```
1 - Submissão de um ficheiro imagem
2 - Obter a lista de todas as características encontradas na imagem
3 - Obter todos os nomes de ficheiros armazenados no sistema entre duas datas e que contêm uma determinada característica
4 - Download de uma imagem a partir do ID do pedido feito anteriormente
5 - Aumento e diminuição do número de instâncias de servidores gRPC
6 - Aumento e diminuição do número de instâncias da aplicação de processamento de imagens.
7 - Escolher servidor gRPC
99 - Exit
```

### Cloud Run Functions

#### Lookup Function (`CN2026LabelsLookupFunc`)

Deploy via `gcloud`:

```bash
cd CN2026LabelsLookupFunc
gcloud functions deploy funcHttpLookup --project=<GCP_PROJECT_ID> --region=<GCP_REGION> --entry-point=cn.ListVMIP --allow-unauthenticated --gen2 --runtime=java25 --trigger-http --source=target/deployment --service-account=<SERVICE_ACCOUNT_EMAIL>
```

#### Logging Function (`CN2026LabelsPubSubCloud`)

```bash
cd CN2026LabelsPubSubCloud
gcloud functions deploy funcPubSub --project=<GCP_PROJECT_ID> --region=<GCP_REGION> --entry-point=cn.Entrypoint --allow-unauthenticated --gen2 --runtime=java25 --trigger-topic labels-topic --source=target/deployment --service-account=<SERVICE_ACCOUNT_EMAIL>
```

---

## Fluxo de Operação Resumido

1. O cliente chama a Lookup Function (HTTP) para obter os IPs dos servidores gRPC e escolhe um dos servidores.
2. O cliente liga-se ao servidor escolhido e submete uma imagem em stream de blocos (`submitImage`).
3. O servidor guarda a imagem no Cloud Storage e publica uma mensagem no tópico Pub/Sub.
4. Um worker recebe a mensagem, obtém a imagem do Cloud Storage, chama a Vision API para detetar labels e a Translation API para as traduzir.
5. O worker guarda os resultados no Firestore.
6. O cliente consulta os resultados usando o ID retornado na submissão (`findImageLabelById` ou `searchImage`).
7. Se o cliente quiser, pode fazer download da imagem original usando o ID do pedido (`downloadImage`).

---

## Pressupostos e Notas

- As VMs dos instance groups devem ter a service account com as roles: `Cloud Database Owner`, `Compute Admin`, `Pub/Sub Admin`, `Storage Admin` e `VisionAI Admin (Beta)`.
- Assume-se que a service account a ser usada é usada em todos os módulos (servidores gRPC, workers, Cloud Functions).
- A Vision API e a Translation API devem estar habilitadas no projeto GCP.
- A elasticidade é gerida via `ScaleGrpcServers` e `ScaleWorkers` (chamadas gRPC que redimensionam os Managed Instance Groups).
- As imagens são armazenadas com o nome original do ficheiro como blob name no Cloud Storage — submissões de ficheiros com o mesmo nome sobrescrevem o blob existente.