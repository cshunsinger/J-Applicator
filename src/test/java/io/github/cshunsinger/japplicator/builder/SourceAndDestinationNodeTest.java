package io.github.cshunsinger.japplicator.builder;

import io.github.cshunsinger.japplicator.BaseUnitTest;
import io.github.cshunsinger.japplicator.annotation.FieldIdentifier;
import io.github.cshunsinger.japplicator.Applicator;
import io.github.cshunsinger.japplicator.annotation.Nested;
import lombok.*;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

public class SourceAndDestinationNodeTest extends BaseUnitTest {
    @SuppressWarnings("unused")
    public static class TestModel {
        @FieldIdentifier //This field will NOT be skipped
        @Getter @Setter
        private String identifiedField;

        @FieldIdentifier //This field will be skipped because there is no accessor methods
        private String identifiedFieldWithoutAccessors;

        //This method will be skipped because there is no identifier
        public String unidentifiedAccessorMethod() {
            return null;
        }

        @Nested //This field will be skipped because NestedModel has no accessors inside it
        private NestedModel nestedWithoutAccessors;

        @Nested //This field will be skipped because String is a jvm type
        @Getter @Setter
        private String nestedIgnoredType;

        @Nested //This method will be skipped because NestedModel has no accessors inside it
        public NestedModel nestedModelWithoutAccessorsMethod() {
            return null;
        }

        @Nested //This method will be skipped because String is a jvm type
        public String nestedIgnoredTypeMethod() {
            return null;
        }
    }

    public static class NestedModel {}

    @Test
    public void test_skipCertainFieldsAndMethodsThatAreNotInterestingOrUnusable() {
        ApplicatorBuilder<TestModel, TestModel> applicatorBuilder = new ApplicatorBuilder<>(TestModel.class, TestModel.class);
        Applicator<TestModel, TestModel> applicator = applicatorBuilder.build();
        String testString = RandomStringUtils.randomAlphanumeric(10);

        assertThat(applicator, notNullValue());

        TestModel model = new TestModel();
        model.setIdentifiedField(testString);
        TestModel result = applicator.apply(model, null);

        assertThat(result, allOf(
            notNullValue(),
            not(model),
            hasProperty("identifiedField", is(testString))
        ));
    }

    public static class EmptyTestModel {}

    @Test
    public void test_handleEmptyTestSourceAndDestinationModel() {
        ApplicatorBuilder<EmptyTestModel, EmptyTestModel> applicatorBuilder = new ApplicatorBuilder<>(EmptyTestModel.class, EmptyTestModel.class);
        Applicator<EmptyTestModel, EmptyTestModel> applicator = applicatorBuilder.build();

        assertThat(applicator, notNullValue());

        EmptyTestModel model = new EmptyTestModel();
        EmptyTestModel result = applicator.apply(model, null);

        assertThat(result, allOf(
            notNullValue(),
            not(model)
        ));
    }

    @Test
    public void test_handleWhenThereIsNoDestinationAvailableForSourceField() {
        ApplicatorBuilder<TestModel, EmptyTestModel> applicatorBuilder = new ApplicatorBuilder<>(TestModel.class, EmptyTestModel.class);
        Applicator<TestModel, EmptyTestModel> applicator = applicatorBuilder.build();

        assertThat(applicator, notNullValue());

        TestModel model = new TestModel();
        model.setIdentifiedField(RandomStringUtils.randomAlphanumeric(10));

        //Should not throw exception and should construct a new EmptyTestModel instance
        EmptyTestModel empty = applicator.apply(model, null);
        assertThat(empty, notNullValue());
    }

    @SuppressWarnings("unused")
    @AllArgsConstructor
    @RequiredArgsConstructor
    public static class TestModelWithNestingMethods {
        private MethodNestedModel innerModel;
        private final MethodNestedModel constModel;

        @Nested
        public MethodNestedModel nestedModel() {
            return this.innerModel;
        }

        @Nested
        public void nestedModel(MethodNestedModel model) {
            this.innerModel = model;
        }

        @Nested
        public MethodNestedModel constModel() {
            return this.constModel;
        }
    }

    @Getter @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @FieldIdentifier
    public static class MethodNestedModel {
        private String testString;
    }

    @Test
    public void test_handleNestedMethodsThatExistIndependentlyOfField() {
        //Test Data
        String testString = RandomStringUtils.randomAlphanumeric(10);

        //Test source model which contains the test data values
        MethodNestedModel sourceInner = new MethodNestedModel(testString);
        MethodNestedModel sourceConst = new MethodNestedModel(testString);
        TestModelWithNestingMethods source = new TestModelWithNestingMethods(sourceInner, sourceConst);

        //Test destination model which the source model will be applied to
        MethodNestedModel destConst = new MethodNestedModel();
        TestModelWithNestingMethods dest = new TestModelWithNestingMethods(destConst);

        //Applicator
        ApplicatorBuilder<TestModelWithNestingMethods, TestModelWithNestingMethods> applicatorBuilder =
            new ApplicatorBuilder<>(TestModelWithNestingMethods.class, TestModelWithNestingMethods.class);
        Applicator<TestModelWithNestingMethods, TestModelWithNestingMethods> applicator = applicatorBuilder.build();

        //Apply source to dest
        applicator.apply(source, dest);

        //Validate
        assertThat(dest.innerModel, allOf(
            notNullValue(),
            not(source.innerModel), //A new instance should be made, not just copying the source reference
            hasProperty("testString", is(testString))
        ));
        assertThat(dest.constModel, hasProperty("testString", is(testString)));
    }

    @SuppressWarnings("unused")
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ModelWithDifferentNestedGetterNames {
        private MethodNestedModel nestedModel;
        private OtherMethodNestedModel otherNestedModel;
        private ExplicitMethodNestedModel explicitNestedModel;

        @Nested
        public MethodNestedModel getNestedModel() {
            return this.nestedModel;
        }

        @Nested
        public void setNestedModel(MethodNestedModel model) {
            this.nestedModel = model;
        }

        @Nested //In case some sick pos uses "is" instead of "get"
        public OtherMethodNestedModel isOtherNestedModel() {
            return this.otherNestedModel;
        }

        @Nested
        public void setOtherNestedModel(OtherMethodNestedModel model) {
            this.otherNestedModel = model;
        }

        @Nested("explicit")
        public ExplicitMethodNestedModel explicitlyNested() {
            return this.explicitNestedModel;
        }

        @Nested("explicit")
        public void explicitlyNestedSetter(ExplicitMethodNestedModel model) {
            this.explicitNestedModel = model;
        }
    }

    @Getter @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @FieldIdentifier
    public static class OtherMethodNestedModel {
        private String otherTestString;
    }

    @Getter @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @FieldIdentifier
    public static class ExplicitMethodNestedModel {
        private String explicitTestString;
    }

    @Test
    public void test_handleDifferentGetterMethodNamesForNestedObjects() {
        //Test values which will be set in the source model instance
        String testString = RandomStringUtils.randomAlphanumeric(10);
        String otherTestString = RandomStringUtils.randomAlphanumeric(10);
        String explicitTestString = RandomStringUtils.randomAlphanumeric(10);

        //Source model containing the source test values
        ModelWithDifferentNestedGetterNames source = new ModelWithDifferentNestedGetterNames(
            new MethodNestedModel(testString),
            new OtherMethodNestedModel(otherTestString),
            new ExplicitMethodNestedModel(explicitTestString)
        );

        //Create the applicator
        ApplicatorBuilder<ModelWithDifferentNestedGetterNames, ModelWithDifferentNestedGetterNames> applicatorBuilder =
            new ApplicatorBuilder<>(ModelWithDifferentNestedGetterNames.class, ModelWithDifferentNestedGetterNames.class);
        Applicator<ModelWithDifferentNestedGetterNames, ModelWithDifferentNestedGetterNames> applicator = applicatorBuilder.build();

        //Apply
        ModelWithDifferentNestedGetterNames dest = applicator.apply(source, null);

        assertThat(dest, notNullValue());
        assertThat(dest.getNestedModel(), allOf(
            notNullValue(),
            hasProperty("testString", is(testString))
        ));
        assertThat(dest.isOtherNestedModel(), allOf(
            notNullValue(),
            hasProperty("otherTestString", is(otherTestString))
        ));
        assertThat(dest.explicitlyNested(), allOf(
            notNullValue(),
            hasProperty("explicitTestString", is(explicitTestString))
        ));
    }
}