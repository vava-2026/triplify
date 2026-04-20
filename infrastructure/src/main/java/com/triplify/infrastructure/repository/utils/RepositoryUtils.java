package com.triplify.infrastructure.repository.utils;

import com.triplify.domain.model.enums.ColorEnum;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import java.util.UUID;

public class RepositoryUtils {
    public static ColorEnum sqlToColor(String color) {
        if (color == null || color.isBlank()) {
            return ColorEnum.GRAY;
        }
        try {
            return ColorEnum.valueOf(color.trim().toUpperCase(java.util.Locale.ROOT));
        } catch (IllegalArgumentException ex) {
            return ColorEnum.GRAY;
        }
    }

    public static void bindParams(PreparedStatement ps, List<Object> params, int startIdx) throws SQLException {
        int idx = startIdx;
        for (Object p : params) {
            ps.setObject(idx++, p);
        }
    }

    public static UUID parseUuid(String value) {
        return value == null ? null : UUID.fromString(value);
    }

    public static String uuidOrNull(UUID uuid) {
        return uuid == null ? null : uuid.toString();
    }
}
