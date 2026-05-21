package net.magical.exchange.desktop;

import java.io.IOException;
import java.net.URL;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import net.magical.exchange.desktop.controllers.AppController;

public class MagicalExchangeDesktopApplication extends Application {

	private static final String APP_FXML = "/net/magical/exchange/desktop/fxml/App.fxml";
	private static final String APP_CSS = "/net/magical/exchange/desktop/styles/app.css";
	private static final String LOGO = "/net/magical/exchange/desktop/assets/brand/logo.png";

	public static void main(String[] args) {
		launch(args);
	}

	@Override
	public void start(Stage stage) throws IOException {
		FXMLLoader loader = new FXMLLoader(requiredResource(APP_FXML));
		Parent root = loader.load();
		AppController controller = loader.getController();
		controller.setHostServices(getHostServices());

		Scene scene = new Scene(root, 1180, 820);
		scene.getStylesheets().add(requiredResource(APP_CSS).toExternalForm());

		stage.setTitle("Magical Exchange");
		stage.setMinWidth(980);
		stage.setMinHeight(720);
		stage.getIcons().add(new Image(requiredResource(LOGO).toExternalForm()));
		stage.setScene(scene);
		stage.show();
	}

	private static URL requiredResource(String path) {
		URL resource = MagicalExchangeDesktopApplication.class.getResource(path);

		if (resource == null) {
			throw new IllegalStateException("Missing desktop resource: " + path);
		}

		return resource;
	}
}
