package com.triplify.ui.pages.trips;

import com.triplify.application.response.TripStatus;
import com.triplify.ui.routing.TriplifyRouterContext;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rahulstech.jfx.routing.element.RouterArgument;
import rahulstech.jfx.routing.lifecycle.SimpleLifecycleAwareController;

public class TripDetailsController extends SimpleLifecycleAwareController {

    private static final Logger log = LoggerFactory.getLogger(TripDetailsController.class);

    @FXML private Label tripNameLabel;
    @FXML private Label tripIdLabel;
    @FXML private Label tripCategoryLabel;
    @FXML private Label tripDatesLabel;
    @FXML private Label tripStatusLabel;

    @Override
    public void onLifecycleInitialize() {
        RouterArgument data = getRouter().getCurrentData();
        String name = data == null ? null : data.getValue("tripName");
        Integer id = data == null ? null : data.getValue("tripId");
        String category = data == null ? null : data.getValue("tripCategory");
        String dates = data == null ? null : data.getValue("tripDates");
        TripStatus status = data == null ? null : data.getValue("tripStatus");

        tripNameLabel.setText(name == null ? "Unknown trip" : name);
        tripIdLabel.setText(id == null ? "-" : String.valueOf(id));
        tripCategoryLabel.setText(category == null ? "-" : category);
        tripDatesLabel.setText(dates == null ? "-" : dates);

        tripStatusLabel.getStyleClass().removeIf(style -> style.startsWith("trip-status-"));
        if (status == null) {
            tripStatusLabel.setText("Unknown");
        } else {
            tripStatusLabel.setText(status.getLabel());
            tripStatusLabel.getStyleClass().add(status.getCssClass());
        }
        log.info("Trip details opened: id={}, name={}", id, name);
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
}
