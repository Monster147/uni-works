import {a, div, form, h1, input, label, li, ul} from "../../../dls.js";
import {API_BASE_URL, handleSubmit, TODAY} from "../../../utils.js";

/**
 * Renderiza a vista com os detalhes de um court específico e um formulário para pesquisar rentals por data.
 *
 * Esta função apresenta informações do court (ID, nome, club associado),
 * um link para os rentals desse court
 * e um formulário que permite ao user pesquisar rentals num dia específico.
 * Ao submeter o formulário, um pedido GET é feito para obter os rentals filtrados pela data.
 *
 * @param {HTMLElement} mainContent Elemento HTML onde o conteúdo será renderizado.
 * @param {{name: string}} club Objeto com dados do club associado ao court.
 * @param {{id: string, name: string, club: string}} court Objeto com dados do court.
 * @returns {Promise<void>} Promise que resolve quando a vista for totalmente renderizada.
 */

export async function getCourtView(mainContent, club, court){
    const courtDetails = await Promise.all([
        li("Id: " + court.id),
        li("Name: " + court.name),
        li(
            await a({href: "#clubs/" + court.club}, "Club: " + club.name)
        ),
        li(
            await a({href: "#rentals/clubs/" + court.club + "/courts/" + court.id}, "Rentals")
        )
    ]);

    const courtDetail = await ul(...courtDetails);

    const rentalForm = await form(
        await label({for: "rentalDate"}, "Search Rentals by Date: "),
        await input({type: "date", id: "rentalDate", value: TODAY}),
        await input({type: "submit", value: "Search"})
    );

    rentalForm.addEventListener("submit", (e) => {
        const rentalDate = document.querySelector("#rentalDate").value;
        handleSubmit(
            e,
            API_BASE_URL + "/rentals/clubs/" + court.club + "/courts/" + court.id + "/date/" + rentalDate,
            "GET",
            undefined, undefined,
            (data) => {
                console.log(data);
                window.location.href = "#rentals/clubs/" + court.club + "/courts/" + court.id + "/date/" + rentalDate;
            },
            (error) => {
                const parsedError = JSON.parse(error);
                alert(`Error searching for rentals: ${parsedError.error}`);
                console.error(parsedError.error)
            }
        );
    });

    const content = await div(
        await h1("Court Details"),
        courtDetail,
        rentalForm
    );

    mainContent.replaceChildren(content);
}