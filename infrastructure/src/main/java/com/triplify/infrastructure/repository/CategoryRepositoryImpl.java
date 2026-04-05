package com.triplify.infrastructure.repository;

import com.triplify.domain.model.Category;
import com.triplify.domain.model.enums.ColorEnum;
import com.triplify.domain.repository.CategoryRepository;
import com.triplify.infrastructure.repository.persistence.SQLiteConnectionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;

public class CategoryRepositoryImpl implements CategoryRepository {

    private static final Logger log = LoggerFactory.getLogger(CategoryRepositoryImpl.class);

    @Override
    public Optional<Category> findById(String id) {
        String sql = """
            SELECT id, created_by, name, name_sk, description, description_sk, emoji_unicode, color
            FROM categories
            WHERE id = ?
            LIMIT 1
            """;

        try (Connection conn = SQLiteConnectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            log.error("Failed to find category by id='{}'", id, e);
            throw new RuntimeException("Database error while finding category by id", e);
        }

        return Optional.empty();
    }

    @Override
    public List<Category> findAll() {
        String sql = """
            SELECT id, created_by, name, name_sk, description, description_sk, emoji_unicode, color
            FROM categories
            ORDER BY name COLLATE NOCASE ASC
            """;

        try (Connection conn = SQLiteConnectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            List<Category> categories = new ArrayList<>();
            while (rs.next()) {
                categories.add(mapRow(rs));
            }
            return categories;
        } catch (SQLException e) {
            log.error("Failed to load categories", e);
            throw new RuntimeException("Database error while finding categories", e);
        }
    }

    @Override
    public void save(Category category) {
        String sql = """
            INSERT INTO categories (id, created_by, name, name_sk, description, description_sk, emoji_unicode, color)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?)
            """;

        try (Connection conn = SQLiteConnectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, category.getId().toString());
            ps.setString(2, category.getCreatedById().toString());
            ps.setString(3, category.getName());
            ps.setString(4, category.getNameSk());
            ps.setString(5, category.getDescription());
            ps.setString(6, category.getDescriptionSk());
            ps.setString(7, category.getEmojiUnicode());
            ps.setString(8, category.getColor().getValue());
            ps.executeUpdate();
        } catch (SQLException e) {
            log.error("Failed to save category id='{}'", category.getId(), e);
            throw new RuntimeException("Database error while saving category", e);
        }
    }

    private Category mapRow(ResultSet rs) throws SQLException {
        String colorRaw = rs.getString("color");
        return new Category(
                UUID.fromString(rs.getString("id")),
                UUID.fromString(rs.getString("created_by")),
                rs.getString("name"),
                rs.getString("name_sk"),
                rs.getString("description"),
                rs.getString("description_sk"),
                rs.getString("emoji_unicode"),
                parseColor(colorRaw)
        );
    }

    private ColorEnum parseColor(String color) {
        if (color == null || color.isBlank()) {
            return ColorEnum.GRAY;
        }
        return ColorEnum.valueOf(color.trim().toUpperCase(Locale.ROOT));
    }
}
