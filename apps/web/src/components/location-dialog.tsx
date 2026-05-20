import { Building2, ExternalLink, Mail, MapPin, Phone } from "lucide-react";
import type { ReactNode } from "react";

import { Dialog } from "@/components/ui/dialog";
import type { TranslationKey } from "@/i18n";
import type { LocationDto } from "@/services/exchange-api";

type LocationDialogProps = {
	location: LocationDto | null;
	onOpenChange: (open: boolean) => void;
	t: (key: TranslationKey) => string;
};

export function LocationDialog({ location, onOpenChange, t }: LocationDialogProps) {
	if (!location) {
		return null;
	}

	return (
		<Dialog
			closeLabel={t("close")}
			description={location.institution.name}
			onOpenChange={onOpenChange}
			open={Boolean(location)}
			title={location.name}
		>
			<div className="grid gap-4 text-sm">
				<DetailRow
					icon={<Building2 className="size-4" />}
					label={t("institution")}
					value={location.institution.name}
				/>
				<DetailRow
					icon={<MapPin className="size-4" />}
					label={t("address")}
					value={location.address}
				/>
				{location.phone && (
					<DetailRow
						icon={<Phone className="size-4" />}
						label={t("phone")}
						value={location.phone}
					/>
				)}
				{location.email && (
					<DetailRow
						icon={<Mail className="size-4" />}
						label={t("email")}
						value={location.email}
					/>
				)}
				{location.institution.websiteUrl && (
					<a
						className="inline-flex items-center gap-2 text-primary text-sm hover:underline"
						href={location.institution.websiteUrl}
						rel="noreferrer"
						target="_blank"
					>
						<ExternalLink className="size-4" />
						{t("website")}
					</a>
				)}
			</div>
		</Dialog>
	);
}

type DetailRowProps = {
	icon: ReactNode;
	label: string;
	value: string;
};

function DetailRow({ icon, label, value }: DetailRowProps) {
	return (
		<div className="grid min-w-0 gap-1 rounded-md border bg-muted/25 p-3">
			<div className="flex items-center gap-2 text-muted-foreground text-xs uppercase">
				{icon}
				<span>{label}</span>
			</div>
			<div className="wrap-break-word font-medium">{value}</div>
		</div>
	);
}
