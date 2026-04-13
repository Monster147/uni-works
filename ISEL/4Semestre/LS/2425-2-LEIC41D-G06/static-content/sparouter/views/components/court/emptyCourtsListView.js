import {a, div, h1, p} from "../../../dls.js";

/**
 * Renderiza uma vista indicando que não existem courts disponíveis para um club específico.
 *
 * Esta função apresenta um título, um link para criar um court associado ao club
 * e uma mensagem a informar que não existem courts registados.
 *
 * @param {HTMLElement} mainContent Elemento HTML onde o conteúdo será renderizado.
 * @param {string} clubId Identificador do club para o qual a lista de courts está vazia.
 * @returns {Promise<void>} Promise que resolve quando a vista for totalmente renderizada.
 */

export async function emptyCourtsListView(mainContent, clubId) {
    const content = await div(
        await h1("Courts"),
        await a({href: "#courts/create/clubId/" + clubId}, "Create a new court"),
        await p("No courts available.")
    );
    mainContent.replaceChildren(content)
}