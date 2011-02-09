package org.gridman.commandline;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author jonathanknight
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface CommandLineArg {
    String name();

    boolean required() default true;

    String[] usage() default {};

    String usageVar() default "";

    boolean hasValue() default true;
}
