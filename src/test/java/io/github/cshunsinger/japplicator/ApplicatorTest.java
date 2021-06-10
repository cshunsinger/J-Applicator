package io.github.cshunsinger.japplicator;

import io.github.cshunsinger.japplicator.annotation.FieldIdentifier;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.junit.jupiter.api.Test;

import static org.apache.commons.lang3.RandomStringUtils.randomAlphanumeric;
import static org.junit.jupiter.api.Assertions.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

public class ApplicatorTest {
    @Getter @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TestSourceModel {
        @FieldIdentifier
        private String testValue;
    }

    @Getter @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TestDestinationModel {
        @FieldIdentifier
        private String testValue;
    }

    @Test
    public void applicatorInstance_createNewInstanceOfDestinationModel_withoutPassingNullParameter() {
        Applicator<TestSourceModel, TestDestinationModel> applicator = Applicator.getInstance(TestSourceModel.class, TestDestinationModel.class);

        String testValue = randomAlphanumeric(32);
        TestSourceModel source = new TestSourceModel(testValue);

        TestDestinationModel destination = applicator.apply(source);
        assertThat(destination, allOf(
            notNullValue(),
            hasProperty("testValue", is(testValue))
        ));
    }

    @Test
    public void applicatorInStaticContext_applyValuesFromNonNullSource_ontoNonNullDestination() {
        String testValue = randomAlphanumeric(32);
        TestSourceModel source = new TestSourceModel(testValue);
        TestDestinationModel destination = new TestDestinationModel();

        //Apply values onto instantiated destination instance
        destination = Applicator.applyValues(source, destination);
        assertThat(destination, hasProperty("testValue", is(testValue)));
    }

    @Test
    public void applicatorInStaticContext_createNewInstanceOfDestinationClass_applyValuesFromSourceObject() {
        String testValue = randomAlphanumeric(32);
        TestSourceModel source = new TestSourceModel(testValue);

        //Create new destination instance and apply values to it
        TestDestinationModel destination = Applicator.applyValues(source, TestDestinationModel.class);
        assertThat(destination, allOf(
            notNullValue(),
            hasProperty("testValue", is(testValue))
        ));
    }
}