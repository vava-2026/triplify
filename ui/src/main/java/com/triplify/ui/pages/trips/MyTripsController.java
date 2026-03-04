package com.triplify.ui.pages.trips;

import com.triplify.ui.routing.RouteIds;
import javafx.fxml.FXML;
import rahulstech.jfx.routing.element.RouterArgument;
import rahulstech.jfx.routing.lifecycle.SimpleLifecycleAwareController;

public class MyTripsController extends SimpleLifecycleAwareController {

    @FXML
    public void onOpenTrip() {
        RouterArgument args = new RouterArgument();
        args.addArgument("tripId", 42);
        args.addArgument("tripName", "Prague Weekend");
        getRouter().moveto(RouteIds.TRIP_DETAILS, args);
    }
}
