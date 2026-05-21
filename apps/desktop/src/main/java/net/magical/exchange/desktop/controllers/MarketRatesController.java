package net.magical.exchange.desktop.controllers;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.util.List;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import net.magical.exchange.desktop.model.AppState;
import net.magical.exchange.desktop.model.BootstrapDto;
import net.magical.exchange.desktop.model.MarketRateDto;
import net.magical.exchange.desktop.model.MarketRateGroup;
import net.magical.exchange.desktop.model.UiLocale;
import net.magical.exchange.desktop.util.ExchangeFormat;
import net.magical.exchange.desktop.util.MarketRateGrouper;

public class MarketRatesController implements PageController {

	@FXML
	private VBox contentBox;

	@FXML
	private VBox statusBox;

	@FXML
	private Label statusLabel;

	@FXML
	private Button retryButton;

	@FXML
	private Label titleLabel;

	@FXML
	private Label subtitleLabel;

	@FXML
	private Label cardTitleLabel;

	@FXML
	private Label cardDescriptionLabel;

	@FXML
	private VBox groupsBox;

	private AppController host;
	private BootstrapDto currentBootstrap;
	private UiLocale currentLocale = UiLocale.EN;

	@FXML
	public void initialize() {
		retryButton.setOnAction(event -> host.refresh());
	}

	@Override
	@SuppressFBWarnings(value = "EI_EXPOSE_REP2", justification = "FXML page controllers intentionally keep their app host.")
	public void setHost(AppController appController) {
		host = appController;
	}

	@Override
	public void refreshText() {
		titleLabel.setText(host.i18n().text("allRatesTitle"));
		cardTitleLabel.setText(host.i18n().text("allRates"));
		cardDescriptionLabel.setText(host.i18n().text("allRatesSubtitle"));
		retryButton.setText(host.i18n().text("retry"));
		renderCurrentContent();
	}

	@Override
	public void render(AppState state) {
		currentLocale = state.locale();
		currentBootstrap = state.bootstrap();

		if (state.bootstrapLoading() && currentBootstrap == null) {
			showStatus(host.i18n().text("loading"), false);
			return;
		}

		if (state.bootstrapError() != null && currentBootstrap == null) {
			showStatus(state.bootstrapError(), true);
			return;
		}

		if (currentBootstrap == null) {
			showStatus(host.i18n().text("statusNoData"), false);
			return;
		}

		ControllerSupport.setManagedVisible(statusBox, false);
		ControllerSupport.setManagedVisible(contentBox, true);
		renderCurrentContent();
	}

	private void renderCurrentContent() {
		if (currentBootstrap == null || host == null) {
			return;
		}

		subtitleLabel.setText(currentBootstrap.city().name() + ", " + currentBootstrap.country().name());
		List<MarketRateGroup> groups = MarketRateGrouper.groupMarketRates(currentBootstrap.marketRates());
		groupsBox.getChildren().clear();

		if (groups.isEmpty()) {
			groupsBox.getChildren().add(ControllerSupport.label(host.i18n().text("emptyMarketRates"), "empty-state"));
			return;
		}

		for (MarketRateGroup group : groups) {
			groupsBox.getChildren().add(createGroupCard(group));
		}
	}

	private VBox createGroupCard(MarketRateGroup group) {
		VBox card = new VBox();
		card.getStyleClass().add("rate-group-card");
		card.getChildren().add(createGroupHeader(group));
		card.getChildren().add(createRatesGrid(group.rates()));

		return card;
	}

	private Button createGroupHeader(MarketRateGroup group) {
		Button button = new Button();
		button.getStyleClass().add("group-header-button");
		button.setMaxWidth(Double.MAX_VALUE);
		button.setOnAction(event -> host.openLocation(group.location()));

		Label institution = ControllerSupport.label(group.location().institution().name(), "provider-label");
		Label count = new Label(String.valueOf(group.rates().size()));
		Label office = ControllerSupport.label(group.location().name(), "muted-small");
		Label address = ControllerSupport.label(group.location().address(), "muted-small-right");
		VBox left = new VBox(4, new HBox(8, institution, count), office);
		GridPane header = new GridPane();

		count.getStyleClass().add("badge-secondary");
		header.getStyleClass().add("group-header-grid");
		GridPane.setHgrow(left, Priority.ALWAYS);
		header.add(left, 0, 0);
		header.add(address, 1, 0);
		button.setGraphic(header);

		return button;
	}

	private GridPane createRatesGrid(List<MarketRateDto> rates) {
		GridPane grid = new GridPane();
		grid.getStyleClass().add("rates-grid");
		grid.getColumnConstraints().setAll(rateGridColumn(26), rateGridColumn(23), rateGridColumn(23), rateGridColumn(28));
		addHeaderCell(grid, host.i18n().text("currency"), 0);
		addHeaderCell(grid, host.i18n().text("buy"), 1);
		addHeaderCell(grid, host.i18n().text("sell"), 2);
		addHeaderCell(grid, host.i18n().text("fetched"), 3);

		int row = 1;

		for (MarketRateDto rate : rates) {
			addRateRow(grid, rate, row);
			row++;
		}

		return grid;
	}

	private void addRateRow(GridPane grid, MarketRateDto rate, int row) {
		Button currencyButton = cellButton(currencyCell(rate), rate);
		Button buyButton = cellButton(new Label(ExchangeFormat.formatRate(rate.buyRate(), currentLocale)), rate);
		Button sellButton = cellButton(new Label(ExchangeFormat.formatRate(rate.sellRate(), currentLocale)), rate);
		Button fetchedButton = cellButton(new Label(ExchangeFormat.formatDateTime(rate.fetchedAt(), currentLocale)), rate);

		grid.add(currencyButton, 0, row);
		grid.add(buyButton, 1, row);
		grid.add(sellButton, 2, row);
		grid.add(fetchedButton, 3, row);
	}

	private VBox currencyCell(MarketRateDto rate) {
		Label code = new Label(rate.currency().code());
		Label name = new Label(rate.currency().name());
		code.getStyleClass().add("strong-label");
		name.getStyleClass().add("muted-small");

		VBox cell = new VBox(2, code, name);
		cell.setAlignment(Pos.CENTER_LEFT);

		return cell;
	}

	private Button cellButton(Node content, MarketRateDto rate) {
		Button button = new Button();
		button.getStyleClass().add("table-cell-button");
		button.setMaxWidth(Double.MAX_VALUE);
		button.setMinHeight(Region.USE_PREF_SIZE);
		button.setPrefHeight(58);
		button.setMaxHeight(Region.USE_PREF_SIZE);
		button.setAlignment(Pos.CENTER_LEFT);
		button.setGraphic(content);
		button.setOnAction(event -> host.openLocation(rate.location()));
		GridPane.setHgrow(button, Priority.ALWAYS);

		return button;
	}

	private void addHeaderCell(GridPane grid, String text, int column) {
		Label label = new Label(text);
		label.getStyleClass().add("table-header-label");
		label.setMaxWidth(Double.MAX_VALUE);
		GridPane.setHgrow(label, Priority.ALWAYS);
		grid.add(label, column, 0);
	}

	private ColumnConstraints rateGridColumn(double percentWidth) {
		ColumnConstraints constraints = new ColumnConstraints();
		constraints.setPercentWidth(percentWidth);
		constraints.setHgrow(Priority.ALWAYS);

		return constraints;
	}

	private void showStatus(String message, boolean canRetry) {
		statusLabel.setText(message);
		ControllerSupport.setManagedVisible(statusBox, true);
		ControllerSupport.setManagedVisible(contentBox, false);
		ControllerSupport.setManagedVisible(retryButton, canRetry);
	}
}
