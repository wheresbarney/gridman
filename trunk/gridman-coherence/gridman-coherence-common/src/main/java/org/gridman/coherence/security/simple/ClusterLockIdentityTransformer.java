package org.gridman.coherence.security.simple;

import com.tangosol.net.security.IdentityTransformer;
import org.apache.log4j.Logger;

import javax.security.auth.Subject;

/**
 * Sends the ClusterLock as the Token.
 * This is the simplest way to secure a cluster.
 * NOT recommended for production usage.
 */
public class ClusterLockIdentityTransformer implements IdentityTransformer {
    private static final Logger logger = Logger.getLogger(ClusterLockIdentityTransformer.class);
    protected static final String LOCK = "org.gridman.coherence.security.clusterLock";

    public ClusterLockIdentityTransformer() {
        logger.debug(ClusterLockIdentityTransformer.class.getName());
    }

    @Override public Object transformIdentity(Subject subject) throws SecurityException {
        logger.debug("transformIdentity");
        String lock = System.getProperty(LOCK);
        if(lock == null) { throw new SecurityException(LOCK + " is not set!"); }
        return lock;
    }
}
