import {API_BASE_URL, TODAY, handleSubmit, createButton} from "../../utils.js";
import {form, label, input, select, option, h1, a} from "../../dls.js";

/**
 * Função assíncrona responsável por criar o formulário de login e logout do utilizador ou go back caso o
 * user não queira realizar mudanças.
 *
 * Esta função constrói dinamicamente o formulário de login caso o utilizador não esteja autenticado,
 * o formulário de create account caso o utilizador não tenha uma conta, e o formulário de go back caso o utilizador não queira realizar mudanças.
 *
 * @param mainContent
 * @returns {Promise<void>}
 */
export default async function notLoggedIn(mainContent) {
    mainContent.style.minWidth = '100px';
    if(!window.token) {
        document.querySelector('p.anim').textContent = "Welcome to our page Chelas Padel. You must login to make changes in our site like create, update or delete something. If you dont have an account dont be shy and create a new one";

        const title = await h1("You must login if you want to make changes")

        const goBack = await createButton("I dont want to make changes right now", true,() => {
            window.history.back();
        });
        const createAccount = await createButton("Create an Account", true,() => {
            window.location.hash = "create";
        });
        const login = await createButton("Login", true,() => {
            window.location.hash = "login-logout";
        });

        mainContent.replaceChildren(title, goBack, createAccount,login);
    }
}