import {API_BASE_URL, LIMIT, createButton, TODAY, handleSubmit} from "../../utils.js";
import {div, h1, ul, li, a, p, form, label, input} from "../../dls.js";
import {enrichRentals, getPaginationParams, renderListTable} from "../viewsUtils.js";
import {fetchAndEnrichRentals, fetchCourtRental} from "../data/rental.js";
import {fetchUsersByIds} from "../data/user.js";
import {fetchCourtsByIds} from "../data/court.js";
import fetchClubsByIds from "../data/club.js";
import {emptyCourtRentalsView} from "../components/court/emptyCourtRentalsView.js";
import {getCourtRentalsView} from "../components/court/getCourtRentalsView.js";

/**
 * Renderiza a vista dos rentals de um court específico, com opções para criar rentals
 * e pesquisar horas disponíveis por data.
 *
 * Recolhe os rentals do court correspondente a um club e apresenta-os paginados. Caso não existam,
 * mostra uma vista informativa apropriada.
 *
 * @param {HTMLElement} mainContent Elemento HTML onde o conteúdo será inserido.
 * @param {Object} params Parâmetros da navegação, incluindo IDs e dados de paginação.
 * @param {string} params.clubId ID do club ao qual o court pertence.
 * @param {string} params.courtId ID do court cujos rentals vão ser apresentados.
 * @returns {Promise<void>} Promise que resolve quando a vista estiver renderizada.
 */

export default async function getCourtRentals(mainContent, params) {
    mainContent.style.minWidth = '1000px';
    document.querySelector('p.anim').textContent = "Welcome to our page Chelas Padel. Here you can find all the rentals for a specific court and even create a new rental. You can also search for available hours for this court on a specific date.";

    const clubId = params.clubId;
    const courtId = params.courtId;
    const {skip, limit, previous, next} = getPaginationParams(params, LIMIT);
    const rentalData = await fetchCourtRental(clubId, courtId);
    if (!rentalData || rentalData.length === 0) {
        return emptyCourtRentalsView(mainContent)
    } else {
        const enrichedRentals = await fetchAndEnrichRentals(rentalData)
        return getCourtRentalsView(mainContent, enrichedRentals, clubId, courtId, limit, previous, next);
    }

}
