package io.github.cshunsinger.japplicator.converters;

import io.github.cshunsinger.asmsauce.code.CodeInsnBuilderLike;
import io.github.cshunsinger.asmsauce.definitions.TypeDefinition;
import io.github.cshunsinger.japplicator.builder.AsmUtils;
import io.github.cshunsinger.japplicator.exception.TypeConversionException;
import io.github.cshunsinger.japplicator.exception.TypeVariableUnsupportedException;
import io.github.cshunsinger.japplicator.exception.WildcardTypeUnsupportedException;
import org.apache.commons.lang3.reflect.TypeUtils;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.*;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;

import static io.github.cshunsinger.asmsauce.DefinitionBuilders.type;
import static io.github.cshunsinger.asmsauce.code.CodeBuilders.*;
import static java.util.Map.entry;

public class CollectionValueConverter {
    /**
     * List of entries for collection interface types, and the concrete type to instantiate by default.
     * The keys in the list of entries should be ordered from less generic to more generic, with Collection.class
     * being the last key since it is the most generic Collection covered by this class.
     */
    private static final List<Map.Entry<TypeDefinition, TypeDefinition>> DEFAULT_COLLECTIONS = List.of(
        entry(type(BlockingDeque.class), type(LinkedBlockingDeque.class)),
        entry(type(BlockingQueue.class), type(ArrayBlockingQueue.class)),
        entry(type(Queue.class), type(ArrayDeque.class)),
        entry(type(Set.class), type(HashSet.class)),
        entry(type(List.class), type(ArrayList.class)),
        entry(type(Collection.class), type(ArrayList.class))
    );

    public static CodeInsnBuilderLike createCollectionToCollectionValueConverter(String sourceLocalVar, Type sourceType, Type destType) throws WildcardTypeUnsupportedException, TypeVariableUnsupportedException {
        Class<?> sourceClass = TypeUtils.getRawType(sourceType, null);
        Class<?> destClass = TypeUtils.getRawType(destType, null);

        if(!Collection.class.isAssignableFrom(sourceClass) || !Collection.class.isAssignableFrom(destClass))
            return null; //This method only handles conversions from one collection to another

        //Because both types are Collection types, exactly 1 type argument will exist.
        //Both types also cannot be GenericArrayType because they are singleton collections.
        Type sourceElementType = ((ParameterizedType)sourceType).getActualTypeArguments()[0];
        Type destElementType = ((ParameterizedType)destType).getActualTypeArguments()[0];

        //Determine the type of collection type which can be instantiated
        TypeDefinition concreteCollectionType = determineCollectionType(type(sourceClass), type(destClass));
        validateNewCollectionType(concreteCollectionType, sourceType, destType);

        Class<?> sourceElementClass = TypeUtils.getRawType(sourceElementType, null);
        if(sourceElementClass == null)
            sourceElementClass = Object.class;

        //Local variable names
        final String sourceValue = sourceLocalVar + "Value";
        final String newCollection = sourceLocalVar + "NewCollection";
        final String iterator = sourceLocalVar + "Iterator";

        //!sourceLocalVar.isEmpty() ? <thenCalculate> : <elseCalculate>
        return ternary(getVar(sourceLocalVar).invoke("isEmpty").isFalse())
            .thenCalculate( // <thenCalculate>
                setVar(newCollection, instantiate(concreteCollectionType)), //CollectionType<DestType> newCollection = new CollectionType<>();
                setVar(iterator, getVar(sourceLocalVar).invoke("iterator")), //Iterator<SrcType> iterator = sourceLocalVar.iterator();

                //while(iterator.hasNext()) { ... }
                while_(getVar(iterator).invoke("hasNext").isTrue()).do_(
                    setVar(sourceValue, cast(sourceElementClass, getVar(iterator).invoke("next"))), //Value sourceValue = (Value)iterator.next();
                    getVar(newCollection).invoke("add", ValueConverters.createValueConverter(sourceValue, sourceElementType, destElementType))
                ),

                //Provide/"return" the newCollection from this side of the ternary statement
                getVar(newCollection)
            )
            .elseCalculate( // <elseCalculate>
                //Source collection is empty so create a new empty collection for the destination field
                instantiate(concreteCollectionType)
            );
    }

    public static CodeInsnBuilderLike createArrayToCollectionValueConverter(String sourceLocalVar, Type sourceType, Type destType) throws WildcardTypeUnsupportedException, TypeVariableUnsupportedException {
        Class<?> sourceClass = TypeUtils.getRawType(sourceType, null);
        Class<?> destinationClass = TypeUtils.getRawType(destType, null);

        if(!sourceClass.isArray() || !Collection.class.isAssignableFrom(destinationClass))
            return null; //This converter only works for array -> collection type

        //Determine the source and destination component/element types
        Class<?> sourceElementClass = sourceClass.getComponentType();
        Type destElementType = ((ParameterizedType)destType).getActualTypeArguments()[0];

        //Determine the type of collection type which can be instantiated
        TypeDefinition concreteCollectionType = determineCollectionType(type(sourceClass), type(destinationClass));
        validateNewCollectionType(concreteCollectionType, sourceType, destType);

        //Local variable names
        final String sourceValue = sourceLocalVar + "Value";
        final String newCollection = "collectionFrom" + sourceLocalVar;
        final String length = sourceLocalVar + "Length";
        final String counter = sourceLocalVar + "Counter";

        //!sourceLocalVar.length > 0 ? <thenCalculate> : <elseCalculate>
        return ternary(getVar(sourceLocalVar).length().gt(literal(0)))
            .thenCalculate( // <thenCalculate>
                setVar(length, getVar(sourceLocalVar).length()), //int length = sourceLocalVar.length;
                setVar(counter, literal(0)), //int counter = 0;
                setVar(newCollection, instantiate(concreteCollectionType)), //Collection newCollection = new CollectionType()

                //while(counter < length) { ... }
                while_(getVar(counter).lt(getVar(length))).do_(
                    setVar(sourceValue, getVar(sourceLocalVar).get(getVar(counter))), //SrcType sourceValue = sourceLocalVar[counter];
                    getVar(newCollection).invoke("add", //newCollection.add(...)
                        ValueConverters.createValueConverter(sourceValue, sourceElementClass, destElementType)
                    ),

                    setVar(counter, getVar(counter).add(literal(1))) //counter = counter + 1;
                ),

                getVar(newCollection)
            )
            .elseCalculate( // <elseCalculate>
                //Source array is empty so create a new empty collection
                instantiate(concreteCollectionType)
            );
    }

    private static void validateNewCollectionType(TypeDefinition concreteCollectionType, Type sourceType, Type destType) {
        if(!AsmUtils.containsEmptyConstructor(concreteCollectionType.getType())) {
            //Throw exception because the desired collection type cannot be instantiated
            String badCollectionReason = "Collection type %s does not have a no-args constructor."
                .formatted(concreteCollectionType.getType().getName());
            throw new TypeConversionException(badCollectionReason, sourceType, destType, null);
        }
    }

    @SuppressWarnings("OptionalGetWithoutIsPresent")
    private static TypeDefinition determineCollectionType(TypeDefinition sourceType, TypeDefinition destType) {
        if(destType.isConcreteClass()) {
            //If destType is a concrete class then it is an ideal candidate.
            return destType;
        }

        if(sourceType.isConcreteClass() && destType.isAssignableFrom(sourceType)) {
            //If sourceType is a concrete class and can be assigned to the destination then use that type
            return sourceType;
        }

        //Neither the source nor destination collection types are concrete. Must find at least one concrete type.
        return DEFAULT_COLLECTIONS.stream()
            .filter(entry -> destType.isAssignableFrom(entry.getKey()))
            .map(Map.Entry::getValue)
            .findFirst()
            .get(); //Guaranteed to have a value
    }
}