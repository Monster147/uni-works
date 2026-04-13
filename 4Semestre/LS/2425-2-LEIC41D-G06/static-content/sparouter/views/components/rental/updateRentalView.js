import {div, form, input, label, p} from "../../../dls.js";
import {API_BASE_URL, createButton, handleSubmit, TODAY} from "../../../utils.js";

export async function updateRentalView(mainContent, rental, rentalId){
    mainContent.style.minWidth = '100px';
    document.querySelector('p.anim').textContent = "Welcome to our page Chelas Padel. Here you can update a rental, just select the options to update it.";

    const info = await p("Select the options to update rental");

    const buttonGroup = await div(
        await input({type: "submit", value: "Yes"}),
        await createButton("No", true, () => window.history.back(), true)
    );
    buttonGroup.style.display = "flex";
    buttonGroup.style.gap = "10px";

    const rentalForm = await form(
        await label({for: "date"}, "Date: "),
        await input({type: "date", id: "date", value: rental.date, min: TODAY}),
        await label({for: "startDuration"}, "Start Duration: "),
        await input({type: "number", id: "startDuration", value: rental.startDuration, min: "0", max: "22"}),
        await label({for: "endDuration"}, "End Duration: "),
        await input({type: "number", id: "endDuration", value: rental.endDuration, min: "1", max: "23"}),
        await p("Do you want to save the changes?"),
        buttonGroup
    );

    rentalForm.addEventListener("submit", (e) => {
        const date = document.getElementById("date").value;
        const startDuration = document.getElementById("startDuration").value;
        const endDuration = document.getElementById("endDuration").value;
        const court = rental.court
        const club = rental.club
        handleSubmit(
            e,
            API_BASE_URL + "/rentals/" + rentalId,
            "POST",
            window.token,
            {
                date,
                startDuration,
                endDuration,
                court,
                club
            },
            () => {
                alert("Update rental was a success!");
                window.history.back();
            },
            (error) => {
                const parsedError = JSON.parse(error);
                alert(`Error updating rental: ${parsedError.error}`);
                console.error(parsedError.error);
            }
        )
    })

    const container = await div(info, rentalForm);
    mainContent.replaceChildren(container);
}