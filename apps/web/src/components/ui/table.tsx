import type { ComponentProps } from "react";

import { cn } from "@/lib/utils";

export function Table({ className, ...props }: ComponentProps<"table">) {
	return (
		<div className="overflow-x-auto">
			<table
				className={cn("w-full border-collapse text-left text-sm", className)}
				{...props}
			/>
		</div>
	);
}

export function TableBody(props: ComponentProps<"tbody">) {
	return <tbody {...props} />;
}

export function TableCell({ className, ...props }: ComponentProps<"td">) {
	return (
		<td
			className={cn("border-border border-t px-3 py-3 align-middle", className)}
			{...props}
		/>
	);
}

export function TableHead({ className, ...props }: ComponentProps<"th">) {
	return (
		<th
			className={cn("h-10 px-3 text-muted-foreground font-medium text-xs uppercase", className)}
			{...props}
		/>
	);
}

export function TableHeader(props: ComponentProps<"thead">) {
	return <thead {...props} />;
}

export function TableRow({ className, ...props }: ComponentProps<"tr">) {
	return (
		<tr
			className={cn("transition-colors hover:bg-muted/45", className)}
			{...props}
		/>
	);
}
