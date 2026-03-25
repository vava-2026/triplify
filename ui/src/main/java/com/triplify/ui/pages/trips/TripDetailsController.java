package com.triplify.ui.pages.trips;

import com.triplify.ui.routing.RouteIds;
import com.triplify.ui.routing.TriplifyRouterContext;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import rahulstech.jfx.routing.element.RouterArgument;
import rahulstech.jfx.routing.lifecycle.SimpleLifecycleAwareController;

public class TripDetailsController extends SimpleLifecycleAwareController {

    @FXML private Label tripNameLabel;
    @FXML private Label tripIdLabel;

    @Override
    public void onLifecycleInitialize() {
        RouterArgument data = getRouter().getCurrentData();
        String name = data == null ? null : data.getValue("tripName");
        Integer id = data == null ? null : data.getValue("tripId");

        tripNameLabel.setText(name == null ? "Unknown trip" : name);
        tripIdLabel.setText(id == null ? "-" : String.valueOf(id));
    }

    @Override
    public void onLifecycleShow() {
        TriplifyRouterContext context = (TriplifyRouterContext) getRouter().getContext();
        context.setFullScreenContent(true);
    }

    @Override
    public void onLifecycleHide() {
        TriplifyRouterContext context = (TriplifyRouterContext) getRouter().getContext();
        context.setFullScreenContent(false);
    }

    @Override
    public void onLifecycleDestroy() {
        TriplifyRouterContext context = (TriplifyRouterContext) getRouter().getContext();
        context.setFullScreenContent(false);
    }

    @FXML
    private void onBack() {
        getRouter().popBackStack();
    }

    @FXML
    private void onAddPlace() {
        RouterArgument currentData = getRouter().getCurrentData();
        Integer id = currentData == null ? null : currentData.getValue("tripId");
        String name = currentData == null ? null : currentData.getValue("tripName");
        if (id == null) {
            return;
        }

        RouterArgument args = new RouterArgument();
        args.addArgument("tripId", id);
        args.addArgument("tripName", name == null ? tripNameLabel.getText() : name);
        getRouter().moveto(RouteIds.ADD_PLACE, args);
    }
}
