import { RefreshCw } from "lucide-react";

import { Button } from "@/components/ui/button";
import { EmptyState } from "@/components/ui/empty-state";

type RetryStateProps = {
	message: string;
	onRetry: () => void;
	retryLabel: string;
};

export function RetryState({ message, onRetry, retryLabel }: RetryStateProps) {
	return (
		<EmptyState>
			<div className="grid justify-items-center gap-3">
				<span>{message}</span>
				<Button
					onClick={onRetry}
					type="button"
					variant="outline"
				>
					<RefreshCw className="size-4" />
					{retryLabel}
				</Button>
			</div>
		</EmptyState>
	);
}
