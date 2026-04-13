import {fetchClubsList} from "../data/club.js";
import {createRentalView} from "../components/rental/createRentalView.js";
import fetchCourtsByClub from "../data/court.js";
import notLoggedIn from "../account/notLoggedIn.js";

/**
 * Renderiza a vista para criar um novo rental, carregando a lista de clubs e respetivos courts.
 *
 * @param {HTMLElement} mainContent Elemento HTML onde o conteúdo será inserido.
 * @returns {Promise<void>} Promise que resolve quando a vista estiver renderizada.
 */
export default async function createRental(mainContent) {
    mainContent.style.minWidth = '100px';
    document.querySelector('p.anim').textContent = "Welcome to our page Chelas Padel. Here you can create a new rental. Please select a club and court, then specify the rental date and duration.";
    const clubs = await fetchClubsList();
    const courtsByClub = {};
    for (const club of clubs) {
        courtsByClub[club.id] = await fetchCourtsByClub(club.id);
    }
    return createRentalView(mainContent, clubs, courtsByClub);
}
