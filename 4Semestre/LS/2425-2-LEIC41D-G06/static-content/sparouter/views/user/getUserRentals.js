import {LIMIT} from "../../utils.js";
import {getPaginationParams} from "../viewsUtils.js";
import {fetchAndEnrichRentals, fetchUserRental} from "../data/rental.js";
import {getUserRentalsEmpty} from "../components/user/getUserRentalsEmpty.js";
import {getUserRentalsView} from "../components/user/getUserRentalsView.js";

/**
 * Renderiza a vista com todos os rentals do user, permitindo criar, atualizar e/ou eliminar rentals.
 *
 * @param {HTMLElement} mainContent Elemento HTML onde o conteúdo será inserido.
 * @param {Object} params Parâmetros da rota, incluindo o ID do user.
 * @param {string} params.id ID do user cujos rentals serão mostrados.
 * @returns {Promise<void>} Promise que resolve quando a vista estiver renderizada.
 */

export default async function getUserRentals(mainContent, params) {
    mainContent.style.minWidth = '1000px';
    document.querySelector('p.anim').textContent = "Welcome to our page Chelas Padel. Here you can find all the rentals of the user modify them (delete them or update them) and even create a new rental.";
    const userId = params.id;
    const {skip, limit, previous, next} = getPaginationParams(params, LIMIT);
    const rentalData = await fetchUserRental(userId)
    if (!rentalData || rentalData.length === 0) {
        return getUserRentalsEmpty(mainContent);
    } else {
        const enrichedRentals = await fetchAndEnrichRentals(rentalData);
        return getUserRentalsView(mainContent, enrichedRentals, skip, limit, next, previous, userId);
    }
}