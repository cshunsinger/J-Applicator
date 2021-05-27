package io.github.cshunsinger.japplicator.exception;

import org.apache.commons.lang3.StringUtils;

import java.lang.reflect.Type;

public class TypeConversionException extends RuntimeException {
    public TypeConversionException(Type fromType, Type toType) {
        this(null, fromType, toType, null);
    }

    public TypeConversionException(String reason, Type fromType, Type toType, Throwable cause) {
        super("Could not convert value from type %s to type %s.%s".formatted(
            fromType.getTypeName(),
            toType.getTypeName(),
            StringUtils.isBlank(reason) ? "" : " " + reason
        ), cause);
    }
}