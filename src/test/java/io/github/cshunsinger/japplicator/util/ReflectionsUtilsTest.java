package io.github.cshunsinger.japplicator.util;

import io.github.cshunsinger.japplicator.BaseUnitTest;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

class ReflectionsUtilsTest extends BaseUnitTest {
    @SuppressWarnings("unused")
    private static class TestMethodsContainer {
        private String testField;
        private boolean testBooleanField;

        public void voidMethod() {} //Invalid "getter": void return
        public String methodWithParam(String p) { return p; } //Invalid "getter": contains parameter
        public static String staticMethod() { return null; } //Invalid "getter": static
        public static void voidStaticMethod() {} //Invalid "setter": static void
        private String privateMethod() { return null; } //Invalid "getter": private
        private void privateVoidMethod() {} //Invalid "setter": private
        public String validGetter() { return null; } //Valid "getter": public, instance, no params

        public void multipleParams(String param1, String param2) {} //Invalid "setter": too many parameters
        public void validSetter(String param) {} //Valid "setter": public, instance, exactly 1 parameter

        public void setTestField(String testField) {} //Valid "setter" for the "testField" field
        public void setTestBooleanField(boolean testBooleanField) {} //Valid "setter" for the boolean "testBooleanField" field
        public boolean isTestBooleanField() { return testBooleanField; } //Valid "getter" for the boolean "testBooleanField" field
        public String getTestField() { return testField; } //Valid "getter" for the "testField" field
    }

    @ParameterizedTest
    @CsvSource({
        "myFakeMethod,No getter method named myFakeMethod found.",
        "voidMethod,Getter method voidMethod cannot be void.",
        "methodWithParam,Getter method methodWithParam must not contain any parameters.",
        "staticMethod,Getter method staticMethod cannot be static.",
        "privateMethod,Getter method privateMethod must be public.",
        "validGetter,"
    })
    public void test_findReasonForInvalidGetterMethod(String methodName, String expectedReason) {
        Method testMethod = findTestMethod(methodName);
        String invalidMethodReason = ReflectionsUtils.getInvalidGetterMethodReason(testMethod, methodName);
        assertThat(invalidMethodReason, is(expectedReason));
    }

    @Test
    public void test_findReasonForInvalidGetterMethod_nullMethod() {
        String invalidGetterReason = ReflectionsUtils.getInvalidGetterMethodReason(null);
        assertThat(invalidGetterReason, is("Method is null."));
    }

    @Test
    public void test_findReasonForInvalidGetterMethod_nullMethodWithOverriddenMethodName() {
        String invalidGetterReason = ReflectionsUtils.getInvalidGetterMethodReason(null, "myFakeMethodName");
        assertThat(invalidGetterReason, is("No getter method named myFakeMethodName found."));
    }

    @Test
    public void test_findReasonForInvalidGetterMethod_methodOnlyWithoutName() {
        Method testMethod = findTestMethod("privateMethod");
        String invalidGetterReason = ReflectionsUtils.getInvalidGetterMethodReason(testMethod);
        assertThat(invalidGetterReason, is("Getter method privateMethod must be public."));
    }

    @ParameterizedTest
    @CsvSource({
        "voidMethod,false",
        "methodWithParam,false",
        "staticMethod,false",
        "privateMethod,false",
        ",false",
        "validGetter,true"
    })
    public void test_isValidGetterMethod(String methodName, boolean expected) {
        Method testMethod = findTestMethod(methodName);
        assertThat(ReflectionsUtils.isValidGetterMethod(testMethod, methodName), is(expected));
    }

    @ParameterizedTest
    @CsvSource({
        "myFakeMethod,No setter method named myFakeMethod found.",
        "methodWithParam,Setter method methodWithParam must have a void return type.",
        "voidStaticMethod,Setter method voidStaticMethod cannot be static.",
        "privateVoidMethod,Setter method privateVoidMethod must be public.",
        "validSetter,"
    })
    public void test_findReasonForInvalidSetterMethod(String methodName, String expectedReason) {
        Method testMethod = findTestMethod(methodName);
        String invalidMethodReason = ReflectionsUtils.getInvalidSetterMethodReason(testMethod, methodName);
        assertThat(invalidMethodReason, is(expectedReason));
    }

    @Test
    public void test_findReasonForInvalidSetterMethod_nullMethod() {
        String invalidSetterReason = ReflectionsUtils.getInvalidSetterMethodReason(null);
        assertThat(invalidSetterReason, is("Method is null."));
    }

    @Test
    public void test_findReasonForInvalidSetterMethod_nullMethodWithOverriddenMethodName() {
        String invalidSetterReason = ReflectionsUtils.getInvalidSetterMethodReason(null, "myFakeMethodName");
        assertThat(invalidSetterReason, is("No setter method named myFakeMethodName found."));
    }

    @Test
    public void test_findReasonForInvalidSetterMethod_methodOnlyWithoutName() {
        Method testMethod = findTestMethod("privateVoidMethod");
        String invalidSetterReason = ReflectionsUtils.getInvalidSetterMethodReason(testMethod);
        assertThat(invalidSetterReason, is("Setter method privateVoidMethod must be public."));
    }

    @ParameterizedTest
    @CsvSource({
        "validSetter,true",
        "privateVoidMethod,false",
        "voidStaticMethod,false",
        ",false"
    })
    public void test_isValidSetterMethod(String methodName, boolean expected) {
        Method testMethod = findTestMethod(methodName);

        assertThat(ReflectionsUtils.isValidSetterMethod(testMethod), is(expected));
        assertThat(ReflectionsUtils.isValidSetterMethod(testMethod, methodName), is(expected));
    }

    @Test
    @SneakyThrows
    public void test_findGetterAndSetterMethodsForField() {
        Field testField = TestMethodsContainer.class.getDeclaredField("testField");

        //Getter method for field
        assertThat(ReflectionsUtils.findGetterMethodForField(TestMethodsContainer.class, testField), allOf(
            notNullValue(),
            hasProperty("name", is("getTestField"))
        ));

        //Setter method for field
        assertThat(ReflectionsUtils.findSetterMethodForField(TestMethodsContainer.class, testField), allOf(
            notNullValue(),
            hasProperty("name", is("setTestField"))
        ));
    }

    @Test
    @SneakyThrows
    public void test_findGetterAndSetterMethodsForPrimitiveBooleanField() {
        Field testField = TestMethodsContainer.class.getDeclaredField("testBooleanField");

        //Getter method for field
        assertThat(ReflectionsUtils.findGetterMethodForField(TestMethodsContainer.class, testField), allOf(
            notNullValue(),
            hasProperty("name", is("isTestBooleanField"))
        ));

        //Setter method for field
        assertThat(ReflectionsUtils.findSetterMethodForField(TestMethodsContainer.class, testField), allOf(
            notNullValue(),
            hasProperty("name", is("setTestBooleanField"))
        ));
    }

    @ParameterizedTest
    @CsvSource({
        "java.lang.String,java/lang/String",
        "java.lang.reflect.Method,java/lang/reflect/Method",
        "java.util.Map,java/util/Map",
        "java.util.Map$Entry,java/util/Map$Entry"
    })
    @SneakyThrows
    public void test_jvmClassnameOfClass(String classname, String jvmClassname) {
        Class<?> testClass = Class.forName(classname);
        assertThat(ReflectionsUtils.jvmClassname(testClass), is(jvmClassname));
    }

    @ParameterizedTest
    @CsvSource({
        "multipleParams,(Ljava/lang/String;Ljava/lang/String;)V",
        "voidMethod,()V",
        "validSetter,(Ljava/lang/String;)V",
        "getTestField,()Ljava/lang/String;"
    })
    public void test_generateJvmSignatureOfMethod(String methodName, String expectedJvmSignature) {
        Method testMethod = findTestMethod(methodName);
        assertThat(ReflectionsUtils.generateJvmMethodSignature(testMethod), is(expectedJvmSignature));
    }

    @ParameterizedTest
    @MethodSource("test_generateJvmTypeDefinitionOfClass_testCases")
    public void test_generateJvmTypeDefinitionOfClass(Class<?> testClass, String expectedTypeDefinition) {
        assertThat(ReflectionsUtils.jvmTypeDefinition(testClass), is(expectedTypeDefinition));
    }

    private static Stream<Arguments> test_generateJvmTypeDefinitionOfClass_testCases() {
        return Stream.of(
            Arguments.of(String.class, "Ljava/lang/String;"),
            Arguments.of(String[].class, "[Ljava/lang/String;"),
            Arguments.of(byte.class, "B"),
            Arguments.of(byte[].class, "[B")
        );
    }

    private static Method findTestMethod(String methodName) {
        return Stream.of(TestMethodsContainer.class.getDeclaredMethods())
            .filter(method -> method.getName().equals(methodName))
            .findFirst()
            .orElse(null);
    }
}