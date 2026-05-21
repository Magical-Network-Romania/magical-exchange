import { useEffect, useRef, useState } from "react";
import type { Translate } from "@/app-types";
import { Button } from "@/components/ui/button";
import type { ExportFormat } from "@/export-data";

type ExportMenuButtonProps = {
	disabled?: boolean;
	onExport: (format: ExportFormat) => void;
	t: Translate;
};

export function ExportMenuButton({ disabled = false, onExport, t }: ExportMenuButtonProps) {
	const [open, setOpen] = useState(false);
	const menuRef = useRef<HTMLDivElement>(null);

	useEffect(() => {
		if (!open) {
			return;
		}

		function closeWhenOutside(event: Event) {
			const target = event.target;

			if (target instanceof Node && !menuRef.current?.contains(target)) {
				setOpen(false);
			}
		}

		document.addEventListener("pointerdown", closeWhenOutside);
		document.addEventListener("focusin", closeWhenOutside);

		return () => {
			document.removeEventListener("pointerdown", closeWhenOutside);
			document.removeEventListener("focusin", closeWhenOutside);
		};
	}, [open]);

	useEffect(() => {
		if (disabled) {
			setOpen(false);
		}
	}, [disabled]);

	function handleExport(format: ExportFormat) {
		setOpen(false);
		onExport(format);
	}

	return (
		<div
			className="relative inline-block"
			ref={menuRef}
		>
			<Button
				aria-expanded={open}
				aria-haspopup="menu"
				disabled={disabled}
				onClick={() => setOpen((current) => !current)}
				type="button"
				variant="outline"
			>
				{t("export")}
			</Button>
			{open && (
				<div
					className="absolute right-0 mt-2 min-w-32 overflow-hidden rounded-md border bg-card p-1 shadow-lg"
					role="menu"
				>
					<button
						className="block w-full rounded-sm px-3 py-2 text-left text-sm hover:bg-accent hover:text-accent-foreground"
						onClick={() => handleExport("csv")}
						role="menuitem"
						type="button"
					>
						{t("exportCsv")}
					</button>
					<button
						className="block w-full rounded-sm px-3 py-2 text-left text-sm hover:bg-accent hover:text-accent-foreground"
						onClick={() => handleExport("txt")}
						role="menuitem"
						type="button"
					>
						{t("exportTxt")}
					</button>
				</div>
			)}
		</div>
	);
}
