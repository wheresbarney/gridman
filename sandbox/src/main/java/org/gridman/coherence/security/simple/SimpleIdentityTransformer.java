package org.gridman.coherence.security.simple;

import com.tangosol.net.security.IdentityTransformer;

import javax.security.auth.Subject;

import org.apache.log4j.Logger;

public class SimpleIdentityTransformer implements IdentityTransformer {
    private static final Logger logger = Logger.getLogger(SimpleIdentityTransformer.class);

    public SimpleIdentityTransformer() {
        logger.debug("SimpleIdentityTransformer");
    }

    @Override public Object transformIdentity(Subject subject) throws SecurityException {
        String name = CoherenceUtils.getFirstPrincipalName(subject);
        logger.debug("SimpleIdentityTransformer : " + subject + " : " + name);
        return name;
    }
}
