import { X } from "lucide-react";
import { type ReactNode, useEffect, useId, useRef } from "react";
import { createPortal } from "react-dom";

import { Button } from "@/components/ui/button";
import { cn } from "@/lib/utils";

type DialogProps = {
	children: ReactNode;
	closeLabel?: string;
	description?: string;
	onOpenChange: (open: boolean) => void;
	open: boolean;
	title: string;
};

export function Dialog({ children, closeLabel = "Close", description, onOpenChange, open, title }: DialogProps) {
	const titleId = useId();
	const descriptionId = useId();
	const dialogRef = useRef<HTMLDivElement>(null);

	useEffect(() => {
		if (!open) {
			return undefined;
		}

		const activeElement = document.activeElement instanceof HTMLElement ? document.activeElement : null;
		const previousOverflow = document.body.style.overflow;

		function handleKeyDown(event: KeyboardEvent) {
			if (event.key === "Escape") {
				onOpenChange(false);
			}
		}

		document.body.style.overflow = "hidden";
		document.addEventListener("keydown", handleKeyDown);
		dialogRef.current?.focus();

		return () => {
			document.body.style.overflow = previousOverflow;
			document.removeEventListener("keydown", handleKeyDown);
			activeElement?.focus();
		};
	}, [onOpenChange, open]);

	if (!open) {
		return null;
	}

	return createPortal(
		<div
			aria-describedby={description ? descriptionId : undefined}
			aria-labelledby={titleId}
			aria-modal="true"
			className="fixed inset-0 z-50 flex items-end justify-center bg-foreground/35 p-0 backdrop-blur-sm sm:items-center sm:p-3"
			role="dialog"
		>
			<div
				aria-hidden="true"
				className="absolute inset-0 cursor-default"
				onClick={() => onOpenChange(false)}
			/>
			<div
				className={cn(
					"relative flex max-h-[calc(100dvh-1rem)] w-full max-w-xl flex-col overflow-hidden rounded-t-lg border bg-card text-card-foreground shadow-xl sm:rounded-lg"
				)}
				ref={dialogRef}
				tabIndex={-1}
			>
				<div className="flex items-start justify-between gap-4 p-5 pb-0">
					<div className="min-w-0">
						<h2
							className="font-semibold text-xl"
							id={titleId}
						>
							{title}
						</h2>
						{description && (
							<p
								className="mt-1 text-muted-foreground text-sm"
								id={descriptionId}
							>
								{description}
							</p>
						)}
					</div>
					<Button
						aria-label={closeLabel}
						className="size-9 px-0"
						onClick={() => onOpenChange(false)}
						type="button"
						variant="ghost"
					>
						<X className="size-4" />
					</Button>
				</div>
				<div className="mt-5 overflow-y-auto px-5 pb-5">{children}</div>
			</div>
		</div>,
		document.body
	);
}
