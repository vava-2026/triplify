package com.triplify.application.usecase.session;

import com.google.inject.Inject;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.triplify.domain.model.User;
import com.triplify.domain.repository.UserRepository;
import com.triplify.domain.util.DefaultDataDirectories;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

public class UserSessionContextImpl implements UserSessionContext {

    private static final Logger log = LoggerFactory.getLogger(UserSessionContextImpl.class);
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private static final Path SESSION_FILE = DefaultDataDirectories.resolve("Triplify").resolve("session.json");

    private final UserRepository userRepository;
    private volatile SessionUser currentUser;
    private final List<Consumer<Optional<SessionUser>>> sessionChangeListeners = new ArrayList<>();

    @Inject
    public UserSessionContextImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public void set(SessionUser user) {
        this.currentUser = user;
        notifySessionChangeListeners();
    }

    @Override
    public void clear() {
        this.currentUser = null;
        notifySessionChangeListeners();
        deletePersistedSession();
    }

    @Override
    public Optional<SessionUser> getCurrent() {
        return Optional.ofNullable(currentUser);
    }

    @Override
    public boolean isLoggedIn() {
        return currentUser != null;
    }

    @Override
    public Optional<SessionUser> load() {
        if (!Files.exists(SESSION_FILE)) {
            currentUser = null;
            notifySessionChangeListeners();
            return Optional.empty();
        }
        log.debug("Loading persisted session from {}", SESSION_FILE);

        SessionUser storedUser;
        try {
            storedUser = OBJECT_MAPPER.readValue(SESSION_FILE.toFile(), SessionUser.class);
            log.info("Loaded persisted session for user email='{}'", storedUser.email());
        } catch (IOException e) {
            log.warn("Failed to load persisted session from {}", SESSION_FILE, e);
            deletePersistedSession();
            currentUser = null;
            notifySessionChangeListeners();
            return Optional.empty();
        }

        if (storedUser == null || storedUser.email() == null || storedUser.email().isBlank()) {
            log.warn("Ignoring invalid session file: missing email");
            deletePersistedSession();
            currentUser = null;
            notifySessionChangeListeners();
            return Optional.empty();
        }

        log.debug("Validating persisted session for user email='{}'", storedUser.email());
        Optional<User> userFromDb;
        try {
            userFromDb = userRepository.findByEmail(storedUser.email());
            log.debug("Validated persisted session for user email='{}'", storedUser.email());
        } catch (RuntimeException e) {
            log.warn("Failed to validate persisted session for email='{}'", storedUser.email(), e);
            currentUser = null;
            notifySessionChangeListeners();
            return Optional.empty();
        }

        if (userFromDb.isEmpty()) {
            log.info("Ignoring persisted session for deleted user email='{}'", storedUser.email());
            deletePersistedSession();
            currentUser = null;
            notifySessionChangeListeners();
            return Optional.empty();
        }

        User user = userFromDb.get();
        currentUser = new SessionUser(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getRole(),
                user.getAvatarImageId());
        notifySessionChangeListeners();
        log.info("Loaded persisted session for user email='{}'", currentUser.email());
        save();
        log.debug("Persisted session loaded successfully");
        return Optional.of(currentUser);
    }

    @Override
    public void save() {
        log.debug("Saving session to {}", SESSION_FILE);
        SessionUser user = currentUser;
        if (user == null) {
            deletePersistedSession();
            return;
        }

        try {
            Files.createDirectories(SESSION_FILE.getParent());
            OBJECT_MAPPER.writerWithDefaultPrettyPrinter().writeValue(
                    SESSION_FILE.toFile(),
                    user);
            log.debug("Session saved successfully");
        } catch (IOException e) {
            log.warn("Failed to save session to {}", SESSION_FILE, e);
        }
    }

    @Override
    public void addSessionChangeListener(Consumer<Optional<SessionUser>> listener) {
        sessionChangeListeners.add(listener);
    }

    @Override
    public void removeSessionChangeListener(Consumer<Optional<SessionUser>> listener) {
        sessionChangeListeners.remove(listener);
    }

    private void notifySessionChangeListeners() {
        Optional<SessionUser> current = Optional.ofNullable(currentUser);
        for (Consumer<Optional<SessionUser>> listener : new ArrayList<>(sessionChangeListeners)) {
            try {
                listener.accept(current);
            } catch (Exception e) {
                log.warn("Error notifying session change listener", e);
            }
        }
    }

    private void deletePersistedSession() {
        log.debug("Deleting persisted session file {}", SESSION_FILE);
        try {
            Files.deleteIfExists(SESSION_FILE);
            log.debug("Persisted session file deleted successfully");
        } catch (IOException e) {
            log.warn("Failed to delete persisted session file {}", SESSION_FILE, e);
        }
    }
}
