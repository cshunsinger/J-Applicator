package io.github.cshunsinger.japplicator.exception;

/**
 * This is an exception thrown by the type conversion code whenever a generic type involving a type variable is
 * encountered. J-Applicator does not support type variables when converting values.
 */
public class TypeVariableUnsupportedException extends Exception {}