package io.github.cshunsinger.japplicator.exception;

import org.apache.commons.lang3.StringUtils;

import java.lang.reflect.Type;

/**
 * This is an exception thrown if the applicator system cannot convert data from one type to another type, for
 * any reason.
 */
public class TypeConversionException extends RuntimeException {
    /**
     * Create an exception with converting from a type to another type.
     * @param fromType The type the applicator tried to convert from.
     * @param toType The type the applicator wants to convert to.
     */
    public TypeConversionException(Type fromType, Type toType) {
        this(null, fromType, toType, null);
    }

    /**
     * Creates an exception with converting from a type to another type, with an added reason and cause.
     * @param reason The additional reason why conversion was unsuccessful.
     * @param fromType The type the applicator tried to convert from.
     * @param toType The type the applicator tried to convert to.
     * @param cause The exception that also caused this exception.
     */
    public TypeConversionException(String reason, Type fromType, Type toType, Throwable cause) {
        super("Could not convert value from type %s to type %s.%s".formatted(
            fromType.getTypeName(),
            toType.getTypeName(),
            StringUtils.isBlank(reason) ? "" : " " + reason
        ), cause);
    }
}