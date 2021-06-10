package io.github.cshunsinger.japplicator.cache;

import io.github.cshunsinger.japplicator.Applicator;
import io.github.cshunsinger.japplicator.BaseUnitTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

public class ParentApplicatorCacheTableTest extends BaseUnitTest {
    public static class TestModel {}
    public static class OtherTestModel {}

    private ParentApplicatorCacheTable cache;
    private Applicator<TestModel, TestModel> preCachedInstance;

    @BeforeEach
    public void init() {
        cache = new ParentApplicatorCacheTable();
        preCachedInstance = cache.getApplicator(TestModel.class, TestModel.class);
    }

    @Test
    public void fetchExistingCachedApplicatorInstance() {
        Applicator<TestModel, TestModel> result = cache.getApplicator(TestModel.class, TestModel.class);

        assertThat(result, sameInstance(preCachedInstance));
    }

    @Test
    public void createNewApplicatorInstanceAndCacheItWhenTheCacheMisses() {
        Applicator<TestModel, OtherTestModel> result = cache.getApplicator(TestModel.class, OtherTestModel.class);

        assertThat(result, not(sameInstance(preCachedInstance)));
    }
}