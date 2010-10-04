package org.gridman.coherence.security.simple;

import com.tangosol.net.security.IdentityAsserter;
import com.tangosol.io.pof.PofPrincipal;

import javax.security.auth.Subject;
import java.security.Principal;
import java.util.Arrays;
import java.util.HashSet;

import org.apache.log4j.Logger;

/**
 * For checking access to Coherence.
 * @author Andrew Wilson
 */
public class CheckIdentityAsserter implements IdentityAsserter {
    private static final Logger logger = Logger.getLogger(CheckIdentityAsserter.class);
    
    public CheckIdentityAsserter() {
        logger.debug("CheckIdentityAsserter");
    }

    @Override public Subject assertIdentity(Object oToken) throws SecurityException {
        logger.debug("assertIdentity " + oToken);
        if(oToken == null) {
            return new Subject(true, new HashSet<Principal>(Arrays.asList(new PofPrincipal(SecurityTest.ALLOWED))),new HashSet(),new HashSet());
        }
        if(oToken.equals(SecurityTest.DISALLOWED)) { throw new SecurityException("Not allowed user"); }
        return new Subject(true, new HashSet<Principal>(Arrays.asList(new PofPrincipal((String)oToken))),new HashSet(),new HashSet());
    }
}
