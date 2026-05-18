import process from "node:process";

const distDirectory = new URL("../dist/", import.meta.url);
const fallbackFile = Bun.file(new URL("index.html", distDirectory));

const host = process.env.HOST ?? "0.0.0.0";
const port = Number(process.env.PORT ?? "3000");

function getStaticFileUrl(pathname: string) {
	const cleanPathname = pathname === "/" ? "/index.html" : pathname;

	if (cleanPathname.includes("\0") || cleanPathname.includes("..")) {
		return null;
	}

	return new URL(`.${cleanPathname}`, distDirectory);
}

const server = Bun.serve({
	hostname: host,
	port,
	async fetch(request) {
		const url = new URL(request.url);
		const fileUrl = getStaticFileUrl(decodeURIComponent(url.pathname));

		if (fileUrl) {
			const file = Bun.file(fileUrl);

			if (await file.exists()) {
				return new Response(file);
			}
		}

		return new Response(fallbackFile, {
			headers: {
				"Content-Type": "text/html; charset=utf-8"
			}
		});
	}
});

console.log(`Magical Exchange web listening on http://${server.hostname}:${server.port}`);
