# Relatório

## Fase 1 - jdbcRepoLib com Kotlin Reflection

### Modélo de domínio de dados

O modelo de domínio utilizado para os testes adicionais, propostos no ponto 1.3, inclui as seguintes entidades:
- **Inventory (Inventário)**: Representa um inventário com propriedades como `location`, `name`, entre outras.
- **Supplier (Fornecedor)**: Representa um fornecedor com propriedades como `name` e `type`.
- **Product (Produto)**: Representa um propriedade com propriedades como `name`, `price`, `category`.

O modelo inclui as seguintes associações:
- Um `Inventory` está associada a um `Supplier`, assim como um `Product` também está associado.
- Um `Product` possui um enum `product_category` para representar o seu tipo.
- Um `Supplier` possui um enum `supplier_type` para representar o seu tipo.

### Resultados dos Benchmarks

O desempenho da implementação `RepositoryReflect` foi comparado com a implementação de referência `ChannelRepositoryJdbc`, utilizando o framework de benchmarking JMH. Os resultados são os seguintes:

#### Resultados

| Benchmark                                   | Modo | Iterações | Tempo Médio (ns/op)  | Erro (ns/op)     |
|--------------------------------------------|------|-----------|-----------------------|------------------|
| `benchRepositoryJdbcGetAllChannels`        | avgt | 4         | 645.413               | ±26.378          |
| `benchRepositoryReflectGetAllChannels`     | avgt | 4         | 269360.247            | ±15238.090       |

#### Observações

- A implementação `RepositoryReflect` é aproximadamente **417× mais lenta** do que a implementação ad-hoc `ChannelRepositoryJdbc`.
- A lentidão esperada era de **3×**, o que indica que a implementação atual tem ineficiências significativas.
- As margens de erro dos testes são pequenas, o que indica consistência nos resultados.

#### Problemas Identificados

1. **Associações Complexas**:
    - O tratamento dinâmico de relações entre entidades adiciona overhead adicional ao sistema.

### Melhorias de Performance
- **Execução Eficiente de Queries**:
    - As queries são preparadas uma vez e reutilizadas quando possível.

### Melhorias Potenciais

1. **Uso de Caches**
    - O uso de caches pode melhorar substancialmente a performance do benchmark,
levando a tempos consideravelmente menores

2. **Uso da Reflection API do Kotlin**
    - Melhorar o uso da API

### Conclusão

A implementação `RepositoryReflect` demonstra a flexibilidade da utilização da `Reflection API` do Kotlin para a geração automática de repositórios. 
No entanto, o desempenho é significativamente inferior à implementação ad-hoc. 
As melhorias identificadas deverão ajudar a reduzir esta diferença de performance e aproximar os resultados da baseline.

## Fase 2 - Geração dinâmica de bytecode

### Melhorias Implementadas

1. **Implementação do `RepositoryDynamic`**:
   - Foi desenvolvida uma implementação dinâmica de repositórios utilizando a API de Class-File do Java 22.
   - Essa abordagem elimina o uso de reflexão para instanciar classes de domínio, gerando código diretamente em tempo de execução.

2. **Otimizações no `RepositoryReflect`**:
   - O código do `RepositoryReflect` foi aprimorado para melhorar a eficiência e reduzir o overhead associado ao uso da API de reflexão do Kotlin.
   - As melhorias incluem:
     - Uso mais eficiente de propriedades de enum.
     - Melhor tratamento de associações entre entidades.
     - Redução de redundâncias no mapeamento de resultados do `ResultSet`.

### Resultados do Benchmark

Os benchmarks foram organizados com base nas operações realizadas, e os tempos médios de execução (em nanosegundos por operação) foram registrados para cada abordagem:

#### Operação: Obter Todos os Canais

| Implementação         | Tempo Médio (ns/op) | Erro (ns/op) |
|-----------------------|---------------------|--------------|
| Dynamic               | 35197,918           | ±5545,661    |
| JDBC                  | 263,326             | ±10,987      |
| Reflection            | 35097,245           | ±406,583     |

#### Operação: Obter Todos os Produtos

| Implementação         | Tempo Médio (ns/op) | Erro (ns/op) |
|-----------------------|---------------------|--------------|
| Dynamic               | 69063,881           | ±120818,229  |
| JDBC                  | 289,739             | ±1,003       |
| Reflection            | 51826,573           | ±229,123     |

### Observações

- A implementação dinâmica (`RepositoryDynamic`) apresentou desempenho semelhante ao `RepositoryReflect`, mas com a vantagem de não depender de reflexão para instanciar entidades.
- A implementação ad-hoc baseada em JDBC continua a ser significativamente mais rápida, especialmente para operações simples como `getAll`.
- As melhorias no `RepositoryReflect` reduziram bastante o valor do tempo médio, mas o uso de reflexão ainda representa um gargalo de desempenho.

### Conclusão

A segunda fase do projeto demonstrou a viabilidade de gerar repositórios dinamicamente em tempo de execução, reduzindo a dependência de reflexão para instanciar classes de domínio. 
Apesar disso, a abordagem ad-hoc baseada em JDBC continua a ser a mais eficiente em termos de desempenho. 
As melhorias no `RepositoryReflect` também contribuíram para reduzir a diferença de desempenho em relação à implementação ad-hoc, mas ainda podem existir otimizações adicionais.