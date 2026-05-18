import { CheckCircle2, Loader2, Server, WalletCards, XCircle } from "lucide-react";
import { useState } from "react";

import { Button } from "@/components/ui/button";
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card";

type ApiStatus = "idle" | "loading" | "success" | "error";

type ApiResult = {
	message: string;
	raw: string;
};

function getDefaultApiBaseUrl() {
	const localHosts = new Set(["localhost", "127.0.0.1", "::1"]);

	if (localHosts.has(window.location.hostname)) {
		return "http://localhost:8080";
	}

	return `${window.location.protocol}//${window.location.hostname}:8080`;
}

async function requestApiPing() {
	const response = await fetch(`${getDefaultApiBaseUrl()}/api/ping`);
	const data: unknown = await response.json();

	if (!response.ok) {
		throw new Error(`API returned ${response.status}`);
	}

	if (!data || typeof data !== "object" || !("message" in data) || typeof data.message !== "string") {
		throw new Error("API response did not match the expected shape");
	}

	return {
		message: data.message,
		raw: JSON.stringify(data, null, 2)
	};
}

export function App() {
	const [apiResult, setApiResult] = useState<ApiResult | null>(null);
	const [errorMessage, setErrorMessage] = useState<string | null>(null);
	const [status, setStatus] = useState<ApiStatus>("idle");

	async function handleApiCheck() {
		setStatus("loading");
		setErrorMessage(null);

		try {
			const result = await requestApiPing();
			setApiResult(result);
			setStatus("success");
		} catch (error) {
			setApiResult(null);
			setErrorMessage(error instanceof Error ? error.message : "Unknown API error");
			setStatus("error");
		}
	}

	const isLoading = status === "loading";

	return (
		<main className="min-h-screen px-5 py-8 sm:px-8 lg:px-12">
			<section className="mx-auto flex w-full max-w-5xl flex-col gap-6">
				<div className="flex flex-col gap-3">
					<div className="flex items-center gap-3 text-primary">
						<div className="flex size-10 items-center justify-center rounded-md bg-primary text-primary-foreground">
							<WalletCards className="size-5" />
						</div>
						<span className="font-medium text-sm">Magical Exchange</span>
					</div>
					<div className="max-w-3xl">
						<h1 className="font-semibold text-3xl tracking-normal sm:text-4xl">Web smoke test</h1>
						<p className="mt-3 text-muted-foreground">
							React, Tailwind, shadcn-style components, and the Spring API are wired together for the first deployment pass.
						</p>
					</div>
				</div>

				<div className="grid gap-4 md:grid-cols-[1fr_1.15fr]">
					<Card>
						<CardHeader>
							<CardTitle>Frontend</CardTitle>
							<CardDescription>Bun serves this React page directly from the HTML entrypoint.</CardDescription>
						</CardHeader>
						<CardContent className="grid gap-3 text-sm">
							<div className="flex items-center justify-between rounded-md border px-3 py-2">
								<span className="text-muted-foreground">Renderer</span>
								<span className="font-medium">React TSX</span>
							</div>
							<div className="flex items-center justify-between rounded-md border px-3 py-2">
								<span className="text-muted-foreground">Styling</span>
								<span className="font-medium">Tailwind + shadcn</span>
							</div>
						</CardContent>
					</Card>

					<Card>
						<CardHeader>
							<CardTitle>API check</CardTitle>
							<CardDescription>The button calls the backend ping endpoint and prints the JSON response.</CardDescription>
						</CardHeader>
						<CardContent className="flex flex-col gap-4">
							<div className="flex flex-col gap-3 sm:flex-row sm:items-center">
								<Button
									disabled={isLoading}
									onClick={handleApiCheck}
								>
									{isLoading ? <Loader2 className="size-4 animate-spin" /> : <Server className="size-4" />}
									Check API
								</Button>
								<div className="flex items-center gap-2 text-sm">
									{status === "success" && <CheckCircle2 className="size-4 text-primary" />}
									{status === "error" && <XCircle className="size-4 text-red-600" />}
									<span className="text-muted-foreground">
										{status === "idle" && "Waiting for a request"}
										{status === "loading" && "Requesting /api/ping"}
										{status === "success" && `API says ${apiResult?.message}`}
										{status === "error" && errorMessage}
									</span>
								</div>
							</div>

							<pre className="min-h-28 overflow-x-auto rounded-md border bg-muted p-4 text-sm">
								{apiResult?.raw ?? '{\n  "message": "Click Check API"\n}'}
							</pre>
						</CardContent>
					</Card>
				</div>
			</section>
		</main>
	);
}
