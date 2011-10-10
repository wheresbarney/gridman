package org.gridman.security;

import java.security.Principal;

/**
 * @author Jonathan Knight
 */
public class MissingPrincipalException extends SecurityException {

    public MissingPrincipalException(Class<? extends Principal> type) {
        super("Subject does not contain a Principal of type " + type.getCanonicalName());
    }
    
}
