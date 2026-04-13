import {API_BASE_URL, LIMIT, createButton, verifyUser} from "../../utils.js";
import {div, p, button, h1} from "../../dls.js";
import {fetchDeleteRental, fetchRentalById} from "../data/rental.js";
import notLoggedIn from "../account/notLoggedIn.js";
import {fetchUserById} from "../data/user.js";
import {cantModify} from "../viewsUtils.js";
import {deleteRentalView} from "../components/rental/deleteRentalView.js";

/**
 * Renderiza a vista para confirmação de eliminação de um rental.
 * Permite ao user confirmar ou cancelar a eliminação.
 *
 * @param {HTMLElement} mainContent Elemento HTML onde o conteúdo será inserido.
 * @param {Object} params Parâmetros, incluindo o ID do rental a eliminar.
 * @param {string} params.id ID do rental a eliminar.
 * @returns {Promise<void>} Promise que resolve quando a vista estiver renderizada.
 */

export default async function deleteRental(mainContent, params) {
    const rentalId = params.id;
    const rental = await fetchRentalById(rentalId);
    const user = await fetchUserById(rental.user);
    if(await verifyUser(user.token)) {
        await deleteRentalView(mainContent, rentalId)
    } else {
        await cantModify(mainContent)
    }
}
