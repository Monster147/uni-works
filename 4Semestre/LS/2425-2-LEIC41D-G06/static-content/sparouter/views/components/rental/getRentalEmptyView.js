import {a, div, h1} from "../../../dls.js";
import {createButton} from "../../../utils.js";

/**
 * Renderiza uma vista, indicando que o rental foi eliminado ou não existe.
 * Apresenta opções para criar um rental ou voltar.
 *
 * @param {HTMLElement} mainContent Elemento HTML onde o conteúdo será renderizado.
 * @returns {Promise<void>} Promise que resolve quando a vista estiver totalmente renderizada.
 */

export async function getRentalEmptyView(mainContent) {
    const content = await div(
        await h1("Rental was deleted or does not exist"),
        await a({href: "#rentals/create"}, "Create a Rental"),
        await div(
            await createButton("Go Back", true, () => {
                    window.history.back()
                }
            )
        )
    );
    mainContent.replaceChildren(content)
}