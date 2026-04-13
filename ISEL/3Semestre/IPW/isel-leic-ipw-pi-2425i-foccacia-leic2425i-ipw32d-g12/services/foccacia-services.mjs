//import * as foccaciaData from '../data/memory/foccacia-data-mem.mjs'; // Importação das funções relacionadas com dados de grupos e equipas.
//import * as usersData from '../data/memory/foccacia-data-mem.mjs'; // Importação das funções relacionadas com utilizadores.
//import * as teamsData from '../data/memory/fapi-teams-data.mjs'; // Importação das funções que acedem a dados de equipas de uma API externa.
import { errors } from '../common/errors.mjs'; // Importação de definições de erros comuns.

export default function init(teamData, usersData, foccaciaData) {
  
  if(!teamData) throw errors.INVALID_ARGUMENT('teamData');

  if(!usersData) throw errors.INVALID_ARGUMENT('usersData')

  if(!foccaciaData) throw errors.INVALID_ARGUMENT('foccaciaData')

  return {
        getAllGroups,
        getGroup,
        addGroup,
        updateGroup,
        deleteGroup,
        addTeamFromAPItoTeams,
        addTeamToGroup,
        removeTeamFromGroup,
        getAllTeams,
        searchTeam,
        createUser,
        getUser,
        getTokenbyUserId,
        addUser,
        getUserId,
        getAllUsers,
  }

  // Funções relacionadas com Grupos

  // Obtém um grupo específico, verificando se o utilizador autenticado tem acesso.
  function getGroup(groupId, userToken) {
    if (!groupId) return Promise.reject(errors.MISSING_PARAMETER('groupId')); // Verifica se o parâmetro groupId foi fornecido.
    const groupPromise = Promise.all([
      getUserId(userToken), foccaciaData.getGroup(groupId)]);
    return groupPromise.then(arrayValues => {
      const userId = arrayValues[0];
      const group = arrayValues[1];
      if (!group) return Promise.reject(errors.GROUP_NOT_FOUND(groupId));
      if (group.userId == userId) return (group);
      else return Promise.reject(errors.GROUP_NOT_FOUND(groupId));
    });
  }

  // Obtém todos os grupos de um utilizador específico.
  async function getAllGroups(userToken) {
    return getUserId(userToken)
      .then(userId => foccaciaData.getAllGroups(userId)); // Obtém o ID do utilizador a partir do token fornecido.
  }

  // Adiciona um novo grupo para o utilizador autenticado.
  async function addGroup(groupData, userToken) {
    const userId = await getUserId(userToken);
    if (!groupData || !groupData.name || !groupData.description)
      return Promise.reject(errors.INVALID_BODY()); // Verifica se os dados do grupo são válidos.
    const toReturn = await foccaciaData.createGroup(groupData, userId); // Cria o grupo na base de dados.
    console.log(toReturn)
    return toReturn; // Retorna o grupo criado.
  }

  // Remove um grupo específico do utilizador autenticado.
  async function deleteGroup(groupId, userToken) {
    const group = await getGroup(groupId, userToken); // Obtém o grupo para verificar se existe e se o utilizador tem acesso.
    if (!group) return Promise.reject(errors.GROUP_NOT_FOUND(groupId)); // Retorna um erro se o grupo não for encontrado.
    const userId = await getUserId(userToken);
    return await foccaciaData.deleteGroup(group.id, userId); // Remove o grupo da base de dados.
  }

  // Atualiza os dados de um grupo específico.
  function updateGroup(groupId, newGroupData, userToken) {
    return getGroup(groupId, userToken)
      .then((group) => {
        if (!group) return Promise.reject(errors.GROUP_NOT_FOUND(groupId)); // Retorna um erro se o grupo não for encontrado.
        if (!newGroupData || (!newGroupData.name && !newGroupData.description))
          return Promise.reject(errors.INVALID_BODY()); // Verifica se os dados fornecidos para atualização são válidos.
        newGroupData.userId = group.userId;
        return foccaciaData.updateGroup(group.id, newGroupData); // Atualiza o grupo na base de dados.
      })
  }

  // Funções relacionadas com Equipas

  // Adiciona uma equipa obtida da API ao armazenamento local de equipas.
  async function addTeamFromAPItoTeams(team) {
    const teams = await foccaciaData.getAllTeams(); // Obtém todas as equipas armazenadas localmente.
    const existingTeam = teams.find(t => t.name === team.name);
    if (existingTeam) {
      return Promise.reject(errors.DUPLICATE_TEAM(existingTeam.name)); 
    }
    if (team) {
      foccaciaData.addTeamtoArray(team); // Adiciona a equipa ao armazenamento local.
      return team; // Retorna a equipa adicionada.
    }
    return null; // Retorna nulo se a equipa não for encontrada na API.
  }

  // Adiciona uma equipa a um grupo específico.
  async function addTeamToGroup(groupId, teamData, userToken) {
    const userId = await getUserId(userToken); // Obtém o ID do utilizador a partir do token fornecido.
    return await foccaciaData.addTeamToGroup(groupId, teamData, userId); // Adiciona a equipa ao grupo na base de dados.
  }

  // Remove uma equipa de um grupo específico.
  async function removeTeamFromGroup(groupId, teamId, userToken) {
    const userId = await getUserId(userToken);
    return await foccaciaData.removeTeamFromGroup(groupId, Number(teamId), userId); // Remove a equipa do grupo na base de dados.
  }

  // Obtém todas as equipas armazenadas localmente.
  function getAllTeams(){
    return foccaciaData.getAllTeams(); // Retorna a lista de equipas.
  }

  // Pesquisa uma equipa pelo seu nome
  async function searchTeam(name){
    return await teamData.getTeamByName(name)
  }

  // Funções relacionadas com Utilizadores

  // Cria um novo utilizador.
  function createUser(newUser) {
    console.log(newUser)
    if (!newUser || !newUser.userName || !newUser.password) 
      return Promise.reject(errors.INVALID_BODY()); // Verifica se os dados do utilizador são válidos.
    return addUser(newUser.userName, newUser.password); // Adiciona o utilizador ao armazenamento local.
  }

  // Obtém os dados de um utilizador a partir do token.
  function getUser(token){
    return usersData.getUser({token: token}); // Retorna o utilizador correspondente ao token.
  }

  // Obtém o token de autenticação de um utilizador pelo ID.
  function getTokenbyUserId(userId){
    return usersData.getTokenByUserId(userId); // Retorna o token do utilizador.
  }

  // Adiciona um utilizador ao armazenamento.
  function addUser(username, password) {
    return usersData.addUser(username, password); // Adiciona o utilizador com o nome fornecido.
  }

  // Obtém o ID de um utilizador pelo token.
  function getUserId(token) {
    return usersData.getUserId(token); // Retorna o ID do utilizador correspondente ao token.
  }

  // Obtém todos os utilizadores
  async function getAllUsers() {
    return await usersData.getAllUsers();
  }
}