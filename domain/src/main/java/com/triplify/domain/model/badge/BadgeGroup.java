package com.triplify.domain.model.badge;

import com.triplify.domain.model.user.User;
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
public class BadgeGroup {
    private UUID id;
    @NonNull
    private User createdBy;
    @NonNull
    private String name;
    private String description;
}