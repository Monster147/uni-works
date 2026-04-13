// Exporta um objeto contendo os códigos de erro definidos para a aplicação
export const ERROR_CODES = {
    MISSING_PARAMETER: 1,   // Parâmetro em falta
    INVALID_PARAMETER: 2,   // Parâmetro inválido
    INVALID_BODY: 3,        // Corpo da requisição inválido
    GROUP_NOT_FOUND: 4,     // Grupo não encontrado
    TEAM_NOT_FOUND: 5,      // Equipa não encontrada
    DUPLICATE_TEAM: 6,      // Equipa duplicada
    USER_NOT_FOUND: 7,      // Utilizador não encontrado
    DUPLICATE_USER: 8,      // Utilizador duplicado
    NOT_AUTHORIZED: 9,      // Não autorizado
    MISSING_TOKEN: 10,       // Token de autenticação em falta
    INVALID_ARGUMENT: 11
};
  
// Função construtora para criar objetos de erro personalizados
function Error(code, description) {
    this.code = code; // Código do erro
    this.description = description; // Descrição detalhada do erro
}
  
// Exporta um conjunto de funções para criar erros personalizados com base nos códigos definidos
export const errors = {
    // Erro de parâmetro em falta, recebe o nome do parâmetro como argumento
    MISSING_PARAMETER: (param) => {
        return new Error(ERROR_CODES.MISSING_PARAMETER, `Missing parameter: ${param}`);
    },
    // Erro de parâmetro inválido, recebe o nome do parâmetro como argumento
    INVALID_PARAMETER: (param) =>{
        return new Error(ERROR_CODES.INVALID_PARAMETER, `Invalid parameter: ${param}`);
    },
    // Erro de corpo da requisição inválido, não requer argumentos adicionais
    INVALID_BODY: () =>{
        return new Error(ERROR_CODES.INVALID_BODY, `Invalid request body`);
    },
    // Erro de grupo não encontrado ou não autorizado, recebe o ID do grupo como argumento
    GROUP_NOT_FOUND: (groupId) =>{
        return new Error(ERROR_CODES.GROUP_NOT_FOUND, `Group ${groupId} not found or unauthorized`);
    },
    // Erro de equipa não encontrada, recebe o ID da equipa como argumento
    TEAM_NOT_FOUND: (teamNameOrId) =>{
        const identifier = typeof teamNameOrId === 'string' ? `Team ${teamNameOrId}` : `Team ${teamNameOrId}`;
        return new Error(ERROR_CODES.TEAM_NOT_FOUND, `${identifier} not found`);
    },
    // Erro de equipa duplicada, recebe o ID da equipa como argumento
    DUPLICATE_TEAM: (teamNameOrId) =>{
        const identifier = typeof teamNameOrId === 'string' ? `Team ${teamNameOrId}` : `Team ${teamNameOrId}`;
        return new Error(ERROR_CODES.DUPLICATE_TEAM, `${identifier} already exists`);
    },
    // Erro de utilizador não encontrado, não requer argumentos adicionais
    USER_NOT_FOUND: () =>{
        return new Error(ERROR_CODES.USER_NOT_FOUND, `User not found`);
    },
    // Erro de utilizador duplicado, recebe o nome de utilizador como argumento
    DUPLICATE_USER: (username) =>{
        return new Error(ERROR_CODES.DUPLICATE_USER, `User ${username} already exists`);
    },
    // Erro de acesso não autorizado, recebe o recurso como argumento
    NOT_AUTHORIZED: (resource) =>{
        return new Error(ERROR_CODES.NOT_AUTHORIZED, `Not authorized to access ${resource}`);
    },
    // Erro de token de autenticação em falta, não requer argumentos adicionais
    MISSING_TOKEN: () =>{
        return new Error(ERROR_CODES.MISSING_TOKEN, `Missing authorization token`);
    },
    INVALID_ARGUMENT: (argName) => {
        return new Error(INTERNAL_ERROR_CODES.INVALID_ARGUMENT, `Invalid argument ${argName}`);
    }
};