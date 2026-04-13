import FS from 'fs'; // Importa o módulo 'fs' (File System) para trabalhar com ficheiros
import dotenv from 'dotenv'; // Importa o módulo 'dotenv' para carregar variáveis de ambiente a partir de um ficheiro .env
import { fileURLToPath } from 'url'; // Importa funções do módulo 'url' para manipular o caminho do ficheiro atual
import { dirname } from 'path'; // Importa a função 'dirname' do módulo 'path' para obter o diretório de um ficheiro
import path from 'path'; // Importa todo o módulo 'path', útil para manipulação de caminhos de ficheiros

// URL base da API de desporto para obter informações de equipas, com um parâmetro 'id'
const API_URL = 'https://v3.football.api-sports.io/teams?id='
const INPUT = 'TeamIDS.json'; // Nome do ficheiro de input que contém os IDs das equipas
const OUTPUT = 'outputPromiseVer.json'; // Nome do ficheiro de output onde os dados processados serão gravados

// Converte o URL do ficheiro em um caminho de sistema de ficheiros usando 'fileURLToPath' e 'dirname'
// 'file_name' contém o caminho completo do ficheiro atual
const file_name = fileURLToPath(import.meta.url);
const dir_name = dirname(file_name); //'__dirname' contém o diretório do ficheiro atual, obtido a partir de '__filename'
const CACHE = path.join(dir_name, 'PromiseVer.cache.json'); // Define o caminho do ficheiro de cache para armazenar respostas da API

dotenv.config(); // Carrega as variáveis de ambiente a partir do arquivo '.env'

// Declara variáveis para saber o limite diário e por minuto de requisições à API
let rateLimitDaily;
let rateLimitMinute;


// Define as opções da requisição, incluindo o método HTTP 'GET' e o cabeçalho de autenticação 'x-rapidapi-key'
// O cabeçalho usa a chave de API carregada a partir das variáveis de ambiente ('process.env.API_KEY')
var requestOptions = {
  method: 'GET',
  headers : {"x-rapidapi-key": process.env.API_KEY},
}

// Função para ler o cache do ficheiro de cache
function readCache() {
  //Verifica se exista um ficheiro de cache
  if (FS.existsSync(CACHE)) {

    //Caso exista, lê esse ficheiro
    const cacheContent = FS.readFileSync(CACHE, 'utf-8');

    if (cacheContent.trim() === '') {
      return {}; // Se o ficheiro estiver vazio, retorna um objeto vazio
    }

    //Retorna o conteúdo da cache convertido para um objeto JavaScript
    return JSON.parse(cacheContent);
  }
  return {}; // Retorna um objeto vazio se o ficheiro de cache não existir
}

// Variável para garantir que a escrita na cache aconteça em sequência
let lockCache = Promise.resolve(); 


// Função para atualizar o cache com os dados de uma equipa específica
function updateCache(teamID, data) {
  // Atualiza o cache em uma cadeia de promessas para evitar acessos simultâneos
  lockCache = lockCache.then(() => {
    // Primeira parte da cadeia: ler a cache atual e atualizar os dados da equipa específica
    return new Promise((resolve, reject) => {
      try {
        const cache = readCache(); // Lê a cache existente
        cache[teamID] = data;      // Atualiza os dados da equipa na cache
        resolve(cache);            // Resolve a promessa com a cache atualizada
      } catch (error) {
        reject(error);             // Rejeita a promessa em caso de erro
      }
    });
  })

  .then((cache) => {
    // Segunda parte da cadeia: escreve a cache atualizada de volta no ficheiro
    return new Promise((resolve, reject) => {
      FS.writeFile(CACHE, JSON.stringify(cache, null, 2), (error) => {
        if (error) {
          reject(error); // Rejeita em caso de erro ao escrever no ficheiro
        } else {
          console.log(`Cache written for team ${teamID}`);
          resolve(); // Resolve a promessa quando a escrita for bem-sucedida
        }
      });
    });
  })

  .catch(error => {
    // Captura qualquer erro durante o processo de atualização do cache
    console.log(`Erro ao atualizar cache: ${error}`);
  });

  // Retorna a promessa associada á cache, garantindo que atualizações futuras sejam encadeadas corretamente
  return lockCache;
}

// Função para obter as informações de uma equipa
function getTeamInfo(teamID) {
  const c = readCache(); // Lê a cache local

  // Caso a cache contenha os dados da equipa solicitada, retorna uma Promise "resolvida" com os dados da cache
  if (c[teamID]) {
    return Promise.resolve(c[teamID]);
  }

  // Caso contrário, faz uma requisição para a API para obter as informações da equipa
  return fetch(`${API_URL}${teamID}`, requestOptions)
    .then(response => {

      //Atualizar as variáveis rateLimitMinute e rateLimitDaily, com os respetivos headers da resposta
      rateLimitMinute = response.headers.get('X-RateLimit-Remaining');
      rateLimitDaily = response.headers.get('X-RateLimit-requests-remaining');
      
      // Se o limite de requisições por minuto for 0, aguarda 1 minuto antes de tentar novamente
      if (rateLimitMinute <= 0) {
        console.log('A esperar 1 minuto antes de fazer outra requisição.');
        return new Promise((resolve) => {
          setTimeout(() => {
            // Após 1 minuto, tenta novamente obter as informações da equipa
            resolve(getTeamInfo(teamID)); 
          }, 60000); // 60000ms == 1 minuto
        });
      };

      // Se o limite de requisições diárias for atingido, exibe uma mensagem de erro e encerra o processo
      if (rateLimitDaily <= 0) {
        console.error('Limite diário atingido. Por favor espere 24 horas antes de fazer outro pedido.');
        process.exit(1);
      }

      console.log('Requisições diária restantes:', rateLimitDaily);
      console.log('Requisições por minuto restantes:', rateLimitMinute);

      // Converte a resposta da API para JSON e atualiza a cache com os dados recebidos
      return response.json().then(data => {
        updateCache(teamID, data); // Escreve os dados atualizados na cache
        return data;
      });
    })
    // Captura e trata qualquer erro durante a requisição e exibe a mensagem de erro
    .catch(error => console.log('Erro:', error));
}


export function showTeamInfoinOutputPromise(){
// Lê o ficheiro de input que contém os IDs das equipas
FS.readFile(INPUT, 'utf8', (err, data) => {
  if (err) if (err) {
    console.error('Erro ao ler o ficheiro de input', err);
    process.exit(1); // Encerra o programa em caso de erro na leitura do ficheiro
  };
  
  // Converte a string JSON lida do arquivo (data) em um objeto JavaScript
  // e acessa o array de IDs das equipas associado à chave 'teams-ids'.
  const teamIds = JSON.parse(data)['teams-ids'];

  // Mapeia cada ID das equipas para uma chamada à função `getTeamInfo`, que retorna uma Promise
  const requests = teamIds.map(id => getTeamInfo(id));
  // Aguarda todas as requisições das equipas serem concluídas
  Promise.all(requests).then(responses => {
    // Mapeia as respostas e extrai as informações relevantes de cada equipa
    const teams = responses.map((obj, idx) => {
      // Verifica se a resposta contém dados válidos
      if (obj && obj.response && obj.response.length > 0) {
        const teamData = obj.response[0] // Extrai os dados da equipa
        return { // Retorna um objeto contendo as informações principais da equipa:
          id: teamData.team.id, // id: o identificador único da equipa
          name: teamData.team.name, // name: o nome da equipa
          stadium: teamData.venue.name // stadium: o nome do estádio onde a equipa joga
        }    
      } else {
        // Exibe uma mensagem de aviso se não houver dados para a equipa solicitada, returnando 'null' para essa equipa
        console.log(`Nenhum dado disponível para a equipa: ${teamIds[idx]}`);
        return null;
      }
      
    });
    // Filtra as equipas para remover aquelas que retornaram 'null'
    const filteredTeams = teams.filter(team => team !== null);

    // Escreve os dados filtrados no ficheiro de output
    FS.writeFile(OUTPUT, JSON.stringify({teams: filteredTeams}, null, 2), err => {
      if(err) throw err; // Lança um erro se ocorrer um problema ao escrever no ficheiro
      console.log(`Dados escrito no ficheiro: ${OUTPUT}`); // Confirmação que os dados foram escritos no ficheiro
    });
    return teams;
  })
  // Captura qualquer erro ao obter os dados das equipas
  .catch(error => console.log('Erro ao buscar os dados para a equipa:', error));
  
});}

showTeamInfoinOutputPromise();