package io.github.cshunsinger.japplicator.builder;

import io.github.cshunsinger.asmsauce.code.CodeInsnBuilderLike;

public class ArrayValueConverter {
    public static CodeInsnBuilderLike createValueConverter(CodeInsnBuilderLike valueBuilder, Class<?> sourceArrayType, Class<?> destArrayType) {
        if(sourceArrayType == destArrayType)
            return valueBuilder; //No conversion necessary if both arrays are of the same type

        return null;
    }
}
