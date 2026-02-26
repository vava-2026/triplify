package com.triplify.domain.model.trip;

import com.triplify.domain.model.place.Place;
import com.triplify.domain.model.route.Route;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.NonNull;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TripPlace {
    private UUID id;
    @NonNull
    private Place place;
    @NonNull
    private String type;
    private Route route;
    private int priority;
    private LocalDateTime createdAt;
    private String status;
    private LocalDate visitDate;
}