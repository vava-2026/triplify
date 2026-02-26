package com.triplify.domain.model.route;

import com.triplify.domain.model.place.Place;
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
public class RoutePlace {
    private UUID id;
    @NonNull
    private Place place;
    private int priority;
    private LocalDateTime createdAt;
    private String status;
    private LocalDate startDate;
    private LocalDate endDate;
}