import { Skeleton } from "@/components/ui/skeleton";

export function MarketRatesSkeleton() {
	return (
		<div className="grid gap-6">
			<Skeleton className="h-24" />
			<Skeleton className="h-96" />
		</div>
	);
}
