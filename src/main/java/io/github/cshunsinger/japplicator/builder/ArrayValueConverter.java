package io.github.cshunsinger.japplicator.builder;

import io.github.cshunsinger.asmsauce.code.CodeInsnBuilderLike;
import io.github.cshunsinger.japplicator.exception.TypeVariableUnsupportedException;
import io.github.cshunsinger.japplicator.exception.WildcardTypeUnsupportedException;
import org.apache.commons.lang3.reflect.TypeUtils;

import java.lang.reflect.Type;

import static io.github.cshunsinger.asmsauce.code.CodeBuilders.*;

public class ArrayValueConverter {
    public static CodeInsnBuilderLike createArrayToArrayValueConverter(String sourceArrayVar, Type sourceArrayType, Type destArrayType) throws WildcardTypeUnsupportedException, TypeVariableUnsupportedException {
        if(!TypeUtils.isArrayType(sourceArrayType) || !TypeUtils.isArrayType(destArrayType))
            return null; //This value converter method only handles when source and destination classes are both array types

        final Type sourceComponentType = TypeUtils.getArrayComponentType(sourceArrayType);
        final Type destComponentType = TypeUtils.getArrayComponentType(destArrayType);
        final Class<?> destComponentClass = TypeUtils.getRawType(destComponentType, null);
        final String sourceValue = sourceArrayVar + "Value";
        final String arrayLength = "arrayLength";
        final String arrayIndex = "arrayIndex";
        final String destinationArray = "destinationArray";

        //if(sourceArrayVar.length > 0) { ... }
        return ternary(getVar(sourceArrayVar).length().gt(literal(0))).thenCalculate(
            setVar(arrayLength, getVar(sourceArrayVar).length()), //int arrayLength = sourceArrayVar.length;
            setVar(arrayIndex, literal(0)), //int arrayIndex = 0;
            setVar(destinationArray, newArray(destComponentClass, getVar(arrayLength))), //Value[] newArray = new Value[arrayLength];

            //while(arrayIndex < arrayLength) { ... }
            while_(getVar(arrayIndex).lt(getVar(arrayLength))).do_(
                //Value sourceValue = sourceArrayVar[arrayIndex];
                setVar(sourceValue, getVar(sourceArrayVar).get(getVar(arrayIndex))),

                //newArray[arrayIndex] = ...;
                getVar(destinationArray).set(getVar(arrayIndex),
                    //Convert the sourceValue into a destination value
                    ValueConverters.createValueConverter(sourceValue, sourceComponentType, destComponentType)
                ),

                //arrayIndex = arrayIndex + 1;
                setVar(arrayIndex, getVar(arrayIndex).add(literal(1)))
            ),

            //This is the new array to be returned by the ternary if-branch
            getVar(destinationArray)
        ).elseCalculate(
            //If no elements existed in the source array, then this ternary else-branch will return an empty destination array
            newArray(destComponentClass, literal(0))
        );
    }
}