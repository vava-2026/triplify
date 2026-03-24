package com.triplify.application.usecase.session;

import java.util.Optional;

public interface UserSessionContext {

    void set(SessionUser user);
    void clear();
    Optional<SessionUser> getCurrent();
    boolean isLoggedIn();}
