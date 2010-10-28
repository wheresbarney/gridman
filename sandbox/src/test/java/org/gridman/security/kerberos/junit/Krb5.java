package org.gridman.security.kerberos.junit;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author Jonathan Knight
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.TYPE})
public @interface Krb5 {
    public static enum FIELDS {
        CREDENTIALS, KEYTAB_FILENAME, REALM, KRB5CONF, KDCPORT, KDCHOST
    }

    public static final String DEFAULT_KRB5CONF = "krb5.conf";
    public static final int DEFAULT_PORT = 0;
    public static final String DEFAULT_HOST = "localhost";
    
    String[] credentials();
    String keytabFile();
    String realm();
    String krb5Conf() default DEFAULT_KRB5CONF;
    int kdcPort() default DEFAULT_PORT;
    String host() default DEFAULT_HOST;
}
