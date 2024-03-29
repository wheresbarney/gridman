package org.gridman.demo.coherence.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.gridman.coherence.security.simple.CacheSecurityProvider;
import org.gridman.coherence.security.simple.CoherenceSecurityUtils;

import javax.security.auth.Subject;

/**
 * Our Security Provider, it checks the permission in the permissionCache.
 */
public class DemoCacheSecurityProvider implements CacheSecurityProvider {
    public static final Logger logger = LoggerFactory.getLogger(DemoCacheSecurityProvider.class);

    public DemoCacheSecurityProvider() {
        logger.debug(DemoCacheSecurityProvider.class.getName());
    }

    /*
     * check the access.
     */
    @Override public boolean checkAccess(Subject subject, String cacheName, boolean readOnly) {
        return DemoSecurityProvider.checkPermission(CoherenceSecurityUtils.getFirstPrincipalName(subject),cacheName,true,readOnly);
    }
}
