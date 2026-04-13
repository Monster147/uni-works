import {form, input, label, option, select} from "../../../dls.js";
import {API_BASE_URL, handleSubmit, TODAY} from "../../../utils.js";
import notLoggedIn from "../../account/notLoggedIn.js";

/**
 * Renderiza um formulário para criar um rental, permitindo ao user selecionar club, court e detalhes do rental.
 *
 * O formulário atualiza dinamicamente as opções de courts conforme o club selecionado.
 * Ao submeter, envia um pedido POST para criar o rental com os dados fornecidos.
 *
 * @param {HTMLElement} mainContent Elemento HTML onde o formulário será renderizado.
 * @param {Array<{id: string, name: string}>} clubs Lista de clubs disponíveis para seleção.
 * @param {Object<string, Array<{id: string, name: string}>>} courtsByClub Objeto que mapeia clubId para lista de courts desse club.
 * @returns {Promise<void>} Promise que resolve quando o formulário estiver renderizado e configurado.
 */

export async function createRentalView(mainContent, clubs, courtsByClub) {
    const selectClub = await select(
        {id: "clubSelect"},
        ...clubs.map(club => option({value: club.id}, club.name))
    );

    const selectCourt = await select({id: "courtSelect"});

    selectClub.addEventListener("change", async () => {
        const clubId = selectClub.value;
        const courts = courtsByClub[clubId] || [];
        selectCourt.innerHTML = "";
        for (const court of courts) {
            const courtOption = await option(
                {value: court.id},
                court.name
            );
            selectCourt.appendChild(courtOption);
        }
    });

    if (clubs.length > 0) {
        selectClub.dispatchEvent(new Event("change"));
    }

    const rentalForm = await form(
        await label({for: "clubSelect"}, "Select Club:"),
        selectClub,
        await label({for: "courtSelect"}, "Select Court:"),
        selectCourt,
        await label({for: "rentalDate"}, "Rental Date:"),
        await input({type: "date", id: "rentalDate", value: TODAY, min: TODAY}),
        await label({for: "startDuration"}, "Start Duration: "),
        await input({type: "number", id: "startDuration", value: "0", min: "0", max: "22"}),
        await label({for: "endDuration"}, "End Duration: "),
        await input({type: "number", id: "endDuration", value: "1", min: "1", max: "23"}),
        await input({type: "submit", value: "Create Rental"})
    );

    rentalForm.addEventListener("submit", (e) => {
        const rentalDate = document.querySelector("#rentalDate").value;
        const startDuration = parseInt(document.querySelector("#startDuration").value);
        const endDuration = parseInt(document.querySelector("#endDuration").value);
        const clubId = document.querySelector("#clubSelect").value;
        const courtId = document.querySelector("#courtSelect").value;
        handleSubmit(
            e,
            API_BASE_URL + "/rentals/",
            "POST",
            window.token,
            {
                date: rentalDate,
                startDuration: startDuration,
                endDuration: endDuration,
                court: courtId,
                club: clubId
            },
            (data) => {
                alert("Rental created successfully");
                console.log(data)
                window.history.back()
            },
            (error) => {
                const parsedError = JSON.parse(error);
                alert(`Error creating rental: ${parsedError.error}`);
                console.error(parsedError.error)
            }
        )
    });

    mainContent.replaceChildren(rentalForm)
    notLoggedIn(mainContent);
}