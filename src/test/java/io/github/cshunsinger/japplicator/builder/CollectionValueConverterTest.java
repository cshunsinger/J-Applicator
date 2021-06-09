package io.github.cshunsinger.japplicator.builder;

import io.github.cshunsinger.japplicator.BaseUnitTest;
import io.github.cshunsinger.japplicator.Applicator;
import io.github.cshunsinger.japplicator.annotation.FieldIdentifier;
import io.github.cshunsinger.japplicator.exception.TypeConversionException;
import io.github.cshunsinger.japplicator.exception.TypeVariableUnsupportedException;
import io.github.cshunsinger.japplicator.exception.WildcardTypeUnsupportedException;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.*;

import static java.util.Collections.emptyList;
import static org.junit.jupiter.api.Assertions.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.when;

public class CollectionValueConverterTest extends BaseUnitTest {
    @Mock
    private ParameterizedType mockSourceParameterizedType;
    @Mock
    private ParameterizedType mockDestParameterizedType;

    @Test
    public void doNotCreateValueConverterForNonCollectionTypes() throws Exception {
        assertNull(CollectionValueConverter.createCollectionToCollectionValueConverter("", String.class, String.class));
        assertNull(CollectionValueConverter.createCollectionToCollectionValueConverter("", List.class, String.class));
        assertNull(CollectionValueConverter.createCollectionToCollectionValueConverter("", String.class, List.class));
    }

    /**
     * This is a collection type used for testing. It does not contain a no-args constructor and therefore bytecode
     * cannot be generated to instantiate it.
     * @param <T>
     */
    private static class BadConcreteCollection<T> extends ArrayList<T> {
        @SuppressWarnings("unused")
        public BadConcreteCollection(String hereIsAnArgument) {}
    }

    @Test
    public void typeConversionException_collectionTypeDoesNotContainNoArgsConstructor() {
        when(mockSourceParameterizedType.getActualTypeArguments()).thenReturn(new Type[] { String.class });
        when(mockSourceParameterizedType.getRawType()).thenReturn(BadConcreteCollection.class);
        when(mockDestParameterizedType.getActualTypeArguments()).thenReturn(new Type[] { String.class });
        when(mockDestParameterizedType.getRawType()).thenReturn(BadConcreteCollection.class);

        assertThrows(
            TypeConversionException.class,
            () -> CollectionValueConverter.createCollectionToCollectionValueConverter("", mockSourceParameterizedType, mockDestParameterizedType)
        );
    }

    /**
     * Test class to act as a source of a simple collection for tests involving simple collections being copied into
     * other simple collections.
     */
    @AllArgsConstructor
    @Getter @Setter
    public static class SourceWithSimpleCollection {
        @FieldIdentifier
        private List<Integer> list;
    }

    /**
     * Test class to test the simple copying of list elements from a source class, without the need to convert any
     * of the elements of the collection first.
     */
    @Getter @Setter
    public static class DestinationWithSimpleIntCollection {
        @FieldIdentifier
        private List<Integer> list;
    }

    /**
     * Test class to test conversion of collection element types from one collection to another.
     * In this case, elements of one type in a source collection must be converted into Strings and placed
     * into the List in this class.
     */
    @Getter @Setter
    public static class DestinationWithSimpleStringCollection {
        @FieldIdentifier
        private Set<String> list;
    }

    @Test
    public void valuesFromSourceCollectionShouldBeCopiedIntoNewDestinationCollection() {
        ApplicatorBuilder<SourceWithSimpleCollection, DestinationWithSimpleIntCollection> builder =
            new ApplicatorBuilder<>(SourceWithSimpleCollection.class, DestinationWithSimpleIntCollection.class);
        Applicator<SourceWithSimpleCollection, DestinationWithSimpleIntCollection> applicator = assertDoesNotThrow(builder::build);

        SourceWithSimpleCollection source = new SourceWithSimpleCollection(List.of(1, 2, 3, 4, 5, 6, 7, 8, 9, 10));
        DestinationWithSimpleIntCollection result = applicator.apply(source, null);

        assertThat(result, hasProperty("list", allOf(
            notNullValue(),
            instanceOf(ArrayList.class),
            hasItems(1, 2, 3, 4, 5, 6, 7, 8, 9, 10))
        ));
    }

    @Test
    public void valuesFromSourceCollectionShouldBeGivenTypeConversionForDestinationCollection() {
        ApplicatorBuilder<SourceWithSimpleCollection, DestinationWithSimpleStringCollection> builder =
            new ApplicatorBuilder<>(SourceWithSimpleCollection.class, DestinationWithSimpleStringCollection.class);
        Applicator<SourceWithSimpleCollection, DestinationWithSimpleStringCollection> applicator = assertDoesNotThrow(builder::build);

        SourceWithSimpleCollection source = new SourceWithSimpleCollection(List.of(1, 2, 3, 4, 5, 6, 7, 8, 9, 10));
        DestinationWithSimpleStringCollection result = applicator.apply(source, null);

        assertThat(result, hasProperty("list", allOf(
            notNullValue(),
            instanceOf(HashSet.class),
            hasItems("1", "2", "3", "4", "5", "6", "7", "8", "9", "10"))
        ));
    }

    @Test
    public void doNotInstantiateNewCollectionInDestinationObjectIfSourceObjectHasNullValueForCollection() {
        ApplicatorBuilder<SourceWithSimpleCollection, DestinationWithSimpleIntCollection> builder =
            new ApplicatorBuilder<>(SourceWithSimpleCollection.class, DestinationWithSimpleIntCollection.class);
        Applicator<SourceWithSimpleCollection, DestinationWithSimpleIntCollection> applicator = assertDoesNotThrow(builder::build);

        SourceWithSimpleCollection nullSource = new SourceWithSimpleCollection(null);

        DestinationWithSimpleIntCollection nullResult = applicator.apply(nullSource, null);

        assertThat(nullResult, hasProperty("list", nullValue()));
    }

    @Test
    public void instantiateNewEmptyCollectionInDestinationObjectIfSourceObjectHasEmptyCollection() {
        ApplicatorBuilder<SourceWithSimpleCollection, DestinationWithSimpleIntCollection> builder =
            new ApplicatorBuilder<>(SourceWithSimpleCollection.class, DestinationWithSimpleIntCollection.class);
        Applicator<SourceWithSimpleCollection, DestinationWithSimpleIntCollection> applicator = assertDoesNotThrow(builder::build);

        SourceWithSimpleCollection emptySource = new SourceWithSimpleCollection(emptyList());

        DestinationWithSimpleIntCollection emptyResult = applicator.apply(emptySource, null);

        assertThat(emptyResult, hasProperty("list", allOf(
            notNullValue(),
            empty(),
            instanceOf(ArrayList.class)
        )));
    }

    @Getter @Setter
    @AllArgsConstructor
    public static class SourceWithNestedCollections {
        @FieldIdentifier
        private List<List<Integer>> nestedLists;
    }

    @Getter @Setter
    @NoArgsConstructor
    public static class DestinationWithNestedIntCollections {
        @FieldIdentifier("nestedLists")
        private Set<Set<Integer>> nestedSets;
    }

    @Getter @Setter
    @NoArgsConstructor
    public static class DestinationWithNestedLongCollections {
        @FieldIdentifier("nestedLists")
        private List<Set<Long>> nestedSets;
    }

    @Test
    public void convertSourceCollectionOfCollectionsIntoDestinationCollectionOfCollections_noElementTypeConversion() {
        ApplicatorBuilder<SourceWithNestedCollections, DestinationWithNestedIntCollections> builder =
            new ApplicatorBuilder<>(SourceWithNestedCollections.class, DestinationWithNestedIntCollections.class);
        Applicator<SourceWithNestedCollections, DestinationWithNestedIntCollections> applicator = assertDoesNotThrow(builder::build);

        SourceWithNestedCollections source = new SourceWithNestedCollections(
            List.of(
                List.of(1, 2, 3),
                List.of(4, 5, 6),
                List.of(7, 8, 9, 10)
            )
        );

        DestinationWithNestedIntCollections result = applicator.apply(source, null);
        assertThat(result, hasProperty("nestedSets", allOf(
            notNullValue(),
            instanceOf(HashSet.class),
            containsInAnyOrder(
                allOf(
                    instanceOf(HashSet.class),
                    containsInAnyOrder(1, 2, 3)
                ),
                allOf(
                    instanceOf(HashSet.class),
                    containsInAnyOrder(4, 5, 6)
                ),
                allOf(
                    instanceOf(HashSet.class),
                    containsInAnyOrder(7, 8, 9, 10)
                )
            )
        )));
    }

    @Test
    public void convertSourceCollectionOfCollectionsIntoDestinationCollectionOfCollections_withElementTypeConversion() {
        ApplicatorBuilder<SourceWithNestedCollections, DestinationWithNestedLongCollections> builder =
            new ApplicatorBuilder<>(SourceWithNestedCollections.class, DestinationWithNestedLongCollections.class);
        Applicator<SourceWithNestedCollections, DestinationWithNestedLongCollections> applicator = assertDoesNotThrow(builder::build);

        SourceWithNestedCollections source = new SourceWithNestedCollections(
            List.of(
                List.of(1, 2, 3),
                List.of(4, 5, 6),
                List.of(7, 8, 9, 10)
            )
        );

        DestinationWithNestedLongCollections result = applicator.apply(source, null);
        assertThat(result, hasProperty("nestedSets", allOf(
            notNullValue(),
            instanceOf(ArrayList.class),
            contains(
                allOf(
                    instanceOf(HashSet.class),
                    containsInAnyOrder(1L, 2L, 3L)
                ),
                allOf(
                    instanceOf(HashSet.class),
                    containsInAnyOrder(4L, 5L, 6L)
                ),
                allOf(
                    instanceOf(HashSet.class),
                    containsInAnyOrder(7L, 8L, 9L, 10L)
                )
            )
        )));
    }

    @Getter @Setter
    @AllArgsConstructor
    public static class SourceConcreteCollection {
        @FieldIdentifier
        private LinkedList<Integer> list;
    }

    @Test
    public void useSourceConcreteTypeWhenAssignableToNonConcreteDestinationType() {
        ApplicatorBuilder<SourceConcreteCollection, DestinationWithSimpleIntCollection> builder =
            new ApplicatorBuilder<>(SourceConcreteCollection.class, DestinationWithSimpleIntCollection.class);
        Applicator<SourceConcreteCollection, DestinationWithSimpleIntCollection> applicator = assertDoesNotThrow(builder::build);

        SourceConcreteCollection source = new SourceConcreteCollection(new LinkedList<>(List.of(1, 2, 3, 4, 5)));
        DestinationWithSimpleIntCollection result = applicator.apply(source, null);

        assertThat(result, hasProperty("list", allOf(
            notNullValue(),
            instanceOf(LinkedList.class),
            contains(1, 2, 3, 4, 5)
        )));
    }

    @Getter @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ConcreteCollectionTest {
        @FieldIdentifier
        private Set<Object> setData;
    }

    @Getter @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class SourceWildcardCollection {
        @FieldIdentifier("setData")
        private Set<?> wildcardSet;
    }

    @Getter @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class DestWildcardCollection {
        @FieldIdentifier("setData")
        private Set<?> wildcardSet;
    }

    @Getter @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class SourceGenericCollection<T> {
        @FieldIdentifier("setData")
        private Set<T> wildcardSet;
    }

    @Getter @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class DestGenericCollection<T> {
        @FieldIdentifier("setData")
        private Set<T> wildcardSet;
    }

    @Test
    public void throwExceptionWhenSourceCollectionUsesWildcardParameter() {
        TypeConversionException ex = assertThrows(
            TypeConversionException.class,
            () -> new ApplicatorBuilder<>(ConcreteCollectionTest.class, DestWildcardCollection.class)
        );

        assertThat(ex, hasProperty("cause", instanceOf(WildcardTypeUnsupportedException.class)));
    }

    @Test
    public void throwExceptionWhenDestinationCollectionUsesWildcardParameter() {
        TypeConversionException ex = assertThrows(
            TypeConversionException.class,
            () -> new ApplicatorBuilder<>(SourceWildcardCollection.class, ConcreteCollectionTest.class)
        );

        assertThat(ex, hasProperty("cause", instanceOf(WildcardTypeUnsupportedException.class)));
    }

    @Test
    public void throwExceptionWhenSourceAndDestinationCollectionsUseWildcardParameters() {
        TypeConversionException ex = assertThrows(
            TypeConversionException.class,
            () -> new ApplicatorBuilder<>(SourceWildcardCollection.class, DestWildcardCollection.class)
        );

        assertThat(ex, hasProperty("cause", instanceOf(WildcardTypeUnsupportedException.class)));
    }

    @Test
    public void throwExceptionWhenSourceCollectionUsesGenericTypeVariable() {
        TypeConversionException ex = assertThrows(
            TypeConversionException.class,
            () -> new ApplicatorBuilder<>(SourceGenericCollection.class, ConcreteCollectionTest.class)
        );

        assertThat(ex, hasProperty("cause", instanceOf(TypeVariableUnsupportedException.class)));
    }

    @Test
    public void throwExceptionWhenDestinationCollectionUsesTypeVariable() {
        TypeConversionException ex = assertThrows(
            TypeConversionException.class,
            () -> new ApplicatorBuilder<>(ConcreteCollectionTest.class, DestGenericCollection.class)
        );

        assertThat(ex, hasProperty("cause", instanceOf(TypeVariableUnsupportedException.class)));
    }

    @Test
    public void throwExceptionWhenSourceAndDestinationCollectionsUseTypeVariables() {
        TypeConversionException ex = assertThrows(
            TypeConversionException.class,
            () -> new ApplicatorBuilder<>(SourceGenericCollection.class, DestGenericCollection.class)
        );

        assertThat(ex, hasProperty("cause", instanceOf(TypeVariableUnsupportedException.class)));
    }
}