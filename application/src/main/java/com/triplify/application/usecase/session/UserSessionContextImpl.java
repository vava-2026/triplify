package com.triplify.application.usecase.session;

import java.util.Optional;

public class UserSessionContextImpl implements UserSessionContext {

    private volatile SessionUser currentUser;

    @Override
    public void set(SessionUser user) {
        this.currentUser = user;
    }

    @Override
    public void clear() {
        this.currentUser = null;
    }

    @Override
    public Optional<SessionUser> getCurrent() {
        return Optional.ofNullable(currentUser);
    }

    @Override
    public boolean isLoggedIn() {
        return currentUser != null;
    }
}
