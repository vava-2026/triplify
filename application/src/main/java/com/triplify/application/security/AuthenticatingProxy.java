package com.triplify.application.security;

import com.triplify.application.usecase.country.CountryServiceImpl;
import com.triplify.application.usecase.session.SessionUser;
import com.triplify.application.usecase.session.UserSessionContext;
import com.triplify.domain.error.UserError;
import com.triplify.domain.model.enums.RoleEnum;
import com.triplify.domain.result.Result;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Arrays;
import java.util.Optional;
import java.util.Set;

/**
 * Dynamic proxy that enforces {@link Authenticated} annotations on service methods.
 *
 * If the annotation is present (on the method or the implementation class) and the user is not authenticated, returns {@code Result.fail(UserError.Unauthorized)}.
 * If specific roles are required and the user's role doesn't match, returns {@code Result.fail(UserError.Forbidden)}.
 */
public class AuthenticatingProxy implements InvocationHandler {

    private static final Logger log = LoggerFactory.getLogger(AuthenticatingProxy.class);
    private final Object target;
    private final UserSessionContext sessionContext;

    private AuthenticatingProxy(Object target, UserSessionContext sessionContext) {
        this.target = target;
        this.sessionContext = sessionContext;
    }

    @SuppressWarnings("unchecked")
    public static <T> T wrap(T target, Class<T> iface, UserSessionContext sessionContext) {
        return (T) Proxy.newProxyInstance(
                iface.getClassLoader(),
                new Class<?>[]{iface},
                new AuthenticatingProxy(target, sessionContext)
        );
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        Authenticated auth = resolveAnnotation(method);

        if (auth != null) {
            Optional<SessionUser> current = sessionContext.getCurrent();

            if (current.isEmpty()) {
                log.warn("Attempt to access protected method {} without authentication", method.getName());
                return Result.fail(new UserError.Unauthorized());
            }

            RoleEnum[] requiredRoles = auth.roles();
            if (requiredRoles.length > 0) {
                Set<RoleEnum> allowed = Set.of(requiredRoles);
                if (!allowed.contains(current.get().role())) {
                    log.warn("Attempt to access protected method {} with insufficient role", method.getName());
                    return Result.fail(new UserError.Forbidden());
                }
            }
        }

        try {
            return method.invoke(target, args);
        } catch (InvocationTargetException ex) {
            throw ex.getCause();
        }
    }

    /**
     * Resolves the {@link Authenticated} annotation for the given interface method.
     * Checks in order: implementation method ==> implementation class
     */
    private Authenticated resolveAnnotation(Method method) {
        try {
            Method implMethod = target.getClass().getMethod(method.getName(), method.getParameterTypes());
            Authenticated methodLevel = implMethod.getAnnotation(Authenticated.class);
            if (methodLevel != null) {
                return methodLevel;
            }
        } catch (NoSuchMethodException ignored) {
            log.warn("Method {} does not exist in implementation class", method.getName());
        }

        return target.getClass().getAnnotation(Authenticated.class);
    }
}
