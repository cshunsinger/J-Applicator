package io.github.cshunsinger.japplicator.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.FIELD, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface Nested {
    /**
     * This annotation can be used on a field or methods.
     *
     * When this annotation is used on a field:
     *  - An attempt will be made to look up a getter and setter method based on the field name. Such as setFieldName and getFieldName
     *
     * When this annotation is used on a method:
     *  - If value is blank: An attempt will be made to find the complimentary method. If the annotated method is a setter,
     *    an attempt to find a getter method that is annotated with an empty value string will be made based on method name.
     *    There is no guarantee that this will be successful.
     *  - If value is not blank: An attempt will be made to find the complimentary method. If the annotated method is a setter,
     *    an attempt to find a getter method that is annotated with the same value String will be made.
     */
    String value() default "";
}