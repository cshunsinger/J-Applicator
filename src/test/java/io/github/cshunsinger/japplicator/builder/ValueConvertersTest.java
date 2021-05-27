package io.github.cshunsinger.japplicator.builder;

import io.github.cshunsinger.japplicator.exception.TypeVariableUnsupportedException;
import io.github.cshunsinger.japplicator.exception.WildcardTypeUnsupportedException;
import lombok.SneakyThrows;
import org.apache.commons.lang3.reflect.TypeUtils;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.lang.reflect.Type;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

public class ValueConvertersTest {
    @ParameterizedTest
    @MethodSource("unsupportedType_exceptionWhenSourceOrDestinationTypesAreInvalid_testCases")
    public void unsupportedType_exceptionWhenSourceOrDestinationTypesAreInvalid(Type testSource, Type testDest, Class<? extends Exception> expectedException) {
        assertThrows(expectedException, () -> ValueConverters.createValueConverter("", testSource, testDest));
    }

    @SneakyThrows
    private static Stream<Arguments> unsupportedType_exceptionWhenSourceOrDestinationTypesAreInvalid_testCases() {
        Type testTypeVariable = List.class.getMethod("get", int.class).getGenericReturnType();

        return Stream.of(
            Arguments.of(TypeUtils.wildcardType().build(), Object.class, WildcardTypeUnsupportedException.class),
            Arguments.of(Object.class, TypeUtils.wildcardType().build(), WildcardTypeUnsupportedException.class),
            Arguments.of(TypeUtils.wildcardType().build(), TypeUtils.wildcardType().build(), WildcardTypeUnsupportedException.class),
            Arguments.of(testTypeVariable, Object.class, TypeVariableUnsupportedException.class),
            Arguments.of(Object.class, testTypeVariable, TypeVariableUnsupportedException.class),
            Arguments.of(testTypeVariable, testTypeVariable, TypeVariableUnsupportedException.class)
        );
    }
}