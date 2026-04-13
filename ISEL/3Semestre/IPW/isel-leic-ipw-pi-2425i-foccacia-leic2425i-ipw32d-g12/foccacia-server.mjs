'use strict';
import express from 'express';
import swaggerUi from 'swagger-ui-express';
import yaml from 'yamljs';
import dotenv from 'dotenv'; // Importa o módulo 'dotenv' para carregar variáveis de ambiente de um arquivo .env
import cors from 'cors';
import hbs from 'hbs';
import path from 'path';
import url from 'url';
import session from 'express-session';
import passport from 'passport';
import cookieParser from 'cookie-parser'; // Only used to see the cookie value


// Carrega as variáveis de ambiente do arquivo .env
dotenv.config();

// Define a porta onde a aplicação vai correr, usando a variável de ambiente PORT ou 8080 por omissão
const PORT = process.env.PORT || 8080;

// Configuração de caminhos para diretórios importantes
const CURRENT_DIR = url.fileURLToPath(new URL('.', import.meta.url));
const PATH_PUBLIC = path.join(CURRENT_DIR, 'web', 'site', 'public');
const PATH_VIEWS = path.join(CURRENT_DIR, 'web', 'site', 'views');
const PATH_PARTIALS = path.join(PATH_VIEWS, 'partials');

// Variáveis
let groupsAPI = undefined;
let usersAPI = undefined;
let teamsAPI = undefined;
let foccaciaSite = undefined;

// Importa os módulos necessários para injeção de dependências
import foccaciaApiInit from './web/api/foccacia-web-api.mjs'
import foccaciaServicesInit from './services/foccacia-services.mjs';
//import groupsDataInit from './data/memory/foccacia-data-mem.mjs'; // Fonte de dados baseada em memória para grupos
import groupsDataInit from './data/elastic/foccacia-data-elastic.mjs'; // Alternativa: Elasticsearch
//import usersDataInit from './data/memory/foccacia-data-mem.mjs'; // Fonte de dados baseada em memória para utilizadores
import usersDataInit from './data/elastic/foccacia-data-elastic.mjs'; // Alternativa: Elasticsearch
import teamsDataInit from './data/memory/fapi-teams-data.mjs';

import foccaciaSiteInit from './web/site/foccacia-web-site.mjs';

try {
  // Inicializa os dados e serviços necessários
  const groupsData = groupsDataInit();
  const usersData = usersDataInit();
  const teamsData = teamsDataInit();
  const foccaciaServices = foccaciaServicesInit(teamsData, usersData, groupsData);
  // Inicializa os módulos com base nos serviços
  foccaciaSite = foccaciaSiteInit(foccaciaServices);
  groupsAPI = foccaciaApiInit(foccaciaServices);
  usersAPI = foccaciaApiInit(foccaciaServices);
  teamsAPI = foccaciaApiInit(foccaciaServices);
}
catch (err) {
  console.error(err);
}

if (foccaciaSite && groupsAPI && usersAPI && teamsAPI) {
  passport.serializeUser((userInfo, done) => { 
    console.log("userInfo", userInfo);
    const user = {
      name: userInfo.name,
      token: userInfo.token,
      password: userInfo.password
    }
    //if(user._source) user = {id: user._id, ...user._source};
    done(null, user); 
  });

  passport.deserializeUser((userInfo, done) => { 
    console.log("userInfo (D)", userInfo);
    done(null, userInfo); 
  });
  
  const sessionHandler = session({
    secret: 'isel-ipw',
    resave: false,
    saveUninitialized: false,
  });
  
  // Cria uma instância da aplicação Express
  const app = express();

  // Configuração para servir ficheiros estáticos (imagens, CSS, JS, etc.)
  app.use(express.static(PATH_PUBLIC));

  // Configura o sistema de templates HBS
  app.set('views', PATH_VIEWS);
  app.set('view engine', 'hbs');
  hbs.registerPartials(PATH_PARTIALS);

  // Carrega o documento de especificação da API no formato YAML
  const swaggerDocument = yaml.load(path.join(CURRENT_DIR, 'docs', 'foccacia-api-spec.yaml'));

  // Configura o endpoint para servir a documentação da API através do Swagger UI
  app.use('/api-doc', swaggerUi.serve, swaggerUi.setup(swaggerDocument));

  // Ativa o CORS para permitir todas as requisições para a API
  app.use(cors());

  // Configura a aplicação para interpretar JSON no corpo das requisições
  app.use(express.json());

  // Configura o parsing do corpo das requisições como JSON e formulários HTML
  app.use(express.urlencoded({extended: false}));

  app.use(sessionHandler);        // Support sessions in req.session
  app.use(passport.session());    // Support login sessions in passport
  app.use(cookieParser()); 		// Only used to see the cookie value

  // Middleware para garantir que o token do utilizador está presente no cabeçalho
  app.use("/groups*", groupsAPI.ensureToken); 
  app.use("/site/groups*", foccaciaSite.ensureToken);
  app.use("/users*", usersAPI.ensureToken); 

  // Rota para a página inicial
  app.use("/site/homepage", foccaciaSite.homepage);

  // Resource protected by authentication
  app.get('/site/users/me', foccaciaSite.authenticate, foccaciaSite.home);

  app.get('/site/login', foccaciaSite.loginHome);
  app.post('/site/login', foccaciaSite.loginAuth);

  app.post('/site/logout', foccaciaSite.logout);

  app.get('/site/signup', foccaciaSite.signupHome);
  app.post('/site/signup', foccaciaSite.signup);

  // Rota para obter informações de um grupo pelo ID
  app.get('/groups/:id', groupsAPI.getGroup);
  app.get('/site/groups/:id',  foccaciaSite.getGroup);

  // Rota para listar todos os grupos
  app.get('/groups', groupsAPI.getAllGroups); 
  app.get('/site/groups',  foccaciaSite.getAllGroups);

  // Rota para adicionar um novo grupo
  app.post('/groups', groupsAPI.addGroup); 
  app.post('/site/groups',  foccaciaSite.addGroup); 

  // Rota para eliminar um grupo pelo ID
  app.delete('/groups/:id?', groupsAPI.deleteGroup); 
  app.post('/site/groups/:id?/delete',  foccaciaSite.deleteGroup);

  // Rota para atualizar um grupo pelo ID
  app.put('/groups/:id?', groupsAPI.updateGroup);
  app.post("/site/groups/:id?/update", foccaciaSite.updateGroup);

  //app.use("/site/users/register",  foccaciaSite.userRegistration);

  // Rota para adicionar um novo utilizador
  app.post("/users/signup", usersAPI.createUser);
  //app.post("/site/register/create",  foccaciaSite.createUser);

  //Rota para obter informações de todos os utilizadores
  app.get('/users/login', usersAPI.getUser); 
  //app.get('/site/users/me',  foccaciaSite.getUser);

  app.use("/site/teams/search",  foccaciaSite.searchTeams); 

  app.post('/team/search', teamsAPI.searchTeam); 
  app.post("/site/team/search",  foccaciaSite.searchTeam)

  // Rota para adicionar uma equipa à lista de equipas a partir da API externa
  app.post('/teams/store', teamsAPI.addTeamFromAPItoTeams); 
  app.post("/site/teams/store",  foccaciaSite.addTeamFromAPItoTeams);

  // Rota para adicionar uma equipa a um grupo específico
  app.post('/groups/:id/teams', teamsAPI.addTeamToGroup); 
  app.post("/site/groups/:id/teams",  foccaciaSite.addTeamToGroup);

  // Rota para remover uma equipa de um grupo específico
  app.delete('/groups/:id?/teams/:teamId?', teamsAPI.removeTeamFromGroup); 
  app.post("/site/groups/:id?/teams/:teamId?/delete",  foccaciaSite.removeTeamFromGroup);

  // Rota para listar todas as equipas 
  app.get('/teams', teamsAPI.getAllTeams); 
  app.get("/site/teams", foccaciaSite.getAllTeams);

  // Middleware para tratar erros específicos das rotas
  app.use("/site*", foccaciaSite.handlerError);
  app.use("/groups*", groupsAPI.handlerError);
  app.use("/users*", usersAPI.handlerError);

  // Inicia o servidor e coloca-o a ouvir na porta configurada
  app.listen(PORT, () =>
    console.log(`FOCCACIA server listening on port ${PORT}! Use http://localhost:${PORT}/site/homepage to access the homepage of the website`),
  );
}
