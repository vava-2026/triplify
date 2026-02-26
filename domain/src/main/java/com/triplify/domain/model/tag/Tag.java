package com.triplify.domain.model.tag;

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
public class Tag {
    private UUID id;
    @NonNull
    private User owner;
    @NonNull
    private String name;
}