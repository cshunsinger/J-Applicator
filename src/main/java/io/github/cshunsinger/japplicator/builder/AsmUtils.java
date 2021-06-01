package io.github.cshunsinger.japplicator.builder;

import io.github.cshunsinger.japplicator.annotation.FieldIdentifier;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.reflect.ConstructorUtils;

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Member;
import java.lang.reflect.Modifier;
import java.util.Set;

/**
 * Utility class with some Java ASM-related utilities.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class AsmUtils {
    /**
     * Illegal package name prefixes because they are used by the Java standard library.
     */
    public static final Set<String> ILLEGAL_PREFIXES = Set.of("java", "javax", "com.sun", "sun");

    /**
     * Returns whether or not a class contains an accessible empty constructor (constructor with 0 parameters).
     * @param type The class to search within for a no-args constructor.
     * @return True if the specified class contains an accessible constructor with 0 parameters. False otherwise.
     * @see #canBeConstructed(Class)
     */
    public static boolean containsEmptyConstructor(Class<?> type) {
        return ConstructorUtils.getAccessibleConstructor(type) != null;
    }

    /**
     * Determines if a class can be instantiated programmatically by the J-Applicator bytecode generation code.
     * A class can be constructed if it is a concrete class (not an interface and not abstract), and if that class
     * contains a declared constructor that takes 0 parameters and is accessible.
     * @param type The class to test.
     * @return True if the class is a concrete type with an accessible no-args constructor.
     * @see #containsEmptyConstructor(Class)
     */
    public static boolean canBeConstructed(Class<?> type) {
        return !type.isInterface()
            && !Modifier.isAbstract(type.getModifiers())
            && containsEmptyConstructor(type);
    }

    /**
     * Determines the field identifier name. A field is either identified by the value of the @FieldIdentifier annotation
     * on the field, or by the name of the field itself. If a member is not annotated with the @FieldIdentifier annotation,
     * then the name of the member itself is returned. If a member is annotated with the @FieldIdentifier annotation, then
     * the "value" of that annotation is returned instead. If the "value" of the @FieldIdentifier annotation is an empty
     * or blank String, then the name of the member is used instead. If the member cannot be counted as an identified
     * field, then null is returned.
     * @param fieldOrMethod The Java member object representing the field or method being checked.
     * @param defaults The @FieldIdentifier annotation at the class level of the member's containing class.
     * @return The String name of the member, or the value of a @FieldIdentifier annotation if one is present. If no
     * annotation exists on the member or on the member's containing class, then null is returned.
     */
    public static String memberIdentifierName(Member fieldOrMethod, FieldIdentifier defaults) {
        FieldIdentifier fieldAnnotation = ((AccessibleObject)fieldOrMethod).getAnnotation(FieldIdentifier.class);

        if(fieldAnnotation != null) { //Field is annotated
            String name = fieldAnnotation.value();
            if(name.isEmpty()) //Name not specified in the @FieldIdentifier annotation, use field name instead
                return fieldOrMethod.getName();
            else
                return name; //Return the custom name specified in the @FieldIdentifier annotation
        }
        else if(defaults != null) //Field not annotated, class is annotated, use field name
            return fieldOrMethod.getName();
        else
            return null; //Field is not counted because it is not annotated and it's class is not annotated
    }
}