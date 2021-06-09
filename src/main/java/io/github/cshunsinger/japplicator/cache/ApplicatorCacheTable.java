package io.github.cshunsinger.japplicator.cache;

import io.github.cshunsinger.japplicator.Applicator;

import java.util.HashMap;
import java.util.Map;

/**
 * This cache table is a table of in-memory mappings of values which is also capable of searching through a parent table.
 * This class is NOT thread-safe. Instances of this class are intended to be kept inside of ThreadLocal objects, while
 * the ParentApplicatorCacheTable is intended to be thread safe.
 * @see ApplicatorCache
 */
public class ApplicatorCacheTable {
    private final ParentApplicatorCacheTable parent;
    private final Map<Class<?>, Map<Class<?>, Applicator<?, ?>>> applicatorMap = new HashMap<>();

    ApplicatorCacheTable(ParentApplicatorCacheTable parent) {
        this.parent = parent;
    }

    /**
     * Gets an applicator instance from the cache which applies data from an object of the source type onto an object
     * of the destination type. If no applicator instance is cached, a new one is obtained and stored in the cache.
     * @param srcType Class of the source type.
     * @param destType Class of the destination type.
     * @param <Src> Source type.
     * @param <Dest> Destination type.
     * @return An applicator instance.
     */
    public <Src, Dest> Applicator<Src, Dest> getApplicator(Class<Src> srcType, Class<Dest> destType) {
        Applicator<Src, Dest> applicator = attemptFindApplicator(srcType, destType);
        return applicator == null ? cacheApplicator(srcType, destType) : applicator;
    }

    private <Src, Dest> Applicator<Src, Dest> cacheApplicator(Class<Src> srcType, Class<Dest> destType) {
        Applicator<Src, Dest> newApplicator = createApplicator(srcType, destType);
        applicatorMapForSourceType(srcType).put(destType, newApplicator);
        return newApplicator;
    }

    /**
     * "creates" an Applicator by calling the parent cache to find or create the applicator.
     * If the parent cache finds an already-existing applicator instance, it will return it and that instance will
     * be stored in this cache without any new classes or objects being instantiated. If the parent cache does not
     * find an already-existing applicator instance, it will create a new one by making a new Java class and instantiating
     * it and returning that instance instead.
     * @param srcType The source type.
     * @param destType The destination type.
     * @param <Src> Source.
     * @param <Dest> Destination.
     * @return An instance of an Applicator which will apply values from a source object onto a destination object.
     */
    <Src, Dest> Applicator<Src, Dest> createApplicator(Class<Src> srcType, Class<Dest> destType) {
        synchronized(parent) {
            return parent.getApplicator(srcType, destType);
        }
    }

    @SuppressWarnings("unchecked")
    private <Src, Dest> Applicator<Src, Dest> attemptFindApplicator(Class<Src> srcType, Class<Dest> destType) {
        Map<Class<?>, Applicator<?, ?>> applicators = applicatorMapForSourceType(srcType);
        return (Applicator<Src, Dest>)applicators.get(destType);
    }

    /**
     * Fetches a map of all applicators for all destinations from a source type. All applicators in the returned map
     * will apply values from the same source type onto some destination type (defined by the keys of the returned map).
     * If no applicators are cached for the given source type, then a new map will be created and added to the cache.
     * @param srcType The source type.
     * @return A mapping of all applicators that apply data from the given source type onto destination types.
     */
    Map<Class<?>, Applicator<?, ?>> applicatorMapForSourceType(Class<?> srcType) {
        return applicatorMap.computeIfAbsent(srcType, i -> new HashMap<>());
    }
}