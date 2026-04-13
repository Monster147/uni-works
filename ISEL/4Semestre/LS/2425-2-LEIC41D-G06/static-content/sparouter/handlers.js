/*
This example creates the students views using directly the DOM Api
But you can create the views in a different way, for example, for the student details you can:
    createElement("ul",
        createElement("li", "Name : " + student.name),
        createElement("li", "Number : " + student.number)
    )
or
    ul(
        li("Name : " + student.name),
        li("Number : " + student.name)
    )
Note: You have to use the DOM Api, but not directly
*/

//import {h1, div, ul, li, a, p} from "./dls";


import getHome from "./views/getHome.js";
import getClub from "./views/club/getClub.js";
import getClubs from "./views/club/getClubs.js";
import getUser from "./views/user/getUser.js";
import getUserRentals from "./views/user/getUserRentals.js";
import getRental from "./views/rental/getRental.js";
import getCourt from "./views/court/getCourt.js";
import getCourtRentals from "./views/court/getCourtRentals.js";
import getCourtRentalsByDate from "./views/court/getCourtRentalsByDate.js";
import courtList from "./views/court/courtList.js";
import getClubsByName from "./views/club/getClubsByName.js";
import createClub from "./views/club/createClub.js";
import getAvailableHours from "./views/court/getAvailableHours.js";
import createCourt from "./views/court/createCourt.js";
import createRental from "./views/rental/createRental.js"
import deleteRental from "./views/rental/deleteRental.js"
import updateRental from "./views/rental/updateRental.js";
import createAccount from "./views/account/create.js";
import createLoginLogout from "./views/account/login-logout.js";
import notLoggedIn from "./views/account/notLoggedIn.js";

export const handlers = {
    getHome,
    getClubs,
    getClub,
    getUser,
    getUserRentals,
    getRental,
    getCourt,
    getCourtRentals,
    getCourtRentalsByDate,
    courtList,
    getClubsByName,
    createClub,
    getAvailableHours,
    createCourt,
    createRental,
    deleteRental,
    updateRental,
    createLoginLogout,
    createAccount,
    notLoggedIn
}



export default handlers

/*

(val id: Int, val name: String, val club: Int)
data class RentalOutput(val id: Int, val date: LocalDate, val startDuration: Int, val endDuration:
 Int, val user: Int, val court: Int, val club: Int)

*/