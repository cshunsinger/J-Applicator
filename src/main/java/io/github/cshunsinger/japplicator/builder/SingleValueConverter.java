package io.github.cshunsinger.japplicator.builder;

import io.github.cshunsinger.asmsauce.code.CodeInsnBuilderLike;

import java.lang.reflect.Type;

import static io.github.cshunsinger.asmsauce.DefinitionBuilders.*;
import static io.github.cshunsinger.asmsauce.code.CodeBuilders.cast;
import static io.github.cshunsinger.asmsauce.code.CodeBuilders.invokeStatic;
import static org.apache.commons.lang3.ClassUtils.*;

/**
 * This class contains the logic for generating bytecode to convert from one type of single value to another type of single
 * value.
 * The generated bytecode will cast primitives, autobox/unbox primitives to and from wrappers, and cast
 */
public class SingleValueConverter {
    public static CodeInsnBuilderLike createSingletonValueConverter(CodeInsnBuilderLike valueBuilder, Type sourceType, Type destType) {
        if(!(sourceType instanceof Class) || !(destType instanceof Class))
            return null; //This converter only deals with classes, not types

        Class<?> sourceClass = (Class<?>)sourceType;
        Class<?> destClass = (Class<?>)destType;

        //If the source and destination types are both primitive or wrapper types then they can be converted
        if(isPrimitiveOrWrapper(sourceClass) && isPrimitiveOrWrapper(destClass))
            return numericToNumericConversionStep(valueBuilder, sourceClass, destClass);

        //If the source is a primitive, whose wrapper can be assigned to the destination type, then that conversion can also happen
        if(sourceClass.isPrimitive()) {
            Class<?> sourceWrapper = primitiveToWrapper(sourceClass);
            if(destClass.isAssignableFrom(sourceWrapper))
                return cast(destClass, numericToNumericConversionStep(valueBuilder, sourceClass, sourceWrapper));
        }

        //If the source type is assignable to the destination type then standard casting can be used
        if(destClass.isAssignableFrom(sourceClass))
            return cast(destClass, valueBuilder);

        //If the destination type is assignable from String then it is always possible to convert the source value
        if(destClass.isAssignableFrom(String.class))
            return toStringConversionStep(valueBuilder, sourceClass, destClass);

        //If this line is reached, type conversion was not possible
        return null;
    }

    /**
     * Any value can be converted into a String or any type assignable-from String.
     * All primitives have a String form.
     * All objects have toString.
     */
    private static CodeInsnBuilderLike toStringConversionStep(CodeInsnBuilderLike valueBuilder, Class<?> sourceType, Class<?> destType) {
        //Anything can be converted into a String
        if(sourceType.isPrimitive()) {
            Class<?> wrapperType = primitiveToWrapper(sourceType);
            valueBuilder = invokeStatic(wrapperType, name("toString"), parameters(sourceType), type(String.class), valueBuilder);
        }
        else if(sourceType == char[].class)
            valueBuilder = invokeStatic(String.class, name("valueOf"), parameters(char[].class), type(String.class), valueBuilder);
        else
            valueBuilder = invokeStatic(String.class, name("valueOf"), parameters(Object.class), type(String.class), valueBuilder);

        if(destType == String.class)
            return valueBuilder; //No additional casting needed
        else
            return cast(destType, valueBuilder);
    }

    /**
     * Any primitive type can be cast straight into any other primitive type.
     */
    private static CodeInsnBuilderLike primitiveToPrimitiveConversionStep(CodeInsnBuilderLike valueBuilder, Class<?> sourcePrimitive, Class<?> destPrimitive) {
        if(sourcePrimitive == destPrimitive)
            return valueBuilder;
        else
            return cast(destPrimitive, valueBuilder);
    }

    /**
     * Any wrapper value can be converted into any primitive type using a combination of unboxing and casting.
     */
    private static CodeInsnBuilderLike wrapperToPrimitiveConversionStep(CodeInsnBuilderLike valueBuilder, Class<?> sourceWrapper, Class<?> destPrimitive) {
        Class<?> sourcePrimitive = wrapperToPrimitive(sourceWrapper);
        return primitiveToPrimitiveConversionStep(cast(sourcePrimitive, valueBuilder), sourcePrimitive, destPrimitive);
    }

    /**
     * Any primitive type can be converted into any wrapper type using a combination of casting and boxing.
     */
    private static CodeInsnBuilderLike primitiveToWrapperConversionStep(CodeInsnBuilderLike valueBuilder, Class<?> sourcePrimitive, Class<?> destWrapper) {
        Class<?> destPrimitive = wrapperToPrimitive(destWrapper);
        valueBuilder = primitiveToPrimitiveConversionStep(valueBuilder, sourcePrimitive, destPrimitive);
        return cast(destWrapper, valueBuilder);
    }

    /**
     * Any wrapper value can be converted into any other wrapper value using a combination of unboxing/boxing and casting.
     */
    private static CodeInsnBuilderLike wrapperToWrapperConversionStep(CodeInsnBuilderLike valueBuilder, Class<?> sourceWrapper, Class<?> destWrapper) {
        Class<?> destPrimitive = wrapperToPrimitive(destWrapper);
        valueBuilder = wrapperToPrimitiveConversionStep(valueBuilder, sourceWrapper, destPrimitive);
        return primitiveToWrapperConversionStep(valueBuilder, destPrimitive, destWrapper);
    }

    /**
     * Any wrapper or primitive type can be converted into any other wrapper or primitive type using a combination
     * of boxing/unboxing and/or casting.
     */
    private static CodeInsnBuilderLike numericToNumericConversionStep(CodeInsnBuilderLike valueBuilder, Class<?> sourceType, Class<?> destType) {
        if(sourceType.isPrimitive() && destType.isPrimitive())
            return primitiveToPrimitiveConversionStep(valueBuilder, sourceType, destType);
        else if(sourceType.isPrimitive())
            return primitiveToWrapperConversionStep(valueBuilder, sourceType, destType);
        else
            return wrapperToWrapperConversionStep(valueBuilder, sourceType, destType);
    }
}