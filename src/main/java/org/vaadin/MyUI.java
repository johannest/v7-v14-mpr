package org.vaadin;

import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.HasElement;
import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.applayout.DrawerToggle;
import com.vaadin.flow.component.orderedlayout.FlexLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.*;
import com.vaadin.mpr.core.LegacyUI;
import com.vaadin.mpr.core.MprTheme;
import com.vaadin.flow.component.page.Push;
import com.vaadin.flow.shared.communication.PushMode;
import com.vaadin.flow.shared.ui.Transport;

@MprTheme("mytheme")
@Route("")
@LegacyUI(OldUI.class)
@Push(value = PushMode.AUTOMATIC, transport = Transport.WEBSOCKET_XHR)
public class MyUI extends AppLayout implements RouterLayout, HasUrlParameter<String> {

    private FlexLayout childWrapper = new FlexLayout();

    public MyUI() {
        DrawerToggle toggle = new DrawerToggle();
        addToNavbar(toggle);
        VerticalLayout layout = new VerticalLayout();
        layout.add(new RouterLink(MainV.TITLE, MainV.class));
        addToDrawer(layout);

        childWrapper.setSizeFull();
        setContent(childWrapper);

    }

    @Override

    public void setParameter(BeforeEvent event, @OptionalParameter String parameter) {
        System.out.println("setParameter: " + parameter);
    }

    @Override
    public void showRouterLayoutContent(HasElement content) {
        childWrapper.removeAll();
        childWrapper.getElement().appendChild(content.getElement());
    }

}
