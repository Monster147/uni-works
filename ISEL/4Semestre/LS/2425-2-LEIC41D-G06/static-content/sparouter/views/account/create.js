import {API_BASE_URL, TODAY, handleSubmit} from "../../utils.js";
import {form, label, input, select, option, h1, a} from "../../dls.js";

/**
 * Função assíncrona responsável por criar o formulário de criação de conta de utilizador.
 *
 * Esta função constrói dinamicamente o formulário de registo, define os campos obrigatórios (nome, email e password)
 * e gere o evento de submissão do formulário. Ao submeter, valida se todos os campos estão preenchidos,
 * envia o pedido de criação de conta para a API e, em caso de sucesso, guarda o token do utilizador e redireciona para a página inicial,
 * modificando o botão de login para logout
 * Em caso de erro, apresenta uma mensagem de erro ao utilizador.
 *
 * @param {HTMLElement} mainContent Elemento HTML onde o formulário será inserido.
 * @returns {Promise<void>} Promise que termina quando o formulário é renderizado e o evento de submissão está configurado.
 */
export default async function createAccount(mainContent) {
    mainContent.style.minWidth = '1000px';
    document.querySelector('p.anim').textContent = "Welcome to our page Chelas Padel. Here you can create a new account to access our services. If you already have an account, you can login instead.";


    const title = await h1("Create Account");

    const createForm = await form(
        await label({for: "name"}, "Name:"),
        await input({type: "text", id: "Name", required: true}),
        await label({for: "email"}, "Email:"),
        await input({type: "text", id: "Email", required: true}),
        await label({for: "password"}, "Password:"),
        await input({type: "password", id: "Password", required: true}),
        await input({type: "submit", value: "Create"})
    )

    const loginAccount = await a({href: "#login-logout"}, "Login to your Account");

    createForm.addEventListener("submit", (e) => {
        const Name = document.querySelector("#Name").value;
        const Password = document.querySelector("#Password").value;
        const Email = document.querySelector("#Email").value;
        console.log(Name)
        console.log(Password)
        if (!Name || !Password || !Email) {
            alert("Por favor preencha todos os campos.");
            return;
        }
        handleSubmit(
            e,
            API_BASE_URL + "/users/",
            "POST",
            undefined,
            {
                name: Name,
                email: Email,
                password: Password
            },
            (data) => {
                alert("Account was created successfully");
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

    mainContent.replaceChildren(title, createForm, loginAccount);
}