import {a, div, h1, li, ul} from "../../../dls.js";

/**
 * Renderiza a vista com os clubes que correspondem ao nome pesquisado.
 *
 * Esta função verifica se há clubes a apresentar. Se houver, gera uma lista com links
 * para cada clube encontrado. Caso contrário, apresenta uma mensagem indicando que
 * nenhum clube foi encontrado para o nome fornecido.
 *
 * @param {HTMLElement} mainContent Elemento HTML onde o conteúdo será renderizado.
 * @param {Array<{ id: string, name: string }>} clubs Lista de clubes a apresentar.
 * @returns {Promise<void>} Promise que resolve quando a vista for totalmente renderizada.
 */

export async function getClubsByNameView(mainContent, clubs){
    let content
    const clubNames = clubs.length > 0 ? await Promise.all(
        clubs.map(async club =>
            li(
                await a({href: "#clubs/" + club.id}, "Club: " + club.name)
            )
        )
    ) : null;

    if (clubNames === null) {
        content = await div(
            await h1("No clubs found with name or partial name: " + clubName),
            await a({href: "#home"}, "Go back to home page")
        );
        mainContent.replaceChildren(content);
    }
    const clubDetail = await ul(...clubNames);

    content = await div(
        await h1("Clubs"),
        clubDetail
    );

    mainContent.replaceChildren(content);
}