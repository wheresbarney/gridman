package org.gridman.kerberos.junit;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.TYPE})
public @interface DirectoryPartition {
    
    /** List of Partition suffixes in the form "dc=gridman,dc=com" */
    String[] suffixes();
    
}
