package com.triplify.application.usecase.session;

import java.util.Optional;

/**
 * Manages the current user's session state within the application lifecycle.
 * This service should be used for all other services that need current user or current user id
 */
public interface UserSessionContext {

    void set(SessionUser user);
    void clear();
    Optional<SessionUser> getCurrent();
    boolean isLoggedIn();
    Optional<SessionUser> load();
    void save();
}
