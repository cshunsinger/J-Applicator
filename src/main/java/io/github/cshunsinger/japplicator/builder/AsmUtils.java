package io.github.cshunsinger.japplicator.builder;

import io.github.cshunsinger.japplicator.FieldIdentifier;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.reflect.ConstructorUtils;

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Member;
import java.lang.reflect.Modifier;
import java.util.Set;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class AsmUtils {
    public static final Set<String> ILLEGAL_PREFIXES = Set.of("java", "javax", "com.sun", "sun");

    public static boolean containsEmptyConstructor(Class<?> type) {
        return ConstructorUtils.getAccessibleConstructor(type) != null;
    }

    public static boolean canBeConstructed(Class<?> type) {
        return !type.isInterface()
            && !Modifier.isAbstract(type.getModifiers())
            && containsEmptyConstructor(type);
    }

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