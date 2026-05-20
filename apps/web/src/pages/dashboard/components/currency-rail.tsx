import type { SelectOption } from "@/app-types";
import { Button } from "@/components/ui/button";
import { cn } from "@/lib/utils";

type CurrencyRailProps = {
	onSelectedCurrencyChange: (currency: string) => void;
	options: SelectOption[];
	selectedCurrency: string;
};

export function CurrencyRail({ onSelectedCurrencyChange, options, selectedCurrency }: CurrencyRailProps) {
	return (
		<div className="flex w-full max-w-full gap-1 overflow-x-auto rounded-md border bg-muted/35 p-1 lg:w-auto lg:max-w-md">
			{options.map((option) => (
				<Button
					className={cn("h-9 px-3", option.value !== selectedCurrency && "bg-card")}
					key={option.value}
					onClick={() => onSelectedCurrencyChange(option.value)}
					type="button"
					variant={option.value === selectedCurrency ? "default" : "ghost"}
				>
					{option.value}
				</Button>
			))}
		</div>
	);
}
