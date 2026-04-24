package com.triplify.infrastructure.repository;

import com.triplify.domain.model.Category;
import com.triplify.domain.model.enums.ColorEnum;
import com.triplify.domain.repository.CategoryRepository;
import com.triplify.infrastructure.repository.persistence.SQLiteConnectionFactory;
import com.triplify.infrastructure.repository.utils.RepositoryUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class CategoryRepositoryImpl implements CategoryRepository {

    private static final Logger log = LoggerFactory.getLogger(CategoryRepositoryImpl.class);

    @Override
    public List<Category> findAll() {
        String sql = "SELECT id, created_by, name, name_sk, description, description_sk, emoji_unicode, color " +
                "FROM categories";

        try (Connection conn = SQLiteConnectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            List<Category> categories = new ArrayList<>();
            while (rs.next()) {
                categories.add(mapRow(rs));
            }
            return categories;
        } catch (SQLException e) {
            log.error("Failed to find all categories", e);
            throw new RuntimeException("Database error while finding all categories", e);
        }
    }

    @Override
    public Optional<Category> findById(UUID id) {
        String sql = "SELECT id, created_by, name, name_sk, description, description_sk, emoji_unicode, color " +
                "FROM categories WHERE id = ? LIMIT 1";

        try (Connection conn = SQLiteConnectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, id.toString());
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
    public boolean existsByName(String name, String nameSk) {
        String sql = "SELECT COUNT(*) FROM categories WHERE name = ? OR name_sk = ?";

        try (Connection conn = SQLiteConnectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, name);
            ps.setString(2, nameSk);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            log.error("Failed to check existence for category name='{}' or nameSk='{}'", name, nameSk, e);
            throw new RuntimeException("Database error while checking category existence", e);
        }
    }

    @Override
    public void create(Category category) {
        String sql = "INSERT INTO categories (id, created_by, name, name_sk, description, description_sk, emoji_unicode, color) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = SQLiteConnectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, category.getId().toString());
            if (category.getCreatedById() == null) {
                ps.setNull(2, java.sql.Types.VARCHAR);
            } else {
                ps.setString(2, category.getCreatedById().toString());
            }
            ps.setString(3, category.getName());
            ps.setString(4, category.getNameSk());
            ps.setString(5, category.getDescription());
            ps.setString(6, category.getDescriptionSk());
            ps.setString(7, category.getEmojiUnicode());
            ps.setString(8, colorToSql(category.getColor()));
            ps.executeUpdate();

            log.debug("Created category with id='{}', name='{}'", category.getId(), category.getName());
        } catch (SQLException e) {
            log.error("Failed to create category with id='{}'", category.getId(), e);
            throw new RuntimeException("Database error while creating category", e);
        }
    }

    @Override
    public void update(Category category) {
        String sql = "UPDATE categories " +
                "SET name = ?, name_sk = ?, description = ?, description_sk = ?, emoji_unicode = ?, color = ? " +
                "WHERE id = ?";

        try (Connection conn = SQLiteConnectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, category.getName());
            ps.setString(2, category.getNameSk());
            ps.setString(3, category.getDescription());
            ps.setString(4, category.getDescriptionSk());
            ps.setString(5, category.getEmojiUnicode());
            ps.setString(6, colorToSql(category.getColor()));
            ps.setString(7, category.getId().toString());
            ps.executeUpdate();

            log.debug("Updated category with id='{}', name='{}'", category.getId(), category.getName());
        } catch (SQLException e) {
            log.error("Failed to update category with id='{}'", category.getId(), e);
            throw new RuntimeException("Database error while updating category", e);
        }
    }

    @Override
    public void delete(Category category) {
        String sql = "DELETE FROM categories WHERE id = ?";

        try (Connection conn = SQLiteConnectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, category.getId().toString());
            ps.executeUpdate();

            log.debug("Deleted category with id='{}', name='{}'", category.getId(), category.getName());
        } catch (SQLException e) {
            log.error("Failed to delete category with id='{}'", category.getId(), e);
            throw new RuntimeException("Database error while deleting category", e);
        }
    }

    private Category mapRow(ResultSet rs) throws SQLException {
        UUID id = UUID.fromString(rs.getString("id"));
        String createdByRaw = rs.getString("created_by");
        UUID createdById = createdByRaw == null || createdByRaw.isBlank() ? null : UUID.fromString(createdByRaw);
        String name = rs.getString("name");
        String nameSk = rs.getString("name_sk");
        String description = rs.getString("description");
        String descriptionSk = rs.getString("description_sk");
        String emojiUnicode = rs.getString("emoji_unicode");
        ColorEnum color = RepositoryUtils.sqlToColor(rs.getString("color"));

        return new Category(id, createdById, name, nameSk, description, descriptionSk, emojiUnicode, color);
    }

    private static String colorToSql(ColorEnum color) {
        if (color == null) {
            return ColorEnum.GRAY.getValue();
        }
        return color.getValue();
    }
}
