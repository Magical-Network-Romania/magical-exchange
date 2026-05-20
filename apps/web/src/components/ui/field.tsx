import type { ComponentProps } from "react";

import { cn } from "@/lib/utils";

type Option = {
	label: string;
	value: string;
};

type SelectFieldProps = Omit<ComponentProps<"select">, "onChange"> & {
	label: string;
	onValueChange: (value: string) => void;
	options: readonly Option[];
};

type InputFieldProps = ComponentProps<"input"> & {
	label: string;
};

export function SelectField({ className, label, onValueChange, options, ...props }: SelectFieldProps) {
	return (
		<label className="grid min-w-0 gap-1.5 text-sm">
			<span className="font-medium text-muted-foreground text-xs uppercase">{label}</span>
			<select
				className={cn(
					"h-10 w-full min-w-0 rounded-md border border-input bg-card px-3 text-sm outline-none transition-colors focus:border-ring focus:ring-[3px] focus:ring-ring/20 disabled:cursor-not-allowed disabled:opacity-60",
					className
				)}
				onChange={(event) => onValueChange(event.target.value)}
				{...props}
			>
				{options.map((option) => (
					<option
						key={option.value}
						value={option.value}
					>
						{option.label}
					</option>
				))}
			</select>
		</label>
	);
}

export function InputField({ className, label, ...props }: InputFieldProps) {
	return (
		<label className="grid min-w-0 gap-1.5 text-sm">
			<span className="font-medium text-muted-foreground text-xs uppercase">{label}</span>
			<input
				className={cn(
					"h-10 w-full min-w-0 rounded-md border border-input bg-card px-3 text-sm outline-none transition-colors focus:border-ring focus:ring-[3px] focus:ring-ring/20 disabled:cursor-not-allowed disabled:opacity-60",
					className
				)}
				{...props}
			/>
		</label>
	);
}
