package net.magical.exchange.desktop.controllers;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.io.IOException;
import java.net.URL;
import java.util.EnumMap;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutionException;
import javafx.application.HostServices;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.util.StringConverter;
import net.magical.exchange.desktop.i18n.I18nService;
import net.magical.exchange.desktop.model.AppState;
import net.magical.exchange.desktop.model.BootstrapDto;
import net.magical.exchange.desktop.model.CityDto;
import net.magical.exchange.desktop.model.CountryDto;
import net.magical.exchange.desktop.model.CurrencyDto;
import net.magical.exchange.desktop.model.LocationDto;
import net.magical.exchange.desktop.model.PageRoute;
import net.magical.exchange.desktop.model.ThemeMode;
import net.magical.exchange.desktop.model.UiLocale;
import net.magical.exchange.desktop.services.AppConfig;
import net.magical.exchange.desktop.services.ExchangeApiClient;

public class AppController {

	private static final String DEFAULT_COUNTRY = "MD";
	private static final String DEFAULT_CITY = "chisinau";
	private static final String DEFAULT_CURRENCY = "EUR";

	private final ExchangeApiClient apiClient = new ExchangeApiClient(AppConfig.apiBaseUri());
	private final EnumMap<PageRoute, Parent> pageNodes = new EnumMap<>(PageRoute.class);
	private final EnumMap<PageRoute, PageController> pageControllers = new EnumMap<>(PageRoute.class);
	private final I18nService i18n = new I18nService(UiLocale.detectDefault());

	@FXML
	private BorderPane root;

	@FXML
	private Label appNameLabel;

	@FXML
	private Label countryLabel;

	@FXML
	private Label cityLabel;

	@FXML
	private Label languageLabel;

	@FXML
	private ComboBox<CountryDto> countryCombo;

	@FXML
	private ComboBox<CityDto> cityCombo;

	@FXML
	private ComboBox<UiLocale> localeCombo;

	@FXML
	private ToggleButton dashboardNavButton;

	@FXML
	private ToggleButton ratesNavButton;

	@FXML
	private ToggleButton historyNavButton;

	@FXML
	private Button themeButton;

	@FXML
	private Button refreshButton;

	@FXML
	private StackPane contentHost;

	private HostServices hostServices;
	private Optional<BootstrapDto> bootstrap = Optional.empty();
	private Optional<String> bootstrapError = Optional.empty();
	private List<CountryDto> countries = List.of();
	private List<CityDto> cities = List.of();
	private String citySlug = DEFAULT_CITY;
	private String countryCode = DEFAULT_COUNTRY;
	private String selectedCurrency = DEFAULT_CURRENCY;
	private ThemeMode themeMode = ThemeMode.LIGHT;
	private ToggleGroup navigationGroup;
	private boolean bootstrapLoading;
	private boolean updatingControls;
	private int requestSerial;

	@FXML
	public void initialize() {
		setupComboBoxes();
		loadPages();
		setupNavigation();
		applyTheme();
		refreshText();
		showRoute(PageRoute.DASHBOARD);
		refreshData();
	}

	public void setHostServices(HostServices nextHostServices) {
		hostServices = nextHostServices;
	}

	public ExchangeApiClient apiClient() {
		return apiClient;
	}

	@SuppressFBWarnings(value = "EI_EXPOSE_REP", justification = "Shared app i18n service.")
	public I18nService i18n() {
		return i18n;
	}

	public HostServices hostServices() {
		return hostServices;
	}

	public void setSelectedCurrency(String nextSelectedCurrency) {
		if (!nextSelectedCurrency.equals(selectedCurrency)) {
			selectedCurrency = nextSelectedCurrency;
			renderPages();
		}
	}

	public void refresh() {
		refreshData();
	}

	public void openLocation(LocationDto location) {
		LocationDialogController.open(root, location, i18n, hostServices);
	}

	@FXML
	public void handleThemeToggle() {
		themeMode = themeMode.next();
		applyTheme();
		refreshText();
	}

	@FXML
	public void handleRefresh() {
		refreshData();
	}

	private void setupComboBoxes() {
		setupCountryComboBox();
		setupCityComboBox();
		setupLocaleComboBox();
		setupSelectionListeners();
	}

	private void setupCountryComboBox() {
		countryCombo.setConverter(new StringConverter<>() {
			@Override
			public String toString(CountryDto country) {
				return country == null ? "" : country.name();
			}

			@Override
			public CountryDto fromString(String value) {
				return countries.stream().filter(country -> country.name().equals(value)).findFirst().orElse(null);
			}
		});
	}

	private void setupCityComboBox() {
		cityCombo.setConverter(new StringConverter<>() {
			@Override
			public String toString(CityDto city) {
				return city == null ? "" : city.name();
			}

			@Override
			public CityDto fromString(String value) {
				return cities.stream().filter(city -> city.name().equals(value)).findFirst().orElse(null);
			}
		});
	}

	@SuppressFBWarnings(value = "SIC_INNER_SHOULD_BE_STATIC_ANON", justification = "FXML converter wiring.")
	private void setupLocaleComboBox() {
		localeCombo.setConverter(new StringConverter<>() {
			@Override
			public String toString(UiLocale locale) {
				return locale == null ? "" : locale.label();
			}

			@Override
			public UiLocale fromString(String value) {
				return localeFromLabel(value);
			}
		});
		localeCombo.setItems(FXCollections.observableArrayList(UiLocale.supported()));
	}

	private void setupSelectionListeners() {
		countryCombo.valueProperty().addListener((observable, oldCountry, newCountry) -> {
			if (!updatingControls && newCountry != null && !newCountry.code().equals(countryCode)) {
				countryCode = newCountry.code();
				refreshData();
			}
		});
		cityCombo.valueProperty().addListener((observable, oldCity, newCity) -> {
			if (!updatingControls && newCity != null && !newCity.slug().equals(citySlug)) {
				citySlug = newCity.slug();
				refreshBootstrap();
			}
		});
		localeCombo.valueProperty().addListener((observable, oldLocale, newLocale) -> {
			if (!updatingControls && newLocale != null && newLocale != i18n.locale()) {
				i18n.setLocale(newLocale);
				refreshText();
				refreshData();
			}
		});
	}

	private void setupNavigation() {
		navigationGroup = new ToggleGroup();
		configureNavButton(dashboardNavButton, PageRoute.DASHBOARD);
		configureNavButton(ratesNavButton, PageRoute.RATES);
		configureNavButton(historyNavButton, PageRoute.HISTORY);
		navigationGroup.selectedToggleProperty().addListener((observable, previousToggle, selectedToggle) -> {
			if (selectedToggle == null) {
				previousToggle.setSelected(true);
				return;
			}

			Object route = selectedToggle.getUserData();

			if (route instanceof PageRoute pageRoute) {
				showRoute(pageRoute);
			}
		});
		dashboardNavButton.setSelected(true);
	}

	private void configureNavButton(ToggleButton button, PageRoute route) {
		button.setToggleGroup(navigationGroup);
		button.setUserData(route);
	}

	private void loadPages() {
		loadPage(PageRoute.DASHBOARD, "/net/magical/exchange/desktop/fxml/DashboardView.fxml");
		loadPage(PageRoute.RATES, "/net/magical/exchange/desktop/fxml/MarketRatesView.fxml");
		loadPage(PageRoute.HISTORY, "/net/magical/exchange/desktop/fxml/HistoryView.fxml");
	}

	private void loadPage(PageRoute route, String resourcePath) {
		try {
			FXMLLoader loader = new FXMLLoader(requiredResource(resourcePath));
			Parent node = loader.load();
			PageController controller = loader.getController();
			controller.setHost(this);
			pageNodes.put(route, node);
			pageControllers.put(route, controller);
		} catch (IOException caughtError) {
			throw new IllegalStateException("Could not load " + resourcePath, caughtError);
		}
	}

	private void showRoute(PageRoute route) {
		contentHost.getChildren().setAll(pageNodes.get(route));
		renderPages();
	}

	private void refreshData() {
		requestSerial += 1;
		int token = requestSerial;
		bootstrapLoading = true;
		bootstrapError = Optional.empty();
		bootstrap = Optional.empty();
		renderPages();

		apiClient.fetchCountries(i18n.locale())
				.whenComplete((nextCountries, failure) -> handleCountriesOnUi(token, nextCountries, failure));
		apiClient.fetchCities(countryCode, i18n.locale())
				.whenComplete((nextCities, failure) -> handleCitiesOnUi(token, nextCities, failure));
	}

	private void refreshBootstrap() {
		requestSerial += 1;
		int token = requestSerial;
		bootstrapLoading = true;
		bootstrapError = Optional.empty();
		bootstrap = Optional.empty();
		renderPages();
		fetchBootstrap(token);
	}

	private void handleCountries(int token, List<CountryDto> nextCountries, Throwable failure) {
		if (isStale(token)) {
			return;
		}

		if (failure == null) {
			countries = nextCountries;
			updateHeaderSelections();
		}
	}

	private void handleCountriesOnUi(int token, List<CountryDto> nextCountries, Throwable failure) {
		Platform.runLater(() -> handleCountries(token, nextCountries, failure));
	}

	private void handleCitiesOnUi(int token, List<CityDto> nextCities, Throwable failure) {
		Platform.runLater(() -> handleCities(token, nextCities, failure));
	}

	private void handleCities(int token, List<CityDto> nextCities, Throwable failure) {
		if (isStale(token)) {
			return;
		}

		if (failure != null) {
			cities = List.of();
			bootstrapLoading = false;
			bootstrapError = Optional.of(failureMessage(failure));
			updateHeaderSelections();
			renderPages();
			return;
		}

		cities = nextCities;

		if (cities.stream().noneMatch(city -> city.slug().equals(citySlug)) && !cities.isEmpty()) {
			citySlug = cities.getFirst().slug();
		}

		updateHeaderSelections();
		fetchBootstrap(token);
	}

	private void fetchBootstrap(int token) {
		apiClient.fetchBootstrap(countryCode, citySlug, i18n.locale()).whenComplete((nextBootstrap, failure) -> {
			Platform.runLater(() -> handleBootstrap(token, nextBootstrap, failure));
		});
	}

	private void handleBootstrap(int token, BootstrapDto nextBootstrap, Throwable failure) {
		if (isStale(token)) {
			return;
		}

		bootstrapLoading = false;

		if (failure != null) {
			bootstrap = Optional.empty();
			bootstrapError = Optional.of(failureMessage(failure));
			renderPages();
			return;
		}

		bootstrap = Optional.of(nextBootstrap);
		bootstrapError = Optional.empty();
		ensureSelectedCurrency();
		renderPages();
	}

	private void ensureSelectedCurrency() {
		if (bootstrap.isEmpty()) {
			return;
		}

		BootstrapDto currentBootstrap = bootstrap.get();
		String baseCurrencyCode = currentBootstrap.country().baseCurrencyCode();
		boolean selectedCurrencyExists = currentBootstrap.currencies().stream()
				.anyMatch(currency -> isSelectedForeignCurrency(currency, baseCurrencyCode));

		if (!selectedCurrencyExists) {
			selectedCurrency = firstForeignCurrency(currentBootstrap, baseCurrencyCode);
		}
	}

	private String firstForeignCurrency(BootstrapDto currentBootstrap, String baseCurrencyCode) {
		return currentBootstrap.currencies().stream().filter(currency -> !currency.code().equals(baseCurrencyCode))
				.map(currency -> currency.code()).findFirst().orElse(DEFAULT_CURRENCY);
	}

	private void updateHeaderSelections() {
		updatingControls = true;
		try {
			countryCombo.setItems(FXCollections.observableArrayList(countries));
			cityCombo.setItems(FXCollections.observableArrayList(cities));
			localeCombo.setItems(FXCollections.observableArrayList(UiLocale.supported()));
			countryCombo.setValue(findCountry(countryCode));
			cityCombo.setValue(findCity(citySlug));
			localeCombo.setValue(i18n.locale());
		} finally {
			updatingControls = false;
		}
	}

	private CountryDto findCountry(String code) {
		return countries.stream().filter(country -> country.code().equals(code)).findFirst()
				.orElseGet(() -> new CountryDto(code, code, "MDL", UiLocale.EN.code()));
	}

	private CityDto findCity(String slug) {
		return cities.stream().filter(city -> city.slug().equals(slug)).findFirst()
				.orElseGet(() -> new CityDto(countryCode, slug, slug, ""));
	}

	private boolean isSelectedForeignCurrency(CurrencyDto currency, String baseCurrencyCode) {
		return !currency.code().equals(baseCurrencyCode) && currency.code().equals(selectedCurrency);
	}

	private void renderPages() {
		UiLocale locale = i18n.locale();
		AppState state = new AppState(locale, countryCode, citySlug, selectedCurrency, bootstrap.orElse(null), bootstrapLoading,
				bootstrapError.orElse(null));

		for (PageController controller : pageControllers.values()) {
			controller.render(state);
		}
	}

	private void refreshText() {
		appNameLabel.setText(i18n.text("appName"));
		countryLabel.setText(i18n.text("country"));
		cityLabel.setText(i18n.text("city"));
		languageLabel.setText(i18n.text("language"));
		dashboardNavButton.setText(i18n.text(PageRoute.DASHBOARD.translationKey()));
		ratesNavButton.setText(i18n.text(PageRoute.RATES.translationKey()));
		historyNavButton.setText(i18n.text(PageRoute.HISTORY.translationKey()));
		themeButton.setText(i18n.text(themeMode == ThemeMode.DARK ? "lightMode" : "darkMode"));
		refreshButton.setText(i18n.text("refresh"));
		updateHeaderSelections();

		for (PageController controller : pageControllers.values()) {
			controller.refreshText();
		}
	}

	private void applyTheme() {
		root.getStyleClass().removeAll(ThemeMode.LIGHT.cssClass(), ThemeMode.DARK.cssClass());
		root.getStyleClass().add(themeMode.cssClass());
	}

	private boolean isStale(int token) {
		return token != requestSerial;
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

	private static UiLocale localeFromLabel(String label) {
		return UiLocale.supported().stream().filter(locale -> locale.label().equals(label)).findFirst().orElse(UiLocale.EN);
	}

	private static URL requiredResource(String path) {
		URL resource = AppController.class.getResource(path);

		if (resource == null) {
			throw new IllegalStateException("Missing desktop resource: " + path);
		}

		return resource;
	}
}
