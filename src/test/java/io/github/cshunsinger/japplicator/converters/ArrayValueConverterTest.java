package io.github.cshunsinger.japplicator.converters;

import io.github.cshunsinger.japplicator.Applicator;
import io.github.cshunsinger.japplicator.annotation.FieldIdentifier;
import io.github.cshunsinger.japplicator.builder.ApplicatorBuilder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

public class ArrayValueConverterTest {
    @Getter
    @RequiredArgsConstructor
    public static class TestSourceWithArray {
        @FieldIdentifier("values")
        private final int[] ints;
    }

    @Getter @Setter
    @NoArgsConstructor
    public static class TestDestinationWithArray {
        @FieldIdentifier("values")
        private int[] ints;

        @FieldIdentifier("values")
        private String[] strings;
    }

    @Test
    public void test_conversionOfArrayOfValuesIntoAnotherArrayOfValues() {
        Applicator<TestSourceWithArray, TestDestinationWithArray> applicator =
            new ApplicatorBuilder<>(TestSourceWithArray.class, TestDestinationWithArray.class).build();

        int[] testValues = new int[] {
            1, 2, 3, 4, 5, 6, 7, 8, 9, 10
        };
        TestSourceWithArray testSource = new TestSourceWithArray(testValues);

        TestDestinationWithArray result = applicator.apply(testSource, null);
        assertThat(result, allOf(
            notNullValue(),
            hasProperty("ints", is(testValues)),
            hasProperty("strings", arrayContaining("1", "2", "3", "4", "5", "6", "7", "8", "9", "10"))
        ));
    }

    @Test
    public void test_conversionOfEmptyArrayOfValuesIntoAnotherEmptyArrayOfValues() {
        Applicator<TestSourceWithArray, TestDestinationWithArray> applicator =
            new ApplicatorBuilder<>(TestSourceWithArray.class, TestDestinationWithArray.class).build();

        TestSourceWithArray testSource = new TestSourceWithArray(new int[0]);

        TestDestinationWithArray result = applicator.apply(testSource, null);
        assertThat(result.getInts().length, is(0));
        assertThat(result.getStrings().length, is(0));
    }
}