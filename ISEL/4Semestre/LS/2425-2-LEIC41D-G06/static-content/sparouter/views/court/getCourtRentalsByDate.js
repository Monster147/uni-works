import {LIMIT} from "../../utils.js";
import {getPaginationParams} from "../viewsUtils.js";
import {fetchAndEnrichRentals, fetchCourtRentalsByDate} from "../data/rental.js";
import {emptyCourtRentalsByDateView} from "../components/court/emptyCourtRentalsByDateView.js";
import {getCourtsRentalsByDateView} from "../components/court/getCourtsRentalsByDateView.js";

/**
 * Renderiza a vista dos rentals de um court para uma data específica, com opções para criar,
 * eliminar e/ou atualizar rentals.
 *
 * Recolhe os rentals do court pertencente a um club, filtrados pela data fornecida.
 * Caso não existam rentals, apresenta uma vista informativa apropriada.
 *
 * @param {HTMLElement} mainContent Elemento HTML onde o conteúdo será inserido.
 * @param {Object} params Parâmetros da navegação, incluindo IDs, data e dados de paginação.
 * @param {string} params.clubId ID do club ao qual o court pertence.
 * @param {string} params.courtId ID do court cujos rentals vão ser apresentados.
 * @param {string} params.date Data dos rentals a filtrar.
 * @returns {Promise<void>} Promise que resolve quando a vista estiver renderizada.
 */

export default async function getCourtRentalsByDate(mainContent, params) {
    mainContent.style.minWidth = '1000px';
    document.querySelector('p.anim').textContent = "Welcome to our page Chelas Padel. Here you can find the all details of the courts rentals on the date you searched. You can also create a new rental, delete or update an existing one.";
    const clubId = params.clubId;
    const courtId = params.courtId;
    const date = params.date;
    const {skip, limit, previous, next} = getPaginationParams(params, LIMIT);
    const rentalData = await fetchCourtRentalsByDate(clubId, courtId, date)
    if (!rentalData || rentalData.error || rentalData.length === 0) {
        return emptyCourtRentalsByDateView(mainContent)
    } else {
        const enrichedRentals = await fetchAndEnrichRentals(rentalData)
        return getCourtsRentalsByDateView(mainContent, enrichedRentals, clubId, courtId, date, limit, skip);
    }
}
