package io.github.cshunsinger.japplicator.builder;

import io.github.cshunsinger.japplicator.BaseUnitTest;
import io.github.cshunsinger.japplicator.HeadOn;
import io.github.cshunsinger.japplicator.annotation.FieldIdentifier;
import io.github.cshunsinger.japplicator.exception.TypeVariableUnsupportedException;
import io.github.cshunsinger.japplicator.exception.WildcardTypeUnsupportedException;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import java.lang.reflect.Type;
import java.util.Collection;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertNull;

public class ArrayToAndFromCollectionValueConverterTest extends BaseUnitTest {
    @Mock
    private Type mockGenericType;

    @Test
    public void collectionToArray_doNotConvertWhenSourceTypeIsNotCollection() throws WildcardTypeUnsupportedException, TypeVariableUnsupportedException {
        assertNull(ArrayValueConverter.createCollectionToArrayValueConverter("", String.class, Object[].class));
    }

    @Test
    public void collectionToArray_doNotConvertWhenDestinationTypeIsNotConcrete() throws WildcardTypeUnsupportedException, TypeVariableUnsupportedException {
        assertNull(ArrayValueConverter.createCollectionToArrayValueConverter("", Collection.class, mockGenericType));
    }

    @Test
    public void collectionToArray_doNotConvertWhenDestinationTypeIsNotArrayType() throws WildcardTypeUnsupportedException, TypeVariableUnsupportedException {
        assertNull(ArrayValueConverter.createCollectionToArrayValueConverter("", Collection.class, Object.class));
    }

    @Test
    public void arrayToCollection_doNotConvertWhenSourceTypeIsNotArray() throws WildcardTypeUnsupportedException, TypeVariableUnsupportedException {
        assertNull(CollectionValueConverter.createArrayToCollectionValueConverter("", Object.class, Object.class));
    }

    @Test
    public void arrayToCollection_doNotConvertWhenDestinationTypeIsNotCollection() throws WildcardTypeUnsupportedException, TypeVariableUnsupportedException {
        assertNull(CollectionValueConverter.createArrayToCollectionValueConverter("", Object[].class, Object.class));
    }

    @Getter @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ArrayValue {
        @FieldIdentifier
        private double[] values;
    }

    @Getter @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CollectionValue {
        @FieldIdentifier
        private List<Integer> values;
    }

    @Test
    public void convertCollectionOfValuesToArrayOfValues() {
        ApplicatorBuilder<CollectionValue, ArrayValue> builder = new ApplicatorBuilder<>(CollectionValue.class, ArrayValue.class);
        HeadOn<CollectionValue, ArrayValue> applicator = assertDoesNotThrow(builder::build);

        CollectionValue source = new CollectionValue(List.of(1, 2, 3, 4, 5));

        ArrayValue destination = applicator.applyDirectlyToTheForehead(source, null);
        assertThat(destination, allOf(
            notNullValue(),
            hasProperty("values", is(new double[] { 1.0, 2.0, 3.0, 4.0, 5.0 }))
        ));
    }

    @Test
    public void convertArrayOfValuesToCollectionOfValues() {
        ApplicatorBuilder<ArrayValue, CollectionValue> builder = new ApplicatorBuilder<>(ArrayValue.class, CollectionValue.class);
        HeadOn<ArrayValue, CollectionValue> applicator = assertDoesNotThrow(builder::build);

        ArrayValue source = new ArrayValue(new double[] {1.2, 2.3, 3.4, 4.5, 5.6});

        CollectionValue destination = applicator.applyDirectlyToTheForehead(source, null);
        assertThat(destination, allOf(
            notNullValue(),
            hasProperty("values", contains(1, 2, 3, 4, 5))
        ));
    }
}