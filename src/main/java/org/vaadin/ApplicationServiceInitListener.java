package org.vaadin;

import com.vaadin.flow.server.*;

public class ApplicationServiceInitListener
        implements VaadinServiceInitListener, SessionInitListener {

    @Override
    public void serviceInit(ServiceInitEvent event) {
        event.getSource().addSessionInitListener(this);
    }

    @Override
    public void sessionInit(SessionInitEvent event) {
        event.getSession().setErrorHandler((ErrorHandler) event1 -> {
            System.out.println("FLOW ERROR HANDLER");
            event1.getThrowable().printStackTrace();
        });
    }
}
