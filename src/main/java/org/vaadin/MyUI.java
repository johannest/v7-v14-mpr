package org.vaadin;

import com.vaadin.annotations.Theme;
import com.vaadin.flow.router.Route;
import com.vaadin.mpr.MprNavigatorRoute;
import com.vaadin.navigator.Navigator;
import com.vaadin.server.VaadinRequest;

/**
 * This UI is the application entry point. A UI may either represent a browser window
 * (or tab) or some part of a html page where a Vaadin application is embedded.
 * <p>
 * The UI is initialized using {@link #init(VaadinRequest)}. This method is intended to be
 * overridden to add component to the user interface and initialize non-component functionality.
 */
@Theme("mytheme")
@Route("")
public class MyUI extends MprNavigatorRoute {

    public VaadinRequest vaadinRequest;

    @Override
    public void configureNavigator(Navigator navigator) {
        navigator.addView("", FirstView.class);
        navigator.addView("second", SecondView.class);
    }

}
