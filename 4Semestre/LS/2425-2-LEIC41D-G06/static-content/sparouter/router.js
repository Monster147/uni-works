const routes = []
let notFoundRouteHandler = () => {
    throw "Route handler for unknown routes not defined"
}

function addRouteHandler(pathTemplate, handler) {
    routes.push({pathTemplate, handler})
}

function addDefaultNotFoundRouteHandler(notFoundRH) {
    notFoundRouteHandler = notFoundRH
}

function getRouteHandler(path) {
    let handler = notFoundRouteHandler
    let params = {}

    routes.find(r => {
        const matchedRoute = matchRoute(r.pathTemplate, path)
        if (matchedRoute.match) {
            handler = r.handler
            params = matchedRoute.params
            return {handler, params}
        }
    })
    return {handler, params}
}

function matchRoute(path, route) {
    const pathParts = path.split("/");
    const routeParts = route.split("/");
    const params = {};
    let match = true;

    if (pathParts.length !== routeParts.length) {
        match = false;
    } else {
        for (let i = 0; i < pathParts.length; i++) {
            if (pathParts[i].startsWith(":")) {
                const paramName = pathParts[i].substring(1);
                params[paramName] = routeParts[i];
            } else if (pathParts[i] !== routeParts[i]) {
                match = false;
                break;
            }
        }
    }

    return {match, params};
}

const router = {
    addRouteHandler,
    getRouteHandler,
    addDefaultNotFoundRouteHandler
}

export default router