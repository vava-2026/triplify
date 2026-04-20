package com.triplify.domain.repository;

import com.triplify.domain.model.Emotion;

import java.util.Optional;

public interface EmotionRepository {
    Optional<Emotion> findById(String id);
    void create(Emotion emotion);
    void update(Emotion emotion);
    void delete(String id);
    Optional<Emotion> findByName(String name);
}
