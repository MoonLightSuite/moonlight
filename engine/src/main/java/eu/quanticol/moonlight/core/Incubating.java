package eu.quanticol.moonlight.core;

public @interface Incubating {
    /**
     * The reason this annotated test class or test method is disabled.
     */
    String value() default "";
}
