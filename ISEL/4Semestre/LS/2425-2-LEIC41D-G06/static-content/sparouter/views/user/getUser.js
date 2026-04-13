import {fetchUserById} from "../data/user.js";
import {getUserView} from "../components/user/getUserView.js";

/**
 * Renderiza a vista com os detalhes do user.
 *
 * @param {HTMLElement} mainContent Elemento HTML onde o conteúdo será inserido.
 * @param {Object} params Parâmetros da rota, incluindo o ID do user.
 * @param {string} params.id ID do user a mostrar.
 * @returns {Promise<void>} Promise que resolve quando a vista estiver renderizada.
 */

export default async function getUser(mainContent, params) {
    mainContent.style.minWidth = '100px';
    document.querySelector('p.anim').textContent = "Welcome to our page Chelas Padel. Here you can find the details of the user.";
    const userId = params.id;

    const user = await fetchUserById(userId)

    return getUserView(mainContent, user);
}