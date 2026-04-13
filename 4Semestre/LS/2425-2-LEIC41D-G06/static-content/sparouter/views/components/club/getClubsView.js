import {a, div, h1, p} from "../../../dls.js";
import {renderListTable} from "../../viewsUtils.js";
import {createButton} from "../../../utils.js";
import getClubs from "../../club/getClubs.js";

/**
 * Renderiza a vista da lista de clubes com navegação por páginas.
 *
 * Esta função apresenta uma tabela com os clubes disponíveis, controlos de navegação
 * (previous/next) para a paginação e um link para criar um clube.
 * Se não existirem clubes, mostra uma mensagem adequada.
 *
 * @param {HTMLElement} mainContent Elemento HTML onde o conteúdo será renderizado.
 * @param {Array<{ id: string, name: string }>} clubs Lista completa de clubes.
 * @param {number} previous Índice de início da página anterior (para paginação).
 * @param {number} next Índice de início da próxima página (para paginação).
 * @param {number} limit Número de clubes por página.
 * @returns {Promise<void>} Promise que resolve quando o conteúdo é totalmente renderizado.
 */

export async function getClubsView(mainContent, clubs, previous, next, limit) {
    let content;
    if (!clubs || clubs.length === 0) {
        content = await div(
            await h1("Clubs"),
            await a({href: "#clubs/create"}, "Create a Club"),
            await p("No clubs available.")
        );
    } else {
        const paginatedClubs = clubs.slice(previous, next);
        const table = await renderListTable(paginatedClubs, ["name"], ["clubs"]);

        const prevButton = await createButton("Previous", previous > 0, () => {
            getClubs(mainContent, {skip: previous - limit, limit});
        });

        const nextButton = await createButton("Next", next < clubs.length, () => {
            getClubs(mainContent, {skip: next, limit});
        });

        const navigation = await div(prevButton, nextButton);

        content = await div(
            await h1("Clubs"),
            await a({href: "#clubs/create"}, "Create a Club"),
            table,
            navigation
        );
    }
    mainContent.replaceChildren(content);
}