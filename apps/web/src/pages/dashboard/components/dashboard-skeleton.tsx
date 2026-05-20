import { Skeleton } from "@/components/ui/skeleton";

export function DashboardSkeleton() {
	return (
		<div className="grid gap-6">
			<Skeleton className="h-24" />
			<div className="grid gap-6 xl:grid-cols-2">
				<Skeleton className="h-96" />
				<Skeleton className="h-96" />
			</div>
		</div>
	);
}
