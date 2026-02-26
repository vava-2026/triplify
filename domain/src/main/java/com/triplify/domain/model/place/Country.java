package com.triplify.domain.model.place;

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
public class Country {
    private UUID id;
    @NonNull
    private User createdBy;
    @NonNull
    private String name;
    private boolean banned;
}