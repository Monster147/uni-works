import {fetchRentalById} from "../data/rental.js";
import {fetchUserById} from "../data/user.js";
import {fetchCourtById} from "../data/court.js";
import {fetchClubById} from "../data/club.js";
import {getRentalEmptyView} from "../components/rental/getRentalEmptyView.js";
import {getRentalView} from "../components/rental/getRentalView.js";

/**
 * Renderiza a vista dos detalhes de um rental.
 * Caso o rental não exista, mostra uma vista vazia.
 * Permite atualizar ou eliminar o rental.
 *
 * @param {HTMLElement} mainContent Elemento HTML onde o conteúdo será inserido.
 * @param {Object} params Parâmetros que incluem o ID do rental.
 * @param {string} params.id ID do rental a mostrar.
 * @returns {Promise<void>} Promise que resolve quando a vista estiver renderizada.
 */

export default async function getRental(mainContent, params) {
    mainContent.style.minWidth = '100px';
    document.querySelector('p.anim').textContent = "Welcome to our page Chelas Padel. Here you can find the details of the rental and u can even update it or delete it if u want to.";
    const rentalId = params.id;
    const rental = await fetchRentalById(rentalId);
    if (!rental || !rental.id) {
        return getRentalEmptyView(mainContent)
    } else {
        const user = await fetchUserById(rental.user)
        const court = await fetchCourtById(rental.court)
        const club = await fetchClubById(rental.club)
        return getRentalView(mainContent, rental, user, court, club);
    }
}