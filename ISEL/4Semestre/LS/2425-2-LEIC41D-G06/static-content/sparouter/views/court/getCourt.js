import {fetchCourtById} from "../data/court.js";
import {fetchClubById} from "../data/club.js";
import {getCourtView} from "../components/court/getCourtView.js";

/**
 * Renderiza a vista com os detalhes de um court permitindo, também, a pesquisa de rentals por data.
 *
 * Obtém os dados do court e do respetivo club a que pertence e apresenta-os na 'interface'.
 *
 * @param {HTMLElement} mainContent Elemento HTML onde o conteúdo será inserido.
 * @param {Object} params Parâmetros da navegação que incluem o ID do court.
 * @param {string} params.id ID do court.
 * @returns {Promise<void>} Promise que resolve quando a vista estiver renderizada.
 */

export default async function getCourt(mainContent, params) {
    mainContent.style.minWidth = '100px';
    document.querySelector('p.anim').textContent = "Welcome to our page Chelas Padel. Here you can find the details of the court and search for rentals by date.";

    const courtId = params.id;

    const court = await fetchCourtById(courtId);
    const club = await fetchClubById(court.club);

    return getCourtView(mainContent, club, court);
}