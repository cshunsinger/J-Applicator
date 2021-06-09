package io.github.cshunsinger.japplicator.builder;

import io.github.cshunsinger.japplicator.BaseUnitTest;
import io.github.cshunsinger.japplicator.annotation.FieldIdentifier;
import io.github.cshunsinger.japplicator.Applicator;
import io.github.cshunsinger.japplicator.annotation.Nested;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

public class ApplicatorBuilderTest extends BaseUnitTest {
    /**
     * These test classes are the most basic: can one class with one field have that field set on to another class
     * that contains a field with a matching identifier???
     */

    @Getter @Setter
    public static class BasicTestFromClass {
        @FieldIdentifier("Test-String")
        private String testString;
    }

    @Getter @Setter
    public static class BasicTestToClass {
        @FieldIdentifier("Test-String")
        private String testString;
    }

    @Test
    @DisplayName("Create a new destination object when null is provided as the destination object.")
    public void createNewDestinationObjectWithValueFromOtherObject() {
        Applicator<BasicTestFromClass, BasicTestToClass> applicator = new ApplicatorBuilder<>(BasicTestFromClass.class, BasicTestToClass.class).build();

        BasicTestFromClass from = new BasicTestFromClass();
        from.setTestString("MyTestString");

        BasicTestToClass to = applicator.apply(from, null);
        assertThat(to, allOf(
            notNullValue(),
            hasProperty("testString", is("MyTestString"))
        ));
    }

    @Test
    @DisplayName("Update fields of an existing non-null destination with non-null fields from the source object.")
    public void overwriteDestinationFieldWhenSourceFieldIsNotNull() {
        Applicator<BasicTestFromClass, BasicTestToClass> applicator = new ApplicatorBuilder<>(BasicTestFromClass.class, BasicTestToClass.class).build();

        BasicTestFromClass from = new BasicTestFromClass();
        from.setTestString("NewTestString");
        BasicTestToClass to = new BasicTestToClass();
        to.setTestString("OldTestString");

        applicator.apply(from, to);
        assertThat(to, hasProperty("testString", is("NewTestString")));
    }

    @Test
    @DisplayName("Do not set fields of the destination object to null if the field in the from object is null.")
    public void doNotOverwriteDestinationFieldWithNull() {
        Applicator<BasicTestFromClass, BasicTestToClass> applicator = new ApplicatorBuilder<>(BasicTestFromClass.class, BasicTestToClass.class).build();

        BasicTestFromClass from = new BasicTestFromClass();
        BasicTestToClass to = new BasicTestToClass();
        to.setTestString("NonNullTestString");

        applicator.apply(from, to);
        assertThat(to, hasProperty("testString", is("NonNullTestString")));
    }

    /**
     * The following test cases deal with the basics of dealing with unidentified fields, missed fields, and/or
     * destinations that cannot be instantiated because there is no accessible no-args constructor
     */

    @Getter
    @AllArgsConstructor(access = AccessLevel.PACKAGE)
    public static class NonCooperativeBasicTestClass {
        @Setter
        @FieldIdentifier("Test-String")
        private String testString;

        //No setter on this String, so while the identifier "Test-String" is a match, no setting can be done
        //field is not set to "final" because that would be "cheating" in the test cases below
        @SuppressWarnings("FieldMayBeFinal")
        @FieldIdentifier("Test-String")
        private String testStringWithoutSetter;

        //The source class shouldn't have this identifier registered, therefore this field won't get set even though it has a setter
        @Setter
        @FieldIdentifier("Source-Class-Does-Not-Have-This-One")
        private String unknownIdentifier;

        //this field is not annotated so it will not be set even though it has a setter
        @Setter
        private String unidentifiedField;
    }

    @Test
    @DisplayName("Return the destination parameter if the source object parameter is null.")
    public void returnDestinationParameterValueWhenSourceParameterIsNull() {
        Applicator<BasicTestFromClass, BasicTestToClass> applicator =
            new ApplicatorBuilder<>(BasicTestFromClass.class, BasicTestToClass.class).build();
        Applicator<BasicTestFromClass, NonCooperativeBasicTestClass> nonCooperativeApplicator =
            new ApplicatorBuilder<>(BasicTestFromClass.class, NonCooperativeBasicTestClass.class).build();

        //If destination and source are both null, and if the destination type can be constructed, return a new instance
        assertThat(applicator.apply(null, null), notNullValue(BasicTestToClass.class));

        //If the destination and source are both null, and if the destination type CANNOT be constructed, return null
        assertThat(nonCooperativeApplicator.apply(null, null), nullValue());

        //If source is null and destination is not null, return destination
        BasicTestToClass to = new BasicTestToClass();
        assertThat(applicator.apply(null, to), is(to));

        //If source is not null, and destination is null and destination cannot be constructed, return null
        BasicTestFromClass from = new BasicTestFromClass();
        assertThat(nonCooperativeApplicator.apply(from, null), nullValue());
    }

    @Test
    @DisplayName("Do not attempt to override a destination field if no setter method is available.")
    public void doNotThrowShitFitIfIdentifiedFieldIsLackingSetterMethod() {
        Applicator<BasicTestFromClass, NonCooperativeBasicTestClass> nonCooperativeApplicator =
            new ApplicatorBuilder<>(BasicTestFromClass.class, NonCooperativeBasicTestClass.class).build();

        BasicTestFromClass from = new BasicTestFromClass();
        from.setTestString("NewTestString");
        NonCooperativeBasicTestClass to = new NonCooperativeBasicTestClass(
            "OldTestString", "UnchangedTestString",
            "UnknownIdentifierString", "UnidentifiedFieldValue");

        nonCooperativeApplicator.apply(from, to);
        assertThat(to, allOf(
            hasProperty("testString", is("NewTestString")), //The one property that could be overridden should have been overridden
            hasProperty("testStringWithoutSetter", is("UnchangedTestString")), //Matching identified property has no setter method therefore it should stay the same
            hasProperty("unknownIdentifier", is("UnknownIdentifierString")), //Source object does not contain a field with a matching identifier, therefore this field should not be overridden
            hasProperty("unidentifiedField", is("UnidentifiedFieldValue")) //Field not annotated, therefore it should never be overridden
        ));
    }

    /**
     * These next tests focus on nesting of objects between the 'source' and the 'destination' objects.
     * These are cases where the destination and/or source objects have nested fields or not.
     * Some tests and assertions may even include multiple destination fields being overridden by a single source value.
     */

    @Getter @Setter
    public static class BasicSourceWithNested {
        @FieldIdentifier("first")
        private String first;
        @FieldIdentifier("second")
        private Integer second;
        @Nested
        private BasicSourceNested nested;
    }

    @Getter @Setter
    public static class BasicSourceNested {
        @FieldIdentifier("third")
        private Object third;
        @FieldIdentifier("fourth")
        private String fourth;
    }

    @Getter @Setter
    public static class BasicDestinationWithNested {
        @FieldIdentifier("first")
        private String first;
        @FieldIdentifier("third")
        private Object third;
        //Nested Objects
        @Nested
        private BasicDestinationNested nested;
        @Nested
        private BasicDestinationNestedNonConstructable nestedNonConstructable;
    }

    @Getter @Setter
    public static class BasicDestinationNested {
        @FieldIdentifier("first")
        private String first;
        @FieldIdentifier("second")
        private Integer second;
        @FieldIdentifier("fourth")
        private String fourth;
    }

    @Getter @Setter
    @AllArgsConstructor(access = AccessLevel.PACKAGE)
    public static class BasicDestinationNestedNonConstructable {
        @FieldIdentifier("third")
        private Object third;
        @FieldIdentifier("fourth")
        private String fourth;
    }

    @Test
    @DisplayName("Handle nested fields in both the source and destination objects when overriding source values onto the destination object.")
    public void overwriteMultipleAndNestedFieldsOnDestinationObjectFromSourceObjectFields() {
        Applicator<BasicSourceWithNested, BasicDestinationWithNested> applicator =
            new ApplicatorBuilder<>(BasicSourceWithNested.class, BasicDestinationWithNested.class).build();

        //The values being set up
        String first = "FirstValue";
        Integer second = 1234;
        Object third = new Object();
        String fourth = "FourthValue";

        //Source object containing the above values and nested objects
        BasicSourceWithNested source = new BasicSourceWithNested();
        source.setFirst(first);
        source.setSecond(second);
        BasicSourceNested sourceNested = new BasicSourceNested();
        source.setNested(sourceNested);
        sourceNested.setThird(third);
        sourceNested.setFourth(fourth);

        //Destination object with the nested objects instantiated in advance for this test
        BasicDestinationWithNested destination = new BasicDestinationWithNested();
        destination.setNested(new BasicDestinationNested());
        destination.setNestedNonConstructable(new BasicDestinationNestedNonConstructable(null, null));

        applicator.apply(source, destination);
        assertThat(destination, allOf(
            hasProperty("first", is(first)),
            hasProperty("third", is(third)),
            hasProperty("nested", allOf(
                notNullValue(),
                hasProperty("first", is(first)),
                hasProperty("second", is(second)),
                hasProperty("fourth", is(fourth))
            )),
            hasProperty("nestedNonConstructable", allOf(
                notNullValue(),
                hasProperty("third", is(third)),
                hasProperty("fourth", is(fourth))
            ))
        ));
    }

    @Test
    @DisplayName("Handle constructing new objects for nested fields when constructable and skip null non-constructable nested fields.")
    public void handleNullConstructableAndNullNonConstructableFieldValues() {
        Applicator<BasicSourceWithNested, BasicDestinationWithNested> applicator =
            new ApplicatorBuilder<>(BasicSourceWithNested.class, BasicDestinationWithNested.class).build();

        //The values being set up
        String first = "FirstValue";
        Integer second = 1234;
        Object third = new Object();
        String fourth = "FourthValue";

        //Source object containing the above values and nested objects
        BasicSourceWithNested source = new BasicSourceWithNested();
        source.setFirst(first);
        source.setSecond(second);
        BasicSourceNested sourceNested = new BasicSourceNested();
        source.setNested(sourceNested);
        sourceNested.setThird(third);
        sourceNested.setFourth(fourth);

        BasicDestinationWithNested destination = applicator.apply(source, null);
        assertThat(destination, allOf(
            notNullValue(),
            hasProperty("first", is(first)),
            hasProperty("third", is(third)),
            hasProperty("nested", allOf(
                notNullValue(),
                hasProperty("first", is(first)),
                hasProperty("second", is(second)),
                hasProperty("fourth", is(fourth))
            )),
            hasProperty("nestedNonConstructable", nullValue()) //Should be null because it's not a type that can be constructed automagically
        ));
    }

    /**
     * These next tests are intended to test that things still work with very deep nesting for both the destination and
     * source objects. These next tests will also perform testing using annotated methods rather than fields.
     *
     * Also in these tests, since so many test classes have to be made, the source and destination types will be the
     * same. This also means the ability for this system to deep-copy and deep-override one object onto another object
     * of the same type will be tested.
     */

    @Getter @Setter
    public static class ObjectWithDeepNesting {
        @Nested
        private ObjectDeepNested nested;
        @Nested
        private ObjectDeepNestedNonConstructable nestedNonConstructable;
    }

    @Getter @Setter
    public static class ObjectDeepNested {
        @Nested
        private ObjectDeepNestedNested nested;
    }

    @Getter @Setter
    public static class ObjectDeepNestedNested {
        @Nested
        private ObjectDeepNestedNestedNested nested;
    }

    @Getter @Setter
    public static class ObjectDeepNestedNestedNested {
        @Nested
        private ObjectDeepNestedNestedNestedNested nested;
    }

    @SuppressWarnings("unused")
    public static class ObjectDeepNestedNestedNestedNested {
        private String firstString;
        private int firstStringHash;

        @FieldIdentifier("first")
        public String getFirstString() {
            return firstString;
        }

        @FieldIdentifier("first")
        public void setFirstString(String firstString) {
            this.firstString = firstString;
        }
    }

    @Getter @Setter
    @AllArgsConstructor(access = AccessLevel.PACKAGE)
    public static class ObjectDeepNestedNonConstructable {
        @Nested
        private ObjectDeepNestedNestedNonConstructable nestedNonConstructable;
    }

    @Getter @Setter
    @AllArgsConstructor(access = AccessLevel.PACKAGE)
    public static class ObjectDeepNestedNestedNonConstructable {
        @Nested
        private ObjectDeepNestedNestedNestedNonConstructable nestedNonConstructable;
    }

    @Getter @Setter
    @AllArgsConstructor(access = AccessLevel.PACKAGE)
    public static class ObjectDeepNestedNestedNestedNonConstructable {
        @Nested
        private ObjectDeepNestedNestedNestedNestedNonConstructable nestedNonConstructable;
    }

    @SuppressWarnings("unused")
    @AllArgsConstructor(access = AccessLevel.PACKAGE)
    public static class ObjectDeepNestedNestedNestedNestedNonConstructable {
        private String secondString;

        @FieldIdentifier("second")
        public String getSecondString() {
            return secondString;
        }

        @FieldIdentifier("second")
        public void setSecondString(String secondString) {
            this.secondString = secondString;
        }
    }

    @Test
    @DisplayName("Test updating a deeply nested destination field with a deeply nested non-null source field.")
    public void handleUpdatingDestinationObjectFromSourceObjectWhenBothAreNestedDeeperThanTheKolaSuperDeepBorehole() {
        Applicator<ObjectWithDeepNesting, ObjectWithDeepNesting> applicator =
            new ApplicatorBuilder<>(ObjectWithDeepNesting.class, ObjectWithDeepNesting.class).build();

        //Test Values
        String firstString = "Test First Value";
        String secondString = "Test Second Value";

        ObjectWithDeepNesting source = new ObjectWithDeepNesting();
        source.setNested(new ObjectDeepNested());
        source.getNested().setNested(new ObjectDeepNestedNested());
        source.getNested().getNested().setNested(new ObjectDeepNestedNestedNested());
        source.getNested().getNested().getNested().setNested(new ObjectDeepNestedNestedNestedNested());
        source.getNested().getNested().getNested().getNested().setFirstString(firstString);
        source.setNestedNonConstructable(new ObjectDeepNestedNonConstructable(
            new ObjectDeepNestedNestedNonConstructable(
                new ObjectDeepNestedNestedNestedNonConstructable(
                    new ObjectDeepNestedNestedNestedNestedNonConstructable(secondString)
                )
            )
        ));

        ObjectWithDeepNesting destination = new ObjectWithDeepNesting();
        destination.setNested(new ObjectDeepNested());
        destination.getNested().setNested(new ObjectDeepNestedNested());
        destination.getNested().getNested().setNested(new ObjectDeepNestedNestedNested());
        destination.getNested().getNested().getNested().setNested(new ObjectDeepNestedNestedNestedNested());
        destination.getNested().getNested().getNested().getNested().setFirstString("Old First String Value");
        destination.setNestedNonConstructable(new ObjectDeepNestedNonConstructable(
            new ObjectDeepNestedNestedNonConstructable(
                new ObjectDeepNestedNestedNestedNonConstructable(
                    new ObjectDeepNestedNestedNestedNestedNonConstructable("Old Second String Value")
                )
            )
        ));

        applicator.apply(source, destination);
        assertThat(destination, allOf(
            hasProperty("nested",
                hasProperty("nested",
                    hasProperty("nested",
                        hasProperty("nested",
                            hasProperty("firstString", is(firstString))
                        )
                    )
                )
            ),
            hasProperty("nestedNonConstructable",
                hasProperty("nestedNonConstructable",
                    hasProperty("nestedNonConstructable",
                        hasProperty("nestedNonConstructable",
                            hasProperty("secondString", is(secondString))
                        )
                    )
                )
            )
        ));
    }

    @Test
    @DisplayName("Test deep-copying a deeply nested source object and skip null nested non-constructable fields.")
    public void deepCopyVeryDeeplyNestedSourceObjectAndSkipNullNestedNonConstructableFields() {
        Applicator<ObjectWithDeepNesting, ObjectWithDeepNesting> applicator =
            new ApplicatorBuilder<>(ObjectWithDeepNesting.class, ObjectWithDeepNesting.class).build();

        //Test Values
        String firstString = "Test First Value";
        String secondString = "Test Second Value";

        ObjectWithDeepNesting source = new ObjectWithDeepNesting();
        source.setNested(new ObjectDeepNested());
        source.getNested().setNested(new ObjectDeepNestedNested());
        source.getNested().getNested().setNested(new ObjectDeepNestedNestedNested());
        source.getNested().getNested().getNested().setNested(new ObjectDeepNestedNestedNestedNested());
        source.getNested().getNested().getNested().getNested().setFirstString(firstString);
        source.setNestedNonConstructable(new ObjectDeepNestedNonConstructable(
            new ObjectDeepNestedNestedNonConstructable(
                new ObjectDeepNestedNestedNestedNonConstructable(
                    new ObjectDeepNestedNestedNestedNestedNonConstructable(secondString)
                )
            )
        ));

        ObjectWithDeepNesting destination = applicator.apply(source, null);
        assertThat(destination, allOf(
            notNullValue(),
            hasProperty("nested",
                hasProperty("nested",
                    hasProperty("nested",
                        hasProperty("nested",
                            hasProperty("firstString", is(firstString))
                        )
                    )
                )
            ),
            hasProperty("nestedNonConstructable", nullValue()) //Cannot automatically construct these so the field is skipped
        ));
    }
}