package io.github.cshunsinger.japplicator.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.reflect.MethodUtils;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Parameter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ReflectionsUtils {
    private static final Map<Class<?>, String> TYPE_MAPPINGS = new HashMap<>();
    static {
        TYPE_MAPPINGS.put(byte.class, "B");
        TYPE_MAPPINGS.put(short.class, "S");
        TYPE_MAPPINGS.put(int.class, "I");
        TYPE_MAPPINGS.put(long.class, "J");
        TYPE_MAPPINGS.put(float.class, "F");
        TYPE_MAPPINGS.put(double.class, "D");
        TYPE_MAPPINGS.put(char.class, "C");
        TYPE_MAPPINGS.put(boolean.class, "Z");
        TYPE_MAPPINGS.put(void.class, "V");
    }

    /**
     * Generates the JVM bytecode representation of a method signature for a given method.
     * @param method The method to generate a signature for.
     * @return A String containing the signature of the provided method.
     */
    public static String generateJvmMethodSignature(Method method) {
        return generateJvmMethodSignature(method.getParameters(), method.getReturnType());
    }

    /**
     * Generates a classname favored by the JVM.
     * Basically gets the fully qualified classname of the given class, and replaces all periods with forward slashes.
     * @param clazz The Java class to generate the jvm class name for.
     * @return A String with the fully qualified JVM representation of the class name.
     */
    public static String jvmClassname(final Class<?> clazz) {
        return clazz.getName().replace('.', '/');
    }

    /**
     * Produces a JVM type definition for a given class type.
     * Primitive types have type references that are one letter long.
     * For example, byte and int are B and I respectively.
     * For non-primitive classes, the definition is LjvmClassname;.
     * For array types, the definition is the same EXCEPT you add a [ to the front.
     * @param clazz The class to produce the JVM type definition for.
     * @return A String representation of the JVM type definition of the provided class.
     */
    public static String jvmTypeDefinition(final Class<?> clazz) {
        if(clazz.isArray()) {
            final Class<?> componentClass = clazz.getComponentType();
            return "[" + jvmTypeDefinition(componentClass);
        }

        return TYPE_MAPPINGS.getOrDefault(clazz, "L" + jvmClassname(clazz) + ";");
    }

    private static String generateJvmMethodSignature(final Parameter[] parameters, final Class<?> returnType) {
        List<Class<?>> parameterTypes = Arrays.stream(parameters)
            .map(Parameter::getType)
            .collect(Collectors.toList());
        return generateJvmMethodSignature(parameterTypes, returnType);
    }

    /**
     * Generates the JVM method signature of a method with the given list of parameter types and the given return type.
     * This is the method to call to generate a method signature without having an actual Method object.
     * @param parameterTypes An ordered list of method parameter types.
     * @param returnType The return type of the method.
     * @return A String containing the signature of a method with the given parameter and return types.
     */
    public static String generateJvmMethodSignature(final List<Class<?>> parameterTypes, final Class<?> returnType) {
        final StringBuilder builder = new StringBuilder("(");
        for(final Class<?> paramType: parameterTypes) {
            builder.append(jvmTypeDefinition(paramType));
        }
        builder.append(')');
        builder.append(jvmTypeDefinition(returnType));
        return builder.toString();
    }

    /**
     * Given a field, attempts to find an accessor method meeting the following criteria:
     *   - is/get method (isFieldName or getFieldName where the name of the field is fieldName or FieldName)
     *   - Zero parameters
     *   - Return type is not void
     *   - Method is not static
     *   - Method is public
     *
     * @param type The class in which to search for methods.
     * @param field The field to use when finding a getter method.
     * @return Returns a Method which meets the above criteria as a getter method for the provided field, or null if
     * none could be found.
     */
    public static Method findGetterMethodForField(Class<?> type, Field field) {
        String fieldName = field.getName();
        String methodName = (field.getType() == boolean.class ? "is" : "get") + StringUtils.capitalize(fieldName);

        log.info("Attempting to find getter method named " + methodName);
        Method getterMethod = MethodUtils.getMatchingAccessibleMethod(type, methodName);

        boolean valid = isValidGetterMethod(getterMethod, methodName);
        return valid ? getterMethod : null;
    }

    /**
     * Given a method, determines if that method is a valid getter method.
     * @param getterMethod The method to test.
     * @param methodName The name of the method, for logging purposes when getterMethod is null.
     * @return True if the provided method meets the criteria for being a valid getter method. False otherwise.
     */
    public static boolean isValidGetterMethod(Method getterMethod, String methodName) {
        String reason = getInvalidGetterMethodReason(getterMethod, methodName);
        if(reason == null) {
            log.info("Found getter method named " + getterMethod.getName());
            return true;
        }
        else {
            log.info(reason);
            return false;
        }
    }

    /**
     * Given a field, attempts to find a setter method meeting the following criteria:
     *   - set method (setFieldName where the name of the field is fieldName or FieldName)
     *   - Exactly 1 parameter matching the type of tje foe;d
     *   - No return value (void method)
     *   - Method is public
     *   - Method is not static
     *
     * @param type The class to search in for the setter method for the field.
     * @param field The field for whom to find a valid setter method.
     * @return Null if no setter method is found for the given field, otherwise the found setter method is returned.
     */
    public static Method findSetterMethodForField(Class<?> type, Field field) {
        Class<?> fieldType = field.getType();
        String fieldName = field.getName();
        String methodName = "set" + StringUtils.capitalize(fieldName);

        log.info("Attempting to find setter method named " + methodName);
        Method setterMethod = MethodUtils.getMatchingAccessibleMethod(type, methodName, fieldType);

        boolean valid = isValidSetterMethod(setterMethod, methodName);

        return valid ? setterMethod : null;
    }

    /**
     * Determines if a setter method is actually a valid setter method by meeting the criteria of a setter method.
     * @param setterMethod The method to test.
     * @return True if the method is a valid setter method. False otherwise.
     * @see #isValidSetterMethod(Method, String)
     * @see #getInvalidSetterMethodReason(Method)
     * @see #getInvalidSetterMethodReason(Method, String)
     */
    public static boolean isValidSetterMethod(Method setterMethod) {
        return setterMethod != null && isValidSetterMethod(setterMethod, setterMethod.getName());
    }

    /**
     * Determines if a setter method is actually a valid setter method by meeting the criteria of a setter method.
     * @param setterMethod The method to test.
     * @param setterName The name of the setter method, for logging purposes when setterMethod is null.
     * @return True if the method is a valid setter method. False otherwise.
     * @see #isValidSetterMethod(Method)
     * @see #getInvalidSetterMethodReason(Method)
     * @see #getInvalidSetterMethodReason(Method, String)
     */
    public static boolean isValidSetterMethod(Method setterMethod, String setterName) {
        String reason = getInvalidSetterMethodReason(setterMethod, setterName);
        if(reason == null) {
            log.info("Found setter method named " + setterName);
            return true;
        }
        else {
            log.info(reason);
            return false;
        }
    }

    /**
     * Determines if a method is a valid setter method, and determines why said method is an invalid setter method if
     * the method is not a valid setter.
     * @param setterMethod The method to test.
     * @return Null if the provided method is a valid setter method, otherwise a String explaining why the provided
     * method is not a valid setter method is returned.
     * @see #isValidSetterMethod(Method)
     * @see #isValidSetterMethod(Method, String)
     * @see #getInvalidSetterMethodReason(Method, String)
     */
    public static String getInvalidSetterMethodReason(Method setterMethod) {
        if(setterMethod == null)
            return "Method is null.";
        else
            return getInvalidSetterMethodReason(setterMethod, setterMethod.getName());
    }

    /**
     * Determines if a method is a valid setter method, and determines why said method is an invalid setter method if
     * the method is not a valid setter.
     * @param setterMethod The method to test.
     * @param setterName The name of the setter method, for logging reasons since setterMethod can be null.
     * @return Null if the provided method is a valid setter method, otherwise a String explaining why the provided
     * method is not a valid setter method is returned.
     * @see #isValidSetterMethod(Method)
     * @see #isValidSetterMethod(Method, String)
     * @see #getInvalidSetterMethodReason(Method)
     */
    public static String getInvalidSetterMethodReason(Method setterMethod, String setterName) {
        if(setterMethod == null)
            return "No setter method named " + setterName + " found.";
        else if(setterMethod.getReturnType() != void.class)
            return "Setter method " + setterName + " must have a void return type.";
        else if(Modifier.isStatic(setterMethod.getModifiers())) {
            return "Setter method " + setterName + " cannot be static.";
        }
        else if(!Modifier.isPublic(setterMethod.getModifiers()))
            return "Setter method " + setterName + " must be public.";
        else
            return null;
    }

    /**
     * Determines if a method is a valid getter method, and determines why said method is an invalid getter
     * method if the method is not a valid getter.
     * @param getterMethod The method to test.
     * @return Null if the provided getterMethod is a valid getterMethod, otherwise a String explaining why the
     * provided method is not a valid getter method is returned.
     * @see #isValidGetterMethod(Method, String)
     * @see #getInvalidGetterMethodReason(Method, String)
     */
    public static String getInvalidGetterMethodReason(Method getterMethod) {
        if(getterMethod == null)
            return "Method is null.";
        else
            return getInvalidGetterMethodReason(getterMethod, getterMethod.getName());
    }

    /**
     * Determines if a method is a valid getter method, and determines why said method is an invalid getter
     * method if the method is not a valid getter.
     * @param getterMethod The method to test.
     * @param methodName The name of the getter method, for logging reasons since getterMethod can be null.
     * @return Null if the provided getterMethod is a valid getterMethod, otherwise a String explaining why the
     * provided method is not a valid getter method is returned.
     * @see #isValidGetterMethod(Method, String)
     * @see #getInvalidGetterMethodReason(Method)
     */
    public static String getInvalidGetterMethodReason(Method getterMethod, String methodName) {
        if(getterMethod == null)
            return "No getter method named " + methodName + " found.";
        else if(getterMethod.getReturnType() == void.class)
            return "Getter method " + methodName + " cannot be void.";
        else if(Modifier.isStatic(getterMethod.getModifiers()))
            return "Getter method " + methodName + " cannot be static.";
        else if(!Modifier.isPublic(getterMethod.getModifiers()))
            return "Getter method " + methodName + " must be public.";
        else if(getterMethod.getParameters().length > 0)
            return "Getter method " + methodName + " must not contain any parameters.";
        else
            return null;
    }

    /**
     * Attempts to determine the name of a field based on the name of a method. This method will simply cut off the
     * "get", "set", or "is" prefix from a method name. The resulting text, after being un-capitalized, is the field
     * name that is returned.
     * @param method The method to determine a field name from.
     * @return Returns the name of the field being accessed by the given accessor method.
     */
    public static String fieldNameFromMethodName(Method method) {
        String methodName = method.getName();
        if(methodName.startsWith("get") || methodName.startsWith("set"))
            methodName = methodName.substring(3);
        else if(methodName.startsWith("is"))
            methodName = methodName.substring(2);

        return StringUtils.uncapitalize(methodName);
    }
}