import { CartesianGrid, Line, LineChart, ResponsiveContainer, Tooltip, XAxis, YAxis } from "recharts";

import { formatRate } from "@/format";
import type { UiLocale } from "@/i18n";
import type { HistoryChartPoint } from "../utils";

type HistoryChartProps = {
	data: HistoryChartPoint[];
	locale: UiLocale;
	selectedCurrency: string;
};

export function HistoryChart({ data, locale, selectedCurrency }: HistoryChartProps) {
	return (
		<div className="h-72 min-w-0 sm:h-80">
			<ResponsiveContainer
				height="100%"
				width="100%"
			>
				<LineChart data={data}>
					<CartesianGrid
						stroke="var(--border)"
						strokeDasharray="3 3"
					/>
					<XAxis
						dataKey="formattedDate"
						minTickGap={28}
						stroke="var(--muted-foreground)"
						tickLine={false}
					/>
					<YAxis
						domain={["auto", "auto"]}
						stroke="var(--muted-foreground)"
						tickFormatter={(value: number) => formatRate(value, locale, 3)}
						tickLine={false}
						width={72}
					/>
					<Tooltip
						content={({ active, payload }) => (
							<ChartTooltip
								active={Boolean(active)}
								locale={locale}
								payload={payload?.[0]?.payload as HistoryChartPoint | undefined}
								selectedCurrency={selectedCurrency}
							/>
						)}
					/>
					<Line
						dataKey="rate"
						dot={false}
						stroke="var(--primary)"
						strokeWidth={2}
						type="monotone"
					/>
				</LineChart>
			</ResponsiveContainer>
		</div>
	);
}

type ChartTooltipProps = {
	active: boolean;
	locale: UiLocale;
	payload: HistoryChartPoint | undefined;
	selectedCurrency: string;
};

function ChartTooltip({ active, locale, payload, selectedCurrency }: ChartTooltipProps) {
	if (!active || !payload) {
		return null;
	}

	return (
		<div className="rounded-md border bg-card px-3 py-2 text-sm shadow-md">
			<div className="text-muted-foreground">{payload.formattedDate}</div>
			<div className="font-semibold">
				{formatRate(payload.rate, locale)} {selectedCurrency}
			</div>
		</div>
	);
}
