package org.gridman.testtools.kerberos;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author Jonathan Knight
 * @deprecated use {@code org.gridman.testtools.junit.RunIsolated} instead
 */
@Deprecated
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD})
public @interface RunIsolated {
    String[] properties();
}
