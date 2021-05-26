package io.github.cshunsinger.japplicator.builder;

import io.github.cshunsinger.asmsauce.code.CodeInsnBuilderLike;

import static io.github.cshunsinger.asmsauce.code.CodeBuilders.*;

public class ArrayValueConverter {
    public static CodeInsnBuilderLike createValueConverter(String sourceArrayVar, Class<?> sourceArrayType, Class<?> destArrayType) {
        if(!sourceArrayType.isArray() || !destArrayType.isArray())
            return null; //This value converter method only handles when source and destination classes are both array types

        final Class<?> sourceComponentType = sourceArrayType.getComponentType();
        final Class<?> destComponentType = destArrayType.getComponentType();
        final String sourceValue = sourceArrayVar + "Value";
        final String arrayLength = "arrayLength";
        final String arrayIndex = "arrayIndex";
        final String destinationArray = "destinationArray";

        //if(sourceArrayVar.length > 0) { ... }
        return ternary(getVar(sourceArrayVar).length().gt(literal(0))).thenCalculate(
            setVar(arrayLength, getVar(sourceArrayVar).length()), //int arrayLength = sourceArrayVar.length;
            setVar(arrayIndex, literal(0)), //int arrayIndex = 0;
            setVar(destinationArray, newArray(destComponentType, getVar(arrayLength))), //Value[] newArray = new Value[arrayLength];

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
            newArray(destComponentType, literal(0))
        );
    }
}