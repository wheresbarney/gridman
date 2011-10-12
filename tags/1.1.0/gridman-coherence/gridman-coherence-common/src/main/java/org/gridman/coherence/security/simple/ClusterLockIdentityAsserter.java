package org.gridman.coherence.security.simple;

import com.tangosol.net.Service;
import com.tangosol.net.security.IdentityAsserter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.security.auth.Subject;

/**
 * Checks that the Cluster Lock is set.
 * This is the simplest way to secure a cluster.
 * NOT recommended for production usage.
 */
public class ClusterLockIdentityAsserter implements IdentityAsserter {
    private static final Logger logger = LoggerFactory.getLogger(ClusterLockIdentityAsserter.class);

    public ClusterLockIdentityAsserter() {
        logger.debug(ClusterLockIdentityAsserter.class.getName());
    }

    @Override public Subject assertIdentity(Object o, Service arg1) throws SecurityException {
        logger.debug("assertIdentity");
        String lock = System.getProperty(ClusterLockIdentityTransformer.LOCK);
        if(lock == null) { throw new SecurityException(ClusterLockIdentityTransformer.LOCK + " is not set!"); }
        if(!lock.equals(o)) { throw new SecurityException("Invalid : " + ClusterLockIdentityTransformer.LOCK); }
        return null;
    }

}
