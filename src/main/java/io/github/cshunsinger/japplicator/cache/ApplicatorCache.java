package io.github.cshunsinger.japplicator.cache;

import io.github.cshunsinger.japplicator.Applicator;

/**
 * This class simply contains a cache of applicator instances to help reduce the number of applicator classes generated
 * as well as to allow applicators to be fetched in static calls.
 *
 * This cache is a thread-safe multi-level cache. At the first level, every thread gets it's own applicator cache
 * which is stored in a ThreadLocal object to avoid any "synchronized" code. At the second level, a parent cache exists
 * which is only called if the thread-specific cache misses. The parent cache has "synchronized" code to obtain a
 * cached applicator or create and cache a new applicator. Any time a thread-specific cache receives an Applicator
 * instance from the parent cache, it caches that result to avoid any synchronized code being executed the next time
 * that Applicator needs to be fetched from cache.
 */
public class ApplicatorCache {
    /**
     * The available instance of this applicator cache.
     */
    public static final ApplicatorCache instance = new ApplicatorCache();

    private final ParentApplicatorCacheTable parentCache;
    private final ThreadLocal<ApplicatorCacheTable> threadCache;

    private ApplicatorCache() {
        this.parentCache = new ParentApplicatorCacheTable();
        this.threadCache = ThreadLocal.withInitial(() -> new ApplicatorCacheTable(this.parentCache));
    }

    /**
     * Obtains an applicator instance which will apply data values from an object of the source type onto an object of
     * the destination type. If no applicator instance exists in the cache, a new one will be created and cached before
     * being returned.
     * @param srcType Class of source type.
     * @param destType Class of destination type.
     * @param <Src> Source type.
     * @param <Dest> Destination type.
     * @return An applicator instance.
     */
    public <Src, Dest> Applicator<Src, Dest> getApplicator(Class<Src> srcType, Class<Dest> destType) {
        return threadCache.get().getApplicator(srcType, destType);
    }
}