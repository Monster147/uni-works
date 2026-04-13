import {API_BASE_URL, handleSubmit} from "../utils.js";
import {h1, form, label, input} from "../dls.js";

/**
 * Renderiza a página home da aplicação, mostrando uma mensagem de boas-vindas
 * e um formulário para pesquisar clubs por nome.
 *
 * @param {HTMLElement} mainContent Elemento HTML onde o conteúdo será inserido.
 * @returns {Promise<void>} Promise que resolve quando a vista estiver renderizada.
 */

export default async function getHome(mainContent) {
    mainContent.style.minWidth = '100px';
    document.querySelector('p.anim').textContent = "Welcome to our page Chelas Padel. Here you can find all the information about our clubs, courts, and rentals. You can also search for clubs by name using the form below.";
    const title = await h1("Home")


    const searchForm = await form(
        await label({for: "clubName"}, "Search Club by Name: "),
        await input({type: "text", id: "clubName"}),
        await input({type: "submit", value: "Search"})
    )

    searchForm.addEventListener('submit', (e) => {
        e.preventDefault();
        const inputName = document.querySelector("#clubName").value
        handleSubmit(
            e,
            API_BASE_URL + "/clubs/search/" + inputName,
            "GET",
            undefined, undefined,
            (data) => {
                console.log(data);
                window.location.hash = "clubs/search/" + inputName;
            },
            (error) => {
                const parsedError = JSON.parse(error);
                alert(`Error searching for clubs: ${parsedError.error}`);
                console.error(parsedError.error)
            }
        )
    })

    mainContent.replaceChildren(title, searchForm)
}