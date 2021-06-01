package io.github.cshunsinger.japplicator;

/**
 * This is the head interface. This interface will be implemented with freshly generated classes.
 * @param <Src> The type of object that will be containing data to apply onto a destination object.
 * @param <Dest> The type of object that will have data values applied to it.
 */
public abstract class HeadOn<Src, Dest> {
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
     */
    public abstract Dest applyDirectlyToTheForehead(Src from, Dest to);
}