//import * as foccaciaServices from "../../services/foccacia-services.mjs"; // Importa os serviços relacionados com grupos, equipas e utilizadores.
//import * as usersServices from "./foccacia-services.mjs"; // Importa serviços relacionados especificamente com utilizadores.
import { errors } from "../../common/errors.mjs"; // Importa definições de erros comuns para a aplicação.
import errorToHttp from '../errors-to-http-responses.mjs'; // Importa uma função que converte erros em respostas HTTP.

let savedTeams = [];

// Registo de funções processadas para manipulação de pedidos (endpoints)
export default function init(foccaciaServices){

  if(!foccaciaServices) throw errors.INVALID_ARGUMENT('foccaciaServices');

  return {
    ensureToken,
    handlerError,
    createUser : processRequest(local_createUser), // Endpoint para criar utilizadores.
    getUser : processRequest(local_getUser), // Endpoint para obter dados de um utilizador.
    getAllGroups : processRequest(local_getAllGroups), // Endpoint para obter todos os grupos de um utilizador.
    getGroup : processRequest(local_getGroup), // Endpoint para obter dados de um grupo específico.
    addGroup : processRequest(local_addGroup), // Endpoint para adicionar um novo grupo.
    updateGroup : processRequest(local_updateGroup), // Endpoint para atualizar os dados de um grupo.
    deleteGroup : processRequest(local_deleteGroup), // Endpoint para eliminar um grupo.
    getAllTeams : processRequest(local_getAllTeams), // Endpoint para obter todas as equipas registadas.
    searchTeam : processRequest(local_searchTeam), // Endpoint para obter os dados de uma equipa.
    addTeamToGroup : processRequest(local_addTeamToGroup), // Endpoint para adicionar uma equipa a um grupo.
    removeTeamFromGroup : processRequest(local_removeTeamFromGroup), // Endpoint para remover uma equipa de um grupo.
    addTeamFromAPItoTeams : processRequest(local_addTeamFromAPItoTeams), // Endpoint para adicionar uma equipa da API externa ao armazenamento local.
  };

  // Função auxiliar para tratar erros de resposta HTTP
  function getResponseError(res, err){
      //console.log(err);
      const responseError = errorToHttp(err);
      res.status(responseError.status);
      return res.json(responseError.body); 
    }

  // Função genérica para processar pedidos HTTP
  function processRequest(operation){
    return function (req, res, next){
      return operation(req, res, next)
      .catch(next);
    }
  }

  function ensureToken(req, res, next){
    const token = getToken(req);
    //console.log("Token:", token);
    if (token){
      req.userToken = token;
      next();
    }
    else {
      next(errors.MISSING_TOKEN());
    }
  }

  function handlerError (err, req, res, next) {
    console.log("ERROR:", err);
    getResponseError(res, err);
  } 

  // Funções relacionadas com utilizadores

  // Criação de um novo utilizador.
  async function local_createUser(req, res){
    const newUserData = req.body; // Obtém os dados do novo utilizador do corpo do pedido.
    const newUserPromise = foccaciaServices.createUser(newUserData); // Chama o serviço para criar o utilizador.
    const newUser = await newUserPromise; // Espera pela conclusão da criação do utilizador.
    res.status(201); // Define o código de estado HTTP para "Criado".
    return res.send({
      status: `User ${newUser.id} was added!`, // Mensagem de sucesso.
      user: newUser // Retorna os dados do novo utilizador.
    });
  }

  // Obtenção dos dados de um utilizador.
  async function local_getUser(req, res) {
    const token = req.userToken; // Obtém o token do utilizador.
    const userPromise = foccaciaServices.getUser(token); // Chama o serviço para obter os dados do utilizador.
    const user = await userPromise; // Espera pelos dados do utilizador.
    return res.send({
      user: user // Retorna os dados do utilizador.
    });
  }

  // Funções relacionadas com grupos

  // Obtenção de dados de um grupo específico.
  function local_getGroup(req, res) {
    const groupId = req.params.id; // Obtém o ID do grupo dos parâmetros do pedido.
    const token = req.userToken; // Obtém o token do utilizador.
    const groupPromise = foccaciaServices.getGroup(groupId, token); // Chama o serviço para obter o grupo.
    return groupPromise.then(group => res.json(group));
  }

  // Obtenção de todos os grupos do utilizador.
  async function local_getAllGroups(req, res) {
    const groupsPromise = foccaciaServices.getAllGroups(req.userToken); // Chama o serviço para obter todos os grupos.
    const groups = await groupsPromise; // Espera pela lista de grupos.
    return res.json(groups); // Retorna a lista como JSON.
  }

  // Adicionar um novo grupo.
  async function local_addGroup(req, res) {
    const groupPromise = foccaciaServices.addGroup(req.body, req.userToken); // Chama o serviço para adicionar um novo grupo.
    const group = await groupPromise; // Espera pela conclusão da operação.
    res.status(201); // Define o código de estado HTTP para "Criado".
    return res.send({
      status: `Group ${group.id} was added!`,
      group: group // Retorna os dados do novo grupo.
    });
  }

  // Atualização dos dados de um grupo.
  function local_updateGroup(req, res) {
    const groupId = req.params.id; // Obtém o ID do grupo dos parâmetros do pedido.
    const newGroup = req.body; // Obtém os novos dados do grupo do corpo do pedido.
    const userToken = req.userToken; // Obtém o token do utilizador.
    const updatedGroupPromise = foccaciaServices.updateGroup(groupId, newGroup, userToken); // Chama o serviço para atualizar o grupo.
    return updatedGroupPromise.then(updatedGroup => 
      res.json({
        status: `Group ${updatedGroup.id} was updated!`,
        group: updatedGroup // Retorna os dados atualizados do grupo.
      })
    );
  }

  // Eliminação de um grupo.
  async function local_deleteGroup(req, res) {
    const groupId = req.params.id; // Obtém o ID do grupo dos parâmetros do pedido.
    const deleteGroupPromise = foccaciaServices.deleteGroup(groupId, req.userToken); // Chama o serviço para eliminar o grupo.
    const deletedGroup = await deleteGroupPromise; // Espera pela conclusão da eliminação.
    return res.json({
      status: `Group ${deletedGroup.id} was deleted!`
    });
  }

  // Funções relacionadas com equipas em grupos

  // Adicionar uma equipa a um grupo.
  async function local_addTeamToGroup(req, res) {
    const groupId = req.params.id; // Obtém o ID do grupo dos parâmetros do pedido.
    const teamData = req.body; // Obtém os dados da equipa do corpo do pedido.
    const token = req.userToken; // Obtém o token do utilizador.
    const teamPromise = foccaciaServices.addTeamToGroup(groupId, teamData, token); // Chama o serviço para adicionar a equipa ao grupo.
    const team = await teamPromise; // Espera pela conclusão da operação.
    return res.send({
      status: `Team ${team.name} was added to group ${groupId}!`,
      team: team // Retorna os dados da equipa adicionada.
    });
  }

    // Remover uma equipa de um grupo.
  async function local_removeTeamFromGroup(req, res) {
    const groupId = req.params.id; // Obtém o ID do grupo dos parâmetros do pedido.
    const teamId = req.params.teamId; // Obtém o ID da equipa dos parâmetros do pedido.
    const token = req.userToken; // Obtém o token do utilizador.
    const removedTeamPromise = foccaciaServices.removeTeamFromGroup(groupId, teamId, token); // Chama o serviço para remover a equipa do grupo.
    const team = await removedTeamPromise; // Espera pela conclusão da operação.
    return res.json({
      status: `Team ${team.name} was removed from group ${groupId}!`
    });
  }

  // Adicionar uma equipa obtida da API ao armazenamento local.
  async function local_addTeamFromAPItoTeams(req, res) {
    const teamData = savedTeams.find(team => team.name == req.body.name); // Obtém os dados da equipa do corpo do pedido.
    const teamPromise = foccaciaServices.addTeamFromAPItoTeams(teamData); // Chama o serviço para adicionar a equipa.
    const team = await teamPromise; // Espera pela conclusão da operação.
    return res.json({
      status: `Team ${team.id} was added to the teams array!`,
      team: team // Retorna os dados da equipa adicionada.
    });
  }

  // Obtenção de todas as equipas registadas.
  async function local_getAllTeams(req, res) {
    const teamsPromise = foccaciaServices.getAllTeams(); // Chama o serviço para obter todas as equipas.
    const teams = await teamsPromise; // Espera pela lista de equipas.
    return res.json(teams); // Retorna a lista como JSON.
  }

  async function local_searchTeam(req, res) {
    const teamName = req.body.name; // Obtém o nome da equipa do corpo do pedido.
    const teamsPromise = foccaciaServices.searchTeam(teamName); 
    const team = await teamsPromise; 
    const teamData = {
      id: team.id,
      name: team.name,
      stadium: team.stadium,
      leagues: team.leagues
    };
    savedTeams.push(teamData);
    return res.json(team); // Retorna a lista como JSON.
  }

  // Auxiliary module function
  function getToken(req) {
    const authToken = req.get("Authorization");
    if (authToken){
      //console.log(authToken);
      const tokenParts = authToken.split(" ");
      if(tokenParts && tokenParts[0] == "Bearer") {
          return tokenParts[1];
      }
    }
  }
}