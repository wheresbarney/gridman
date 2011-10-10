package org.gridman.coherence.security.simple;

import com.tangosol.io.pof.PofPrincipal;
import com.tangosol.net.security.IdentityAsserter;
import org.apache.log4j.Logger;

import javax.security.auth.Subject;
import java.util.Arrays;
import java.util.HashSet;

/**
 * A simple asserter - it lets everyone in.  Subclass accordingly.
 */
public class SimpleIdentityAsserter implements IdentityAsserter {
    private static final Logger logger = Logger.getLogger(SimpleIdentityAsserter.class);

    public SimpleIdentityAsserter() {
        logger.debug(SimpleIdentityAsserter.class.getName());
    }

    @Override public Subject assertIdentity(Object userName) throws SecurityException {
        logger.debug("assertIdentity " + userName);
        boolean allowed = SimpleSecurityProvider.getInstance().containsUser((String)userName);
        if(!allowed) { logger.warn("No permissions for user " + userName); }
        if(!allowed && !SimpleSecurityProvider.getInstance().getDefaultResponse()) {
            throw new SecurityException("Rejecting user : " + userName);
        }
        if(userName == null) { userName = "Guest"; }
        return new Subject(true, new HashSet(Arrays.asList(new PofPrincipal((String)userName))),new HashSet(),new HashSet());
    }
}
