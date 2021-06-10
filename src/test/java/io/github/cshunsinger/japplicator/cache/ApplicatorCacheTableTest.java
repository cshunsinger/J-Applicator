package io.github.cshunsinger.japplicator.cache;

import io.github.cshunsinger.japplicator.Applicator;
import io.github.cshunsinger.japplicator.BaseUnitTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class ApplicatorCacheTableTest extends BaseUnitTest {
    @Mock
    private Applicator<?, ?> mockApplicatorInstance;
    @Mock
    private ParentApplicatorCacheTable mockParentCache;

    private ApplicatorCacheTable cache;

    @BeforeEach
    public void init() {
        cache = new ApplicatorCacheTable(mockParentCache);
    }

    @Test
    public void fetchApplicatorFromThreadCacheWithoutNeedingToAccessParentCache() {
        //Insert the mock applicator instance into the cache
        Map<Class<?>, Applicator<?, ?>> objectApplicatorMap = cache.applicatorMapForSourceType(Object.class);
        objectApplicatorMap.put(Object.class, mockApplicatorInstance);

        //Get the mock applicator from the cache
        Applicator<Object, Object> applicator = cache.getApplicator(Object.class, Object.class);
        assertThat(applicator, is(mockApplicatorInstance));

        //Ensure that the parent cache was never accessed
        verify(mockParentCache, never()).getApplicator(any(), any());
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    @Test
    public void fetchApplicatorFromParentCacheWhenThreadCacheMisses() {
        //Parent cache mocks
        when(mockParentCache.getApplicator(Object.class, Object.class)).thenReturn((Applicator)mockApplicatorInstance);

        //Get the mock applicator from the cache
        Applicator<Object, Object> applicator = cache.getApplicator(Object.class, Object.class);
        assertThat(applicator, is(mockApplicatorInstance));

        //Ensure that the parent cache was accessed
        verify(mockParentCache).getApplicator(Object.class, Object.class);
    }
}