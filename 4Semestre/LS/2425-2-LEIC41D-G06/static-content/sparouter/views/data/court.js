import {API_BASE_URL} from "../../utils.js";
import fetchClubsByIds from "./club.js";

/**
 * Obtém a lista de courts de um club pelo seu ID.
 *
 * @param {string} clubId ID do club.
 * @returns {Promise<Array<Object>>} Promise que resolve para a lista de courts do club.
 * @throws {Error} Lança um erro caso a fetch falhe.
 */

export default async function fetchCourtsByClub(clubId) {
    const res = await fetch(API_BASE_URL + "/courts/clubs/" + clubId);
    if (!res.ok) {
        throw new Error("Error fetching court data");
    } else return res.json();
}

/**
 * Obtém os detalhes de um court pelo seu ID.
 *
 * @param {string} courtId ID do court.
 * @returns {Promise<Object>} Promise que resolve para os dados do court.
 * @throws {Error} Lança um erro caso a fetch falhe.
 */

export async function fetchCourtById(courtId) {
    const res = await fetch(API_BASE_URL + "/courts/" + courtId);
    if (!res.ok) {
        throw new Error("Error fetching court");
    } else return res.json();
}

/**
 * Obtém vários courts dado um array de IDs.
 *
 * @param {Array<string>} ids Array de IDs dos courts.
 * @returns {Promise<Array<Object>>} Promise que resolve para uma lista de courts.
 */

export async function fetchCourtsByIds(ids) {
    return Promise.all(ids.map(fetchCourtById));
}

/**
 * Enriquece os dados dos courts com a informação do club correspondente.
 *
 * @param {Array<Object>|Object} courtsData Array ou objeto único de courts.
 * @returns {Promise<Array<Object>>} Promise que resolve para a lista de courts enriquecidos com informação do club.
 */

export async function fetchAndEnrichCourts(courtsData) {
    const courts = Array.isArray(courtsData) ? courtsData : [courtsData];
    const clubIds = [...new Set(courts.map(r => r.club))];
    const clubs = await fetchClubsByIds(clubIds);

    return courts.map(court => {
        const club = clubs.find(club => club.id === court.club);
        return {
            ...court,
            club: {
                name: club?.name || court.club,
                id: club?.id || court.club,
            }
        };
    });
}
