import notLoggedIn from "../account/notLoggedIn.js";
import {createCourtView} from "../components/court/createCourtView.js";

/**
 * Renderiza a vista para criar um court para um determinado club.
 *
 * Apresenta um formulário para inserção do nome do court e trata o envio dos dados para a API.
 *
 * @param {HTMLElement} mainContent Elemento HTML onde o conteúdo será inserido.
 * @param {Object} params Parâmetros da navegação que incluem o ID do club.
 * @param {string} params.id ID do club onde o court será criado.
 * @returns {Promise<void>} Promise que resolve quando a vista estiver renderizada.
 */

export default async function createCourt(mainContent, params) {
    notLoggedIn(mainContent);
    const clubId = params.id;
    await createCourtView(mainContent, clubId);
}