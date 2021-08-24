package org.vaadin;

import com.vaadin.flow.component.UI;
import com.vaadin.server.DefaultErrorHandler;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.Label;
import com.vaadin.ui.VerticalLayout;

public class V7TabSheetView extends CustomComponent {

    public V7TabSheetView(UI flowUI) {
        setSizeFull();

        Label label1 = new Label();
        label1.setContentMode(ContentMode.HTML);
        label1.setValue("<a href='#'>link that navigates</a>");

        Label label2 = new Label();
        label2.setContentMode(ContentMode.HTML);
        label2.setValue("<a href='/v7_v14_mpr_war_exploded/main#'>link that does not navigate (Note! The path is hardcoded to ´/v7_v14_mpr_war_exploded/main#´) </a>");

        setCompositionRoot(new VerticalLayout(new Label("V7 view"), label1, label2));
    }

    @Override
    public void attach() {
        super.attach();
        getUI().setErrorHandler(new DefaultErrorHandler() {
            @Override
            public void error(com.vaadin.server.ErrorEvent event) {
                System.out.println("V7 ERROR HANDLER");
                throw new RuntimeException(event.getThrowable());
            }
        });
    }

    private String getContent(int i) {
        return "<h4>Test content "+i+"</h4>";
    }
}
