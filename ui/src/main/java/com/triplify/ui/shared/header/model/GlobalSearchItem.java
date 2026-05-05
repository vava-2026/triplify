package com.triplify.ui.shared.header.model;

import lombok.Builder;
import lombok.Value;
import java.util.UUID;
import com.triplify.domain.model.enums.StatusEnum;
import com.triplify.application.shared.ColorTheme;

@Value
@Builder
public class GlobalSearchItem {
    public enum Type {
        TRIP, ROUTE, PLACE
    }

    UUID id;
    String title;
    Type type;
    ColorTheme colorTheme;
}

