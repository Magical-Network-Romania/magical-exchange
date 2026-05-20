import type { ReactNode } from "react";

type MetricCardProps = {
	icon: ReactNode;
	label: string;
	value: string;
};

export function MetricCard({ icon, label, value }: MetricCardProps) {
	return (
		<div className="min-w-0 rounded-lg border bg-card p-4">
			<div className="flex min-w-0 items-center gap-2 text-muted-foreground text-sm">
				{icon}
				<span className="truncate">{label}</span>
			</div>
			<div className="mt-2 wrap-break-word font-semibold text-2xl">{value}</div>
		</div>
	);
}
