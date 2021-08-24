package org.vaadin;

import com.vaadin.flow.component.ClientCallable;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;
import com.vaadin.mpr.LegacyWrapper;

@Route(value = "main", layout = MyUI.class)
public class MainV extends VerticalLayout {
    public static final String TITLE = "Main";
    private final LegacyWrapper upload;

    public MainV() {
        setSizeFull();
        add(new LegacyWrapper(new V7TabSheetView(UI.getCurrent())));
        upload = new LegacyWrapper(new AsBuiltFileUploadBox(null, UI.getCurrent(), this));
        add(new Label("Upload with workaround:"));
        add(upload);
        add(new Label("Upload without workaround:"));
        add(new LegacyWrapper(new AsBuiltFileUploadBox(null, UI.getCurrent(), null)));
    }

    // the workaround for the stuck upload progress
    @ClientCallable
    public void uploadWorkaround() {
        System.out.println("....");
        upload.getLegacyComponent().markAsDirtyRecursive();
    }
}
