import router from "./router.js";
import handlers from "./handlers.js";

// For more information on ES6 modules, see https://www.javascripttutorial.net/es6/es6-modules/ or
// https://www.w3schools.com/js/js_modules.asp

window.addEventListener('load', loadHandler)
window.addEventListener('hashchange', hashChangeHandler)

function loadHandler() {

    router.addRouteHandler("home", handlers.getHome)
    router.addRouteHandler("clubs", handlers.getClubs)
    router.addRouteHandler("clubs/create", handlers.createClub)
    router.addRouteHandler("clubs/:id", handlers.getClub)
    router.addRouteHandler("users/:id", handlers.getUser)
    router.addRouteHandler("rentals/users/:id", handlers.getUserRentals)
    router.addRouteHandler("rentals/create", handlers.createRental)
    router.addRouteHandler("rentals/:id", handlers.getRental)
    router.addRouteHandler("courts/create/clubId/:id", handlers.createCourt)
    router.addRouteHandler("courts/:id", handlers.getCourt)
    router.addRouteHandler("rentals/clubs/:clubId/courts/:courtId", handlers.getCourtRentals)
    router.addRouteHandler("rentals/clubs/:clubId/courts/:courtId/date/:date", handlers.getCourtRentalsByDate)
    router.addRouteHandler("courts/clubs/:id", handlers.courtList)
    router.addRouteHandler("clubs/search/:name", handlers.getClubsByName)
    router.addRouteHandler("rentals/available/clubs/:clubId/courts/:courtId/date/:date", handlers.getAvailableHours)
    router.addRouteHandler("rentals/:id/delete", handlers.deleteRental)
    router.addRouteHandler("rentals/:id/update", handlers.updateRental)
    router.addRouteHandler("login-logout", handlers.createLoginLogout)
    router.addRouteHandler("create", handlers.createAccount)
    router.addDefaultNotFoundRouteHandler(() => window.location.hash = "home")

    hashChangeHandler()
}

function hashChangeHandler() {

    const mainContent = document.getElementById("mainContent")
    const fullPath = window.location.hash.replace("#", "");
    const [path, queryString] = fullPath.split("?");
    const handler = router.getRouteHandler(path)
    const params = handler.params
    if (queryString) {
        const queryParams = Object.fromEntries(new URLSearchParams(queryString).entries());
        Object.assign(params, queryParams);
    }
    const handlers = handler.handler
    handlers(mainContent, params)
}

