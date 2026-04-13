import {fetchRentalAvailableHours} from "../data/rental.js";
import {fetchClubById} from "../data/club.js";
import {fetchCourtById} from "../data/court.js";
import {getAvailableHoursView} from "../components/court/getAvailableHoursView.js";

/**
 * Renderiza a vista com as horas disponíveis para um court num determinado club e data.
 *
 * Obtém os dados do club, court e respetivas horas disponíveis e apresenta-os na ‘interface’.
 *
 * @param {HTMLElement} mainContent Elemento HTML onde o conteúdo será inserido.
 * @param {Object} params Parâmetros da navegação que incluem os IDs do club e court e a data.
 * @param {string} params.clubId ID do club.
 * @param {string} params.courtId ID do court.
 * @param {string} params.date Data para a qual se pretendem ver as horas disponíveis (formato ISO).
 * @returns {Promise<void>} Promise que resolve quando a vista estiver renderizada.
 */

export default async function getAvailableHours(mainContent, params) {
    mainContent.style.minWidth = '100px';
    document.querySelector('p.anim').textContent = "Welcome to our page Chelas Padel. Here you can find the available hours for a specific court on a specific date.";

    const clubId = params.clubId;
    const courtId = params.courtId;
    const date = params.date;
    const availableHours = await fetchRentalAvailableHours(clubId, courtId, date);
    const club = await fetchClubById(clubId)
    const court = await fetchCourtById(courtId)
    return getAvailableHoursView(mainContent, club, court, availableHours);
}