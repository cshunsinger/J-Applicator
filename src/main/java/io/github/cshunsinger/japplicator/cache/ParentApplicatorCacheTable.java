package io.github.cshunsinger.japplicator.cache;

import io.github.cshunsinger.japplicator.Applicator;
import io.github.cshunsinger.japplicator.builder.ApplicatorBuilder;

/**
 * This cache table is a table of in-memory mappings of values which is also capable of searching through parent tables
 * if one exists.
 */
public class ParentApplicatorCacheTable extends ApplicatorCacheTable {
    /**
     * Creates the parent cache. The intent is for a single parent cache to be shared among all thread-level caches.
     */
    ParentApplicatorCacheTable() {
        super(null);
    }

    @Override
    <Src, Dest> Applicator<Src, Dest> createApplicator(Class<Src> srcType, Class<Dest> destType) {
        ApplicatorBuilder<Src, Dest> builder = new ApplicatorBuilder<>(srcType, destType);
        return builder.build();
    }
}