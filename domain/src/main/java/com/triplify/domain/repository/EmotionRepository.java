package com.triplify.domain.repository;

import com.triplify.domain.model.Emotion;

import java.util.Optional;
import java.util.UUID;

public interface EmotionRepository {
    Optional<Emotion> findById(UUID id);
    void create(Emotion emotion);
    void update(Emotion emotion);
    void delete(UUID id);
    Optional<Emotion> findByName(String name);
}
