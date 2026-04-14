package com.triplify.ui.pages.account;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import rahulstech.jfx.routing.lifecycle.SimpleLifecycleAwareController;

public class StatisticsController extends SimpleLifecycleAwareController {

    @FXML private Label countriesValue;
    @FXML private Label tripsValue;
    @FXML private Label placesValue;
    @FXML private Label travelDaysValue;
    @FXML private Label photosValue;

    @FXML
    public void initialize() {
        // TODO: Replace with actual data fetching logic
        countriesValue.setText("33");
        tripsValue.setText("67");
        placesValue.setText("128");
        travelDaysValue.setText("90");
        photosValue.setText("534");
    }
}
