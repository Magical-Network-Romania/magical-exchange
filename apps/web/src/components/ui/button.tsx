import { cva, type VariantProps } from "class-variance-authority";
import type { ComponentProps } from "react";

import { cn } from "@/lib/utils";

const buttonVariants = cva(
	"inline-flex h-10 shrink-0 items-center justify-center gap-2 whitespace-nowrap rounded-md px-4 py-2 font-medium text-sm outline-none transition-colors focus-visible:border-ring focus-visible:ring-[3px] focus-visible:ring-ring/50 disabled:pointer-events-none disabled:opacity-50",
	{
		defaultVariants: {
			size: "default",
			variant: "default"
		},
		variants: {
			size: {
				default: "h-10 px-4 py-2",
				sm: "h-9 px-3",
				lg: "h-11 px-6"
			},
			variant: {
				default: "bg-primary text-primary-foreground hover:bg-primary/90",
				ghost: "hover:bg-accent hover:text-accent-foreground",
				outline: "border border-input bg-background hover:bg-accent hover:text-accent-foreground",
				secondary: "bg-secondary text-secondary-foreground hover:bg-secondary/80"
			}
		}
	}
);

type ButtonProps = ComponentProps<"button"> & VariantProps<typeof buttonVariants>;

export function Button({ className, size, variant, ...props }: ButtonProps) {
	return (
		<button
			className={cn(buttonVariants({ className, size, variant }))}
			{...props}
		/>
	);
}
