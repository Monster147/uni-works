import {div, h1} from "../../../dls.js";
import {createButton} from "../../../utils.js";
import {fetchDeleteRental} from "../../data/rental.js";

export async function deleteRentalView(mainContent, rentalId){
    mainContent.style.minWidth = '1000px';
    document.querySelector('p.anim').textContent = "Welcome to our page Chelas Padel. Here u can delete a rental, but do you really wanna to delete ?.";
    const info = await h1("Do you really want to delete this rental?")

    const yesButton = await createButton("Yes", true,async () => {
        await fetchDeleteRental(rentalId);
        window.history.back();
    });
    const noButton = await createButton("No", true,() => {
        window.history.back();
    }, true);

    const container = await div(info, yesButton, noButton);

    mainContent.replaceChildren(container);
}