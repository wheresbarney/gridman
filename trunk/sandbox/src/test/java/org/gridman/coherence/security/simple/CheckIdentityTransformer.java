package org.gridman.coherence.security.simple;

import com.tangosol.net.security.IdentityTransformer;

import javax.security.auth.Subject;

import org.apache.log4j.Logger;

public class CheckIdentityTransformer implements IdentityTransformer {
    private static final Logger logger = Logger.getLogger(CheckIdentityTransformer.class);

    public CheckIdentityTransformer() {
        logger.debug("CheckIdentityTransformer");
    }

    @Override public Object transformIdentity(Subject subject) throws SecurityException {
        String name = CoherenceUtils.getFirstPrincipalName(subject);
        logger.debug("CheckIdentityTransformer : " + subject + " : " + name);
        return name;
    }
}
