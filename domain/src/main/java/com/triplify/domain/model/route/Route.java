package com.triplify.domain.model.route;

import com.triplify.domain.model.media.Image;
import com.triplify.domain.model.user.User;
import java.time.LocalDateTime;
import java.util.List;
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
public class Route {
    private UUID id;
    @NonNull
    private User owner;
    private Image coverImage;
    @NonNull
    private String title;
    private String description;
    private double totalLength;
    private String status;
    private LocalDateTime createdAt;
    private List<RoutePlace> places;
    private List<Image> images;
}