import {a, div, h1, p} from "../../../dls.js";

/**
 * Renderiza uma vista indicando que não existem rentals associados ao user.
 * Apresenta uma mensagem e um link para criar um rental.
 *
 * @param {HTMLElement} mainContent Elemento HTML onde o conteúdo será inserido.
 * @returns {Promise<void>} Promise que resolve quando a vista estiver renderizada.
 */

export async function getUserRentalsEmpty(mainContent) {
    const content = await div(
        await h1("User Rentals"),
        await p("No rentals available."),
        await a({href: "#rentals/create"}, "Create a Rental")
    );

    mainContent.replaceChildren(content)
}