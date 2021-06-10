package io.github.cshunsinger.japplicator.cache;

import io.github.cshunsinger.japplicator.Applicator;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

public class ApplicatorCacheTest {
    public static class TestModel {}

    @Test
    public void findOrCreateApplicatorInstanceInCache() {
        Applicator<TestModel, TestModel> firstApplicator = ApplicatorCache.instance.getApplicator(TestModel.class, TestModel.class);
        Applicator<TestModel, TestModel> cachedApplicator = ApplicatorCache.instance.getApplicator(TestModel.class, TestModel.class);

        assertThat(firstApplicator, sameInstance(cachedApplicator));
    }
}