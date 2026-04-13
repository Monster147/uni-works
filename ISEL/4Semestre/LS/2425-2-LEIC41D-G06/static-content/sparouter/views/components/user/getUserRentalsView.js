import {createButton} from "../../../utils.js";
import {renderListTable} from "../../viewsUtils.js";
import {a, div, h1} from "../../../dls.js";
import getUserRentals from "../../user/getUserRentals.js";

/**
 * Renderiza a vista dos rentals de um user, com paginação e opções para atualizar e/ou eliminar cada rental.
 *
 * @param {HTMLElement} mainContent Elemento HTML onde o conteúdo será inserido.
 * @param {Array} enrichedRentals Lista de rentals enriquecidos com informação adicional.
 * @param {number} skip Índice inicial para paginação.
 * @param {number} limit Número máximo de rentals a mostrar por página.
 * @param {number} next Índice para a próxima página.
 * @param {number} previous Índice para a página anterior.
 * @param {string} userId ID do user cujos rentals estão a ser mostrados.
 * @returns {Promise<void>} Promise que resolve quando a vista estiver renderizada.
 */

export async function getUserRentalsView(mainContent, enrichedRentals, skip, limit, next, previous, userId) {
    const paginatedRentals = enrichedRentals.slice(skip, skip + limit);
    const paginatedRentalsDelete = await Promise.all(
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
    const rentalTable = await renderListTable(paginatedRentalsDelete, ["id", "user"], ["rentals", "users"]);

    const prevButton = await createButton("Previous", previous > 0, () => {
            getUserRentals(mainContent, {id: userId, skip: skip - limit, limit: limit});
        }
    );

    const nextButton = await createButton("Next", next < enrichedRentals.length, () => {
            getUserRentals(mainContent, {id: userId, skip: next, limit: limit})
        }
    );

    const navigation = await div(prevButton, nextButton);

    const content = await div(
        await h1("User Rentals"),
        await a({href: "#rentals/create"}, "Create a Rental"),
        rentalTable,
        navigation
    );
    mainContent.replaceChildren(content);
}