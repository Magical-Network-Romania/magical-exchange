export type AppRoute = "dashboard" | "history" | "rates";

export function routeFromPathname(pathname: string): AppRoute {
	if (pathname === "/history") {
		return "history";
	}

	if (pathname === "/rates") {
		return "rates";
	}

	return "dashboard";
}

export function pathnameForRoute(route: AppRoute) {
	return {
		dashboard: "/",
		history: "/history",
		rates: "/rates"
	}[route];
}
