package net.magical.exchange.desktop.i18n;

import java.util.ResourceBundle;
import net.magical.exchange.desktop.model.UiLocale;

public final class I18nService {

	private static final String BUNDLE_BASE_NAME = "net.magical.exchange.desktop.i18n.messages";

	private ResourceBundle bundle;
	private UiLocale currentLocale;

	public I18nService(UiLocale initialLocale) {
		currentLocale = initialLocale;
		bundle = loadBundle(initialLocale);
	}

	public UiLocale locale() {
		return currentLocale;
	}

	public void setLocale(UiLocale nextLocale) {
		currentLocale = nextLocale;
		bundle = loadBundle(nextLocale);
	}

	public String text(String key) {
		if (bundle.containsKey(key)) {
			return bundle.getString(key);
		}

		return key;
	}

	private static ResourceBundle loadBundle(UiLocale locale) {
		return ResourceBundle.getBundle(BUNDLE_BASE_NAME, locale.toLocale());
	}
}
