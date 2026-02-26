package com.triplify.domain.model.media;

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
public class Image {
    private UUID id;
    @NonNull
    private String url;
    private String storageKey;
    private String description;
    private LocalDateTime uploadedAt;
}