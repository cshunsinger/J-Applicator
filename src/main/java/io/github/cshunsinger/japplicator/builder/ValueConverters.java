package io.github.cshunsinger.japplicator.builder;

import io.github.cshunsinger.asmsauce.code.CodeInsnBuilderLike;

public class ValueConverters {
    public static CodeInsnBuilderLike createValueConverter(CodeInsnBuilderLike valueBuilder, Class<?> sourceType, Class<?> destType) {
        if(sourceType.isArray() && destType.isArray())
            return ArrayValueConverter.createValueConverter(valueBuilder, sourceType, destType);
        else
            return SingleValueConverter.createValueConverter(valueBuilder, sourceType, destType);
    }
}