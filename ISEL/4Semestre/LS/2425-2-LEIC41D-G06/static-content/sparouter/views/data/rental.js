import {API_BASE_URL} from "../../utils.js";
import {enrichRentals} from "../viewsUtils.js";
import {fetchUsersByIds} from "./user.js";
import {fetchCourtsByIds} from "./court.js";
import fetchClubsByIds from "./club.js";

/**
 * Obtém os detalhes de um rental pelo seu ID.
 *
 * @param {string} rentalId ID do rental.
 * @returns {Promise<Object>} Promise que resolve para os dados do rental.
 * @throws {Error} Lança um erro caso a fetch falhe.
 */

export async function fetchRentalById(rentalId) {
    const res = await fetch(API_BASE_URL + "/rentals/" + rentalId);
    if (!res.ok) {
        throw new Error(`Error fetching rental data`);
    } else return res.json();
}

/**
 * Obtém as horas disponíveis para um court específico num dado dia.
 *
 * @param {string} clubId ID do club.
 * @param {string} courtId ID do court.
 * @param {string} date Data no formato ISO (YYYY-MM-DD).
 * @returns {Promise<Array<string>>} Promise que resolve para a lista de horas disponíveis.
 * @throws {Error} Lança um erro caso a fetch falhe.
 */

export async function fetchRentalAvailableHours(clubId, courtId, date) {
    const res = await fetch(API_BASE_URL + "/rentals/available/clubs/" + clubId + "/courts/" + courtId + "/date/" + date);
    if (!res.ok) {
        throw new Error("Error fetching available hours");
    }
    return res.json();
}

/**
 * Obtém os rentals de um court num club numa data específica.
 *
 * @param {string} clubId ID do club.
 * @param {string} courtId ID do court.
 * @param {string} date Data no formato ISO (YYYY-MM-DD).
 * @returns {Promise<Array<Object>>} Promise que resolve para a lista de rentals.
 * @throws {Error} Lança um erro caso a fetch falhe.
 */

export async function fetchCourtRentalsByDate(clubId, courtId, date) {
    const res = await fetch(API_BASE_URL + "/rentals/clubs/" + clubId + "/courts/" + courtId + "/date/" + date);
    if (!res.ok) {
        throw new Error("Error fetching rental data");
    }
    return res.json();
}

/**
 * Elimina um rental pelo seu ID.
 *
 * @param {string} rentalId ID do rental a eliminar.
 * @returns {Promise<void>} Promise que resolve quando o rental for eliminado.
 */

export async function fetchDeleteRental(rentalId) {
    await fetch(API_BASE_URL + "/rentals/" + rentalId, { method: "DELETE" });
}

/**
 * Obtém todos os rentals de um court.
 *
 * @param {string} clubId ID do club.
 * @param {string} courtId ID do court.
 * @returns {Promise<Array<Object>>} Promise que resolve para a lista de rentals.
 * @throws {Error} Lança um erro caso a fetch falhe.
 */

export async function fetchCourtRental(clubId, courtId) {
    const res = await fetch(API_BASE_URL + "/rentals/clubs/" + clubId + "/courts/" + courtId);
    if (!res.ok) {
        throw new Error("Error fetching rental data");
    }
    return res.json();
}

/**
 * Obtém todos os rentals associados a um user.
 *
 * @param {string} userId ID do user.
 * @returns {Promise<Array<Object>>} Promise que resolve para a lista de rentals do user.
 * @throws {Error} Lança um erro caso a fetch falhe.
 */

export async function fetchUserRental(userId) {
    const res = await fetch(API_BASE_URL + "/rentals/users/" + userId);
    if (!res.ok) {
        throw new Error("Error fetching rental data");
    }
    return res.json();
}

/**
 * Atualiza os dados de um rental pelo seu ID.
 *
 * @param {string} rentalId ID do rental a atualizar.
 * @param {Object} params Objeto com os dados a atualizar.
 * @returns {Promise<Response>} Promise que resolve para a resposta da API.
 */

export async function updateRentalAPI(rentalId, params) {
    const options = {
        method: "POST",
        headers: {
            "Content-Type": "application/json",
            "Accept": "application/json",
            "authorization": window.token
        },
        body: JSON.stringify(params)
    };
    return await fetch(API_BASE_URL + "/rentals/" + rentalId, options);
}

/**
 * Enriquece os dados dos rentals com as informações de users, courts e clubs correspondentes.
 *
 * @param {Array<Object>|Object} rentalData Array ou objeto único de rentals.
 * @returns {Promise<Array<Object>>} Promise que resolve para a lista de rentals enriquecidos.
 */

export async function fetchAndEnrichRentals(rentalData) {
    const rentals = Array.isArray(rentalData) ? rentalData : [rentalData];
    const userIds = [...new Set(rentals.map(r => r.user))];
    const courtIds = [...new Set(rentals.map(r => r.court))];
    const clubIds = [...new Set(rentals.map(r => r.club))];

    const [users, courts, clubs] = await Promise.all([
        fetchUsersByIds(userIds),
        fetchCourtsByIds(courtIds),
        fetchClubsByIds(clubIds)
    ]);

    return enrichRentals(rentals, users, courts, clubs);
}
