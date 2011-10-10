package org.gridman.security.kerberos.junit;

import com.sun.security.auth.module.Krb5LoginModule;

import javax.security.auth.spi.LoginModule;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author Jonathan Knight
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.TYPE})
public @interface JAAS {
    public static enum FIELDS {
        FILENAME, MODULENAME, MODULECLASS, SETTINGS
    }
    
    String fileName();
    String moduleName();
    Class<? extends LoginModule> loginModuleClass() default Krb5LoginModule.class;
    String[] settings();
}
