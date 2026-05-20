import type { ReactNode } from "react";

import { cn } from "@/lib/utils";

type EmptyStateProps = {
	children: ReactNode;
	className?: string;
};

export function EmptyState({ children, className }: EmptyStateProps) {
	return (
		<div className={cn("rounded-lg border border-dashed bg-muted/35 px-4 py-8 text-center text-muted-foreground text-sm", className)}>
			{children}
		</div>
	);
}
