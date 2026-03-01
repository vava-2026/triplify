package com.triplify.ui.pages.trips;

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

    @FXML
    private void onBack() {
        getRouter().popBackStack();
    }
}
