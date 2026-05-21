package net.magical.exchange.desktop.controllers;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.math.BigDecimal;
import java.util.List;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import net.magical.exchange.desktop.model.AppState;
import net.magical.exchange.desktop.model.BootstrapDto;
import net.magical.exchange.desktop.model.CurrencyDto;
import net.magical.exchange.desktop.model.MarketRateDto;
import net.magical.exchange.desktop.model.OfficialRateDto;
import net.magical.exchange.desktop.model.RateOffer;
import net.magical.exchange.desktop.model.RateOfferKind;
import net.magical.exchange.desktop.model.UiLocale;
import net.magical.exchange.desktop.util.DashboardCalculator;
import net.magical.exchange.desktop.util.ExchangeFormat;

public class DashboardController implements PageController {

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
	private Label converterTitleLabel;

	@FXML
	private Label converterDescriptionLabel;

	@FXML
	private HBox currencyRail;

	@FXML
	private Label buyTitleLabel;

	@FXML
	private Label buyRateLabel;

	@FXML
	private Label buyInputLabel;

	@FXML
	private TextField buyInputField;

	@FXML
	private Label buyInputCurrencyLabel;

	@FXML
	private Label buyOutputLabel;

	@FXML
	private TextField buyOutputField;

	@FXML
	private Label buyOutputCurrencyLabel;

	@FXML
	private Label buyOffersHeadingLabel;

	@FXML
	private VBox buyOffersBox;

	@FXML
	private Label sellTitleLabel;

	@FXML
	private Label sellRateLabel;

	@FXML
	private Label sellInputLabel;

	@FXML
	private TextField sellInputField;

	@FXML
	private Label sellInputCurrencyLabel;

	@FXML
	private Label sellOutputLabel;

	@FXML
	private TextField sellOutputField;

	@FXML
	private Label sellOutputCurrencyLabel;

	@FXML
	private Label sellOffersHeadingLabel;

	@FXML
	private VBox sellOffersBox;

	@FXML
	private Label officialDateMetricLabel;

	@FXML
	private Label officialDateMetricValue;

	@FXML
	private Label locationsMetricLabel;

	@FXML
	private Label locationsMetricValue;

	@FXML
	private Label marketRatesMetricLabel;

	@FXML
	private Label marketRatesMetricValue;

	@FXML
	private Label baseCurrencyMetricLabel;

	@FXML
	private Label baseCurrencyMetricValue;

	@FXML
	private Label officialRatesTitleLabel;

	@FXML
	private Label officialRatesDescriptionLabel;

	@FXML
	private TableView<OfficialRateDto> officialRatesTable;

	private AppController host;
	private BootstrapDto currentBootstrap;
	private List<RateOffer> buyOffers = List.of();
	private List<RateOffer> sellOffers = List.of();
	private UiLocale currentLocale = UiLocale.EN;
	private String selectedCurrency = "EUR";

	@FXML
	public void initialize() {
		retryButton.setOnAction(event -> host.refresh());
		buyInputField.setText("1000");
		sellInputField.setText("1");
		buyInputField.textProperty().addListener((observable, previousValue, currentValue) -> renderConversions());
		sellInputField.textProperty().addListener((observable, previousValue, currentValue) -> renderConversions());
		setupOfficialRatesTable();
	}

	@Override
	@SuppressFBWarnings(value = "EI_EXPOSE_REP2", justification = "FXML page controllers intentionally keep their app host.")
	public void setHost(AppController appController) {
		host = appController;
	}

	@Override
	public void refreshText() {
		titleLabel.setText(host.i18n().text("dashboardTitle"));
		subtitleLabel.setText(host.i18n().text("dashboardSubtitle"));
		converterTitleLabel.setText(host.i18n().text("converter"));
		buyTitleLabel.setText(host.i18n().text("buyForeign"));
		sellTitleLabel.setText(host.i18n().text("sellForeign"));
		buyInputLabel.setText(host.i18n().text("youPay"));
		buyOutputLabel.setText(host.i18n().text("youReceive"));
		sellInputLabel.setText(host.i18n().text("youPay"));
		sellOutputLabel.setText(host.i18n().text("youReceive"));
		officialDateMetricLabel.setText(host.i18n().text("latestOfficialDate"));
		locationsMetricLabel.setText(host.i18n().text("locations"));
		marketRatesMetricLabel.setText(host.i18n().text("marketRates"));
		baseCurrencyMetricLabel.setText(host.i18n().text("baseCurrency"));
		officialRatesTitleLabel.setText(host.i18n().text("cardOfficialTitle"));
		officialRatesDescriptionLabel.setText(host.i18n().text("cardOfficialDescription"));
		retryButton.setText(host.i18n().text("retry"));
		refreshTableHeaders();
		renderCurrentContent();
	}

	@Override
	public void render(AppState state) {
		currentLocale = state.locale();
		currentBootstrap = state.bootstrap();
		selectedCurrency = state.selectedCurrency();

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

		String baseCurrency = currentBootstrap.country().baseCurrencyCode();
		List<MarketRateDto> selectedRates = DashboardCalculator.ratesForCurrency(currentBootstrap.marketRates(), selectedCurrency);
		buyOffers = DashboardCalculator.buildRateOffers(selectedRates, RateOfferKind.BUY);
		sellOffers = DashboardCalculator.buildRateOffers(selectedRates, RateOfferKind.SELL);
		String cityCountry = currentBootstrap.city().name() + ", " + currentBootstrap.country().name();
		converterDescriptionLabel.setText(cityCountry + " · " + host.i18n().text("baseSideLocked"));

		renderCurrencyRail(baseCurrency);
		renderOfferPanelLabels(baseCurrency);
		renderConversions();
		renderOffers(buyOffersBox, buyOffers, host.i18n().text("noMarketRatesForCurrency"));
		renderOffers(sellOffersBox, sellOffers, host.i18n().text("noMarketRatesForCurrency"));
		renderMetrics(baseCurrency);
		officialRatesTable.setItems(FXCollections.observableArrayList(currentBootstrap.officialRates().rates()));
		officialRatesTable.refresh();
	}

	private void renderCurrencyRail(String baseCurrency) {
		currencyRail.getChildren().clear();

		for (CurrencyDto currency : currentBootstrap.currencies()) {
			if (currency.code().equals(baseCurrency)) {
				continue;
			}

			Button button = new Button(currency.code());
			button.getStyleClass().add(currency.code().equals(selectedCurrency) ? "rail-button-active" : "rail-button");
			button.setOnAction(event -> host.setSelectedCurrency(currency.code()));
			currencyRail.getChildren().add(button);
		}
	}

	private void renderOfferPanelLabels(String baseCurrency) {
		MarketRateDto bestBuyRate = buyOffers.isEmpty() ? null : buyOffers.getFirst().rate();
		MarketRateDto bestSellRate = sellOffers.isEmpty() ? null : sellOffers.getFirst().rate();
		String buyBestRate = DashboardCalculator.formatBestRate(bestBuyRate, RateOfferKind.BUY, baseCurrency, selectedCurrency,
				currentLocale);
		String sellBestRate = DashboardCalculator.formatBestRate(bestSellRate, RateOfferKind.SELL, baseCurrency, selectedCurrency,
				currentLocale);
		buyRateLabel.setText(buyBestRate);
		sellRateLabel.setText(sellBestRate);
		buyInputCurrencyLabel.setText(baseCurrency);
		buyOutputCurrencyLabel.setText(selectedCurrency);
		sellInputCurrencyLabel.setText(selectedCurrency);
		sellOutputCurrencyLabel.setText(baseCurrency);
		buyOffersHeadingLabel.setText(host.i18n().text("bestBuyCaption") + " 1 " + selectedCurrency);
		sellOffersHeadingLabel.setText(host.i18n().text("bestSellCaption") + " 1 " + selectedCurrency);
	}

	private void renderConversions() {
		if (currentBootstrap == null) {
			buyOutputField.setText("—");
			sellOutputField.setText("—");
			return;
		}

		MarketRateDto bestBuyRate = buyOffers.isEmpty() ? null : buyOffers.getFirst().rate();
		MarketRateDto bestSellRate = sellOffers.isEmpty() ? null : sellOffers.getFirst().rate();
		BigDecimal baseAmount = DashboardCalculator.parseAmount(buyInputField.getText());
		BigDecimal foreignAmount = DashboardCalculator.parseAmount(sellInputField.getText());
		BigDecimal buyResult = bestBuyRate == null ? null : DashboardCalculator.convertBaseToForeign(baseAmount, bestBuyRate);
		BigDecimal sellResult = bestSellRate == null ? null : DashboardCalculator.convertForeignToBase(foreignAmount, bestSellRate);
		buyOutputField.setText(ExchangeFormat.formatConvertedAmount(buyResult, currentLocale));
		sellOutputField.setText(ExchangeFormat.formatConvertedAmount(sellResult, currentLocale));
	}

	private void renderOffers(VBox box, List<RateOffer> offers, String emptyLabel) {
		box.getChildren().clear();

		if (offers.isEmpty()) {
			box.getChildren().add(ControllerSupport.label(emptyLabel, "empty-state"));
			return;
		}

		int index = 0;

		for (RateOffer offer : offers) {
			box.getChildren().add(createOfferRow(offer, index == 0));
			index++;
		}
	}

	private Button createOfferRow(RateOffer offer, boolean bestOffer) {
		Button button = new Button();
		button.getStyleClass().add("offer-row");
		button.setMaxWidth(Double.MAX_VALUE);
		button.setMinHeight(Region.USE_PREF_SIZE);
		button.setPrefHeight(58);
		button.setMaxHeight(Region.USE_PREF_SIZE);
		button.setOnAction(event -> host.openLocation(offer.rate().location()));

		GridPane row = new GridPane();
		row.getStyleClass().add("offer-row-grid");
		row.setMaxWidth(Double.MAX_VALUE);
		Label provider = ControllerSupport.label(offer.rate().institution().name(), "provider-label");
		Label location = ControllerSupport.label(offer.rate().location().name(), "muted-small");
		String rateText = formatOfferRate(offer);
		Label rate = new Label(rateText);
		Label fetched = new Label(ExchangeFormat.formatDateTime(offer.rate().fetchedAt(), currentLocale));
		VBox left = createOfferIdentity(provider, location, bestOffer);
		VBox right = new VBox(4, rate, fetched);

		rate.getStyleClass().add("strong-label");
		fetched.getStyleClass().add("muted-small");
		left.setAlignment(Pos.CENTER_LEFT);
		right.setAlignment(Pos.CENTER_RIGHT);
		right.setMinWidth(130);
		GridPane.setHgrow(left, Priority.ALWAYS);
		row.add(left, 0, 0);
		row.add(right, 1, 0);

		button.setGraphic(row);

		return button;
	}

	private VBox createOfferIdentity(Label provider, Label location, boolean bestOffer) {
		if (!bestOffer) {
			return new VBox(4, provider, location);
		}

		Label badge = new Label(host.i18n().text("bestRate"));
		badge.getStyleClass().add("badge-positive");
		HBox providerLine = new HBox(8, provider, badge);
		providerLine.setAlignment(Pos.CENTER_LEFT);

		return new VBox(4, providerLine, location);
	}

	private String formatOfferRate(RateOffer offer) {
		return ExchangeFormat.formatRate(offer.value(), currentLocale) + " " + currentBootstrap.country().baseCurrencyCode();
	}

	private void renderMetrics(String baseCurrency) {
		officialDateMetricValue.setText(ExchangeFormat.formatDate(currentBootstrap.officialRates().rateDate(), currentLocale));
		locationsMetricValue.setText(ExchangeFormat.formatNumber(currentBootstrap.locations().size(), currentLocale));
		marketRatesMetricValue.setText(ExchangeFormat.formatNumber(currentBootstrap.marketRates().size(), currentLocale));
		baseCurrencyMetricValue.setText(baseCurrency);
	}

	private void showStatus(String message, boolean canRetry) {
		statusLabel.setText(message);
		ControllerSupport.setManagedVisible(statusBox, true);
		ControllerSupport.setManagedVisible(contentBox, false);
		ControllerSupport.setManagedVisible(retryButton, canRetry);
	}

	private void setupOfficialRatesTable() {
		TableColumn<OfficialRateDto, OfficialRateDto> currencyColumn = new TableColumn<>();
		currencyColumn.setCellValueFactory(data -> new ReadOnlyObjectWrapper<>(data.getValue()));
		currencyColumn.setCellFactory(column -> new OfficialRateCurrencyCell());
		TableColumn<OfficialRateDto, String> unitColumn = new TableColumn<>();
		unitColumn.setCellValueFactory(data -> new ReadOnlyStringWrapper(String.valueOf(data.getValue().unit())));
		TableColumn<OfficialRateDto, String> rateColumn = new TableColumn<>();
		rateColumn.setCellValueFactory(data -> new ReadOnlyStringWrapper(formatOfficialRate(data.getValue())));
		currencyColumn.setStyle("-fx-alignment: CENTER_LEFT;");
		unitColumn.setStyle("-fx-alignment: CENTER_LEFT;");
		rateColumn.setStyle("-fx-alignment: CENTER_LEFT;");
		currencyColumn.prefWidthProperty().bind(officialRatesTable.widthProperty().multiply(0.50));
		unitColumn.prefWidthProperty().bind(officialRatesTable.widthProperty().multiply(0.20));
		rateColumn.prefWidthProperty().bind(officialRatesTable.widthProperty().multiply(0.28));
		officialRatesTable.getColumns().setAll(List.of(currencyColumn, unitColumn, rateColumn));
		officialRatesTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
		refreshTableHeaders();
	}

	private String formatOfficialRate(OfficialRateDto rate) {
		return ExchangeFormat.formatRate(rate.rate(), currentLocale);
	}

	private static final class OfficialRateCurrencyCell extends TableCell<OfficialRateDto, OfficialRateDto> {

		@Override
		protected void updateItem(OfficialRateDto item, boolean empty) {
			super.updateItem(item, empty);

			if (empty || item == null) {
				setGraphic(null);
				return;
			}

			Label code = new Label(item.currency().code());
			Label name = new Label(item.currency().name());
			code.getStyleClass().add("strong-label");
			name.getStyleClass().add("muted-small");
			VBox content = new VBox(2, code, name);
			content.setAlignment(Pos.CENTER_LEFT);
			setGraphic(content);
		}
	}

	private void refreshTableHeaders() {
		if (officialRatesTable.getColumns().isEmpty() || host == null) {
			return;
		}

		officialRatesTable.getColumns().get(0).setText(host.i18n().text("currency"));
		officialRatesTable.getColumns().get(1).setText(host.i18n().text("unit"));
		officialRatesTable.getColumns().get(2).setText(host.i18n().text("rate"));
		officialRatesTable.refresh();
	}
}
