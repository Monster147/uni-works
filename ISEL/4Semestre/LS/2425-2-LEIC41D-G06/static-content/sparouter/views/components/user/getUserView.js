import {a, div, h1, li, ul} from "../../../dls.js";

/**
 * Renderiza a vista de detalhes de um user.
 *
 * Mostra o nome, ID, email do user e um link para a lista de rentals associados.
 *
 * @param {HTMLElement} mainContent Elemento HTML onde o conteúdo será inserido.
 * @param {Object} user Objeto que representa o user cujos detalhes serão mostrados.
 * @returns {Promise<void>} Promise que resolve quando a vista estiver renderizada.
 */

export async function getUserView(mainContent, user) {
    const userDetails = await Promise.all([
        li("Name: " + user.name),
        li("Id: " + user.id),
        li("Owner: " + user.email),
        li(
            await a({href: "#rentals/users/" + user.id}, "Rentals")
        )
    ]);

    const userDetailList = await ul(...userDetails);

    const content = await div(
        await h1("User"),
        userDetailList
    );

    mainContent.replaceChildren(content);
}