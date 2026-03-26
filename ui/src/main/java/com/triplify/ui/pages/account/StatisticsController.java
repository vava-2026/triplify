package com.triplify.ui.pages.account;

import jakarta.inject.Singleton;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;

import java.net.URL;
import java.util.ResourceBundle;

@Singleton
public class StatisticsController implements Initializable {

    @FXML private Label countriesValue;
    @FXML private Label tripsValue;
    @FXML private Label placesValue;
    @FXML private Label travelDaysValue;
    @FXML private Label photosValue;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // TODO: Replace with actual data fetching logic
        countriesValue.setText("33");
        tripsValue.setText("67");
        placesValue.setText("128");
        travelDaysValue.setText("90");
        photosValue.setText("534");
    }
}