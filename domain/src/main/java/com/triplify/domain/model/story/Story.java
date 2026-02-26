package com.triplify.domain.model.story;

import com.triplify.domain.model.media.Image;
import com.triplify.domain.model.place.Place;
import com.triplify.domain.model.route.Route;
import com.triplify.domain.model.tag.Tag;
import com.triplify.domain.model.trip.Trip;
import com.triplify.domain.model.user.User;
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
public class Story {
    private UUID id;
    @NonNull
    private User author;
    private Trip trip;
    private Place place;
    private Route route;
    private Emotion emotion;
    @NonNull
    private String title;
    private String description;
    private LocalDateTime time;
    private double latitude;
    private double longitude;
    private LocalDateTime createdAt;
    private Set<Tag> tags;
    private List<Image> images;
}