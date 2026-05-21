package net.magical.exchange.desktop.controllers;

import java.io.IOException;
import java.net.URL;
import javafx.application.HostServices;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import net.magical.exchange.desktop.i18n.I18nService;
import net.magical.exchange.desktop.model.LocationDto;

public class LocationDialogController {

	private static final String DIALOG_FXML = "/net/magical/exchange/desktop/fxml/LocationDialog.fxml";

	@FXML
	private Label titleLabel;

	@FXML
	private Label descriptionLabel;

	@FXML
	private Label institutionLabel;

	@FXML
	private Label institutionValueLabel;

	@FXML
	private Label addressLabel;

	@FXML
	private Label addressValueLabel;

	@FXML
	private VBox phoneRow;

	@FXML
	private Label phoneLabel;

	@FXML
	private Label phoneValueLabel;

	@FXML
	private VBox emailRow;

	@FXML
	private Label emailLabel;

	@FXML
	private Label emailValueLabel;

	@FXML
	private Button websiteButton;

	public static void open(BorderPane owner, LocationDto location, I18nService i18n, HostServices hostServices) {
		try {
			FXMLLoader loader = new FXMLLoader(requiredResource(DIALOG_FXML));
			Parent content = loader.load();
			LocationDialogController controller = loader.getController();
			controller.render(location, i18n, hostServices);

			Dialog<ButtonType> dialog = new Dialog<>();
			dialog.setTitle(i18n.text("locationDetails"));
			dialog.initOwner(owner.getScene().getWindow());
			dialog.getDialogPane().setContent(content);
			dialog.getDialogPane().getButtonTypes().setAll(ButtonType.CLOSE);
			dialog.getDialogPane().getStylesheets().addAll(owner.getScene().getStylesheets());
			dialog.getDialogPane().getStyleClass().addAll(owner.getStyleClass());
			Button closeButton = (Button) dialog.getDialogPane().lookupButton(ButtonType.CLOSE);
			closeButton.setText(i18n.text("close"));
			dialog.showAndWait();
		} catch (IOException caughtError) {
			throw new IllegalStateException("Could not load location dialog", caughtError);
		}
	}

	private void render(LocationDto location, I18nService i18n, HostServices hostServices) {
		titleLabel.setText(location.name());
		descriptionLabel.setText(location.institution().name());
		institutionLabel.setText(i18n.text("institution"));
		institutionValueLabel.setText(location.institution().name());
		addressLabel.setText(i18n.text("address"));
		addressValueLabel.setText(location.address());
		phoneLabel.setText(i18n.text("phone"));
		phoneValueLabel.setText(location.phone());
		emailLabel.setText(i18n.text("email"));
		emailValueLabel.setText(location.email());
		websiteButton.setText(i18n.text("website"));
		ControllerSupport.setManagedVisible(phoneRow, location.phone() != null && !location.phone().isBlank());
		ControllerSupport.setManagedVisible(emailRow, location.email() != null && !location.email().isBlank());
		ControllerSupport.setManagedVisible(websiteButton, location.institution().websiteUrl() != null);
		websiteButton.setOnAction(event -> openWebsite(location, hostServices));
	}

	private void openWebsite(LocationDto location, HostServices hostServices) {
		if (hostServices != null && location.institution().websiteUrl() != null) {
			hostServices.showDocument(location.institution().websiteUrl());
		}
	}

	private static URL requiredResource(String path) {
		URL resource = LocationDialogController.class.getResource(path);

		if (resource == null) {
			throw new IllegalStateException("Missing desktop resource: " + path);
		}

		return resource;
	}
}
