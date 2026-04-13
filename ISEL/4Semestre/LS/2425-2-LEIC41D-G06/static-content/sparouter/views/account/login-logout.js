import {API_BASE_URL, TODAY, handleSubmit, createButton} from "../../utils.js";
import {form, label, input, select, option, h1, a} from "../../dls.js";

/**
 * Função assíncrona responsável por criar o formulário de login e logout do utilizador.
 *
 * Esta função constrói dinamicamente o formulário de login caso o utilizador não esteja autenticado,
 * validando os campos obrigatórios (nome e password) e gerindo o evento de submissão.
 * Ao submeter, envia o pedido de autenticação para a API e, em caso de sucesso, guarda o token do utilizador,
 * redireciona para a página inicial e altera o botão de login para logout.
 * Em caso de erro, apresenta uma mensagem de erro ao utilizador.
 * Caso o utilizador já esteja autenticado, apresenta a interface de logout, permitindo terminar sessão ou continuar autenticado.
 * Caso o utililizador decida terminar sessão, o token é removido e o botão de logout é alterado para login.
 *
 * @param {HTMLElement} mainContent Elemento HTML onde o formulário será inserido.
 * @returns {Promise<void>} Promise que termina quando o formulário é renderizado e o evento de submissão está configurado.
 */
export default async function createLoginLogout(mainContent) {
    mainContent.style.minWidth = '100px';
    if(!window.token) {
        document.querySelector('p.anim').textContent = "Welcome to our page Chelas Padel. You must login to see our site. If you dont have an account dont be shy and create a new one";

        const title = await h1("Login")

        //const showPassword = await input({type: "checkbox", id: "showPassword"})
        const loginForm = await form(
            await label({for: "name"}, "Name:"),
            await input({type: "text", id: "Name", required: true}),
            await label({for: "password"}, "Password:"),
            await input({type: "password", id: "Password", required: true}),
            //showPassword,
            await input({type: "submit", value: "Login"})
        )

        const createAccount = await a({href: "#create"}, "Create an Account");

        loginForm.addEventListener("submit", (e) => {
            const Name = document.querySelector("#Name").value;
            const Password = document.querySelector("#Password").value;
            console.log(Name)
            console.log(Password)
            if (!Name || !Password) {
                alert("Por favor preencha todos os campos.");
                return;
            }
            handleSubmit(
                e,
                API_BASE_URL + "/users/login",
                "POST",
                undefined,
                {
                    name: Name,
                    password: Password
                },
                (data) => {
                    alert("Login was successful");
                    console.log(data)
                    window.token = data.token;
                    console.log("Token:", window.token);
                    window.location.hash = "home"
                    document.querySelector('a[href="#login-logout"]').textContent = "Logout";
                },
                (error) => {
                    const parsedError = JSON.parse(error);
                    console.log(Name)
                    console.log(Password)
                    alert(`Error logging in ${parsedError.error}`);
                    console.error(parsedError.error)
                }
            )
        });
        mainContent.replaceChildren(title, loginForm, createAccount);
    }
    else  {
        document.querySelector('p.anim').textContent = "Welcome to our page Chelas Padel. This is the logout page. You are already logged in. If you want to logout just click the button below";
        const title = await h1("Are you sure you want to logout?");
        const logoutBtn = await createButton("Logout", true,
            () => {
                window.token = null;
                document.querySelector('a[href="#login-logout"]').textContent = "Login";
                window.location.hash = "home";
            }
        );

        const continueBtn = await createButton("Continue Logged In", true,
            () => {
                window.location.hash = "home";
            }
        );

        mainContent.replaceChildren(title, logoutBtn, continueBtn);
    }
}