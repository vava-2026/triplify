package com.triplify.domain.model.user;

import com.triplify.domain.model.media.Image;
import com.triplify.domain.model.tag.Tag;
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
public class User {
    private UUID id;
    @NonNull
    private String username;
    @NonNull
    private String email;
    @NonNull
    private String passwordHash;
    @NonNull
    private String role;
    private Image avatar;
    private LocalDateTime createdAt;
    private Set<Tag> tags;
    private List<UserBadge> badges;
}