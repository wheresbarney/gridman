package org.gridman.coherence.security.simple;

import org.apache.log4j.Logger;

import javax.security.auth.Subject;

/**
 * For checking access to Coherence Caches.
 * Currently uses an the Entitled Named Cache for Cache Security.
 * This will be replaced by CacheServiceProxy when it is released by Jason Howse.
 * @author Andrew Wilson
 */
public class CheckCacheSecurityProvider implements CacheSecurityProvider {
    private static final Logger logger = Logger.getLogger(CheckCacheSecurityProvider.class);

    public CheckCacheSecurityProvider() {
        logger.debug("CheckCacheSecurityProvider");
    }

    @Override public boolean checkAccess(Subject subject, String cacheName, boolean readOnly) {
        // Only allow the allowed user.
        logger.debug("checkAccess" + subject );
        if(readOnly) {
            return !CoherenceSecurityUtils.checkFirstPrincipalName(SecurityTest.DISALLOWED_CACHE);
        } else {
            return CoherenceSecurityUtils.checkFirstPrincipalName(SecurityTest.ALLOWED);
        }
    }
}
