package org.vaadin;

import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener;
import com.vaadin.server.VaadinRequest;
import com.vaadin.server.VaadinServlet;
import com.vaadin.server.VaadinSession;
import com.vaadin.ui.Label;
import com.vaadin.ui.VerticalLayout;

public class FirstView extends VerticalLayout implements View {
    @Override
    public void enter(ViewChangeListener.ViewChangeEvent viewChangeEvent) {
        String parameters = viewChangeEvent.getParameters();
        if (parameters != null && !("".equals(parameters))) {
            addComponent(new Label("parameters:" + parameters));
        }

    }
    public FirstView() {
        addComponent(new Label("first"));
        setSizeFull();
    }
}
