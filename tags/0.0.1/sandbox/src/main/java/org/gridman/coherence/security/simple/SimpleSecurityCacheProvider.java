package org.gridman.coherence.security.simple;

import org.apache.log4j.Logger;

import javax.security.auth.Subject;

/**
 * Our Security Provider, it checks the permission in the permissionCache.
 */
public class SimpleSecurityCacheProvider implements BaseSecurityCacheProvider {
    public static final Logger logger = Logger.getLogger(SimpleSecurityCacheProvider.class);

    public SimpleSecurityCacheProvider() {
        logger.debug(SimpleSecurityCacheProvider.class.getName());
    }

    /*
     * check the access.
     */
    @Override public boolean checkAccess(Subject subject, String cacheName, boolean readOnly) {
        return SimpleSecurityProvider.getInstance().checkPermission(CoherenceSecurityUtils.getFirstPrincipalName(subject), cacheName, true, readOnly);
    }
}
