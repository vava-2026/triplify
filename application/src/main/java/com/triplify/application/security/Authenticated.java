package com.triplify.application.security;

import com.triplify.domain.model.enums.RoleEnum;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a service method (or entire interface) as requiring an authenticated user.
 * When placed on the interface type, all methods require authentication.
 * When placed on a method, only that method requires authentication.
 * Method-level annotations override type-level annotations.
 *
 * Optionally restricts access to specific roles via {@link #roles()}
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface Authenticated {

    /**
     * If non-empty, the authenticated user must have one of these roles
     * If empty (default), any authenticated user is allowed
     */
    RoleEnum[] roles() default {};
}
