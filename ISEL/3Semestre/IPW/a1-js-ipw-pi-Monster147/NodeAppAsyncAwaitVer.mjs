import FS from 'fs'; // Importa o módulo 'fs' para operações de sistema de arquivos
import dotenv from 'dotenv'; // Importa o módulo 'dotenv' para carregar variáveis de ambiente de um arquivo .env
import { fileURLToPath } from 'url'; // Importa a função para converter URLs de arquivo em caminhos de arquivo
import { dirname } from 'path'; // Importa a função para obter o diretório de um caminho
import path from 'path'; // Importa o módulo 'path' para manipulação de caminhos de arquivos

// Define a URL base da API para obter informações de equipes
const API_URL = 'https://v3.football.api-sports.io/teams?id=';
const INPUT = 'TeamIDS.json'; // Nome do arquivo de entrada que contém os IDs das equipes
const OUTPUT = 'outputAsyncAwaitVer.json'; // Nome do arquivo de saída onde os dados processados serão armazenados
const __filename = fileURLToPath(import.meta.url); // Obtém o nome do arquivo atual
const __dirname = dirname(__filename); // Obtém o diretório do arquivo atual
const CACHE = path.join(__dirname, 'AsyncAwaitVer.cache.json'); // Define o caminho do arquivo de cache

// Carrega as variáveis de ambiente do arquivo .env
dotenv.config();

let rateLimitDaily; // Variável para armazenar o limite de requisições diárias
let rateLimitMinute; // Variável para armazenar o limite de requisições por minuto

// Opções para a requisição HTTP
const requestOptions = {
  method: 'GET', // Método da requisição
  headers: { "x-rapidapi-key": process.env.API_KEY }, // Cabeçalhos com a chave da API
};

let lockCache = Promise.resolve(); // Inicializa uma promessa resolvida para controle de cache

// Função assíncrona para ler o cache com verificação de conteúdo
async function readCache() {
  try {
    // Verifica se o arquivo de cache existe
    if (FS.existsSync(CACHE)) {
      const cacheContent = FS.readFileSync(CACHE, 'utf-8'); // Lê o conteúdo do arquivo de cache
      if (cacheContent.trim() === '') {
        return {}; // Retorna um objeto vazio se o arquivo estiver vazio
      }

      // Tenta fazer parse do conteúdo do arquivo de cache
      try {
        return JSON.parse(cacheContent); // Retorna o conteúdo do cache como objeto JavaScript
      } catch (error) {
        console.error('Erro ao fazer parse do ficheiro de cache. Ficheiro corrompido.'); // Mensagem de erro se o parse falhar
        return {}; // Retorna um objeto vazio em caso de corrupção do arquivo
      }
    }
    return {}; // Retorna um objeto vazio se o arquivo de cache não existir
  } catch (error) {
    console.error('Erro ao ler o ficheiro de cache:', error); // Captura e exibe erro ao ler o arquivo
    return {}; // Retorna um objeto vazio em caso de erro
  }
}

// Função assíncrona para atualizar o cache com os dados de uma equipe específica
async function updateCache(teamID, data) {
  // Garante que apenas uma operação de cache ocorra por vez
  lockCache = lockCache.then(async () => {
    try {
      const cache = await readCache(); // Lê o cache existente

      // Atualiza a cache com os novos dados da equipa
      cache[teamID] = data;

      // Escreve a cache atualizada no arquivo
      await new Promise((resolve, reject) => {
        FS.writeFile(CACHE, JSON.stringify(cache, null, 2), (error) => {
          if (error) {
            reject(error); // Rejeita em caso de erro ao escrever no arquivo
          } else {
            console.log(`Cache atualizado para a equipa ${teamID}`); // Mensagem de sucesso
            resolve(); // Resolve a promessa quando a escrita for bem-sucedida
          }
        });
      });
    } catch (error) {
      console.error(`Erro ao atualizar o cache para a equipa ${teamID}:`, error); // Captura e exibe erro ao atualizar a cache
    }
  });

  // Retorna a Promise `lockCache` para garantir que atualizações futuras sejam encadeadas corretamente
  return lockCache;
}

// Função assíncrona para obter as informações de uma equipe
async function getTeamInfo(teamID) {
  const cache = await readCache(); // Lê o cache local

  // Verifica se os dados da equipa já estão na cache
  if (cache[teamID]) {
    return cache[teamID]; // Retorna os dados da cache se existirem
  }

  try {
    // Faz uma requisição à API para obter as informações da equipa
    const response = await fetch(`${API_URL}${teamID}`, requestOptions);

    // Atualiza as variáveis de limite de requisições com os cabeçalhos da resposta
    rateLimitMinute = response.headers.get('X-RateLimit-Remaining'); 
    rateLimitDaily = response.headers.get('X-RateLimit-requests-remaining');

    // Se o limite de requisições por minuto for 0, aguarda 1 minuto antes de tentar novamente
    if (rateLimitMinute == 0) {
      console.log('A esperar 1 minuto antes de fazer outra requisição.'); // Mensagem de espera
      await new Promise(resolve => setTimeout(resolve, 60000)); // Aguarda 1 minuto
      return getTeamInfo(teamID); // Tenta novamente após o intervalo
    }

    // Se o limite diário for atingido, exibe uma mensagem e encerra o processo
    if (rateLimitDaily == 0) {
      console.error('Limite diário atingido. Por favor espere 24 horas antes de fazer outro pedido.'); // Mensagem de erro
      process.exit(1); // Encerra o processo
    }

    // Exibe o número de requisições restantes
    console.log('Requisições diárias restantes:', rateLimitDaily);
    console.log('Requisições por minuto restantes:', rateLimitMinute);

    // Converte a resposta da API para JSON
    const data = await response.json();

    // Atualiza a cache com os dados recebidos e retorna os dados
    await updateCache(teamID, data);
    return data;
  } catch (error) {
    console.log('Erro ao obter informações da equipa:', error); // Captura e exibe erro ao obter informações da equipa
  }
}

// Função assíncrona para mostrar as informações das equipas no arquivo de output
export async function showTeamInfoinOutputAsyncAwait() {
  try {
    // Lê o arquivo de entrada que contém os IDs das equipes
    const data = await FS.promises.readFile(INPUT, 'utf8');
    const teamIds = JSON.parse(data)['teams-ids']; // Faz parse do JSON e obtém os IDs das equipas
    const requests = teamIds.map(id => getTeamInfo(id)); // Cria uma lista de promessas para obter as informações das equipas
    const responses = await Promise.all(requests); // Espera que todas as promessas sejam resolvidas
    const teams = responses.map((obj, idx) => {
      if (obj && obj.response && obj.response.length > 0) {
        const teamData = obj.response[0]; // Acessa os dados da equipe
        return {
          id: teamData.team.id, // id: o identificador único da equipa
          name: teamData.team.name, // name: o nome da equipa
          stadium: teamData.venue.name, // stadium: o nome do estádio onde a equipa joga
        };
      } else {
        console.log(`Nenhum dado disponível para a equipa: ${teamIds[idx]}`); // Mensagem de erro se não houver dados
        return null; // Retorna null se não houver dados
      }
    });

    const filteredTeams = teams.filter(team => team !== null); // Filtra equipes válidas (não null)

    // Escreve os dados filtrados no arquivo de saída
    await FS.promises.writeFile(OUTPUT, JSON.stringify({ teams: filteredTeams }, null, 2));

    console.log(`Dados escrito no ficheiro: ${OUTPUT}:`); // Mensagem de sucesso
    return teams;
  } catch (error) {
    console.log('Erro ao buscar os dados para a equipa:', error); // Captura e exibe erro ao buscar dados
  }
  
}

// Chama a função para mostrar as informações das equipes no arquivo de saída
showTeamInfoinOutputAsyncAwait();
