package io.github.cshunsinger.japplicator.converters;

import io.github.cshunsinger.asmsauce.code.CodeInsnBuilderLike;
import io.github.cshunsinger.japplicator.exception.TypeVariableUnsupportedException;
import io.github.cshunsinger.japplicator.exception.WildcardTypeUnsupportedException;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.reflect.TypeUtils;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Collection;

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

    public static CodeInsnBuilderLike createCollectionToArrayValueConverter(String sourceLocalVar, Type sourceType, Type destType) throws WildcardTypeUnsupportedException, TypeVariableUnsupportedException {
        Class<?> sourceClass = TypeUtils.getRawType(sourceType, null);

        if(!Collection.class.isAssignableFrom(sourceClass))
            return null; //If source type is not a Collection then this value converter does not apply

        if(!(destType instanceof Class<?>))
            return null; //If destination type is not a concrete type then this value converter does not apply

        Class<?> destClass = (Class<?>)destType;
        if(!destClass.isArray())
            return null; //If destination class is not an array class then this value converter does not apply

        //Get the component type
        Type sourceComponentType = ((ParameterizedType)sourceType).getActualTypeArguments()[0];
        Class<?> sourceComponentClass = TypeUtils.getRawType(sourceComponentType, null);
        if(sourceComponentClass == null)
            sourceComponentClass = Object.class;
        Class<?> destComponentClass = destClass.getComponentType();

        //Some new local variable names
        final String destArrayVar = "arrayFrom" + StringUtils.capitalize(sourceLocalVar);
        final String sourceValue = sourceLocalVar + "Value";
        final String iterator = sourceLocalVar + "Iterator";
        final String counter = sourceLocalVar + "Counter";
        final String length = sourceLocalVar + "Length";

        //!sourceLocalVar.isEmpty() ? <thenCalculate> : <elseCalculate>
        return ternary(getVar(sourceLocalVar).invoke("isEmpty").isFalse())
            .thenCalculate(
                setVar(length, getVar(sourceLocalVar).invoke("size")), //int length = sourceLocalVar.size();
                setVar(destArrayVar, newArray(destComponentClass, getVar(length))), //DestType[] destArrayVar = new DestType[sourceLocalVar.size()]
                setVar(counter, literal(0)), //int counter = 0;
                setVar(iterator, getVar(sourceLocalVar).invoke("iterator")), //Iterator<SrcType> iterator = sourceLocalVar.iterator();

                //while(iterator.hasNext()) { ... }
                while_(getVar(iterator).invoke("hasNext").isTrue()).do_(
                    setVar(sourceValue, cast(sourceComponentClass, getVar(iterator).invoke("next"))), //SrcValue value = iterator.next();
                    getVar(destArrayVar).set( //destArrayVar[counter] = ... converted `sourceValue` ...
                        getVar(counter),
                        ValueConverters.createValueConverter(sourceValue, sourceComponentType, destComponentClass)
                    ),
                    setVar(counter, getVar(counter).add(literal(1))) //counter = counter + 1;
                ),

                //Provide the newly generated array
                getVar(destArrayVar)
            )
            .elseCalculate( // <elseCalculate>
                //Source collection is empty therefore just create a new empty array
                newArray(destComponentClass, literal(0))
            );
    }
}