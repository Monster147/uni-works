/**
 * Cria um elemento HTML com a tag especificada, atributos e filhos.
 * Pode receber filhos como strings, elementos HTML ou Promises que resolvam para esses tipos.
 *
 * @param {string} tag Tag HTML a criar (ex: 'div', 'p', 'a').
 * @param {Object|string|HTMLElement|null} attributes Objeto com atributos do elemento,
 * ou string/elemento diretamente para ser adicionado como filho único.
 * @param  {...(string|HTMLElement|Promise)} children Filhos a adicionar ao elemento.
 * @returns {Promise<HTMLElement>} Elemento HTML criado e preenchido com filhos.
 */

async function createElement(tag, attributes, ...children) {
    const element = document.createElement(tag);
    if (isElement(attributes) || typeof attributes === "string")
        appendChild(element, attributes); // if its string or html dom tag, we add it to the root element
    else if (attributes != null && typeof attributes === "object")
        setElementAttributes(element, attributes); // if its an object its element's attributes
    for (let child of children) {
        child = await child; // child can be a promise
        if (isElement(child) != null || typeof child === "string")
            appendChild(element, child)
    }
    return element;
}

/**
 * Define os atributos de um elemento HTML a partir de um objeto.
 * Ignora atributos com valor null ou undefined.
 *
 * @param {HTMLElement} element Elemento HTML onde os atributos serão definidos.
 * @param {Object} attributes Objeto com pares chave-valor para atributos.
 */

function setElementAttributes(element, attributes) {
    for (const attr in attributes) {
        if (attr == null)
            continue;
        const value = attributes[attr];
        if (value == null)
            continue;
        element.setAttribute(attr, value)
    }
}

//Appends a child element or text node to an HTML element.
const appendChild = (element, child) => {
    element.appendChild(typeof child === "string" ? document.createTextNode(child) : child);
};

//check if an object is an HTML element
const isElement = (obj) => obj && typeof obj === "object" && obj.nodeType === 1 && typeof obj.nodeName === "string";


export const ul = (attr, ...children) => createElement("ul", attr, ...children);
export const li = (attr, ...children) => createElement("li", attr, ...children);
export const div = (attr, ...children) => createElement("div", attr, ...children);
export const h1 = (attr, ...children) => createElement("h1", attr, ...children);
export const a = (attr, ...children) => createElement("a", attr, ...children);
export const p = (attr, ...children) => createElement("p", attr, ...children);
export const button = (attr, ...children) => createElement("button", attr, ...children);
export const form = (attr, ...children) => createElement("form", attr, ...children);
export const label = (attr, ...children) => createElement("label", attr, ...children);
export const Name = (attr, ...children) => createElement("Name", attr, ...children);
export const input = (attr, ...children) => createElement("input", attr, ...children);
export const select = (attr, ...children) => createElement("select", attr, ...children);
export const option = (attr, ...children) => createElement("option", attr, ...children);