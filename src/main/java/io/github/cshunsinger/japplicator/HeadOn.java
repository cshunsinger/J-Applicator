package io.github.cshunsinger.japplicator;

/**
 * This is the head interface. This interface will be implemented with freshly generated classes
 */
public abstract class HeadOn {
    /**
     * The method builder class that generates an implementation of this method will be expecting
     * the first parameter of this method to be the "from" parameter, and the second parameter to be the "to"
     * parameter.
     *
     * Do not mess with the natural order of things if you don't know what you're doing.
     */
    public abstract Object applyDirectlyToTheForehead(Object from, Object to);
}