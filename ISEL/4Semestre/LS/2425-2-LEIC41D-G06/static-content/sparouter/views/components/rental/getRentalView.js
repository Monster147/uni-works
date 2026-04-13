import {a, div, h1, li, ul} from "../../../dls.js";
import {createButton} from "../../../utils.js";

/**
 * Renderiza a vista com os detalhes de um rental específico, incluindo links para o user e o court relacionados
 * e botões para eliminar e/ou atualizar o rental.
 *
 * @param {HTMLElement} mainContent Elemento HTML onde o conteúdo será apresentado.
 * @param {Object} rental Objeto com os detalhes do rental.
 * @param {Object} user Objeto com os dados do user associado ao rental.
 * @param {Object} court Objeto com os dados do court associado ao rental.
 * @param {Object} club Objeto com os dados do club associado ao rental.
 * @returns {Promise<void>} Promise que resolve quando a vista estiver totalmente renderizada.
 */

export async function getRentalView(mainContent, rental, user, court, club) {
    const rentalDetails = await Promise.all([
        li("Id: " + rental.id),
        li("Date: " + rental.date),
        li("StartDuration: " + rental.startDuration),
        li("EndDuration: " + rental.endDuration),
        li(
            await a({href: "#users/" + rental.user}, "User: " + user.name)
        ),
        li(
            await a({href: "#courts/" + rental.court}, "Court: " + court.name)
        ),
        li("Club: " + club.name),
        li(
            await createButton("Delete", true, () => {
                window.location.href = "#rentals/" + rental.id + "/delete";
            }, true),
            await createButton("Update", true, () => {
                window.location.href = "#rentals/" + rental.id + "/update";
            }, true)
        )
    ]);

    const content = await div(
        await h1("Rental Details"),
        await ul(...rentalDetails)
    );

    mainContent.replaceChildren(content);
}