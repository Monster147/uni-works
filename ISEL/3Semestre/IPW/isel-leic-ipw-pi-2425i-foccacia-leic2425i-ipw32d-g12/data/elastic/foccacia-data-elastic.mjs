import crypto from 'node:crypto';
import { errors } from '../../common/errors.mjs';
import { fetchElastic } from './fetch-elastic.mjs';

export default function init() {
    return {
        getAllGroups,           // Obter todos os grupos de um utilizador
        getGroup,               // Obter um grupo específico pelo ID
        createGroup,            // Criar um novo grupo
        updateGroup,            // Atualizar os dados de um grupo
        deleteGroup,            // Eliminar um grupo
        getUserId,              // Obter o ID do utilizador a partir do token
        getUserByName,          // Obter um utilizador pelo nome
        getTokenByUserId,       // Obter o token de um utilizador pelo ID
        addUser,                // Adicionar um novo utilizador
        getUser,                // Obter dados de um utilizador com base em critérios
        addTeamToGroup,         // Adicionar uma equipa a um grupo
        removeTeamFromGroup,    // Remover uma equipa de um grupo
        getAllTeams,            // Obter todas as equipas registadas
        addTeamtoArray,         // Adicionar uma equipa à lista de equipas
        getAllUsers             // Obter todos os utilizadores registados
    };

    // Função auxiliar para transformar um grupo do Elastic em formato utilizável
    function aGroupFromElastic(elasticGroup) {
        return { id: elasticGroup._id, ...elasticGroup._source };
    }

    // Função auxiliar para transformar uma equipa do Elastic em formato utilizável
    function aTeamFromElastic(elasticTeam) {
        return { id: elasticTeam._id, ...elasticTeam._source };
    }

    // --- Gestão de Grupos ---

    // Obter todos os grupos de um utilizador, com base no seu ID
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

    // Obter um grupo específico pelo ID
    function getGroup(id) {
        return fetchElastic('GET', '/groups/_doc/' + id)
            .then(resp => {
                if (resp.found) {
                    return aGroupFromElastic(resp); // Retornar o grupo em formato utilizável
                }
                return Promise.reject(errors.GROUP_NOT_FOUND(id)); // Erro caso o grupo não seja encontrado
            });
    }

    // Criar um novo grupo, associando-o ao utilizador
    function createGroup(newGroup, userId) {
        newGroup.userId = userId; // Associar o grupo ao utilizador
        newGroup.teams = []; // Inicializar com uma lista vazia de equipas
        return fetchElastic('POST', '/groups/_doc?refresh=true', newGroup)
            .then(body => { 
              newGroup.id = body._id; // Definir o ID do novo grupo
              return newGroup  // Retornar o grupo criado
            });
    }

    // Atualizar os dados de um grupo existente
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

    // Eliminar um grupo baseado no ID e verificar se o utilizador tem permissão
    function deleteGroup(id, userId) {
        return fetchElastic('GET', `/groups/_doc/${id}`)
            .then(resp => {
                if (!resp.found || resp._source.userId != userId) {
                    return Promise.reject(errors.GROUP_NOT_FOUND(id)); // Erro caso o grupo não exista ou o utilizador não tenha permissão
                }
                return fetchElastic('DELETE', `/groups/_doc/${id}?refresh=true`); // Eliminar o grupo
            })
    }

    // --- Gestão de Utilizadores ---

    // Obter um utilizador com base num critério de pesquisa
    function getUser(matchObj) {
        const query = { query: { match: matchObj } };
        return fetchElastic("POST", "/users/_search", query)
            .then(resp => {
                if (resp.error) {
                    console.error(resp.error.reason);
                    return null; // Erro na pesquisa do utilizador
                }
                if(resp.hits.hits.length > 0) return resp.hits.hits[0] // Retornar o primeiro utilizador encontrado
            });
    }

    // Obter o ID de um utilizador a partir de um token
    function getUserId(token) {
        return getUser({ token: token })
            .then(user => {
                if (user) return user._id // Retornar o ID do utilizador
                else Promise.reject(errors.USER_NOT_FOUND())
            }); // Erro se o utilizador não for encontrado
    }

    // Obter um utilizador pelo nome
    function getUserByName(username) {
        return getUser({ name: username })
    }

    // Obter o token de um utilizador baseado no ID
    function getTokenByUserId(userId) {
        return getUser({ id: userId })
            .then(user => user?._source.token || Promise.reject(errors.USER_NOT_FOUND())); // Retornar o token ou erro se não encontrado
    }   

    // Adicionar um novo utilizador
    function addUser(username, password) {
        return getUserByName(username)
            .then(user => {
                if(user){
                    return Promise.reject(errors.DUPLICATE_USER(username)) // Erro se o utilizador já existir
                }
                const newUser = {
                    token: crypto.randomUUID(), // Gerar um token
                    name: username,
                    password
                };
                //console.log(newUser.token)
                return fetchElastic('POST', '/users/_doc?refresh=true', newUser)
                    .then(resp => ({ id: resp._id, ...newUser })); // Retornar o utilizador criado
        });
    }

    // Obter todos os utilizadores
    function getAllUsers() {
        const query = { query: { match_all: {} } };
        return fetchElastic('POST', '/users/_search', query)
            .then(resp => {
                if (resp.error) {
                    console.error(resp.error.reason);
                    return []; // Retornar lista vazia em caso de erro
                }
                return resp.hits.hits.map(hit => hit._source); // Mapea todos os utilizadores
            });
    }

    // --- Gestão de Equipas ---

    // Obter todas as equipas registadas
    function getAllTeams() {
        const query = { query: { match_all: {} } };
        return fetchElastic('POST', '/teams/_search', query)
            .then(resp => {
                if (resp.error) {
                    console.error(resp.error.reason);
                    return []; // Retornar lista vazia em caso de erro
                }
                return resp.hits.hits.map(aTeamFromElastic); // Mapea as equipas para o formato correto
            });
    }

    // Adicionar uma equipa à base de dados
    function addTeamtoArray(team) {
        return fetchElastic('POST', '/teams/_doc', team)
            .then(resp => ({ id: resp._id, ...team })); // Retorna a equipa adicionada
    }

    // Adicionar uma equipa a um grupo
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
                console.log(group)
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

    // Remover uma equipa de um grupo
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
}