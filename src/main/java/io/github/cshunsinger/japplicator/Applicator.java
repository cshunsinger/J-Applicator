package io.github.cshunsinger.japplicator;

import io.github.cshunsinger.japplicator.cache.ApplicatorCache;
import lombok.NonNull;

/**
 * This is the head interface. This interface will be implemented with freshly generated classes.
 * @param <Src> The type of object that will be containing data to apply onto a destination object.
 * @param <Dest> The type of object that will have data values applied to it.
 */
public abstract class Applicator<Src, Dest> {
    /**
     * The method builder class that generates an implementation of this method will be expecting
     * the first parameter of this method to be the "from" parameter, and the second parameter to be the "to"
     * parameter.
     *
     * Do not mess with the natural order of things if you don't know what you're doing.
     *
     * @param from The object containing data to be applied onto another object.
     * @param to The object to which the data will be applied.
     * @return The `to` reference if `to` is not null. If `to` is null, and if a new instance of `Dest` could be created,
     * then a new instance of `Dest` is returned.
     * @see #apply(Src from)
     * @see #applyValues(Src from, Dest to)
     * @see #applyValues(Src from, Class destClass)
     */
    public abstract Dest apply(Src from, Dest to);

    /**
     * This method is a shortcut of calling <code>applicatorInstance.apply(from, null)</code>
     * If the 'Dest' type can be instantiated (meaning it has a public constructor with no parameters) then a new instance
     * will be created, and it will be populated with values from the source object provided in this method.
     * @param from The source object.
     * @return A new instance of the destination object with values applied from the source object.
     * @see #apply(Src from, Dest to)
     * @see #applyValues(Src from, Dest to)
     * @see #applyValues(Src from, Class destClass)
     */
    public Dest apply(Src from) {
        return this.apply(from, null);
    }

    /**
     * This method applies values from a non-null source object onto a non-null destination object. This static method is
     * a shortcut to avoid having to create and store an applicator instance. This method is less efficient because it has
     * to get the class of the passed-in objects and then lookup the applicator instance from a cache and create a new
     * applicator instance of none already exist in the cache. This method is great for quick use, but for code that needs
     * to perform a lot of these operations, it is recommended to fetch dedicated applicator instances and call them separately.
     * @param from Source object containing data.
     * @param to Destination object to apply data onto.
     * @param <Src> Source object type.
     * @param <Dest> Destination object type.
     * @return the destination object instance.
     */
    @SuppressWarnings("unchecked")
    public static <Src, Dest> Dest applyValues(@NonNull Src from, @NonNull Dest to) {
        Applicator<Src, Dest> applicator = getInstance((Class<Src>)from.getClass(), (Class<Dest>)to.getClass());
        return applicator.apply(from, to);
    }

    /**
     * This method fetches an applicator that will convert from a source object onto a new instance of a destination class.
     * This static method is
     * a shortcut to avoid having to create and store an applicator instance. This method is less efficient because it has
     * to get the class of the passed-in objects and then lookup the applicator instance from a cache and create a new
     * applicator instance of none already exist in the cache. This method is great for quick use, but for code that needs
     * to perform a lot of these operations, it is recommended to fetch dedicated applicator instances and call them separately.
     * @param from Source object containing data.
     * @param destClass Class of object to instantiate and apply data onto.
     * @param <Src> Source object type.
     * @param <Dest> Destination object type.
     * @return a new instance of the destination class with values applied onto it, or null if the destination class could
     * not be instantiated due to lacking an accessible constructor with zero parameters.
     */
    @SuppressWarnings("unchecked")
    public static <Src, Dest> Dest applyValues(@NonNull Src from, Class<Dest> destClass) {
        Applicator<Src, Dest> applicator = getInstance((Class<Src>)from.getClass(), destClass);
        return applicator.apply(from);
    }

    /**
     * This method fetches an Applicator instance which can automatically map data from an instance of one class onto
     * an instance of another class. This method utilizes the cache which means calling this method repeatedly will
     * not generate infinite new applicator classes and instances of those classes.
     * @param srcClass Source class.
     * @param destClass Destination class.
     * @param <Src> Source type.
     * @param <Dest> Destination type.
     * @return A new applicator instance which can map data from a source object onto a destination object.
     */
    public static <Src, Dest> Applicator<Src, Dest> getInstance(Class<Src> srcClass, Class<Dest> destClass) {
        return ApplicatorCache.instance.getApplicator(srcClass, destClass);
    }
}