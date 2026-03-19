package com.triplify.ui.pages.trips;

import com.triplify.ui.routing.RouteIds;
import javafx.fxml.FXML;
import rahulstech.jfx.routing.element.RouterArgument;
import rahulstech.jfx.routing.lifecycle.SimpleLifecycleAwareController;

public class MyTripsController extends SimpleLifecycleAwareController {
    private final int id;
    private static String city;
    private static String country;
    private static int places;
    private static int days;
    private static int photos;

    public MyTripsController(int id, String city, String country, int places, int days, int photos) {
        this.id = id;
        MyTripsController.city = city;
        MyTripsController.country = country;
        MyTripsController.places = places;
        MyTripsController.days = days;
        MyTripsController.photos = photos;
    }

    @FXML
    public void onOpenTrip(int id,  String name) {
        RouterArgument args = new RouterArgument();
        args.addArgument("tripId", id);
        args.addArgument("tripName", name);
        getRouter().moveto(RouteIds.TRIP_DETAILS, args);
    }

    public int getId() {
        return id;
    }

    public String getCity() {
        return city;
    }

    public String getCountry() {
        return country;
    }

    public static int getPlaces() {
        return places;
    }

    public static int getDays() {
        return days;
    }

    public static int getPhotos() {
        return photos;
    }
}

