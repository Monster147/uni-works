import {a, div, h1, p} from "../../../dls.js";
import {createButton} from "../../../utils.js";

/**
 * Renderiza uma vista indicando que não existem rentals de campos disponíveis.
 *
 * Esta função apresenta um título, um link para criar um rental,
 * uma mensagem a informar que não existem rentals e um botão para voltar à página anterior.
 *
 * @param {HTMLElement} mainContent Elemento HTML onde o conteúdo será renderizado.
 * @returns {Promise<void>} Promise que resolve quando a vista for totalmente renderizada.
 */

export async function emptyCourtRentalsByDateView(mainContent){
    const content = await div(
        await h1("Court Rentals"),
        await a({href: "#rentals/create"}, "Create a Rental"),
        await p("No rentals available for this date."),
        await createButton("Go Back", true, () => {
            window.history.back()
        })
    );
    mainContent.replaceChildren(content);
}