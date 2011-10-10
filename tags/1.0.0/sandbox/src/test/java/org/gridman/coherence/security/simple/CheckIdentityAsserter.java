package org.gridman.coherence.security.simple;

import com.tangosol.io.pof.PofPrincipal;
import com.tangosol.net.security.IdentityAsserter;
import org.apache.log4j.Logger;

import javax.security.auth.Subject;

import static org.gridman.testing.Utils.asSubject;

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
            return asSubject(new PofPrincipal(SecurityTest.ALLOWED));
        }
        if(oToken.equals(SecurityTest.DISALLOWED)) { throw new SecurityException("Not allowed user"); }
        return asSubject(new PofPrincipal((String)oToken));
    }
}
