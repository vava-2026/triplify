package com.triplify.application.validation;

import com.triplify.domain.error.FieldViolation;
import com.triplify.domain.error.ValidationError;
import com.triplify.domain.result.Result;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.List;
import java.util.Set;

public class ValidatingProxy implements InvocationHandler {

    private static final jakarta.validation.Validator VALIDATOR =
            Validation.buildDefaultValidatorFactory().getValidator();

    private final Object target;

    private ValidatingProxy(Object target) {
        this.target = target;
    }

    @SuppressWarnings("unchecked")
    public static <T> T wrap(T target, Class<T> iface) {
        return (T) Proxy.newProxyInstance(
                iface.getClassLoader(),
                new Class<?>[]{iface},
                new ValidatingProxy(target)
        );
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        if (args != null) {
            for (Object arg : args) {
                if (arg != null) {
                    Result<?> validationResult = validate(arg);
                    if (validationResult.isFailure()) {
                        return validationResult;
                    }
                }
            }
        }

        try {
            return method.invoke(target, args);
        } catch (InvocationTargetException ex) {
            throw ex.getCause();
        }
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private Result<?> validate(Object arg) {
        Set<ConstraintViolation<Object>> violations = (Set) VALIDATOR.validate(arg);
        if (violations.isEmpty()) {
            return Result.ok();
        }

        List<FieldViolation> fieldViolations = violations.stream()
                .map(v -> new FieldViolation(
                        v.getPropertyPath().toString(),
                        v.getInvalidValue(),
                        v.getConstraintDescriptor().getAnnotation().annotationType().getSimpleName(),
                        stripBraces(v.getMessageTemplate())
                ))
                .toList();

        return Result.fail(new ValidationError(fieldViolations));
    }

    private String stripBraces(String template) {
        if (template.startsWith("{") && template.endsWith("}")) {
            return template.substring(1, template.length() - 1);
        }
        return template;
    }
}
