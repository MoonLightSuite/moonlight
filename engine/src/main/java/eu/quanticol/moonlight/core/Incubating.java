package eu.quanticol.moonlight.core;

/**
 * Used to mark unstable classes or methods
 */
public @interface Incubating {
    /**
     * The reason this annotated class or method is disabled.
     */
    String value() default "";
}
