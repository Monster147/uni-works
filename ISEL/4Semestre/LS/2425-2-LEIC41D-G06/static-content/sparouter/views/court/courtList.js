import {LIMIT} from "../../utils.js";
import {getPaginationParams} from "../viewsUtils.js";
import fetchCourtsByClub, {fetchAndEnrichCourts} from "../data/court.js";
import {courtsListView} from "../components/court/courtsListView.js";
import {emptyCourtsListView} from "../components/court/emptyCourtsListView.js";

/**
 * Renderiza a lista de courts de um determinado club, com suporte a paginação.
 *
 * Caso não existam courts, renderiza uma vista informativa a indicar a ausência.
 * Caso existam, apresenta uma tabela paginada com os courts disponíveis.
 *
 * @param {HTMLElement} mainContent Elemento HTML onde o conteúdo será inserido.
 * @param {Object} params Parâmetros da navegação, incluindo o ID do club e os parâmetros de paginação.
 * @param {string} params.id ID do club cujos courts devem ser mostrados.
 * @param {number} params.skip Índice inicial para paginação.
 * @param {number} params.limit Número máximo de courts a mostrar por página.
 * @param {number} params.previous Índice para a página anterior.
 * @param {number} params.next Índice para a próxima página.
 * @returns {Promise<void>} Promise que resolve quando a vista estiver renderizada.
 */

export default async function courtList(mainContent, params) {
    mainContent.style.minWidth = '100px';
    document.querySelector('p.anim').textContent = "Welcome to our page Chelas Padel. Here you can find all the courts of a specific club and even create a new court.";
    const clubId = params.id;
    const {skip, limit, previous, next} = getPaginationParams(params, LIMIT);
    const courtsData = await fetchCourtsByClub(clubId);
    if (!courtsData || courtsData.length === 0) {
        return emptyCourtsListView(mainContent, clubId);
    } else {
        const transformedCourts = await fetchAndEnrichCourts(courtsData)
        return courtsListView(mainContent, transformedCourts, clubId, skip, limit, previous, next);
    }
}