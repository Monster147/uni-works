import {createButton} from "../../../utils.js";
import {a, div, h1} from "../../../dls.js";
import getCourtRentalsByDate from "../../court/getCourtRentalsByDate.js";
import {renderListTable} from "../../viewsUtils.js";

/**
 * Renderiza a vista dos rentals de um court num dia específico, com paginação e ações para atualização e/ou remoção.
 *
 * Esta função apresenta uma tabela com os rentals filtrados pela data,
 * inclui botões para apagar e atualizar cada rental
 * e controlos de navegação para páginas anteriores e seguintes.
 * Também inclui um link para criar um rental.
 *
 * @param {HTMLElement} mainContent Elemento HTML onde o conteúdo será renderizado.
 * @param {Array<Object>} enrichedRentals Lista de rentals enriquecidos filtrados pela data.
 * @param {string} clubId Identificador do club associado ao court.
 * @param {string} courtId Identificador do court.
 * @param {string} date Data para a qual os rentals são filtrados (formato ISO, ex: "YYYY-MM-DD").
 * @param {number} limit Número máximo de rentals a apresentar por página.
 * @param {number} skip Índice para o início da página atual (para paginação).
 * @returns {Promise<void>} Promise que resolve quando a vista for renderizada.
 */

export async function getCourtsRentalsByDateView(mainContent, enrichedRentals, clubId, courtId, date, limit, skip){
    const paginatedRentals = enrichedRentals.slice(skip, skip + limit);
    const paginatedRentalsDateDelete = await Promise.all(
        paginatedRentals.map(async rental => {
            const deleteButton = await createButton("Delete", true, () => {
                window.location.href = "#rentals/" + rental.id + "/delete";
            }, true);
            const updateButton = await createButton("Update", true, () => {
                window.location.href = "#rentals/" + rental.id + "/update";
            }, true);
            return {
                ...rental,
                delete: deleteButton,
                update: updateButton
            };
        })
    );
    const rentalTable = renderListTable(paginatedRentalsDateDelete, ["id", "court"], ["rentals", "courts"]);

    const prevButton = await createButton("Previous", skip > 0, () => {
        getCourtRentalsByDate(mainContent, {clubId, courtId, date, skip: skip - limit, limit});
    });

    const nextButton = await createButton("Next", skip + limit < enrichedRentals.length, () => {
        getCourtRentalsByDate(mainContent, {clubId, courtId, date, skip: skip + limit, limit});
    });

    const navigation = await div(prevButton, nextButton);

    const content = await div(
        await h1("Court Rentals"),
        await a({href: "#rentals/create"}, "Create a Rental"),
        rentalTable,
        navigation
    );

    mainContent.replaceChildren(content);
}