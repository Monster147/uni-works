import notLoggedIn from "../account/notLoggedIn.js";
import {createClubView} from "../components/club/createClubView.js";

/**
 * Função assíncrona que cria e gere um formulário para a criação de um novo club na página.
 *
 * Esta função atualiza o elemento 'mainContent' com um formulário onde o user pode
 * introduzir o nome de um novo club. Ao submeter o formulário, é feito um pedido POST ao
 * endpoint '/clubs/', incluindo o nome do club e o token de autorização.
 *
 * Dá feedback ao user em caso de sucesso ou erro e redireciona para a lista de clubs
 * se a criação for bem-sucedida.
 *
 * @param {HTMLElement} mainContent Elemento HTML onde o formulário será inserido.
 * @returns {Promise<void>} Promise que termina quando o formulário é renderizado e o evento de submissão está configurado.
 */
export default async function createClub(mainContent) {
    notLoggedIn(mainContent);
    await createClubView(mainContent);
}