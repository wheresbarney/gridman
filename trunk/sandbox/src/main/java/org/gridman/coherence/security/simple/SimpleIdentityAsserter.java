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

    @Override public Subject assertIdentity(Object oToken) throws SecurityException {
        logger.debug("assertIdentity " + oToken);
        // @todo should we ban guests?
        // if(oToken == null) { oToken = "Guest"; }
        return new Subject(true, new HashSet(Arrays.asList(new PofPrincipal((String)oToken))),new HashSet(),new HashSet());
    }
}
