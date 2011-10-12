package org.gridman.coherence.security.simple;

import com.tangosol.io.pof.PofPrincipal;
import com.tangosol.net.Service;
import com.tangosol.net.security.IdentityAsserter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.security.auth.Subject;
import java.util.Arrays;
import java.util.HashSet;

/**
 * A simple asserter - it lets everyone in.  Subclass accordingly.
 */
public class SimpleIdentityAsserter implements IdentityAsserter {
    private static final Logger logger = LoggerFactory.getLogger(SimpleIdentityAsserter.class);

    public SimpleIdentityAsserter() {
        logger.debug(SimpleIdentityAsserter.class.getName());
    }

    @Override public Subject assertIdentity(Object oToken, Service arg1) throws SecurityException {
        logger.debug("assertIdentity " + oToken);
        // @todo should we ban guests?
        // if(oToken == null) { oToken = "Guest"; }
        return new Subject(true, new HashSet(Arrays.asList(new PofPrincipal((String)oToken))),new HashSet(),new HashSet());
    }
}
