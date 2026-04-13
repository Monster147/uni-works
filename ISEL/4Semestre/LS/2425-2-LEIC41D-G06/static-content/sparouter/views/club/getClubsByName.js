import {fetchClubsByName} from "../data/club.js";
import {getClubsByNameView} from "../components/club/getClubsByNameView.js";

/**
 * Função assíncrona que obtém e apresenta os clubs cujo nome corresponde ao critério de pesquisa.
 *
 * Esta função define uma largura mínima no conteúdo principal, atualiza a mensagem de boas-vindas
 * e utiliza o valor 'params.name' para buscar clubs que correspondam ao nome fornecido.
 * Em seguida, renderiza a vista com os resultados da pesquisa.
 *
 * @param {HTMLElement} mainContent Elemento HTML onde os resultados da pesquisa serão apresentados.
 * @param {{ name: string }} params Objeto que contém o nome do clube a ser pesquisado.
 * @returns {Promise<void>} Promise que resolve quando a vista dos clubs filtrados é renderizada.
 */

export default async function getClubsByName(mainContent, params) {
    mainContent.style.minWidth = '100px';
    document.querySelector('p.anim').textContent = "Welcome to our page Chelas Padel. Here are all the clubs that match your search criteria.";
    const clubName = params.name;
    const clubs = await fetchClubsByName(clubName);
    return getClubsByNameView(mainContent, clubs);
}