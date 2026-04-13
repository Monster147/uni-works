import {form, input, label} from "../../../dls.js";
import {API_BASE_URL, handleSubmit} from "../../../utils.js";

export async function createClubView(mainContent) {
    mainContent.style.minWidth = '100px';
    document.querySelector('p.anim').textContent = "Welcome to our page Chelas Padel. Here you can create a new club just put the club name to create.";

    const clubForm = await form(
        await label({for: "clubNameId"}, "Club Name"),
        await input({type: "text", id: "clubNameId"}),
        await input({type: "submit", value: "Create Club"})
    );

    clubForm.addEventListener('submit', (e) => {
        const inputClubName = document.querySelector("#clubNameId").value

        handleSubmit(
            e,
            API_BASE_URL + "/clubs/",
            "POST",
            window.token,
            {name: inputClubName},
            (data) => {
                alert(`Club "${inputClubName}" created successfully`)
                console.log(data)
                window.location.hash = "clubs"
            },
            (error) => {
                const parsedError = JSON.parse(error);
                alert(`Error creating club: ${parsedError.error}`)
                console.error(parsedError.error)
            })

    });

    mainContent.replaceChildren(clubForm)
}