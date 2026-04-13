import {API_BASE_URL} from "../../utils.js";

/**
 * Obtém a lista de clubs a partir da API.
 *
 * @returns {Promise<Array>} Promise que resolve para um array com os dados dos clubs.
 * @throws {Error} Lança erro se a resposta da API não for bem-sucedida.
 */

export async function fetchClubsList(){
    return fetch(API_BASE_URL + "/clubs")
        .then(res => {
            if (!res.ok) {
                throw new Error("Error fetching club data");
            }
            return res.json();
        });
}

/**
 * Obtém os dados de um club específico pela sua ID.
 *
 * @param {string} clubId - ID do club a obter.
 * @returns {Promise<Object>} Promise que resolve para o objeto com os dados do club.
 * @throws {Error} Lança erro se a resposta da API não for bem-sucedida.
 */

export async function fetchClubById(clubId) {
    const res = await fetch(API_BASE_URL + "/clubs/" + clubId);
    if (!res.ok) {
        throw new Error(`Error fetching club data`);
    } else return res.json();
}

/**
 * Obtém uma lista de clubs pelos seus IDs.
 *
 * @param {Array<string>} ids - Lista de IDs dos clubs a obter.
 * @returns {Promise<Array<Object>>} Promise que resolve para uma lista de objetos com os dados dos clubs.
 */

export default function fetchClubsByIds(ids) {
    return Promise.all(ids.map(fetchClubById));
}

/**
 * Pesquisa clubs pelo nome.
 *
 * @param {string} clubName Nome do club a pesquisar.
 * @returns {Promise<Array<Object>>} Promise que resolve para uma lista de clubs que correspondem ao nome pesquisado.
 * @throws {Error} Lança um erro se a pesquisa falhar.
 */

export async function fetchClubsByName(clubName) {
    const res = await fetch(API_BASE_URL + "/clubs/search/" + clubName);
    if (!res.ok) {
        throw new Error(`Error fetching clubs by name`);
    } else return res.json();
}

