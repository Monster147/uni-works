import {API_BASE_URL} from "../../utils.js";

/**
 * Obtém os dados de um user pelo seu ID.
 *
 * @param {string} userId ID do user.
 * @returns {Promise<Object>} Promise que resolve para os dados do user.
 * @throws {Error} Lança um erro caso a fetch falhe.
 */

export async function fetchUserById(userId) {
    const res = await fetch(API_BASE_URL + "/users/" + userId);
    if (!res.ok) {
        throw new Error(`Error fetching user data`);
    } else return res.json();
}

/**
 * Obtém os dados do user proprietário de um clube.
 *
 * @param {Object} club Objeto club que contém a propriedade 'owner' com o ID do user.
 * @returns {Promise<Object>} Promise que resolve para os dados do user proprietário.
 * @throws {Error} Lança um erro caso a fetch falhe.
 */
export default async function fetchUserByClub(club) {
    const res = await fetch(API_BASE_URL + "/users/" + club.owner);
    if (!res.ok) {
        throw new Error(`Error fetching user data`);
    } else return res.json();
}

/**
 * Obtém os dados de múltiplos users a partir de uma lista de IDs.
 *
 * @param {Array<string>} ids Lista de IDs dos users.
 * @returns {Promise<Array<Object>>} Promise que resolve para a lista de dados dos users.
 */
export async function fetchUsersByIds(ids) {
    return Promise.all(ids.map(id =>
        fetch(API_BASE_URL + "/users/" + id)
            .then(res => res.json())
    ));
}
