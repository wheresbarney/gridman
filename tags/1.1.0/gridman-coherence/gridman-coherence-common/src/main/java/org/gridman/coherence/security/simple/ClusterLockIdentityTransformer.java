package org.gridman.coherence.security.simple;

import com.tangosol.net.Service;
import com.tangosol.net.security.IdentityTransformer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.security.auth.Subject;

/**
 * Sends the ClusterLock as the Token.
 * This is the simplest way to secure a cluster.
 * NOT recommended for production usage.
 */
public class ClusterLockIdentityTransformer implements IdentityTransformer {
    private static final Logger logger = LoggerFactory.getLogger(ClusterLockIdentityTransformer.class);
    protected static final String LOCK = "org.gridman.coherence.security.clusterLock";

    public ClusterLockIdentityTransformer() {
        logger.debug(ClusterLockIdentityTransformer.class.getName());
    }

    @Override public Object transformIdentity(Subject subject, Service arg1) throws SecurityException {
        logger.debug("transformIdentity");
        String lock = System.getProperty(LOCK);
        if(lock == null) { throw new SecurityException(LOCK + " is not set!"); }
        return lock;
    }
}
