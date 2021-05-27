package io.github.cshunsinger.japplicator.builder;

import io.github.cshunsinger.asmsauce.code.CodeInsnBuilderLike;
import io.github.cshunsinger.japplicator.exception.TypeVariableUnsupportedException;
import io.github.cshunsinger.japplicator.exception.WildcardTypeUnsupportedException;

import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;

import static io.github.cshunsinger.asmsauce.code.CodeBuilders.getVar;

public class ValueConverters {
    public static CodeInsnBuilderLike createValueConverter(String sourceLocalVar, Type sourceType, Type destType) throws WildcardTypeUnsupportedException, TypeVariableUnsupportedException {
        //Wildcards and variable generic types are not supported
        if(sourceType instanceof WildcardType || destType instanceof WildcardType)
            throw new WildcardTypeUnsupportedException();
        else if(sourceType instanceof TypeVariable || destType instanceof TypeVariable)
            throw new TypeVariableUnsupportedException();

        CodeInsnBuilderLike codeBuilder;

        if((codeBuilder = CollectionValueConverter.createCollectionToCollectionValueConverter(sourceLocalVar, sourceType, destType)) != null)
            return codeBuilder;

        if((codeBuilder = ArrayValueConverter.createArrayToArrayValueConverter(sourceLocalVar, sourceType, destType)) != null)
            return codeBuilder;

        return SingleValueConverter.createSingletonValueConverter(getVar(sourceLocalVar), sourceType, destType);
    }
}