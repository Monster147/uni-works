import * as dotenv from 'dotenv';
import fetch from 'node-fetch';
import { errors } from '../../common/errors.mjs';
dotenv.config();

const apiKey = process.env.API_KEY; // Sports API key
const baseURL = 'https://v3.football.api-sports.io';

const requestOptions = {
    method: 'GET', // Método de requisição
    headers: { "x-rapidapi-key": apiKey },
};

const searchedTeams = [];

export default function init(){
  return {
    getTeamByName,
    getLeaguesByTeamId
  }
//Procura informações de uma equipa específica pelo nome.
async function getTeamByName(name){
    try{
      const team = searchedTeams.find((team) => team.name == name);
      console.log('Team in cache:', team);
      if(team) return team;
      // Faz a requisição para obter informações da equipa pelo nome fornecido
      const response = await fetch(`${baseURL}/teams?name=${name}`, requestOptions);
      const data = await response.json();
      const teamData = data.response[0]; // Seleciona o primeiro resultado encontrado
      // Faz uma nova requisição para obter as temporadas em que a equipa participou
      if(teamData === undefined) return Promise.reject(errors.TEAM_NOT_FOUND(name));
      const seasonResponse = await fetch(`${baseURL}/teams/seasons?team=${teamData.team.id}`, requestOptions);
      const seasonData = await seasonResponse.json();

      // Calcula a temporada mais recente disponível, limitando a até duas temporadas atrás
      let lastSeason = Math.max(...seasonData.response);
      if(lastSeason > lastSeason-3) lastSeason = lastSeason-3;

      // Obtém as ligas em que a equipa participou na temporada calculada
      const leagues = await getLeaguesByTeamId(teamData.team.id, lastSeason);

      // Cria o objeto final com as informações detalhadas da equipa
      const toReturn = {
        id: teamData.team.id,
        name: teamData.team.name,
        logo: teamData.team.logo,
        stadium: teamData.venue.name,
        leagues,
      };
      searchedTeams.push(toReturn);
      return toReturn;
    }
    catch (error) {
      // Trata qualquer erro ocorrido durante a execução e imprime na consola
      console.error('Error fetching teams by name:', error);
      return []; // Retorna um array vazio em caso de erro
    }
}



// Obtém as ligas em que uma equipa específica participou numa determinada temporada.
async function getLeaguesByTeamId(teamID, season){
    try {
      // Faz a requisição para obter as ligas em que a equipa participou na temporada especificada
      const response = await fetch(`${baseURL}/leagues?team=${teamID}&season=${season}`, requestOptions);
      const data = await response.json();
      // Cria um array para armazenar as ligas encontradas
      const leagues = [];
      data.response.forEach((leagueData) => {
        // Adiciona cada liga encontrada ao array de ligas
        leagues.push({
          id: leagueData.league.id,
          name: leagueData.league.name,
          season: season,
        })
      });
      return leagues; // Retorna a lista de ligas
    } catch (error) {
      // Trata qualquer erro ocorrido durante a execução e imprime na consola
      console.error('Error fetching leagues by team ID:', error);
      return []; // Retorna um array vazio em caso de erro
    }
}
}