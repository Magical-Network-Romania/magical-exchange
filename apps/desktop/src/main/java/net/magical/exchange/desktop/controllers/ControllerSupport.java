package net.magical.exchange.desktop.controllers;

import javafx.scene.Node;
import javafx.scene.control.Label;

final class ControllerSupport {

	private ControllerSupport() {
	}

	static void setManagedVisible(Node node, boolean visible) {
		node.setManaged(visible);
		node.setVisible(visible);
	}

	static Label label(String text, String styleClass) {
		Label label = new Label(text);
		label.getStyleClass().add(styleClass);
		label.setWrapText(true);

		return label;
	}
}
