package org.gridman.coherence.security.demo.security;

import com.tangosol.net.security.IdentityTransformer;

import javax.security.auth.Subject;

/**
 *
 */
public class SimpleIdentityTransformer implements IdentityTransformer {
    @Override public Object transformIdentity(Subject subject) throws SecurityException {
        return null;
    }
}
