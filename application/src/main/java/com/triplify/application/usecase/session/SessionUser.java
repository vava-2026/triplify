package com.triplify.application.usecase.session;

import com.triplify.domain.model.enums.RoleEnum;

import java.util.UUID;

public record SessionUser(UUID userId, String username, String email, RoleEnum role) {}
