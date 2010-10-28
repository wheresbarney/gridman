package org.gridman.security.kerberos.junit;

import java.lang.annotation.*;

/**
 * @author Jonathan Knight
 */
@Documented
@Inherited
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.TYPE})
public @interface PartitionFactory {
    Class value();
}