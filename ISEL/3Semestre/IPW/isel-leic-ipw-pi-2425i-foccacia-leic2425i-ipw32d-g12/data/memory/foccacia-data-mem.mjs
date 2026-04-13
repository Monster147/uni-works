import crypto from 'node:crypto';
import {errors} from '../../common/errors.mjs';

// Base de dados fictícia de utilizadores
export const USERS = [
  { id: 1, name: 'jsaldanha', token: 'b0506867-77c3-4142-9437-1f627deebd67', password: '123456' },
  { id: 2, name: 'rpinto', token: 'f1d1cdbc-97f0-41c4-b206-051250684b19', password: '1234587' },
  { id: 3, name: 'test', token: '123-456-789', password: 'test' },
];

// Base de dados fictícia de equipas e respetivas ligas associadas
export const TEAMS = [
  { id: 1, 
    name: 'Manchester United',
    stadium: 'Old Trafford',
    leagues: [
      {
        id: 39,
        name: "Premier League",
        season: 2022
      },
      {
        id: 667,
        name: "Friendlies Clubs",
        season: 2022
      },
      {
        id: 48,
        name: "League Cup",
        season: 2022
      },
      {
        id: 45,
        name: "FA Cup",
        season: 2022
      },
      {
        id: 3,
        name: "UEFA Europa League",
        season: 2022
      }
    ] 
  },
  { id: 2, 
    name: 'Real Madrid',
    stadium: 'Estadio Santiago Bernabéu',
    leagues: [
      {
        id: 531,
        name: "UEFA Super Cup",
        season: 2022
      },
      {
        id: 2,
        name: "UEFA Champions League",
        season: 2022
      },
      {
        id: 556,
        name: "Super Cup",
        season: 2022
      },
      {
        id: 667,
        name: "Friendlies Clubs",
        season: 2022
      },
      {
        id: 140,
        name: "La Liga",
        season: 2022
      },
      {
        id: 143,
        name: "Copa del Rey",
        season: 2022
      },
      {
        id: 15,
        name: "FIFA Intercontinental Cup",
        season: 2022
      }
    ]
  },
  { id: 3, 
    name: 'Barcelona',
    stadium: "Estadi Olímpic Lluís Companys",
    leagues: [
      {
        id: 2,
        name: "UEFA Champions League",
        season: 2022
      },
      {
        id: 556,
        name: "Super Cup",
        season: 2022
      },
      {
        id: 667,
        name: "Friendlies Clubs",
        season: 2022
      },
      {
        id: 140,
        name: "La Liga",
        season: 2022
      },
      {
        id: 3,
        name: "UEFA Europa League",
        season: 2022
      },
      {
        id: 143,
        name: "Copa del Rey",
        season: 2022
      }
    ]
  },
  { id: 4, name: 'Test1' },
  { id: 5, name: 'Test2' },
  { id: 6, name: 'Test3' },
];

// Base de dados fictícia de grupos de equipas
export const GROUPS = [
  {
    id: 1,
    name: 'My Favorites',
    description: 'Top teams I support',
    userId: 1,
    teams: [TEAMS[0], TEAMS[1]], // Example: Manchester United, Real Madrid
  },
  {
    id: 2,
    name: 'European Giants',
    description: 'Elite teams in Europe',
    userId: 2,
    teams: [TEAMS[1], TEAMS[2]], // Example: Real Madrid, Barcelona
  },
  {
    id: 3,
    name: 'Test',
    description: 'Test',
    userId: 3,
    teams: [TEAMS[3], TEAMS[4]], // Example: Test1, Test2
  },
];
  
export default function init(){

  return {
    getAllGroups,
    getGroup,
    createGroup,
    updateGroup,
    deleteGroup,
    getUserId,
    getUserByName,
    getTokenByUserId,
    addUser,
    getUser,
    addTeamToGroup,
    removeTeamFromGroup,
    getAllTeams,
    addTeamtoArray,
    getAllUsers
  };

  
  
  // Função auxiliar para gerar o próximo ID com base na entidade (utilizador ou grupo)
  function nextId(entity) {
    // Variáveis para gerir IDs de utilizadores e grupos
    let currentGroupId = GROUPS.length+1;
    let currentUserId = USERS.length+1;
    if (entity === 'user') return currentUserId++;
    if (entity === 'group') return currentGroupId++;
  }

  // --- Gestão de Grupos ---

  function aGroup(newGroup, id, userId){
    this.id = id;
    this.name = newGroup.name;
    this.description = newGroup.description;
    this.userId = userId
    this.teams = []
  }

  // Obtém todos os grupos
  function getAllGroups(userId) {
    return Promise.resolve(GROUPS.filter(group => group.userId == userId));
  }

  // Obtém um grupo específico com base no ID
  function getGroup(id) {
    return Promise.resolve(GROUPS.find(group => group.id == id));
  }

  // Cria um novo grupo e associa-o a um utilizador
  function createGroup(groupData, userId) {
    return new Promise((resolve) => {
        const newGroupId = nextId('group')
        const newGroup = new aGroup(groupData, newGroupId, userId)
        GROUPS.push(newGroup); // Adiciona o novo grupo à lista
        resolve(newGroup);
    });
  }

  // Atualiza as informações de um grupo
  function updateGroup(id, groupData) {
    return new Promise((resolve, reject) => {
      let groupIDX = GROUPS.findIndex(group => group.id == id);
      if (groupIDX != -1) {
        GROUPS[groupIDX].name = groupData.name;
        GROUPS[groupIDX].description = groupData.description; 
        resolve(GROUPS[groupIDX]);
      } else reject(errors.GROUP_NOT_FOUND(id));
    });
  }

  // Remove um grupo
  function deleteGroup(id, userId) {
    return new Promise((resolve, reject) => {
      const groupIDX = GROUPS.findIndex(group => group.id == id && group.userId == userId); 
      if (groupIDX == -1) return reject(errors.GROUP_NOT_FOUND(id)); // Verifica se o grupo existe
      const deletedGroup = GROUPS.splice(groupIDX, 1)[0]; // Remove o grupo da lista
      resolve(deletedGroup);
    });
  }
  
  // --- Gestão de Utilizadores ---

  // Obtém o ID de utilizador com base no token
  function getUserId(token) {
    return new Promise((resolve, reject) => {
      const user = USERS.find(user => user.token === token);
      user ? resolve(user.id) : reject(errors.USER_NOT_FOUND());
    });
  }

  // Obtém um utilizador com base no nome
  function getUserByName(name) {
    return new Promise((resolve, reject) => {
      const user = USERS.find(user => user.name == name);
      user ? resolve(user) : reject(errors.USER_NOT_FOUND());
    });
  }

  // Obtém o token de um utilizador com base no ID
  function getTokenByUserId(userId) {
    return new Promise((resolve, reject) => {
      const user = USERS.find(user => user.id === userId);
      user ? resolve(user.token) : reject(errors.USER_NOT_FOUND());
    });
  }

  // Adiciona um novo utilizador
  function addUser(username, password) {
    return new Promise((resolve, reject) => {
      if (!username) return reject(errors.INVALID_BODY()); // Verifica se o nome foi fornecido
      if(USERS.find(user => user.name == username))
        return reject(errors.DUPLICATE_USER(username)); // Verifica duplicados
      const newUser = {
        id: nextId('user'), // Gera um novo ID
        name: username,
        token: crypto.randomUUID(),
        password: password
      };
      USERS.push(newUser); // Adiciona o utilizador à lista
      resolve(newUser);
    });
  }

  // Obtém os detalhes de um utilizador com base no token
  function getUser(matchObj) {
    console.log(matchObj)
    return new Promise((resolve, reject) => {
      const user = USERS.find(user => user.token == matchObj.token);
      user ? resolve(user) : reject(errors.USER_NOT_FOUND());
    });
  }

  function getAllUsers(){
    return Promise.resolve(USERS)
  }

  // --- Gestão de Equipas ---

  // Adiciona uma equipa a um grupo
  function addTeamToGroup(groupId, teamData, userId) {
    return new Promise((resolve, reject) => {
      getGroup(groupId, userId).then(group => {
        if (!group) return reject(errors.GROUP_NOT_FOUND(groupId)); // Verifica se o grupo existe
        const team = TEAMS.find(team => 
          team.name.toLowerCase() == teamData.name.toLowerCase());
        if (!team) return reject(errors.TEAM_NOT_FOUND(teamData.name)); // Verifica se a equipa existe
        if (group.teams.some(t => t.name == teamData.name)) return reject(errors.DUPLICATE_TEAM(teamData.name)); // Verifica duplicados
        group.teams.push(team); // Adiciona a equipa ao grupo
        resolve(team);
      });
    });
  }

  // Remove uma equipa de um grupo
  async function removeTeamFromGroup(groupId, teamId) {
    return new Promise((resolve, reject) => {
      getGroup(groupId).then(group => {
        if (!group) return reject(errors.GROUP_NOT_FOUND(groupId)); // Verifica se o grupo existe
        const teamIndex = group.teams.findIndex(team => team.id == teamId);
        if (teamIndex === -1) return reject(errors.TEAM_NOT_FOUND(teamId)); // Verifica se a equipa existe
        resolve(group.teams.splice(teamIndex, 1)[0]); // Remove e devolve a equipa
      });
    });
  }

  // Obtém todas as equipas
  function getAllTeams(){
    return Promise.resolve(TEAMS);
  }  

  // Adiciona uma equipa ao array das equipas
  function addTeamtoArray(team){
    return new Promise((resolve, reject) => {
      TEAMS.push(team);
      resolve(TEAMS);
    });
  }
}