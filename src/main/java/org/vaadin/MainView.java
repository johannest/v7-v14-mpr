package org.vaadin;

import com.vaadin.navigator.Navigator;
import com.vaadin.ui.*;

public class MainView extends HorizontalLayout {

    private Panel viewDisplay = new Panel();
    private final Navigator navigator;
    private final VerticalLayout menu;

    public MainView() {
        setSizeFull();
        menu = new VerticalLayout();
        menu.setSpacing(true);
        menu.setHeight("100%");
        viewDisplay.setSizeFull();
        navigator = new Navigator(UI.getCurrent(), viewDisplay);
        addComponents(menu, viewDisplay);
        navigator.addView("", FirstView.class);
        navigator.addView("second", SecondView.class);

        menu.addComponent(new Button("first", e -> navigateTo("")));
        Button b2 = new Button("second", e -> navigateTo("second"));
        menu.addComponent(b2);
        menu.setExpandRatio(b2, 1);

    }

    private void navigateTo(String s) {
        navigator.navigateTo(s);
    }
}
