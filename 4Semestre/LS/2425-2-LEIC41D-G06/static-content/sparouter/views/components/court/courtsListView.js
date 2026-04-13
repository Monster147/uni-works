import {renderListTable} from "../../viewsUtils.js";
import {createButton} from "../../../utils.js";
import {a, div, h1} from "../../../dls.js";
import courtList from "../../court/courtList.js";

/**
 * Renderiza a vista paginada da lista de courts associados a um clube.
 *
 * Esta função apresenta uma tabela com os courts paginados, botões para navegar entre páginas,
 * um título com o ID do club e um link para criar um court associado ao club.
 *
 * @param {HTMLElement} mainContent Elemento HTML onde o conteúdo será renderizado.
 * @param {Array<Object>} transformedCourts Lista de courts já transformados para exibição.
 * @param {string} clubId Identificador do club a que os courts pertencem.
 * @param {number} skip Índice inicial para paginação.
 * @param {number} limit Número máximo de courts a apresentar por página.
 * @param {number} previous Índice do início da página anterior (para paginação).
 * @param {number} next Índice do início da próxima página (para paginação).
 * @returns {Promise<void>} Promise que resolve quando a vista dos courts for renderizada.
 */

export async function courtsListView(mainContent, transformedCourts, clubId, skip, limit, previous, next) {
    const paginatedCourts = transformedCourts.slice(previous, next);
    const courtTable = await renderListTable(paginatedCourts, ["id", "club"], ["courts", "clubs"]);

    const prevButton = await createButton("Previous", previous > 0, () => {
        courtList(mainContent, {id: clubId, skip: skip - limit, limit});
    });

    const nextButton = await createButton("Next", next < transformedCourts.length, () => {
        courtList(mainContent, {id: clubId, skip: next, limit});
    });

    const navigation = await div(prevButton, nextButton);

    const content = await div(
        await h1("Courts of club: " + clubId),
        await a({href: "#courts/create/clubId/" + clubId}, "Create a new court"),
        courtTable,
        navigation
    );

    mainContent.replaceChildren(content);
}