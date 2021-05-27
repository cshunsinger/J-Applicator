package io.github.cshunsinger.japplicator.exception;

/**
 * This is an exception thrown by the type conversion code whenever a generic type involving a wildcard is encountered.
 * J-Applicator does not support Wildcard types when converting values.
 */
public class WildcardTypeUnsupportedException extends Exception {}