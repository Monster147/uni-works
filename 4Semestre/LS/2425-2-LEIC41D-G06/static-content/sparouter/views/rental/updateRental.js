import {verifyUser} from "../../utils.js";
import {fetchRentalById} from "../data/rental.js";
import {fetchUserById} from "../data/user.js";
import {cantModify} from "../viewsUtils.js";
import {updateRentalView} from "../components/rental/updateRentalView.js";

/**
 * Renderiza a vista para atualizar um rental.
 * Permite alterar a data, hora de início e/ou fim do rental.
 * Inclui botões para confirmar ou cancelar a atualização.
 *
 * @param {HTMLElement} mainContent Elemento HTML onde o conteúdo será inserido.
 * @param {Object} params Parâmetros, incluindo o ID do rental.
 * @param {string} params.id ID do rental a atualizar.
 * @returns {Promise<void>} Promise que resolve quando a vista estiver renderizada.
 */

export default async function updateRental(mainContent, params) {
    const rentalId = params.id;
    const rental = await fetchRentalById(rentalId)
    const user = await fetchUserById(rental.user);
    if(await verifyUser(user.token)){
        await updateRentalView(mainContent, rental, rentalId)
    } else {
        await cantModify(mainContent)
    }
}