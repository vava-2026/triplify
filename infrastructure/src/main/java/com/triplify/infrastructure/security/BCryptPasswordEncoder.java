package com.triplify.infrastructure.security;

import at.favre.lib.crypto.bcrypt.BCrypt;
import com.triplify.domain.service.PasswordEncoder;

public class BCryptPasswordEncoder implements PasswordEncoder {

    private static final int COST = 10;

    @Override
    public String encode(String rawPassword) {
        return BCrypt.withDefaults().hashToString(COST, rawPassword.toCharArray());
    }

    @Override
    public boolean matches(String rawPassword, String encodedPassword) {
        return BCrypt.verifyer().verify(rawPassword.toCharArray(), encodedPassword).verified;
    }
}
