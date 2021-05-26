package io.github.cshunsinger.japplicator.builder;

import io.github.cshunsinger.asmsauce.code.CodeInsnBuilderLike;

import static io.github.cshunsinger.asmsauce.code.CodeBuilders.getVar;

public class ValueConverters {
    public static CodeInsnBuilderLike createValueConverter(String sourceLocalVar, Class<?> sourceType, Class<?> destType) {
        //If source and destination types are the same then no conversion is necessary
        if(sourceType == destType)
            return getVar(sourceLocalVar);

        CodeInsnBuilderLike convertedValueBuilder = ArrayValueConverter.createValueConverter(sourceLocalVar, sourceType, destType);
        if(convertedValueBuilder != null)
            return convertedValueBuilder;

        //Throws an exception if sourceType cannot be converted into destType
        return SingleValueConverter.createValueConverter(getVar(sourceLocalVar), sourceType, destType);
    }
}