import { X } from "lucide-react";
import { type ReactNode, useEffect, useId } from "react";
import { createPortal } from "react-dom";

import { Button } from "@/components/ui/button";
import { cn } from "@/lib/utils";

type DialogProps = {
	children: ReactNode;
	description?: string;
	onOpenChange: (open: boolean) => void;
	open: boolean;
	title: string;
};

export function Dialog({ children, description, onOpenChange, open, title }: DialogProps) {
	const titleId = useId();
	const descriptionId = useId();

	useEffect(() => {
		if (!open) {
			return undefined;
		}

		function handleKeyDown(event: KeyboardEvent) {
			if (event.key === "Escape") {
				onOpenChange(false);
			}
		}

		document.addEventListener("keydown", handleKeyDown);

		return () => document.removeEventListener("keydown", handleKeyDown);
	}, [onOpenChange, open]);

	if (!open) {
		return null;
	}

	return createPortal(
		<div
			aria-describedby={description ? descriptionId : undefined}
			aria-labelledby={titleId}
			aria-modal="true"
			className="fixed inset-0 z-50 flex items-end justify-center bg-foreground/35 p-3 backdrop-blur-sm sm:items-center"
			role="dialog"
		>
			<button
				aria-label="Close"
				className="absolute inset-0 cursor-default"
				onClick={() => onOpenChange(false)}
				type="button"
			/>
			<div className={cn("relative w-full max-w-xl rounded-lg border bg-card p-5 text-card-foreground shadow-xl")}>
				<div className="flex items-start justify-between gap-4">
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
						aria-label="Close"
						className="size-9 px-0"
						onClick={() => onOpenChange(false)}
						type="button"
						variant="ghost"
					>
						<X className="size-4" />
					</Button>
				</div>
				<div className="mt-5">{children}</div>
			</div>
		</div>,
		document.body
	);
}
