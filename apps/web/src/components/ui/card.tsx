import type { ComponentProps } from "react";

import { cn } from "@/lib/utils";

export function Card({ className, ...props }: ComponentProps<"div">) {
	return (
		<div
			className={cn("min-w-0 rounded-lg border bg-card text-card-foreground shadow-sm", className)}
			{...props}
		/>
	);
}

export function CardContent({ className, ...props }: ComponentProps<"div">) {
	return (
		<div
			className={cn("min-w-0 p-4 sm:p-6", className)}
			{...props}
		/>
	);
}

export function CardDescription({ className, ...props }: ComponentProps<"p">) {
	return (
		<p
			className={cn("wrap-break-word text-muted-foreground text-sm", className)}
			{...props}
		/>
	);
}

export function CardHeader({ className, ...props }: ComponentProps<"div">) {
	return (
		<div
			className={cn("flex min-w-0 flex-col gap-1.5 p-4 sm:p-6", className)}
			{...props}
		/>
	);
}

export function CardTitle({ className, ...props }: ComponentProps<"h2">) {
	return (
		<h2
			className={cn("wrap-break-word font-semibold text-xl tracking-normal", className)}
			{...props}
		/>
	);
}
