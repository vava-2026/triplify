package com.triplify.domain.model.place;

import com.triplify.domain.model.media.Image;
import com.triplify.domain.model.user.User;
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
public class Place {
    private UUID id;
    @NonNull
    private User owner;
    @NonNull
    private String title;
    private String description;
    @NonNull
    private Country country;
    private Image coverImage;
    private double latitude;
    private double longitude;
    private LocalDateTime createdAt;
}