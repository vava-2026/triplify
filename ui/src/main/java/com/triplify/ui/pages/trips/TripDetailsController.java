package com.triplify.ui.pages.trips;

import com.triplify.application.response.TripStatus;
import com.triplify.ui.routing.RouteIds;
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
        setFullScreen(false);
    }

    @Override
    public void onLifecycleHide() {
        setFullScreen(false);
    }

    @Override
    public void onLifecycleDestroy() {
        setFullScreen(false);
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

        if (id == null) return;

        RouterArgument args = new RouterArgument();
        args.addArgument("tripId", id);
        args.addArgument("tripName", name == null ? tripNameLabel.getText() : name);
        getRouter().moveto(RouteIds.ADD_PLACE, args);
    }

    @FXML
    private void onViewPlaces() {
        RouterArgument current = getRouter().getCurrentData();
        Integer id = current == null ? null : current.getValue("tripId");
        String name = current == null ? null : current.getValue("tripName");

        if (id == null) {
            log.warn("Cannot open places — tripId is null");
            return;
        }

        RouterArgument args = new RouterArgument();
        args.addArgument("tripId", id);
        args.addArgument("tripName", name != null ? name : tripNameLabel.getText());

        log.info("Navigating to places for trip id={}", id);
        getRouter().moveto(RouteIds.TRIP_PLACES, args);
    }

    private void setFullScreen(boolean value) {
        TriplifyRouterContext ctx = (TriplifyRouterContext) getRouter().getContext();
        ctx.setFullScreenContent(value);
    }
}