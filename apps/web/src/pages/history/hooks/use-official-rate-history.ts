import { useEffect, useState } from "react";

import { fetchOfficialRateHistory, isAbortError, type OfficialRateHistoryPoint } from "@/services/exchange-api";

export function useOfficialRateHistory(country: string, currency: string, from: string, to: string, enabled: boolean) {
	const [history, setHistory] = useState<OfficialRateHistoryPoint[]>([]);
	const [isLoading, setIsLoading] = useState(false);
	const [error, setError] = useState<string | null>(null);

	useEffect(() => {
		const controller = new AbortController();

		if (!enabled) {
			setHistory([]);
			setIsLoading(false);
			setError(null);

			return () => controller.abort();
		}

		setIsLoading(true);
		setError(null);

		fetchOfficialRateHistory(country, currency, from, to, { signal: controller.signal })
			.then((points) => {
				setHistory(points);
				setIsLoading(false);
			})
			.catch((caughtError: unknown) => {
				if (!isAbortError(caughtError)) {
					setError(caughtError instanceof Error ? caughtError.message : "Unknown API error");
					setHistory([]);
					setIsLoading(false);
				}
			});

		return () => controller.abort();
	}, [country, currency, enabled, from, to]);

	return { error, history, isLoading };
}
