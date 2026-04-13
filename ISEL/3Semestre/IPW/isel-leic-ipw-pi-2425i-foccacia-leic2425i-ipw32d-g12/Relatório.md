# Relatório - FOCCACIA (FOotball Complete Clubs API and Chelas Internet Application)

Projeto realizado por:
```
José Carlos Saldanha, nº 51445
Ricardo Pinto, nº 51447
```

FOCCACIA (FOotball Complete Clubs API and Chelas Internet Application) é uma API em que pudemos criar uma conta e pesquisar por equipas.

Com uma conta podemos criar grupos e editar esses grupos, podendo adicionar ou remover equipas. Para estes grupos damos um nome e uma descrição, para sabermos qual grupo é qual.

## Índice
- [Relatório - FOCCACIA (FOotball Complete Clubs API and Chelas Internet Application)](#relatório---foccacia-football-complete-clubs-api-and-chelas-internet-application)
  - [Índice](#índice)
  - [Descrição da Estrutura da Aplicação](#descrição-da-estrutura-da-aplicação)
    - [Componente Servidor](#componente-servidor)
    - [Componente Cliente](#componente-cliente)
  - [Design de Armazenamento de Dados](#design-de-armazenamento-de-dados)
  - [Mapeamento de Documentos do ElasticSearch](#mapeamento-de-documentos-do-elasticsearch)
    - [Objetos](#objetos)
    - [Arrays](#arrays)
  - [Documentação da API do Servidor](#documentação-da-api-do-servidor)
  - [Instruções para Executar a Aplicação](#instruções-para-executar-a-aplicação)
    - [Correr a aplicação](#correr-a-aplicação)
    - [Usar dados de memória em vez do ElasticSearch](#usar-dados-de-memória-em-vez-do-elasticsearch)
    - [Correr os testes](#correr-os-testes)
  - [Conclusão](#conclusão)

## Descrição da Estrutura da Aplicação

### Componente Servidor
A estrutura do servidor é composta por várias camadas:
- **Servidor**: Configura a API e todas as rotas/endpoints possíveis e escuta as requisições.
- **Web API**: Wrapper da API contendo funções para cada rota/endpoint.
- **Serviços**: Processa os dados da rota e usa a camada de Modelo para armazená-los.
- **Dados (Modelo)**: Recebe dados e armazena-os em algum lugar.

Em baixo encontram-se exemplos de como podemos implementar estes componentes

- `foccacia-server.mjs`

```js
import foccaciaApiInit from './web/api/foccacia-web-api.mjs'
import foccaciaServicesInit from './services/foccacia-services.mjs';
import groupsDataInit from './data/memory/foccacia-data-mem.mjs'; 
import usersDataInit from './data/memory/foccacia-data-mem.mjs'; 
import teamsDataInit from './data/memory/fapi-teams-data.mjs';

const groupsData = groupsDataInit();
const usersData = usersDataInit();
const teamsData = teamsDataInit();
const foccaciaServices = foccaciaServicesInit(teamsData, usersData, groupsData);
groupsAPI = foccaciaApiInit(foccaciaServices);
usersAPI = foccaciaApiInit(foccaciaServices);
teamsAPI = foccaciaApiInit(foccaciaServices);

/* 
Implementação de funções para o que queremos que a API faça juntamente com o que é mostrado no site
Aqui está um exemplo:
*/

app.get('/groups', groupsAPI.getAllGroups); 

app.listen(PORT, () =>
    console.log(`FOCCACIA server listening on port ${PORT}! Use http://localhost:${PORT}/site/homepage to access the homepage of the website`),
);
```

- `foccacia-web-api.mjs`

```js
export default function init(foccaciaServices){

  if(!foccaciaServices) throw errors.INVALID_ARGUMENT('foccaciaServices');

  return {
    getAllGroups : processRequest(local_getAllGroups)
  };

  function processRequest(operation){
    return function (req, res, next){
      return operation(req, res, next)
      .catch(next);
    }
  }

  //...

  async function local_getAllGroups(req, res) {
    const groupsPromise = foccaciaServices.getAllGroups(req.userToken); 
    const groups = await groupsPromise;
    return res.json(groups);
  }

  //...
}
```

- `foccacia-services.mjs`

```js
export default function init(teamData, usersData, foccaciaData) {
  
  if(!teamData) throw errors.INVALID_ARGUMENT('teamData');

  if(!usersData) throw errors.INVALID_ARGUMENT('usersData')

  if(!foccaciaData) throw errors.INVALID_ARGUMENT('foccaciaData')

  return {
    getAllGroups
  }
  
  //...

  async function getAllGroups(userToken) {
    return getUserId(userToken)
      .then(userId => foccaciaData.getAllGroups(userId)); 
  }

  //...
}
```

- `foccacia-data-mem.mjs`

```js
export default function init(){

  return {
    getAllGroups
  };

  //...

  function getAllGroups(userId) {
    return Promise.resolve(GROUPS.filter(group => group.userId == userId));
  }

  //...
}
```

Como podemos ver, cada módulo utiliza o módulo que se encontra em baixo, desta forma criamos um modelo de **injeção de dependências**, uma vez que *injetamos* dependências de módulo para módulo.

### Componente Cliente
A estrutura do cliente é composta por:
- **Web UI**: Contém arquivos de apresentação para cada rota/endpoint.

Esta parte também usa o modelo de injeção de dependências
```js
const groupsData = groupsDataInit();
const usersData = usersDataInit();
const teamsData = teamsDataInit();
const foccaciaServices = foccaciaServicesInit(teamsData, usersData, groupsData);
foccaciaSite = foccaciaSiteInit(foccaciaServices);
groupsAPI = foccaciaApiInit(foccaciaServices);
usersAPI = foccaciaApiInit(foccaciaServices);
teamsAPI = foccaciaApiInit(foccaciaServices);
```

Também tem rotas separadas para apresentar o conteúdo da API ao utilizador, por exemplo:

- `foccacia-server.mjs`

```js
 //...

 app.get('/site/groups',  foccaciaSite.getAllGroups);

 //...
```

- `foccacia-web-site.mjs`

```js
export default function init(foccaciaServices){

  // Verify the dependencies:
  if(! foccaciaServices){
    throw errors.INVALID_ARGUMENT('foccaciaServices');
  }

  return {
    getAllGroups : processRequest(local_getAllGroups)
  };

  function processRequest(operation){
    return function (req, res, next){
      return operation(req, res, next)
      .catch(next);
    }
  }

  //...

  function local_getAllGroups(req, res) {
    const groupsPromise = foccaciaServices.getAllGroups(req.userToken);
    const isAuthenticated = req.session && req.session.token;
    return groupsPromise.then(groups => {
      res.render("groups-view", { groups, isAuthenticated }); // Renderizar a página com todos os grupos
    });
  } 

  //...
}
```
Na parte de `render`, fazemos render de ficheiros com a extensão `.hbs`. 

No código acima, temos uma linha que faz a renderização de um ficheiro com o nome `groups-view`, isto é uma chamada para o ficheiro `groups-view.hbs`.
Este ficheiro é composto da seguinte maneira:

```hbs
<section class="container p-2">
  {{> header}}

  {{> groups}}

  {{> newGroupForm}}

  {{> footer}}
</section>
```

Como podemos ver, para além de renderizar o ficheiro, ainda renderizamos estas *partes*, uma vez que assim facilita a identificação de erros e torna as coisas mas simples de se ver. Como exemplo, olhemos para o ficheiro com o nome `groups`:

```hbs
<section class="container p-2">
  <h2>List of Groups:</h2>
  <table class="table text-nowrap">
    <tr>
      <th>Name</th>
      <th>Description</th>
      <th></th>
      <th></th>
    </tr>
    {{#each groups}}
    <tr>
      <td>{{this.name}}</td>
      <td>{{this.description}}</td>
      <td>
        <a href="/site/groups/{{this.id}}" class="btn btn-primary">Edit or View Info</a>
      </td>
      <td>
        <form action="/site/groups/{{this.id}}/delete" method="POST">
          <input type="submit" value="Delete" class="btn btn-primary">
        </form>
      </td>
    </tr>
    {{/each}}
  </table>
</section>
```

Assim, são compostos os nossos ficheiros com a extensão `.hbs`

## Design de Armazenamento de Dados
O design de armazenamento de dados é feito no ElasticSearch, com os seguintes detalhes:
- **Índices**: `users`, `groups`, `teams`
- **Propriedades dos Documentos**: Cada índice possui propriedades específicas, como `name`, `description`, `userId`, `teams`, etc.
- **Relações entre Documentos**: Documentos de `groups` possuem uma relação com documentos de `teams` através de IDs de equipe.

Os dados das equipas são feitos através de requests a uma API externa chamada API-FOOTBALL. Depois de pesquisada a informação podemos adicionar esta ao ElasticSearch. A pesquisa é feita da seguinte maneira:

- `fapi-teams-data.mjs`

```js
export default function init(){
    return {
        getTeamByName
    }

    async function getTeamByName(name){
        try{
        const response = await fetch(`${baseURL}/teams?name=${name}`, requestOptions);
        const data = await response.json();
        const teamData = data.response[0];
        const seasonResponse = await fetch(`${baseURL}/teams/seasons?team=${teamData.team.id}`, requestOptions);
        const seasonData = await seasonResponse.json();
        let lastSeason = Math.max(...seasonData.response);
        if(lastSeason > lastSeason-3) lastSeason = lastSeason-3;
        const leagues = await getLeaguesByTeamId(teamData.team.id, lastSeason);
        const toReturn = {
            id: teamData.team.id,
            name: teamData.team.name,
            logo: teamData.team.logo,
            stadium: teamData.venue.name,
            leagues,
        };
        return toReturn;
        }
        catch (error) {
        console.error('Error fetching teams by name:', error);
        return []; 
        }
    }
}
```

Com o ElasticSearch, quando queremos fazer um pedido para retirar uma informação ou adicionar/atualizar/remover informações, usamos a seguinte função:

- `fetch-elastic.mjs`

```js
const URI_PREFIX='http://localhost:9200';

export async function fetchElastic(method, path, body=undefined){
    const options = {
        method: method,
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify(body)
    };

    const response = await fetch(URI_PREFIX + path, options);
    return await response.json();
}
```

Com isto podemos escolher o que queremos fazer através do parametro `method`, para onde queremos fazer esse pedido atráves de `path`. O parametro `body` é para quando adicionamos informação ao ElasticSearch ou queremos atualizar informações de algo existente.

Para se adicionar uma equipa que pesquisamos às equipas disponíveis na API, fazemos da seguinte maneira:

- `foccacia-data-elastic.mjs`

```js
function addTeamtoArray(team) {
    return fetchElastic('POST', '/teams/_doc', team)
        .then(resp => ({ id: resp._id, ...team })); // Retorna a equipa adicionada
}
```
Depois de adicionada a equipa ao array das equipa disponíveis na API, podemos adicionar essa equipa ao array das equipas de um grupo e, caso essa equipa exista no grupo, podemos removê-la do grupo. Fazemos isto da seguinte maneira:

- `foccacia-data-elastic.mjs`

```js
function addTeamToGroup(groupId, teamData) {
    return Promise.all([
        getGroup(groupId), // Obter o grupo
        fetchElastic('GET', `/teams/_search/`) // Obter todas as equipas
    ])
        .then(([group, elasticTeam]) => {
            const teamHit = elasticTeam.hits.hits.find(hit => 
                hit._source.name.toLowerCase() == teamData.name.toLowerCase()); // Encontrar a equipa pelo nome
            if (!teamHit) {
                return Promise.reject(errors.TEAM_NOT_FOUND(teamData.name)); // Erro se a equipa não for encontrada
            }
            const elasticTeamData = teamHit._source;
            if (group.teams.some(t => t.name == teamData.name)) {
                return Promise.reject(errors.DUPLICATE_TEAM(teamData.id)); // Erro se a equipa já existir no grupo
            }
            group.teams.push({
                id: elasticTeamData.id,
                name: elasticTeamData.name,
                stadium: elasticTeamData.stadium,
                leagues: elasticTeamData.leagues
            });
            console.log(group.teams)
            return fetchElastic('POST', `/groups/_update/${groupId}?refresh=true`, { doc: { teams: group.teams } }) // Atualizar o grupo
            .then(() => group.teams); // Retornar as equipas do grupo
        })        
}

function removeTeamFromGroup(groupId, teamId) {
    return getGroup(groupId)
        .then(group => {
            const teamIndex = group.teams.findIndex(team => team.id === teamId); // Encontrar o índice da equipa
            if (teamIndex === -1) {
                return Promise.reject(errors.TEAM_NOT_FOUND(teamId)); // Erro se a equipa não for encontrada
            }
            group.teams.splice(teamIndex, 1); // Remover a equipa do grupo
            return fetchElastic('POST', `/groups/_update/${groupId}?refresh=true`, { doc: { teams: group.teams } }); // Atualizar o grupo
        });
}
```

A informação dos grupos está implementada como o seguinte exemplo:

- `foccacia-data-elastic.mjs`

```js
function aGroupFromElastic(elasticGroup) {
    return { id: elasticGroup._id, ...elasticGroup._source };
}

function getAllGroups(userId) {
    const filter = { query: { match: { userId: userId } } };
    return fetchElastic('POST', '/groups/_search', filter)
        .then(resp => {
            if (resp.error) {
                console.error(resp.error.reason);
                return [];
            }
            return resp.hits.hits;
        })
        .then(groups => groups.map(aGroupFromElastic)); // Mapear os grupos para o formato correto
}

function updateGroup(groupId, updatedData) {
    return getGroup(groupId) 
        .then(group => {
            const teams = group.teams || [];
            const dataToUpdate = {
                ...updatedData,
                teams: updatedData.teams || teams // Atualizar a lista de equipas, se fornecido
            };
            return fetchElastic('PUT', `/groups/_doc/${groupId}?refresh=true`, dataToUpdate);
        })
        .then(aGroupFromElastic); // Retornar o grupo atualizado
}
```

A informação dos utilizadores está implementada como o seguinte exemplo:

- `foccacia-data-elastic.mjs`

```js
function getAllUsers() {
  const query = { query: { match_all: {} } };
  return fetchElastic('POST', '/users/_search', query)
    .then(resp => {
      if (resp.error) {
        console.error(resp.error.reason);
        return [];
      }
      return resp.hits.hits.map(hit => hit._source);
  });
}

function addUser(username, password) {
  return getUserByName(username)
    .then(user => {
        if(user){
          return Promise.reject(errors.DUPLICATE_USER(username))
        }
        const newUser = {
          token: crypto.randomUUID(),
          name: username,
          password
        };
        return fetchElastic('POST', '/users/_doc?refresh=true', newUser)
          .then(resp => ({ id: resp._id, ...newUser }));
      });
}
```

## Mapeamento de Documentos do ElasticSearch
Os documentos do ElasticSearch são mapeados para objetos do modelo da aplicação web da seguinte forma:
- **Utilizadores**: Documentos no índice `users` são mapeados para objetos de utilizador na aplicação.
- **Grupos**: Documentos no índice `groups` são mapeados para objetos de grupo na aplicação.
- **Equipes**: Documentos no índice `teams` são mapeados para objetos de equipe na aplicação.

### Objetos

Para os utilizadores nós criamos objetos como o seguinte:

```js
{ 
  name: 'jsaldanha', 
  token: 'b0506867-77c3-4142-9437-1f627deebd67', 
  password: '123456' 
}
```

Para armazenarmos este objeto no ElasticSearch, como podemos ver anteriormente na menção do ficheiro `fetch-elastic.mjs`, temos de converter o objeto em um objeto JSON.

Para retirarmos informações do ElasticSearch usamos um search ao index que queremos. Como exemplo, usaremos o index do utilizadores `users`. Na função `getAllUsers` recebemos uma lista de todos os utilizadores que se encontram no ElasticSearch. Ora o resultado que sai de `resp.hit.hits` é o seguinte:

```
[
  {
    _index: 'users',
    _id: 'zCCcJ5QBW1BrwZFKBWPT',
    _score: 1,
    _source: {
      token: '11fdfcdd-032f-4f9c-b28b-6c1d1ff85813',
      name: 'jsaldanha',
      password: '123456'
    }
  },
  {
    _index: 'users',
    _id: 'j3PpMZQBTWodDljGOFi6',
    _score: 1,
    _source: {
      token: 'dc2abcb5-e888-4f57-a2d1-d848a23feac9',
      name: 'rpinto',
      password: '1234587'
    }
  }
]
```

Para a aplicação, com a função de receber todos os utilizadores não precisamos de todos os dados, só mesmo os importantes. Para este caso o que é importante é o que está dentro de `_source`. Para isso na função do `getAllUsers` fazemos:

```js
resp.hits.hits.map(hit => hit._source)
```

Com isto ficamos só com os resultados de todos os utilzadores mas só o que se encontra dentro de `_score`:
```
[
  {
    token: '11fdfcdd-032f-4f9c-b28b-6c1d1ff85813',
    name: 'jsaldanha',
    password: '123456'
  },
  {
    token: 'dc2abcb5-e888-4f57-a2d1-d848a23feac9',
    name: 'rpinto',
    password: '1234587'
  }
]
```

Também usamos esta lógica para as outra funções que se encontram no ficheiro `foccacia-data-elastic.mjs`, quer sejam elas para os utilizadores, grupos ou equipas.

### Arrays

Para mencionar que para os grupos, já que estes podem returnar arrays, em vez de acedermos a `_source` acedemos a `hits`, uma vez que o nosso resultado encontra-se aí:

```
[
  {
    _index: 'groups',
    _id: 'zSCiJ5QBW1BrwZFK12Pa',
    _score: 0.2876821,
    _source: {
      name: 'TOP TEAMS OF PORTUGAL AND ENGLAND',
      description: ' group test',
      userId: 'zCCcJ5QBW1BrwZFKBWPT',
      teams: [
        {
          leagues: [
            { name: 'Super Cup', season: 2021, id: 550 },
            { name: 'UEFA Champions League', season: 2021, id: 2 },
            { name: 'Taça da Liga', season: 2021, id: 97 },
            { name: 'Primeira Liga', season: 2021, id: 94 },
            { name: 'Friendlies Clubs', season: 2021, id: 667 },
            { name: 'Taça de Portugal', season: 2021, id: 96 }
            ],
          name: 'Sporting CP',
          stadium: 'Estádio José Alvalade',
          id: 228
        },
        {
          leagues: [
            { name: 'UEFA Champions League', season: 2022, id: 2 },
            { name: 'Friendlies Clubs', season: 2022, id: 667 },
            { name: 'Primeira Liga', season: 2022, id: 94 },
            { name: 'Taça de Portugal', season: 2022, id: 96 },
            { name: 'Taça da Liga', season: 2022, id: 97 }
            ],
          name: 'Benfica',
          stadium: 'Estádio do Sport Lisboa e Benfica (da Luz)',
          id: 211
        },
        {
          leagues: [
            { name: 'UEFA Champions League', season: 2021, id: 2 },
            { name: 'Premier League', season: 2021, id: 39 },
            { name: 'League Cup', season: 2021, id: 48 },
            { name: 'FA Cup', season: 2021, id: 45 },
            { name: 'Friendlies Clubs', season: 2021, id: 667 }
            ],
          name: 'Liverpool',
          stadium: 'Anfield',
          id: 40
        }
      ]
    }
  }
]
```

## Documentação da API do Servidor
A documentação da API do servidor inclui todas as rotas e endpoints disponíveis, com detalhes sobre os parâmetros e respostas esperadas. Em baixo encontra-se uma versão comprimida das rotas disponíveis na API.

Rotas:

- /users/signup
  - Método: POST
  - Descrição: Cria um novo utilizador
  - Parâmetro: Nenhum
  - Requiremento:
    - Token no header 'Authorization'
    - Corpo com um nome e password do novo utilizador

- /users/login
  - Método: GET
  - Descrição: Obtém informações do utilizador autenticado
  - Parâmetro: Nenhum
  - Requiremento:
    - Token no header 'Authorization'

- /team/search
  - Método: POST
  - Descrição: Pesquisa equipas pelo nome usando a API externa
  - Parâmetro: Nenhum
  - Requiremento:
    - Corpo com o nome da equipa a pesquisar

- /teams/store
  - Método: POST
  - Descrição: Armazena as equipes pesquisadas em cache na memória local
  - Parâmetro: Nenhum
  - Requiremento:
    - Corpo com o nome da equipa a armazenar

- /teams
  - Método: GET
  - Descrição: Obtém todas as equipes armazenadas
  - Parâmetro: Nenhum

- /groups
  - Método: GET
  - Descrição: Lista todos os grupos do utilizador autenticado
  - Parâmetro: Nenhum
  - Requiremento:
    - Token no header 'Authorization'

  - Método: POST
  - Descrição: Adiciona um novo grupo para o utilizador autenticado
  - Parâmetro: Nenhum
  - Requiremento:
    - Token no header 'Authorization'
    - Corpo com o nome e descrição do novo grupo

- /groups/{groupId}
  - Método: GET
  - Descrição: Obtém detalhes de um grupo específico
  - Parâmetro:
    - nome: groupId
      - onde: Caminho da Requisição
  - Requiremento:
    - Token no header 'Authorization'

  - Método: PUT
  - Descrição: Atualiza os detalhes de um grupo específico
  - Parâmetro: 
    - nome: groupId
      - onde: Caminho da Requisição
  - Requiremento:
    - Token no header 'Authorization'
    - Corpo com o nove nome e nova descrição para o grupo

  - Método: DELETE
  - Descrição: Remove um grupo pelo seu ID
  - Parâmetro: 
    - nome: groupId
      - onde: Caminho da Requisição
  - Requiremento:
    - Token no header 'Authorization'

- /groups/{groupId}/teams
  - Método: POST
  - Descrição: Adiciona uma equipa a um grupo
  - Parâmetro: 
    - nome: groupId
      - onde: Caminho da Requisição
  - Requiremento:
    - Token no header 'Authorization'

- /groups/{groupId}/teams/{teamId}
  - Método: DELETE
  - Descrição: Remove uma equipa de um grupo
  - Parâmetro: 
    - nome: groupId
      - onde: Caminho da Requisição
    - nome: teamId
      - onde: Caminho da requisição

## Instruções para Executar a Aplicação
Para executar a aplicação, siga os passos abaixo:

Para poder correr a aplicação é necessário ter o seguinte instalado na sua máquina:
- git
- npm
- NodeJS
- ElasticSearch
  - No ficheiro `elasticsearch.yml` que se encontra na pasta `config` no ElasticSearch, adicione as seguintes linhas:
    ```
    xpack.security.enabled: false
    xpack.security.enrollment.enabled: false
    ```
- A sua chave da API-FOOTBALL

### Correr a aplicação

Para correr a aplicação, vamos precisar de dois terminais, um para correr a aplicação, outro para correr o ElasticSearch.
No primeiro faça os seguintes comandos:

```sh
git clone https://github.com/isel-leic-ipw/isel-leic-ipw-pi-2425i-foccacia-leic2425i-ipw32d-g12
cd isel-leic-ipw-pi-2425i-foccacia-leic2425i-ipw32d-g12
npm install
npm run runServer
```
O comando `npm run runServer` é o comando que inicia o server.

O segundo terminal tem de ser aberto dentro da pasta do ElasticSearch que fez download e extraiu. Dentro desse terminal, faça o seguinte comando:
```sh
.\bin\elasticsearch.bat
```

Em alternativa: pode fazer download do código, extrair a pasta que contem o código, abrir no Visual Studio Code e dar run no ficheiro `foccacia-server.mjs`. Ainda é possivel que tenha de fazer o comando `npm install`

Depois do server estar aberto, é só carregar no link que aparece na consola/terminal (ctrl+click (Microsoft) ou command+click (Apple)), e irá abrir no seu browser predefinido o site que foi criado por nós.

Crie ainda um ficheiro `.env` que deve conter as variáveis `API_KEY` e `PORT`, exemplo:

```
API_KEY = 458n6q9g18q014qr3lm74b5h6q147480
PORT = 8080
```

Utilizamos este método para não expor keys privadas.

Para além da criação do ficheiro `.env`, também será necessário por a `API_KEY` no ficheiro `.http`, caso queira mexer só com a API

Se quiser mexer no site na sua totalidade não se esqueça de criar uma conta.

### Usar dados de memória em vez do ElasticSearch

Caso não queira, usar o ElasticSearch e usar os dados em memória criados para testar a aplicação, terá de ir ao ficheiro `foccacia-server.mjs` e alterar o seguinte,

```js
// Importa os módulos necessários para injeção de dependências
import foccaciaApiInit from './web/api/foccacia-web-api.mjs'
import foccaciaServicesInit from './services/foccacia-services.mjs';
//import groupsDataInit from './data/memory/foccacia-data-mem.mjs'; // Fonte de dados baseada em memória para grupos
import groupsDataInit from './data/elastic/foccacia-data-elastic.mjs'; // Alternativa: Elasticsearch
//import usersDataInit from './data/memory/foccacia-data-mem.mjs'; // Fonte de dados baseada em memória para utilizadores
import usersDataInit from './data/elastic/foccacia-data-elastic.mjs'; // Alternativa: Elasticsearch
import teamsDataInit from './data/memory/fapi-teams-data.mjs';
```

para

```js
// Importa os módulos necessários para injeção de dependências
import foccaciaApiInit from './web/api/foccacia-web-api.mjs'
import foccaciaServicesInit from './services/foccacia-services.mjs';
import groupsDataInit from './data/memory/foccacia-data-mem.mjs'; // Fonte de dados baseada em memória para grupos
//import groupsDataInit from './data/elastic/foccacia-data-elastic.mjs'; // Alternativa: Elasticsearch
import usersDataInit from './data/memory/foccacia-data-mem.mjs'; // Fonte de dados baseada em memória para utilizadores
//import usersDataInit from './data/elastic/foccacia-data-elastic.mjs'; // Alternativa: Elasticsearch
import teamsDataInit from './data/memory/fapi-teams-data.mjs';
```

Se quiser voltar para o ElasticSearch é só voltar a trocar.

### Correr os testes

Para correr os testes, abra o terminal e faça os seguintes comandos:

```sh
git clone https://github.com/isel-leic-ipw/isel-leic-ipw-pi-2425i-foccacia-leic2425i-ipw32d-g12
cd isel-leic-ipw-pi-2425i-foccacia-leic2425i-ipw32d-g12
npm run testName
```
Em que testName pode assumir três valores:
  - testServices, para testar o ficheiro `foccacia-services.mjs`
  - testDataMem, para testar o ficheiro `foccacia-data-mem.mjs`
  - testTeamsData, para testar o ficheiro `fapi-teams-data.mjs`

Cerifique-se que usa os dados em memória e não os dados do ElasticSearch, veja [como mudar](#usar-dados-de-memória-em-vez-do-elasticsearch).

Em alternativa: Caso não queira fazer a primeira linha de comando, basta ir ao repositório e fazer download da versão mais recente do código e trabalhar a partir daí, abrindo o terminal dentro da pasta onde se encontra o código

## Conclusão

Com este trabalho conseguimos atingir os objetivos ao conseguirmos construir uma aplicação funcional. O uso do ElasticSearch, Express e Handlebars resultou em um sistema otimizado, que inclui funcionalidades práticas e fáceis de entender e ainda um design prático e intuítivo. Para além disto, através deste trabalho, com o que aprendemos sobre API's, autenticação de utilizadores e gerenciamento de dados, conseguimos obter bases fortes para trabalhos futuros.