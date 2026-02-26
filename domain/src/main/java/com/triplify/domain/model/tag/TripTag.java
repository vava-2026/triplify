package com.triplify.domain.model.tag;

import com.triplify.domain.model.trip.Trip;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.NonNull;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TripTag {
    @NonNull
    private Trip trip;
    @NonNull
    private Tag tag;
}