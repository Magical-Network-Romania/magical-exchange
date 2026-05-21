package net.magical.exchange.desktop.controllers;

import net.magical.exchange.desktop.model.AppState;

public interface PageController {

	void setHost(AppController appController);

	void refreshText();

	void render(AppState state);
}
