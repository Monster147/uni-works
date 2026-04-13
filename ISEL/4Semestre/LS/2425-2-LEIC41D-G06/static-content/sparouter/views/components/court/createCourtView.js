import {form, input, label} from "../../../dls.js";
import {API_BASE_URL, handleSubmit} from "../../../utils.js";

export async function createCourtView(mainContent, clubId){
    mainContent.style.minWidth = '100px';
    document.querySelector('p.anim').textContent = "Welcome to our page Chelas Padel. Here you can create a new court. Please select the court name.";

    const courtForm = await form(
        await label({for: "courtNameId"}, "Court Name"),
        await input({type: "text", id: "courtNameId"}),
        await input({type: "submit", value: "Create Court"})
    );

    courtForm.addEventListener('submit', (e) => {
        const inputCourtName = document.querySelector("#courtNameId").value
        handleSubmit(
            e,
            API_BASE_URL + "/courts/",
            "POST",
            window.token,
            {name: inputCourtName, club: clubId},
            (data) => {
                alert(`Court "${inputCourtName}" created successfully`)
                console.log(data)
                window.location.hash = "courts/clubs/" + clubId
            },
            (error) => {
                const parsedError = JSON.parse(error);
                alert(`Error creating court: ${parsedError.error}`)
                console.error(parsedError.error)
            }
        )
    })

    mainContent.replaceChildren(courtForm)
}