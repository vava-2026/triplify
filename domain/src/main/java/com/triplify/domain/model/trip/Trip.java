package com.triplify.domain.model.trip;

import com.triplify.domain.model.media.Image;
import com.triplify.domain.model.place.Country;
import com.triplify.domain.model.tag.Tag;
import com.triplify.domain.model.user.User;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
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
public class Trip {
    private UUID id;
    @NonNull
    private User owner;
    private Category category;
    private Country country;
    @NonNull
    private String title;
    private String description;
    private Image coverImage;
    private LocalDate startDate;
    private LocalDate endDate;
    private String status;
    private LocalDateTime createdAt;
    private List<TripPlace> places;
    private List<TripRoute> routes;
    private Set<Tag> tags;
    private List<Image> images;
}