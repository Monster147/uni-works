import {button, div} from "./dls.js";

export const API_BASE_URL = "https://img-ls-2425-2-41d-g06.onrender.com"
export const LIMIT = 1
export const TODAY = new Date().toISOString().split("T")[0]

/**
 * Cria um botão HTML com um texto, ativado ou desativado, e um evento de clique.
 * Se o botão estiver desativado (isEnabled === false), devolve um div vazio.
 *
 * @param {string} buttonName Texto exibido no botão.
 * @param {boolean} isEnabled Define se o botão está ativo (true) ou não (false).
 * @param {function} onClick Função callback a ser executada ao clicar no botão.
 * @param {boolean} [noTopMargin=false] Se true, não adiciona margem superior ao botão.
 * @returns {Promise<HTMLElement>} Elemento botão criado ou div vazio.
 */

export async function createButton(buttonName, isEnabled, onClick, noTopMargin = false) {
    if (!isEnabled) {
        return await div("");
    }

    const style = noTopMargin ? "" : "margin-left: 5px; margin-top: 10px;";

    const botao = await button(
        {type: "button", style: style},
        buttonName
    );

    botao.addEventListener("click", onClick);
    return botao;
}


/**
 * Função genérica para tratar submissão de formulários ou chamadas à API.
 * Realiza fetch com method, endpoint e corpo definidos, e executa callbacks de sucesso ou erro.
 *
 * @param {Event} event Evento da submissão do formulário.
 * @param {string} apiEndpoint URL do endpoint da API para onde enviar o pedido.
 * @param {string} [method="POST"] Method HTTP (GET, POST, PUT, DELETE, etc.).
 * @param {string|null} [token=null] Token de autorização (se existir).
 * @param {Object} [body={}] Corpo do pedido para métodos que aceitam corpo (ex: POST).
 * @param {function} [onSuccess] Callback executado com o resultado da API em caso de sucesso.
 * @param {function} [onError] Callback executado em caso de erro, com a mensagem de erro.
 */

export async function handleSubmit(event, apiEndpoint, method = "POST", token = null, body = {}, onSuccess, onError) {
    event.preventDefault();
    try {
        const headers = {
            "Content-Type": "application/json"
        }

        if (token) headers["authorization"] = token

        const options = {
            method: method,
            headers: headers
        }

        if (method !== "GET" && method !== "HEAD") {
            options.body = JSON.stringify(body);
        }


        const response = await fetch(apiEndpoint, options)

        if (response.ok) {
            const data = await response.json();
            if (onSuccess) onSuccess(data);
        } else {
            const errorMessage = await response.text()
            if (onError) onError(errorMessage);
        }
    } catch (e) {
        if (onError) onError(e.message);
    }
}

export async function verifyUser(userToken){
    return userToken == window.token;
}