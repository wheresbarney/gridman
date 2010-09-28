package org.gridman.coherence.security.demo;

import org.apache.log4j.Logger;
import org.gridman.coherence.security.simple.CacheSecurityProvider;
import org.gridman.coherence.security.simple.CoherenceUtils;

import javax.security.auth.Subject;

/**
 * Our Security Provider, it checks the permission in the permissionCache.
 */
public class DemoCacheSecurityProvider implements CacheSecurityProvider {
    public static final Logger logger = Logger.getLogger(DemoCacheSecurityProvider.class);

    public DemoCacheSecurityProvider() {
        logger.debug(DemoCacheSecurityProvider.class.getName());
    }

    /*
     * check the access.
     */
    @Override public boolean checkAccess(Subject subject, String cacheName, boolean readOnly) {
        return DemoServer.checkPermission(CoherenceUtils.getFirstPrincipalName(subject),cacheName,true,readOnly);
    }
}
