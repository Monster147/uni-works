import {li, ul} from "../../../dls.js";
import {createButton} from "../../../utils.js";

/**
 * Renderiza uma vista com as horas disponíveis para um determinado court de um club.
 *
 * Esta função apresenta o nome do court, as horas disponíveis, um botão para voltar atrás
 * e informações resumidas sobre o club e o court.
 *
 * @param {HTMLElement} mainContent Elemento HTML onde o conteúdo será renderizado.
 * @param {{ name: string }} club Objeto com dados do club.
 * @param {{ name: string }} court Objeto com dados do court.
 * @param {string} availableHours Texto com as horas disponíveis para o court.
 * @returns {Promise<void>} Promise que resolve quando a vista for totalmente renderizada.
 */

export async function getAvailableHoursView(mainContent, club, court, availableHours){
    const courtDetails = await Promise.all([
        li("Name: " + court.name),
        li("Available Hours : " + availableHours),
        await createButton("Go Back", true, () => {
            window.history.back()
        })
    ])
    const courtDetail = await ul(...courtDetails);
    const content = await ul(
        await li("Club: " + club.name),
        await li("Court Details:"),
        courtDetail
    );

    mainContent.replaceChildren(content);
}