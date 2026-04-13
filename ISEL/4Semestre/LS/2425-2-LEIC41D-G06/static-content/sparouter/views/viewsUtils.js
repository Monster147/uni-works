import {a, div, h1} from "../dls.js";
import {createButton} from "../utils.js";
import notLoggedIn from "./account/notLoggedIn.js";

/**
 * Obtém os parâmetros de paginação a partir dos parâmetros recebidos.
 * Usa valores padrão caso os parâmetros skip ou limit não estejam definidos.
 *
 * @param {Object} params Objeto contendo os parâmetros da query string.
 * @param {number} defaultLimit Valor padrão para o limite de itens por página.
 * @returns {{skip: number, limit: number, previous: number, next: number}}
 */

export function getPaginationParams(params, defaultLimit) {
    const queryParams = new URLSearchParams(params);
    const skip = parseInt(queryParams.get("skip")) || 0;
    const limit = parseInt(queryParams.get("limit")) || defaultLimit;
    const previous = Math.max(skip, 0);
    const next = skip + limit;
    return {skip, limit, previous, next};
}

/**
 * Renderiza uma tabela simples com dados, onde algumas colunas são links para outras páginas.
 *
 * @param {Array<Object>} data Lista de objetos que representam as linhas da tabela.
 * @param {Array<string>} linkKeys Chaves das colunas que deverão ser links.
 * @param {Array<string>} redirectKeys Chaves usadas para construir a URL dos links.
 * @returns {HTMLElement} Elemento HTML que representa a tabela renderizada.
 */

export async function renderListTable(data, linkKeys, redirectKeys) {
    const headers = Object.keys(data[0]);

    const headerRow = div(
        {
            style: `
                display: flex;
                font-weight: bold;
                border-bottom: 1px solid black;
                padding: 4px 0;
            `
        },
        ...headers.map(h =>
            div({style: "flex: 1; padding: 0 8px;"}, h)
        )
    );

    const rows = data.map(row =>
        div(
            {
                style: `
                    display: flex;
                    border-bottom: 1px solid #ccc;
                    padding: 4px 0;
                `
            },
            ...headers.map(key => {
                const value = row[key]?.name || String(row[key]);
                const linkIndex = linkKeys.indexOf(key)
                if (linkIndex !== -1) {
                    const associatedId = row[key]?.id || row.id;
                    return a(
                        {href: `#${redirectKeys[linkIndex]}/${associatedId}`, style: "flex: 1; padding: 0 8px;"},
                        value
                    );
                }
                const cell = document.createElement("div");
                cell.style.flex = "1";
                cell.style.padding = "0 8px";
                if (row[key] instanceof HTMLElement) {
                    cell.appendChild(row[key]);
                } else {
                    cell.textContent = row[key]?.name || String(row[key]);
                }
                return cell;
            })
        )
    );

    return div(
        {style: "font-family: sans-serif; width: 100%; max-width: 1200px;"},
        headerRow,
        ...rows
    );
}

/**
 * Enriquece a lista dos rentals substituindo os ids por objetos com id e nome
 * para user, court e club, buscando os nomes a partir das listas fornecidas.
 *
 * @param {Array<Object>} rentals Lista de rentals a enriquecer.
 * @param {Array<Object>} users Lista de users com propriedades id e nome.
 * @param {Array<Object>} courts Lista de courts com propriedades id e nome.
 * @param {Array<Object>} clubs Lista de clubs com propriedades id e nome.
 * @returns {Array<Object>} Rentals enriquecidos com informações completas.
 */

export function enrichRentals(rentals, users, courts, clubs) {
    return rentals.map(rental => ({
        ...rental,
        user: {
            id: rental.user,
            name: users.find(user => user.id === rental.user)?.name || rental.user,
        },
        court: {
            id: rental.court,
            name: courts.find(court => court.id === rental.court)?.name || rental.court,
        },
        club: {
            id: rental.club,
            name: clubs.find(club => club.id === rental.club)?.name || rental.club,
        },
    }));
}

export async function cantModify(mainContent){
    mainContent.style.minWidth = '1000px';
    document.querySelector('p.anim').textContent = "Welcome to our page Chelas Padel. This rental does not belong to you. You cannot delete it.";
    const title = await h1("This rental does not belong to you.")

    const goBack = await createButton("Go back", true, () => {
        window.history.back();
    });

    const container = await div(title, goBack);

    mainContent.replaceChildren(container);
    notLoggedIn(mainContent)
}
