package net.magical.exchange.desktop.controllers;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutionException;
import javafx.application.Platform;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.util.StringConverter;
import net.magical.exchange.desktop.model.AppState;
import net.magical.exchange.desktop.model.BootstrapDto;
import net.magical.exchange.desktop.model.CurrencyDto;
import net.magical.exchange.desktop.model.OfficialRateHistoryPoint;
import net.magical.exchange.desktop.model.UiLocale;
import net.magical.exchange.desktop.util.ExchangeFormat;
import net.magical.exchange.desktop.util.HistoryChartPoint;
import net.magical.exchange.desktop.util.HistoryMapper;

public class HistoryController implements PageController {

	@FXML
	private Label titleLabel;

	@FXML
	private Label subtitleLabel;

	@FXML
	private Label currencyLabel;

	@FXML
	private Label fromLabel;

	@FXML
	private Label toLabel;

	@FXML
	private ComboBox<String> currencyCombo;

	@FXML
	private DatePicker fromPicker;

	@FXML
	private DatePicker toPicker;

	@FXML
	private Label chartTitleLabel;

	@FXML
	private Label chartDescriptionLabel;

	@FXML
	private Label chartStatusLabel;

	@FXML
	private LineChart<String, Number> historyChart;

	@FXML
	private CategoryAxis historyXAxis;

	@FXML
	private NumberAxis historyYAxis;

	@FXML
	private Label tableTitleLabel;

	@FXML
	private Label tableDescriptionLabel;

	@FXML
	private TableView<OfficialRateHistoryPoint> historyTable;

	private AppController host;
	private BootstrapDto currentBootstrap;
	private List<OfficialRateHistoryPoint> history = List.of();
	private LocalDate lastFrom;
	private LocalDate lastTo;
	private String countryCode = "MD";
	private String lastCountryCode;
	private String lastCurrency;
	private String selectedCurrency = "EUR";
	private UiLocale currentLocale = UiLocale.EN;
	private boolean historyLoading;
	private boolean updatingCurrency;
	private int historyRequestSerial;

	@FXML
	public void initialize() {
		fromPicker.setValue(LocalDate.now().minusDays(30));
		toPicker.setValue(LocalDate.now());
		currencyCombo.valueProperty().addListener((observable, previousValue, currentValue) -> {
			if (!updatingCurrency && currentValue != null && !currentValue.equals(selectedCurrency)) {
				host.setSelectedCurrency(currentValue);
			}
		});
		fromPicker.valueProperty().addListener((observable, previousValue, currentValue) -> reloadHistoryIfNeeded());
		toPicker.valueProperty().addListener((observable, previousValue, currentValue) -> reloadHistoryIfNeeded());
		historyYAxis.setTickLabelFormatter(new StringConverter<>() {
			@Override
			public String toString(Number value) {
				BigDecimal decimalValue = value == null ? null : BigDecimal.valueOf(value.doubleValue());

				return ExchangeFormat.formatRate(decimalValue, currentLocale, 3);
			}

			@Override
			public Number fromString(String value) {
				return 0;
			}
		});
		setupHistoryTable();
	}

	@Override
	@SuppressFBWarnings(value = "EI_EXPOSE_REP2", justification = "FXML page controllers intentionally keep their app host.")
	public void setHost(AppController appController) {
		host = appController;
	}

	@Override
	public void refreshText() {
		titleLabel.setText(host.i18n().text("historyTitle"));
		subtitleLabel.setText(host.i18n().text("historySubtitle"));
		currencyLabel.setText(host.i18n().text("currency"));
		fromLabel.setText(host.i18n().text("from"));
		toLabel.setText(host.i18n().text("to"));
		tableTitleLabel.setText(host.i18n().text("tableHistory"));
		refreshTableHeaders();
		renderChrome();
		renderHistory();
	}

	@Override
	public void render(AppState state) {
		currentLocale = state.locale();
		countryCode = state.countryCode();
		selectedCurrency = state.selectedCurrency();
		currentBootstrap = state.bootstrap();
		updateCurrencyOptions();
		renderChrome();
		reloadHistoryIfNeeded();
	}

	private void updateCurrencyOptions() {
		updatingCurrency = true;
		try {
			List<String> options = currencyOptions();
			currencyCombo.setItems(FXCollections.observableArrayList(options));
			currencyCombo.setValue(selectedCurrency);
		} finally {
			updatingCurrency = false;
		}
	}

	private List<String> currencyOptions() {
		if (currentBootstrap == null) {
			return List.of(selectedCurrency);
		}

		String baseCurrencyCode = currentBootstrap.country().baseCurrencyCode();
		List<String> options = currentBootstrap.currencies().stream().filter(currency -> !currency.code().equals(baseCurrencyCode))
				.map(CurrencyDto::code).toList();

		if (options.contains(selectedCurrency)) {
			return options;
		}

		List<String> nextOptions = new ArrayList<>(options);
		nextOptions.add(selectedCurrency);

		return nextOptions;
	}

	private void reloadHistoryIfNeeded() {
		if (host == null || fromPicker.getValue() == null || toPicker.getValue() == null) {
			return;
		}

		LocalDate from = fromPicker.getValue();
		LocalDate to = toPicker.getValue();

		if (from.isAfter(to)) {
			history = List.of();
			historyLoading = false;
			renderHistoryStatus(host.i18n().text("invalidDateRange"));
			historyTable.setItems(FXCollections.observableArrayList());
			return;
		}

		boolean unchanged = countryCode.equals(lastCountryCode) && selectedCurrency.equals(lastCurrency) && from.equals(lastFrom)
				&& to.equals(lastTo);

		if (unchanged && !historyLoading) {
			renderHistory();
			return;
		}

		lastCountryCode = countryCode;
		lastCurrency = selectedCurrency;
		lastFrom = from;
		lastTo = to;
		loadHistory(from, to);
	}

	private void loadHistory(LocalDate from, LocalDate to) {
		historyRequestSerial += 1;
		int token = historyRequestSerial;
		historyLoading = true;
		history = List.of();
		renderHistoryStatus(host.i18n().text("loading"));
		historyTable.setItems(FXCollections.observableArrayList());
		host.apiClient().fetchOfficialRateHistory(countryCode, selectedCurrency, from, to)
				.whenComplete((points, failure) -> Platform.runLater(() -> handleHistory(token, points, failure)));
	}

	private void handleHistory(int token, List<OfficialRateHistoryPoint> points, Throwable failure) {
		if (token != historyRequestSerial) {
			return;
		}

		historyLoading = false;

		if (failure != null) {
			history = List.of();
			renderHistoryStatus(failureMessage(failure));
			historyTable.setItems(FXCollections.observableArrayList());
			return;
		}

		history = points;
		renderHistory();
	}

	private void renderChrome() {
		String baseCurrency = currentBootstrap == null ? "MDL" : currentBootstrap.country().baseCurrencyCode();
		LocalDate from = fromPicker.getValue();
		LocalDate to = toPicker.getValue();
		chartTitleLabel.setText(selectedCurrency + " / " + baseCurrency);
		String formattedFrom = ExchangeFormat.formatDate(from, currentLocale);
		String formattedTo = ExchangeFormat.formatDate(to, currentLocale);
		chartDescriptionLabel.setText(formattedFrom + " - " + formattedTo);
		tableDescriptionLabel.setText(selectedCurrency);
	}

	private void renderHistory() {
		renderChrome();

		if (historyLoading) {
			renderHistoryStatus(host.i18n().text("loading"));
			return;
		}

		if (history.isEmpty()) {
			renderHistoryStatus(host.i18n().text("emptyHistory"));
			historyTable.setItems(FXCollections.observableArrayList());
			return;
		}

		ControllerSupport.setManagedVisible(chartStatusLabel, false);
		ControllerSupport.setManagedVisible(historyChart, true);
		renderChart();
		renderTable();
	}

	private void renderChart() {
		List<HistoryChartPoint> data = HistoryMapper.toHistoryChartData(history, currentLocale);
		XYChart.Series<String, Number> series = new XYChart.Series<>();

		for (HistoryChartPoint point : data) {
			series.getData().add(new XYChart.Data<>(point.formattedDate(), point.rate()));
		}

		historyChart.getData().clear();
		historyChart.getData().add(series);
		historyXAxis.setTickLabelRotation(data.size() > 8 ? -35 : 0);
	}

	private void renderTable() {
		List<OfficialRateHistoryPoint> reversedHistory = new ArrayList<>(history);
		Collections.reverse(reversedHistory);
		historyTable.setItems(FXCollections.observableArrayList(reversedHistory));
		historyTable.refresh();
	}

	private void renderHistoryStatus(String message) {
		chartStatusLabel.setText(message);
		historyChart.getData().clear();
		ControllerSupport.setManagedVisible(chartStatusLabel, true);
		ControllerSupport.setManagedVisible(historyChart, false);
	}

	private void setupHistoryTable() {
		TableColumn<OfficialRateHistoryPoint, String> dateColumn = new TableColumn<>();
		dateColumn.setCellValueFactory(data -> new ReadOnlyStringWrapper(formatHistoryDate(data.getValue())));
		TableColumn<OfficialRateHistoryPoint, String> unitColumn = new TableColumn<>();
		unitColumn.setCellValueFactory(data -> new ReadOnlyStringWrapper(String.valueOf(data.getValue().unit())));
		TableColumn<OfficialRateHistoryPoint, String> rateColumn = new TableColumn<>();
		rateColumn.setCellValueFactory(data -> new ReadOnlyStringWrapper(formatHistoryRate(data.getValue())));
		TableColumn<OfficialRateHistoryPoint, String> fetchedColumn = new TableColumn<>();
		fetchedColumn.setCellValueFactory(data -> new ReadOnlyStringWrapper(formatFetchedAt(data.getValue())));
		dateColumn.prefWidthProperty().bind(historyTable.widthProperty().multiply(0.25));
		unitColumn.prefWidthProperty().bind(historyTable.widthProperty().multiply(0.15));
		rateColumn.prefWidthProperty().bind(historyTable.widthProperty().multiply(0.25));
		fetchedColumn.prefWidthProperty().bind(historyTable.widthProperty().multiply(0.33));
		historyTable.getColumns().setAll(List.of(dateColumn, unitColumn, rateColumn, fetchedColumn));
		historyTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
		refreshTableHeaders();
	}

	private String formatHistoryRate(OfficialRateHistoryPoint point) {
		return ExchangeFormat.formatRate(point.rate(), currentLocale);
	}

	private String formatHistoryDate(OfficialRateHistoryPoint point) {
		return ExchangeFormat.formatDate(point.rateDate(), currentLocale);
	}

	private String formatFetchedAt(OfficialRateHistoryPoint point) {
		return ExchangeFormat.formatDateTime(point.fetchedAt(), currentLocale);
	}

	private void refreshTableHeaders() {
		if (historyTable.getColumns().isEmpty() || host == null) {
			return;
		}

		historyTable.getColumns().get(0).setText(host.i18n().text("from"));
		historyTable.getColumns().get(1).setText(host.i18n().text("unit"));
		historyTable.getColumns().get(2).setText(host.i18n().text("rate"));
		historyTable.getColumns().get(3).setText(host.i18n().text("fetched"));
		historyTable.refresh();
	}

	private static String failureMessage(Throwable failure) {
		Throwable unwrappedFailure = unwrap(failure);
		String message = unwrappedFailure.getMessage();

		if (message == null || message.isBlank()) {
			return "Unknown API error";
		}

		return message;
	}

	private static Throwable unwrap(Throwable failure) {
		Throwable currentFailure = failure;

		while (currentFailure instanceof CompletionException || currentFailure instanceof ExecutionException) {
			if (currentFailure.getCause() == null) {
				return currentFailure;
			}

			currentFailure = currentFailure.getCause();
		}

		return currentFailure;
	}
}
