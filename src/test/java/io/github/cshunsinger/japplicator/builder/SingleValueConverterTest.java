package io.github.cshunsinger.japplicator.builder;

import io.github.cshunsinger.japplicator.annotation.FieldIdentifier;
import io.github.cshunsinger.japplicator.HeadOn;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.junit.jupiter.api.Test;

import static org.apache.commons.lang3.RandomUtils.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.core.AllOf.allOf;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class SingleValueConverterTest {
    @Getter
    @RequiredArgsConstructor
    @FieldIdentifier
    public static class TestFromByte {
        private final byte value;
        private final Byte wrappedValue;
    }

    @Getter
    @RequiredArgsConstructor
    @FieldIdentifier
    public static class TestFromCharacter {
        private final char value;
        private final Character wrappedValue;
    }

    @Getter
    @RequiredArgsConstructor
    @FieldIdentifier
    public static class TestFromShort {
        private final short value;
        private final Short wrappedValue;
    }

    @Getter
    @RequiredArgsConstructor
    @FieldIdentifier
    public static class TestFromInteger {
        private final int value;
        private final Integer wrappedValue;
    }

    @Getter
    @RequiredArgsConstructor
    @FieldIdentifier
    public static class TestFromLong {
        private final long value;
        private final Long wrappedValue;
    }

    @Getter
    @RequiredArgsConstructor
    @FieldIdentifier
    public static class TestFromFloat {
        private final float value;
        private final Float wrappedValue;
    }

    @Getter
    @RequiredArgsConstructor
    @FieldIdentifier
    public static class TestFromDouble {
        private final double value;
        private final Double wrappedValue;
    }

    @Getter @Setter
    @NoArgsConstructor
    public static class TestDestinationModel {
        @FieldIdentifier("value")
        private byte byteValue;
        @FieldIdentifier("wrappedValue")
        private Byte wrappedByteValue;

        @FieldIdentifier("value")
        private char charValue;
        @FieldIdentifier("wrappedValue")
        private Character wrappedCharValue;

        @FieldIdentifier("value")
        private short shortValue;
        @FieldIdentifier("wrappedValue")
        private Short wrappedShortValue;

        @FieldIdentifier("value")
        private int intValue;
        @FieldIdentifier("wrappedValue")
        private Integer wrappedIntValue;

        @FieldIdentifier("value")
        private long longValue;
        @FieldIdentifier("wrappedValue")
        private Long wrappedLongValue;

        @FieldIdentifier("value")
        private float floatValue;
        @FieldIdentifier("wrappedValue")
        private Float wrappedFloatValue;

        @FieldIdentifier("value")
        private double doubleValue;
        @FieldIdentifier("wrappedValue")
        private Double wrappedDoubleValue;

        @FieldIdentifier("value")
        private Object objectFromPrimitiveValue;
        @FieldIdentifier("wrappedValue")
        private Object objectFromWrappedValue;

        @FieldIdentifier("value")
        private String stringFromPrimitiveValue;
        @FieldIdentifier("wrappedValue")
        private String stringFromWrappedValue;
    }

    @Test
    public void convertByteToOtherTypes() {
        HeadOn<TestFromByte, TestDestinationModel> converter = new ApplicatorBuilder<>(TestFromByte.class, TestDestinationModel.class).build();

        TestDestinationModel destination = new TestDestinationModel();
        byte testValue = (byte)(nextInt() % Byte.MAX_VALUE);
        byte testWrappedValue = (byte)(nextInt() % Byte.MAX_VALUE);
        TestFromByte source = new TestFromByte(testValue, testWrappedValue);

        converter.applyDirectlyToTheForehead(source, destination);
        assertThat(destination, allOf(
            hasProperty("byteValue", is(testValue)),
            hasProperty("wrappedByteValue", is(testWrappedValue)),
            hasProperty("charValue", is((char)testValue)),
            hasProperty("wrappedCharValue", is((char)testWrappedValue)),
            hasProperty("shortValue", is((short)testValue)),
            hasProperty("wrappedShortValue", is((short)testWrappedValue)),
            hasProperty("intValue", is((int)testValue)),
            hasProperty("wrappedIntValue", is((int)testWrappedValue)),
            hasProperty("longValue", is((long)testValue)),
            hasProperty("wrappedLongValue", is((long)testWrappedValue)),
            hasProperty("floatValue", is((float)testValue)),
            hasProperty("wrappedFloatValue", is((float)testWrappedValue)),
            hasProperty("doubleValue", is((double)testValue)),
            hasProperty("wrappedDoubleValue", is((double)testWrappedValue)),
            hasProperty("objectFromPrimitiveValue", is(testValue)),
            hasProperty("objectFromWrappedValue", is(testWrappedValue)),
            hasProperty("stringFromPrimitiveValue", is("" + testValue)),
            hasProperty("stringFromWrappedValue", is("" + testWrappedValue))
        ));
    }

    @Test
    public void convertCharacterToOtherTypes() {
        HeadOn<TestFromCharacter, TestDestinationModel> converter = new ApplicatorBuilder<>(TestFromCharacter.class, TestDestinationModel.class).build();

        TestDestinationModel destination = new TestDestinationModel();
        char testValue = (char)(nextInt() % Character.MAX_VALUE);
        char testWrappedValue = (char)(nextInt() % Character.MAX_VALUE);
        TestFromCharacter source = new TestFromCharacter(testValue, testWrappedValue);

        converter.applyDirectlyToTheForehead(source, destination);
        assertThat(destination, allOf(
            hasProperty("byteValue", is((byte)testValue)),
            hasProperty("wrappedByteValue", is((byte)testWrappedValue)),
            hasProperty("charValue", is(testValue)),
            hasProperty("wrappedCharValue", is(testWrappedValue)),
            hasProperty("shortValue", is((short)testValue)),
            hasProperty("wrappedShortValue", is((short)testWrappedValue)),
            hasProperty("intValue", is((int)testValue)),
            hasProperty("wrappedIntValue", is((int)testWrappedValue)),
            hasProperty("longValue", is((long)testValue)),
            hasProperty("wrappedLongValue", is((long)testWrappedValue)),
            hasProperty("floatValue", is((float)testValue)),
            hasProperty("wrappedFloatValue", is((float)testWrappedValue)),
            hasProperty("doubleValue", is((double)testValue)),
            hasProperty("wrappedDoubleValue", is((double)testWrappedValue)),
            hasProperty("objectFromPrimitiveValue", is(testValue)),
            hasProperty("objectFromWrappedValue", is(testWrappedValue)),
            hasProperty("stringFromPrimitiveValue", is("" + testValue)),
            hasProperty("stringFromWrappedValue", is("" + testWrappedValue))
        ));
    }

    @Test
    public void convertShortToOtherTypes() {
        HeadOn<TestFromShort, TestDestinationModel> converter = new ApplicatorBuilder<>(TestFromShort.class, TestDestinationModel.class).build();

        TestDestinationModel destination = new TestDestinationModel();
        short testValue = (short)(nextInt() % Short.MAX_VALUE);
        short testWrappedValue = (short)(nextInt() % Short.MAX_VALUE);
        TestFromShort source = new TestFromShort(testValue, testWrappedValue);

        converter.applyDirectlyToTheForehead(source, destination);
        assertThat(destination, allOf(
            hasProperty("byteValue", is((byte)testValue)),
            hasProperty("wrappedByteValue", is((byte)testWrappedValue)),
            hasProperty("charValue", is((char)testValue)),
            hasProperty("wrappedCharValue", is((char)testWrappedValue)),
            hasProperty("shortValue", is(testValue)),
            hasProperty("wrappedShortValue", is(testWrappedValue)),
            hasProperty("intValue", is((int)testValue)),
            hasProperty("wrappedIntValue", is((int)testWrappedValue)),
            hasProperty("longValue", is((long)testValue)),
            hasProperty("wrappedLongValue", is((long)testWrappedValue)),
            hasProperty("floatValue", is((float)testValue)),
            hasProperty("wrappedFloatValue", is((float)testWrappedValue)),
            hasProperty("doubleValue", is((double)testValue)),
            hasProperty("wrappedDoubleValue", is((double)testWrappedValue)),
            hasProperty("objectFromPrimitiveValue", is(testValue)),
            hasProperty("objectFromWrappedValue", is(testWrappedValue)),
            hasProperty("stringFromPrimitiveValue", is("" + testValue)),
            hasProperty("stringFromWrappedValue", is("" + testWrappedValue))
        ));
    }

    @Test
    public void convertIntegerToOtherTypes() {
        HeadOn<TestFromInteger, TestDestinationModel> converter = new ApplicatorBuilder<>(TestFromInteger.class, TestDestinationModel.class).build();

        TestDestinationModel destination = new TestDestinationModel();
        int testValue = nextInt();
        int testWrappedValue = nextInt();
        TestFromInteger source = new TestFromInteger(testValue, testWrappedValue);

        converter.applyDirectlyToTheForehead(source, destination);
        assertThat(destination, allOf(
            hasProperty("byteValue", is((byte)testValue)),
            hasProperty("wrappedByteValue", is((byte)testWrappedValue)),
            hasProperty("charValue", is((char)testValue)),
            hasProperty("wrappedCharValue", is((char)testWrappedValue)),
            hasProperty("shortValue", is((short)testValue)),
            hasProperty("wrappedShortValue", is((short)testWrappedValue)),
            hasProperty("intValue", is(testValue)),
            hasProperty("wrappedIntValue", is(testWrappedValue)),
            hasProperty("longValue", is((long)testValue)),
            hasProperty("wrappedLongValue", is((long)testWrappedValue)),
            hasProperty("floatValue", is((float)testValue)),
            hasProperty("wrappedFloatValue", is((float)testWrappedValue)),
            hasProperty("doubleValue", is((double)testValue)),
            hasProperty("wrappedDoubleValue", is((double)testWrappedValue)),
            hasProperty("objectFromPrimitiveValue", is(testValue)),
            hasProperty("objectFromWrappedValue", is(testWrappedValue)),
            hasProperty("stringFromPrimitiveValue", is("" + testValue)),
            hasProperty("stringFromWrappedValue", is("" + testWrappedValue))
        ));
    }

    @Test
    public void convertLongToOtherTypes() {
        HeadOn<TestFromLong, TestDestinationModel> converter = new ApplicatorBuilder<>(TestFromLong.class, TestDestinationModel.class).build();

        TestDestinationModel destination = new TestDestinationModel();
        long testValue = nextLong();
        long testWrappedValue = nextLong();
        TestFromLong source = new TestFromLong(testValue, testWrappedValue);

        converter.applyDirectlyToTheForehead(source, destination);
        assertThat(destination, allOf(
            hasProperty("byteValue", is((byte)testValue)),
            hasProperty("wrappedByteValue", is((byte)testWrappedValue)),
            hasProperty("charValue", is((char)testValue)),
            hasProperty("wrappedCharValue", is((char)testWrappedValue)),
            hasProperty("shortValue", is((short)testValue)),
            hasProperty("wrappedShortValue", is((short)testWrappedValue)),
            hasProperty("intValue", is((int)testValue)),
            hasProperty("wrappedIntValue", is((int)testWrappedValue)),
            hasProperty("longValue", is(testValue)),
            hasProperty("wrappedLongValue", is(testWrappedValue)),
            hasProperty("floatValue", is((float)testValue)),
            hasProperty("wrappedFloatValue", is((float)testWrappedValue)),
            hasProperty("doubleValue", is((double)testValue)),
            hasProperty("wrappedDoubleValue", is((double)testWrappedValue)),
            hasProperty("objectFromPrimitiveValue", is(testValue)),
            hasProperty("objectFromWrappedValue", is(testWrappedValue)),
            hasProperty("stringFromPrimitiveValue", is("" + testValue)),
            hasProperty("stringFromWrappedValue", is("" + testWrappedValue))
        ));
    }

    @Test
    public void convertFloatToOtherTypes() {
        HeadOn<TestFromFloat, TestDestinationModel> converter = new ApplicatorBuilder<>(TestFromFloat.class, TestDestinationModel.class).build();

        TestDestinationModel destination = new TestDestinationModel();
        float testValue = nextFloat();
        float testWrappedValue = nextFloat();
        TestFromFloat source = new TestFromFloat(testValue, testWrappedValue);

        converter.applyDirectlyToTheForehead(source, destination);
        assertThat(destination, allOf(
            hasProperty("byteValue", is((byte)testValue)),
            hasProperty("wrappedByteValue", is((byte)testWrappedValue)),
            hasProperty("charValue", is((char)testValue)),
            hasProperty("wrappedCharValue", is((char)testWrappedValue)),
            hasProperty("shortValue", is((short)testValue)),
            hasProperty("wrappedShortValue", is((short)testWrappedValue)),
            hasProperty("intValue", is((int)testValue)),
            hasProperty("wrappedIntValue", is((int)testWrappedValue)),
            hasProperty("longValue", is((long)testValue)),
            hasProperty("wrappedLongValue", is((long)testWrappedValue)),
            hasProperty("floatValue", is(testValue)),
            hasProperty("wrappedFloatValue", is(testWrappedValue)),
            hasProperty("doubleValue", is((double)testValue)),
            hasProperty("wrappedDoubleValue", is((double)testWrappedValue)),
            hasProperty("objectFromPrimitiveValue", is(testValue)),
            hasProperty("objectFromWrappedValue", is(testWrappedValue)),
            hasProperty("stringFromPrimitiveValue", is("" + testValue)),
            hasProperty("stringFromWrappedValue", is("" + testWrappedValue))
        ));
    }

    @Test
    public void convertDoubleToOtherTypes() {
        HeadOn<TestFromDouble, TestDestinationModel> converter = new ApplicatorBuilder<>(TestFromDouble.class, TestDestinationModel.class).build();

        TestDestinationModel destination = new TestDestinationModel();
        double testValue = nextDouble();
        double testWrappedValue = nextDouble();
        TestFromDouble source = new TestFromDouble(testValue, testWrappedValue);

        converter.applyDirectlyToTheForehead(source, destination);
        assertThat(destination, allOf(
            hasProperty("byteValue", is((byte)testValue)),
            hasProperty("wrappedByteValue", is((byte)testWrappedValue)),
            hasProperty("charValue", is((char)testValue)),
            hasProperty("wrappedCharValue", is((char)testWrappedValue)),
            hasProperty("shortValue", is((short)testValue)),
            hasProperty("wrappedShortValue", is((short)testWrappedValue)),
            hasProperty("intValue", is((int)testValue)),
            hasProperty("wrappedIntValue", is((int)testWrappedValue)),
            hasProperty("longValue", is((long)testValue)),
            hasProperty("wrappedLongValue", is((long)testWrappedValue)),
            hasProperty("floatValue", is((float)testValue)),
            hasProperty("wrappedFloatValue", is((float)testWrappedValue)),
            hasProperty("doubleValue", is(testValue)),
            hasProperty("wrappedDoubleValue", is(testWrappedValue)),
            hasProperty("objectFromPrimitiveValue", is(testValue)),
            hasProperty("objectFromWrappedValue", is(testWrappedValue)),
            hasProperty("stringFromPrimitiveValue", is("" + testValue)),
            hasProperty("stringFromWrappedValue", is("" + testWrappedValue))
        ));
    }

    @Getter
    @RequiredArgsConstructor
    @FieldIdentifier
    public static class TestFromObject {
        private final Object value;
    }

    @Getter
    @RequiredArgsConstructor
    @FieldIdentifier
    public static class TestFromCharArray {
        private final char[] value;
    }

    @Getter @Setter
    @NoArgsConstructor
    public static class TestDestinationWithString {
        @FieldIdentifier
        private String value;
        @FieldIdentifier("value")
        private CharSequence valueSequence;
    }

    @Test
    public void testConversionOfObjectToStringUsingToString() {
        HeadOn<TestFromObject, TestDestinationWithString> applicator = new ApplicatorBuilder<>(TestFromObject.class, TestDestinationWithString.class).build();

        Object testObj = new Object();
        String expectedString = testObj.toString();

        TestFromObject testSource = new TestFromObject(testObj);
        TestDestinationWithString testDestination = applicator.applyDirectlyToTheForehead(testSource, null);

        assertThat(testDestination, allOf(
            hasProperty("value", is(expectedString)),
            hasProperty("valueSequence", is(expectedString))
        ));
    }

    @Test
    public void testConversionOfCharArrayToString() {
        HeadOn<TestFromCharArray, TestDestinationWithString> applicator = new ApplicatorBuilder<>(TestFromCharArray.class, TestDestinationWithString.class).build();

        String expectedString = "My Favorite Test String";
        char[] stringChars = expectedString.toCharArray();

        TestFromCharArray testSource = new TestFromCharArray(stringChars);
        TestDestinationWithString testDestination = applicator.applyDirectlyToTheForehead(testSource, null);

        assertThat(testDestination, allOf(
            hasProperty("value", is(expectedString)),
            hasProperty("valueSequence", is(expectedString))
        ));
    }

    @FieldIdentifier
    @Getter
    @RequiredArgsConstructor
    public static class SourceTestType {
        private final Object value;
    }

    @FieldIdentifier
    @Getter @Setter
    public static class DestinationTestType {
        private int value;
    }

    @Test
    public void throwIllegalArgumentException_typeCannotBeConvertedToOtherType() {
        IllegalArgumentException ex = assertThrows(
            IllegalArgumentException.class,
            () -> new ApplicatorBuilder<>(SourceTestType.class, DestinationTestType.class).build()
        );

        assertThat(ex, hasProperty("message",
            is("No conversion exists from type %s to type %s.".formatted(Object.class.getName(), int.class.getName()))
        ));
    }
}