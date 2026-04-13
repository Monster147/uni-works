import {a, div, h1, p} from "../../../dls.js";

/**
 * Renderiza uma vista indicando que não existem rentals de campos disponíveis.
 *
 * Esta função apresenta um título, um link para criar um rentals
 * e uma mensagem a informar que não existem rentals registados.
 *
 * @param {HTMLElement} mainContent Elemento HTML onde o conteúdo será renderizado.
 * @returns {Promise<void>} Promise que resolve quando a vista for totalmente renderizada.
 */

export async function emptyCourtRentalsView(mainContent){
    const content = await div(
        await h1("Court Rentals"),
        await a({href: "#rentals/create"}, "Create a Rental"),
        await p("No rentals available.")
    );

    mainContent.replaceChildren(content)
}