import router from "../sparouter/router.js";
import handlers from "../sparouter/handlers.js";
import {evaluateTests} from "./test.utils.js";

describe('Router tests', function () {
    before(function () {
        //
    });

    describe('Found specific router Handler', function () {
        it('should find getClubs', function () {

            router.addRouteHandler("home", handlers.getHome)
            router.addRouteHandler("clubs", handlers.getClubs)
            router.addRouteHandler("clubs/2", handlers.getClub)

            const handler = router.getRouteHandler("clubs")
            handler.handler.name.should.be.equal("getClubs")
        })

        it('should find getHome', function () {

            router.addRouteHandler("home", handlers.getHome)
            router.addRouteHandler("clubs", handlers.getClubs)
            router.addRouteHandler("clubs/1", handlers.getClub)

            const handler = router.getRouteHandler("home")

            handler.handler.name.should.be.equal("getHome")
        })

        it('should find getClubs/2', function () {

            router.addRouteHandler("home", handlers.getHome)
            router.addRouteHandler("clubs", handlers.getClubs)
            router.addRouteHandler("clubs/3", handlers.getClub)

            const handler = router.getRouteHandler("clubs/3")

            handler.handler.name.should.be.equal("getClub")
        })

        it('should find getClubs', function () {
            router.addRouteHandler("clubs/1", handlers.getClub)
            router.addRouteHandler("clubs", handlers.getClubs)

            const handler = router.getRouteHandler("clubs")

            handler.handler.name.should.be.equal("getClubs")
        })

        it('should find courtList', function () {
            router.addRouteHandler("home", handlers.getHome)
            router.addRouteHandler("clubs", handlers.getClubs)
            router.addRouteHandler("clubs/1", handlers.getClub)
            router.addRouteHandler("courts", handlers.courtList)

            const handler = router.getRouteHandler("courts")

            handler.handler.name.should.be.equal("courtList")
        })

        it('should find getCourt/1', function () {
            router.addRouteHandler("home", handlers.getHome)
            router.addRouteHandler("clubs", handlers.getClubs)
            router.addRouteHandler("clubs/1", handlers.getClub)
            router.addRouteHandler("courts", handlers.courtList)
            router.addRouteHandler("courts/1", handlers.getCourt)

            const handler = router.getRouteHandler("courts/1")

            handler.handler.name.should.be.equal("getCourt")
        })

        it('should find getClub/1', function () {
            router.addRouteHandler("courts", handlers.courtList)
            router.addRouteHandler("clubs/1", handlers.getClub)

            const handler = router.getRouteHandler("clubs/1")

            handler.handler.name.should.be.equal("getClub")
        })

        it('should find getCourtRentals/1/1', function () {
            router.addRouteHandler("home", handlers.getHome)
            router.addRouteHandler("clubs", handlers.getClubs)
            router.addRouteHandler("clubs/1", handlers.getClub)
            router.addRouteHandler("courts", handlers.courtList)
            router.addRouteHandler("courts/1", handlers.getCourt)
            router.addRouteHandler("rentals/clubs/1/courts/1", handlers.getCourtRentals)

            const handler = router.getRouteHandler("rentals/clubs/1/courts/1")

            handler.handler.name.should.be.equal("getCourtRentals")
        })

        it('should find getClub/1', function () {
            router.addRouteHandler("courts/1", handlers.getCourt)
            router.addRouteHandler("clubs/1", handlers.getClub)

            const handler = router.getRouteHandler("clubs/1")

            handler.handler.name.should.be.equal("getClub")
        })

        it('should find getCourt/1', function () {
            router.addRouteHandler("rentals/clubs/1/courts/1", handlers.getCourtRentals)
            router.addRouteHandler("court/1", handlers.getCourt)

            const handler = router.getRouteHandler("court/1")

            handler.handler.name.should.be.equal("getCourt")
        });

        it('should find getRental/1', function () {
            router.addRouteHandler("home", handlers.getHome)
            router.addRouteHandler("clubs", handlers.getClubs)
            router.addRouteHandler("clubs/1", handlers.getClub)
            router.addRouteHandler("courts", handlers.courtList)
            router.addRouteHandler("courts/1", handlers.getCourt)
            router.addRouteHandler("rentals/clubs/1/courts/1", handlers.getCourtRentals)
            router.addRouteHandler("rentals/1", handlers.getRental)

            const handler = router.getRouteHandler("rentals/1")

            handler.handler.name.should.be.equal("getRental")
        })

        it('should find getUser/1', function () {
            router.addRouteHandler("home", handlers.getHome)
            router.addRouteHandler("clubs", handlers.getClubs)
            router.addRouteHandler("clubs/1", handlers.getClub)
            router.addRouteHandler("courts", handlers.courtList)
            router.addRouteHandler("courts/1", handlers.getCourt)
            router.addRouteHandler("rentals/clubs/1/courts/1", handlers.getCourtRentals)
            router.addRouteHandler("rentals/1", handlers.getRental)
            router.addRouteHandler("users/1", handlers.getUser)

            const handler = router.getRouteHandler("users/1")

            handler.handler.name.should.be.equal("getUser")
        })

        it('should find getUserRentals/1', function () {
            router.addRouteHandler("home", handlers.getHome)
            router.addRouteHandler("clubs", handlers.getClubs)
            router.addRouteHandler("clubs/1", handlers.getClub)
            router.addRouteHandler("courts", handlers.courtList)
            router.addRouteHandler("courts/1", handlers.getCourt)
            router.addRouteHandler("rentals/clubs/1/courts/1", handlers.getCourtRentals)
            router.addRouteHandler("rentals/1", handlers.getRental)
            router.addRouteHandler("users/1", handlers.getUser)
            router.addRouteHandler("rentals/users/1", handlers.getUserRentals)

            const handler = router.getRouteHandler("rentals/users/1")

            handler.handler.name.should.be.equal("getUserRentals")
        })

        it('should find getUser/1', function () {
            router.addRouteHandler("clubs/1", handlers.getClub)
            router.addRouteHandler("users/1", handlers.getUser)

            const handler = router.getRouteHandler("users/1")

            handler.handler.name.should.be.equal("getUser")
        });

        it('should find getUser/1', function () {
            router.addRouteHandler("rentals/users/1", handlers.getUserRentals)
            router.addRouteHandler("users/1", handlers.getUser)

            const handler = router.getRouteHandler("users/1")

            handler.handler.name.should.be.equal("getUser")
        });

        it('should find getRental/1', function () {
            router.addRouteHandler("rentals/users/1", handlers.getUserRentals)
            router.addRouteHandler("rentals/1", handlers.getRental)

            const handler = router.getRouteHandler("rentals/1")

            handler.handler.name.should.be.equal("getRental")
        })

        it('should find getCourt/1', function () {
            router.addRouteHandler("rentals/clubs/1/courts/1", handlers.getCourtRentals)
            router.addRouteHandler("court/1", handlers.getCourt)

            const handler = router.getRouteHandler("court/1")

            handler.handler.name.should.be.equal("getCourt")
        })
    })

    describe('Errors with the route handler', function () {

        it('should not found the route handler', function () {
            router.addRouteHandler("home", handlers.getHome)
            router.addRouteHandler("clubs", handlers.getClubs)
            router.addRouteHandler("clubs/1", handlers.getClub)
            router.addRouteHandler("courts", handlers.courtList)
            router.addRouteHandler("courts/1", handlers.getCourt)
            router.addRouteHandler("rentals/clubs/1/courts/1", handlers.getCourtRentals)
            router.addRouteHandler("rentals/1", handlers.getRental)
            router.addRouteHandler("users/1", handlers.getUser)
            router.addRouteHandler("rentals/users/1", handlers.getUserRentals)

            const handler = router.getRouteHandler("invalid/invalid")
            if (handler.handler.name !== "notFoundRouteHandler")
                throw new Error("RouteHandler supposed to not exist")
        })
        it('should add default for route handler not found', function () {
            const notFoundHandler = () => 'Route not found';
            router.addDefaultNotFoundRouteHandler(notFoundHandler);

            const handler = router.getRouteHandler('/not/a/valid/route');
            handler.handler().should.be.equal('Route not found');
        })
    })
})

evaluateTests();