import type { ComponentProps } from "react";

import { cn } from "@/lib/utils";

type BadgeProps = ComponentProps<"span"> & {
	tone?: "accent" | "default" | "positive" | "secondary";
};

const toneClasses = {
	accent: "border-primary/20 bg-primary/10 text-primary",
	default: "border-border bg-card text-card-foreground",
	positive: "border-positive/20 bg-positive/10 text-positive",
	secondary: "border-border bg-secondary text-secondary-foreground"
};

export function Badge({ className, tone = "default", ...props }: BadgeProps) {
	return (
		<span
			className={cn("inline-flex h-6 shrink-0 items-center rounded-md border px-2 font-medium text-xs", toneClasses[tone], className)}
			{...props}
		/>
	);
}
