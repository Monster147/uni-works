import {fetchClubById} from "../data/club.js";
import fetchUserByClub from "../data/user.js";
import {getClubView} from "../components/club/getClubView.js";

/**
 * Função assíncrona que obtém os dados de um club e apresenta os seus detalhes na página.
 *
 * Esta função atualiza o texto introdutório e define a largura mínima do conteúdo principal.
 * Em seguida, utiliza o valor 'params.id' para obter os dados do club e do user associado,
 * e renderiza a vista detalhada correspondente.
 *
 * @param {HTMLElement} mainContent Elemento HTML onde os detalhes do club serão renderizados.
 * @param {{ id: string }} params Objeto que contém o identificador do club a ser buscado.
 * @returns {Promise<void>}  Promise que resolve quando a vista do club é renderizada com sucesso.
 */
export default async function getClub(mainContent, params) {
    document.querySelector('p.anim').textContent = "Welcome to our page Chelas Padel. Here you can find the all details of the club.";
    mainContent.style.minWidth = '100px';
    const clubId = params.id;

    const club = await fetchClubById(clubId);
    const user = await fetchUserByClub(club);

    return getClubView(mainContent, club, user);
}
