package org.gridman.coherence.security.demo.security;

import com.tangosol.net.security.IdentityAsserter;

import javax.security.auth.Subject;

/**
 * @todo implement
 */
public class SimpleIdentityAsserter implements IdentityAsserter {
    @Override public Subject assertIdentity(Object o) throws SecurityException {
       return null;
    }
}
