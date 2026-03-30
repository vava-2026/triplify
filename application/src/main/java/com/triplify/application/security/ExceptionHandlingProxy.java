package com.triplify.application.security;

import com.triplify.application.error.ApplicationError;
import com.triplify.domain.result.Result;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

/**
 * Dynamic proxy that catches any exception thrown by the underlying service implementation and converts it to {@code Result.fail(ApplicationError.Unexpected)}.
 *
 * Only applies to methods whose return type is {@link Result}.
 * For any other return type the exception propagates normally.
 */
public class ExceptionHandlingProxy implements InvocationHandler {

    private static final Logger log = LoggerFactory.getLogger(ExceptionHandlingProxy.class);

    private final Object target;

    private ExceptionHandlingProxy(Object target) {
        this.target = target;
    }

    @SuppressWarnings("unchecked")
    public static <T> T wrap(T target, Class<T> iface) {
        return (T) Proxy.newProxyInstance(
                iface.getClassLoader(),
                new Class<?>[]{iface},
                new ExceptionHandlingProxy(target)
        );
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        try {
            return method.invoke(target, args);
        } catch (InvocationTargetException ex) {
            Throwable cause = ex.getCause();

            if (Result.class.isAssignableFrom(method.getReturnType())) {
                String operation = target.getClass().getSimpleName() + "." + method.getName();
                log.error("Unhandled exception in {}", operation, cause);
                return Result.fail(new ApplicationError.Unexpected(operation, cause));
            }

            throw cause;
        }
    }
}
