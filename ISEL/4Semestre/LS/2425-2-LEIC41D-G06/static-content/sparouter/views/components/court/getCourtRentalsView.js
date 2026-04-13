import {API_BASE_URL, createButton, handleSubmit, TODAY} from "../../../utils.js";
import {renderListTable} from "../../viewsUtils.js";
import {a, div, form, h1, input, label} from "../../../dls.js";
import getCourtRentals from "../../court/getCourtRentals.js";

/**
 * Renderiza a vista dos alugueres de um court específico, com paginação, ações e formulário para pesquisa de horas disponíveis.
 *
 * Esta função apresenta uma tabela com os rentals enriquecidos (incluindo botões para apagar e atualizar),
 * controlos de navegação (previous/next) para paginação, um link para criar um rental,
 * e um formulário para o user pesquisar as horas disponíveis para um dado dia no court.
 *
 * @param {HTMLElement} mainContent Elemento HTML onde o conteúdo será renderizado.
 * @param {Array<Object>} enrichedRentals Lista de rentals já enriquecidos para exibição.
 * @param {string} clubId Identificador do club associado ao court.
 * @param {string} courtId Identificador do court.
 * @param {number} limit Número máximo de rentals a apresentar por página.
 * @param {number} previous Índice do início da página anterior (para paginação).
 * @param {number} next Índice do início da próxima página (para paginação).
 * @returns {Promise<void>} Promise que resolve quando a vista for totalmente renderizada.
 */

export async function getCourtRentalsView(mainContent, enrichedRentals, clubId, courtId, limit, previous, next){
    const paginatedRentals = enrichedRentals.slice(previous, next);
    const paginatedRentalsWithButtons = await Promise.all(
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
    const rentalTable = renderListTable(paginatedRentalsWithButtons, ["id", "court"], ["rentals", "courts"]);

    const prevButton = await createButton("Previous", previous > 0, () => {
            getCourtRentals(mainContent, {clubId, courtId, skip: previous - limit, limit});
        }
    );

    const nextButton = await createButton("Next", next < enrichedRentals.length, () => {
            getCourtRentals(mainContent, {clubId, courtId, skip: next, limit})
        }
    );

    const navigation = await div(prevButton, nextButton);

    const rentalHoursForm = await form(
        await label({for: "rentalDate"}, "Get available hours for this court:"),
        await input({type: "date", id: "rentalDate", value: TODAY, min: TODAY}),
        await input({type: "submit", value: "Search"})
    )

    rentalHoursForm.addEventListener("submit", (e) => {
        const rentalDate = document.querySelector("#rentalDate").value;
        handleSubmit(
            e,
            API_BASE_URL + "/rentals/clubs/" + clubId + "/courts/" + courtId + "/date/" + rentalDate,
            "GET",
            undefined, undefined,
            (data) => {
                console.log(data);
                window.location.href = "#rentals/available/clubs/" + clubId + "/courts/" + courtId + "/date/" + rentalDate;
            },
            (error) => {
                const parsedError = JSON.parse(error);
                alert(`Error getting available hours: ${parsedError.error}`);
                console.error(parsedError.error)
            }
        )
    });

    const content = await div(
        await h1("Court Rentals"),
        await a({href: "#rentals/create"}, "Create a Rental"),
        rentalTable,
        navigation,
        rentalHoursForm
    );

    mainContent.replaceChildren(content);
}