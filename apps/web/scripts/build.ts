import { copyFile, rm } from "node:fs/promises";
import tailwind from "bun-plugin-tailwind";

await rm("dist", { force: true, recursive: true });

const result = await Bun.build({
	entrypoints: ["src/index.html"],
	outdir: "dist",
	plugins: [tailwind],
	target: "browser"
});

if (!result.success) {
	for (const log of result.logs) {
		console.error(log);
	}

	throw new Error("Web build failed");
}

await copyFile("logo.svg", "dist/logo.svg");
await copyFile("logo.png", "dist/logo.png");
