import {a, div, h1, li, ul} from "../../../dls.js";

/**
 * Renderiza a vista de detalhes de um clube específico.
 *
 * Esta função apresenta os dados do clube fornecido, incluindo o nome, ID, dono (com link para o perfil)
 * e um link para os campos (courts) associados ao clube. O conteúdo é renderizado dinamicamente
 * no elemento 'mainContent'.
 *
 * @param {HTMLElement} mainContent Elemento HTML onde os detalhes do clube serão renderizados.
 * @param {{ id: string, name: string, owner: string }} club Objeto com os dados do clube.
 * @param {{ name: string }} user Objeto com os dados do utilizador dono do clube.
 * @returns {Promise<void>} Promise que resolve quando a vista de detalhes do clube for renderizada.
 */

export async function getClubView(mainContent, club, user) {
    const clubDetails = await Promise.all([
        li("Name: " + club.name),
        li("Number: " + club.id),
        li(
            await a({href: "#users/" + club.owner}, "Owner: " + user.name)
        ),
        li(
            await a({href: "#courts/clubs/" + club.id}, "Courts")
        )
    ]);

    const clubDetail = await ul(...clubDetails);

    const content = await div(
        await h1("Club"),
        clubDetail
    );

    mainContent.replaceChildren(content);
}