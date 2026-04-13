import {LIMIT} from "../../utils.js";
import {getPaginationParams} from "../viewsUtils.js";
import {fetchClubsList} from "../data/club.js";
import {getClubsView} from "../components/club/getClubsView.js";

/**
 * Função assíncrona que obtém a lista de clubs e apresenta a vista correspondente na página.
 *
 * Esta função define uma largura mínima no conteúdo principal, atualiza a mensagem de boas-vindas,
 * obtém parâmetros de paginação com base em 'params' e carrega a lista de clubs a partir da API.
 * Por fim, chama a função de renderização da vista dos clubs.
 *
 * @param {HTMLElement} mainContent Elemento HTML onde a lista de clubs será renderizada.
 * @param {Object} params Objeto contendo parâmetros da rota, como dados de paginação.
 * @returns {Promise<void>} Promise que resolve quando a lista de clubs é apresentada.
 */

export default async function getClubs(mainContent, params) {
    mainContent.style.minWidth = '100px';
    document.querySelector('p.anim').textContent = "Welcome to our page Chelas Padel. Here you can find all the clubs and even create a new one.";
    const {skip, limit, previous, next} = getPaginationParams(params, LIMIT);
    const clubs = await fetchClubsList();
    return getClubsView(mainContent, clubs, previous, next, limit);
}
