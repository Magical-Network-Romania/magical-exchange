import { cn } from "@/lib/utils";

type AmountBoxProps = {
	currency: string;
	label: string;
	onChange?: (value: string) => void;
	readOnly?: boolean;
	value: string;
};

export function AmountBox({ currency, label, onChange, readOnly = false, value }: AmountBoxProps) {
	return (
		<label className="grid min-w-0 gap-1.5 text-sm">
			<span className="font-medium text-muted-foreground text-xs uppercase">{label}</span>
			<span
				className={cn(
					"grid h-12 grid-cols-[minmax(0,1fr)_auto] overflow-hidden rounded-md border bg-card",
					readOnly && "bg-muted/40"
				)}
			>
				<input
					aria-label={`${label} ${currency}`}
					className="min-w-0 bg-transparent px-3 text-right font-medium text-lg outline-none"
					inputMode="decimal"
					onChange={(event) => onChange?.(event.target.value)}
					readOnly={readOnly}
					type="text"
					value={value}
				/>
				<span className="flex min-w-14 items-center justify-center border-border border-l bg-muted px-3 font-semibold text-sm">
					{currency}
				</span>
			</span>
		</label>
	);
}
