package net.magical.exchange.desktop.controllers;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.stage.FileChooser;
import javafx.stage.Window;
import net.magical.exchange.desktop.i18n.I18nService;
import net.magical.exchange.desktop.util.ExchangeExport;

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

	static void saveExport(Window owner, I18nService i18n, String fileBaseName, ExchangeExport.Format format, String content) {
		FileChooser chooser = new FileChooser();
		chooser.setTitle(i18n.text("export"));
		chooser.setInitialFileName(fileBaseName + "." + format.extension());
		String label = format.extension().toUpperCase(Locale.ROOT) + " files";
		String pattern = "*." + format.extension();
		chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter(label, pattern));
		File file = chooser.showSaveDialog(owner);

		if (file == null) {
			return;
		}

		try {
			Files.writeString(pathWithExtension(file, format), content, StandardCharsets.UTF_8);
		} catch (IOException caughtError) {
			Alert alert = new Alert(Alert.AlertType.ERROR);
			alert.initOwner(owner);
			alert.setTitle(i18n.text("exportFailed"));
			alert.setHeaderText(i18n.text("exportFailed"));
			alert.setContentText(caughtError.getMessage());
			alert.showAndWait();
		}
	}

	private static Path pathWithExtension(File file, ExchangeExport.Format format) {
		String expectedExtension = "." + format.extension();

		if (file.getName().toLowerCase(Locale.ROOT).endsWith(expectedExtension)) {
			return file.toPath();
		}

		return file.toPath().resolveSibling(file.getName() + expectedExtension);
	}
}
