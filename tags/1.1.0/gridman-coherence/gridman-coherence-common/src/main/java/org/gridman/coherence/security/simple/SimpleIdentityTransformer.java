package org.gridman.coherence.security.simple;

import com.tangosol.net.Service;
import com.tangosol.net.security.IdentityTransformer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.security.auth.Subject;

/*
 * Simple Identity Transformer, just passes the first principal name through
 */
public class SimpleIdentityTransformer implements IdentityTransformer {
    private static final Logger logger = LoggerFactory.getLogger(SimpleIdentityTransformer.class);

    public SimpleIdentityTransformer() {
        logger.debug(SimpleIdentityTransformer.class.getName());
    }

    @Override public Object transformIdentity(Subject subject, Service arg1) throws SecurityException {
        String name = CoherenceSecurityUtils.getFirstPrincipalName(subject);
        logger.debug("SimpleIdentityTransformer : " + subject + " : " + name);
        return name;
    }
}
