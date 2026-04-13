import { errors } from '../../common/errors.mjs';
//import * as tasksServices from "../../services/tasks-services.mjs";
import errorToHttp from '../errors-to-http-responses.mjs';

// FUNCTIONS (WEB API):

let savedTeams = [];

export default function init(foccaciaServices){

  // Verify the dependencies:
  if(! foccaciaServices){
    throw errors.INVALID_ARGUMENT('foccaciaServices');
  }

  return {
    handlerError,
    ensureToken,
    homepage,
    searchTeams,
    home,
    createUser : processRequest(local_createUser), // Endpoint para criar utilizadores.
    getUser : processRequest(local_getUser), // Endpoint para obter dados de um utilizador.
    authenticate,
    loginHome,
    signupHome,
    loginAuth,
    logout,
    signup,
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

  function processRequest(operation){
    return function (req, res, next){
      //console.log(req.userToken)
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
    getResponseError(req, res, err);
  }

  function getResponseError(req, res, err){
    //console.log(err);
    const responseError = errorToHttp(err);
    const errorBody = responseError.body;
    const isAuthenticated = req.session && req.session.token;
    res.status(responseError.status);
    return res.render("errors-view", {errorBody, isAuthenticated});
    //return res.json(responseError.body);
  }

  /*// Auxiliary module function
  function getToken(req) {
    return "b0506867-77c3-4142-9437-1f627deebd67"; // jsaldanha in memory
    //return "e1e41977-45cf-46fc-9b16-339f847ef30d"; // jsaldanha in Elastic DB
  }*/
  function getToken(req) {
    if (req.session && req.session.token) {
      return req.session.token; // Return the token of the logged-in user
    } else {
      return null; // No token available
    }
  }


  // Página inicial
  function homepage(req, res){
    const isAuthenticated = req.session && req.session.token;
    return res.render("homepage", { isAuthenticated })
  }

  // Página de pesquisa de equipas
  function searchTeams(req, res){
    const isAuthenticated = req.session && req.session.token;
    return res.render("search-teams", { isAuthenticated })
  }

  function home(req, res){
    console.log("Session:", req.session);
    console.log("req.cookies:", req.cookies);
    console.log("req.user:", req.user);
    const user = req.user
    if(user._source) user = {id: user._id, ...user._source};
    const isAuthenticated = req.session && req.session.token;
    return res.render("loggedInHome", { user, isAuthenticated });
  }

  // Funções relacionadas com utilizadores

  // Criação de um novo utilizador.
  function local_createUser(req, res){
    const newUserData = req.body;
    const newUserPromise = foccaciaServices.createUser(newUserData);
    return newUserPromise.then(newUser => {
      console.log(foccaciaServices.getAllUsers())
      res.status(201);
      res.redirect("/site/homepage"); // Redirecionar para a página inicial
    });
  }

  // Obtenção dos dados de um utilizador.
  function local_getUser(req, res) {
    const token = req.userToken;
    const userPromise = foccaciaServices.getUser(token);
    return userPromise.then(user => {
      if(user._source) user = {id: user._id, ...user._source};
      res.render("user-view", {user}); // Renderizar a página de detalhes do utilizador
    });
  }

  function authenticate(req, res, next){
    if (! req.isAuthenticated())
      return res.redirect('/site/login');
    next();
  }

  function loginHome(req, res){
    console.log("Session in login home:", req.session);
    console.log("Cookies in login home:", req.cookies);
  
    // If user is already authenticated, go to home
    if (req.isAuthenticated())
      return res.redirect('/site/homepage');
    const isAuthenticated = req.session && req.session.token;
    return res.render("noLoginHome", isAuthenticated);
  }

  function signupHome(req, res){
    console.log("Session in signup home:", req.session);
    console.log("Cookies in signup home:", req.cookies);
    const isAuthenticated = req.session && req.session.token;
    return res.render("signUpHome", { isAuthenticated });
  }

  function loginAuth(req, res, next){
    console.log("Session before login:", req.session);
    console.log("Req.user before login:", req.user);
  
    const username = req.body.username; 
    const password = req.body.password;

    foccaciaServices.getAllUsers().then(
      USERS => {
        const userIndex = USERS.findIndex(user => username === user.name);
  
        if (userIndex != -1 && isValidUser(USERS[userIndex], username, password)) {
          console.log('login:', username);
          return req.login(USERS[userIndex], loginAction);
        } 
        else {
          const errorMessage = 'Invalid user or password'
          res.status(401);
          return res.render('noLoginHome', { errorMessage });
        }
      
        function isValidUser(user, username, password) {
          return user && user.name === username && user.password === password;
        }
      
        function loginAction(err) {
          if (err) return next(err);
        
          req.session.token = req.user.token;
          console.log('>> login ok for', req.user.name);
          return res.redirect('/site/homepage');
        }	
      }
    ); 
  }

  function logout (req, res) {
    return req.logout(function (){
      return res.redirect('/site/homepage');
    });
  }

  async function signup(req, res, next) {
    try{
      const username = req.body.username;
      const password = req.body.password;
      const user = {
        userName: username,
        password: password
      }
      const createdUser = await foccaciaServices.createUser(user)
    
      // Login with the new user:
      console.log("Created User:", createdUser);
      return req.login(createdUser, loginAction);

      function loginAction(err) {
        if (err) return next(err);
        req.session.token = req.user.token; // Save token in the session for the logged-in user
        console.log('>> login ok for', req.user.name);
        return res.redirect('/site/homepage');
      }
    } catch (err) {
      next(err);
    }
    }
  
  // Funções relacionadas com grupos

  // Obtenção de dados de um grupo específico.
  function local_getGroup(req, res) {
    const groupId = req.params.id;
    const token = req.userToken;
    const groupPromise = foccaciaServices.getGroup(groupId, token);
    const isAuthenticated = req.session && req.session.token;
    return groupPromise.then(group => {
      res.render("group-view", {group, isAuthenticated}); // Renderizar a página de detalhes do grupo
    });
  }

  // Obtenção de todos os grupos do utilizador.
  function local_getAllGroups(req, res) {
    const groupsPromise = foccaciaServices.getAllGroups(req.userToken);
    const isAuthenticated = req.session && req.session.token;
    return groupsPromise.then(groups => {
      res.render("groups-view", { groups, isAuthenticated }); // Renderizar a página com todos os grupos
    });
  } 

  // Adicionar um novo grupo.
  function local_addGroup(req, res) {
    const groupPromise = foccaciaServices.addGroup(req.body, req.userToken);
    return groupPromise.then(group => {
      res.status(201);
      res.redirect("/site/groups"); // Redirecionar para a página de grupos
    });
  }

  // Atualização dos dados de um grupo.
  function local_updateGroup(req, res) {
    const groupId = req.params.id;
    const newGroup = req.body;
    const userToken = req.userToken;
    const updatedGroupPromise = foccaciaServices.updateGroup(groupId, newGroup, userToken);
    return updatedGroupPromise.then(updatedGroup => {
      res.redirect("/site/groups"); // Redirecionar para a página de grupos
    });
  }

  // Eliminação de um grupo.
  function local_deleteGroup(req, res) {
    const groupId = req.params.id;
    const deleteGroupPromise = foccaciaServices.deleteGroup(groupId, req.userToken);
    return deleteGroupPromise.then(deletedGroup => {
      res.redirect("/site/groups"); // Redirecionar para a página de grupos
    });
  }

  // Funções relacionadas com equipas em grupos

  // Adicionar uma equipa a um grupo.
  function local_addTeamToGroup(req, res) {
    const groupId = req.params.id;
    const teamData = req.body;
    const token = req.userToken;
    const teamPromise = foccaciaServices.addTeamToGroup(groupId, teamData, token);
    return teamPromise.then(team => {
      res.redirect(`/site/groups/${groupId}`); // Redirecionar para a página do grupo com a nova equipa
    });
  }

  // Remover uma equipa de um grupo.
  function local_removeTeamFromGroup(req, res) {
    const groupId = req.params.id;
    const teamId = req.params.teamId;
    const token = req.userToken;
    const removedTeamPromise = foccaciaServices.removeTeamFromGroup(groupId, teamId, token);
    return removedTeamPromise.then(team => {
      res.redirect(`/site/groups/${groupId}`); // Redirecionar para a página do grupo após a remoção
    });
  }

  // Adicionar uma equipa obtida da API ao armazenamento local.
  function local_addTeamFromAPItoTeams(req, res) {
    const teamName = req.body.name;
    const teamData = savedTeams.find(team => team.name == teamName)
    const teamPromise = foccaciaServices.addTeamFromAPItoTeams(teamData);
    return teamPromise.then(team => {
      res.redirect("/site/teams"); // Redirecionar para a página de equipas após a adição
    });
  }

  // Obtenção de todas as equipas registadas.
  function local_getAllTeams(req, res) {
    const teamsPromise = foccaciaServices.getAllTeams();
    const isAuthenticated = req.session && req.session.token;
    return teamsPromise.then(teams => {
      res.render("teams-view", { teams , isAuthenticated}); // Renderizar a página com todas as equipas
    });
  }

// Função de pesquisa de equipa
  function local_searchTeam(req, res) {
    const teamName = req.body.name
    const teamsPromise = foccaciaServices.searchTeam(teamName); 
    const isAuthenticated = req.session && req.session.token;
    return teamsPromise.then(
      team => {
        const teamData = {
          id: team.id,
          name: team.name,
          stadium: team.stadium,
          leagues: team.leagues
        };
        // Adicionar a equipa apenas se não estiver já salva
        if (!savedTeams.some(savedTeam => savedTeam.id === teamData.id)) {
          savedTeams.push(teamData)
        }
        console.log(savedTeams)
        res.render("search-teams", { team , isAuthenticated});
      }
    );
  }
}