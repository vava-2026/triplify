package com.triplify.domain.model.user;

import com.triplify.domain.model.badge.Badge;
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
public class UserBadge {
    private UUID id;
    @NonNull
    private Badge badge;
    private int progressValue;
    private boolean unlocked;
}