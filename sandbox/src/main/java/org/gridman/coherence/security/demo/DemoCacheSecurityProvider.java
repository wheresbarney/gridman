package org.gridman.coherence.security.demo;

import com.tangosol.net.CacheFactory;
import com.tangosol.net.NamedCache;
import com.tangosol.util.Filter;
import com.tangosol.util.filter.AndFilter;
import com.tangosol.util.filter.EqualsFilter;
import org.apache.log4j.Logger;
import org.gridman.coherence.security.simple.CoherenceUtils;
import org.gridman.coherence.security.simple.CacheSecurityProvider;

import javax.security.auth.Subject;
import java.util.Map;
import java.util.Set;

/**
 * Our Security Provider, it checks the permission in the permissionCache.
 */
public class DemoCacheSecurityProvider implements CacheSecurityProvider {
    public static final Logger logger = Logger.getLogger(DemoCacheSecurityProvider.class);

    public DemoCacheSecurityProvider() {
        logger.debug(DemoCacheSecurityProvider.class.getName());
    }
    
    @Override public boolean checkAccess(Subject subject, boolean readOnly, String cacheName) {
        logger.debug("checkAccess " + cacheName + " : " + readOnly + " : " + subject);
        String firstPrincipalName = CoherenceUtils.getFirstPrincipalName(subject);
        if(cacheName.equals(SecurityPermission.PERMISSION_CACHE) && readOnly) { return true; }
        NamedCache permissionCache = CacheFactory.getCache(SecurityPermission.PERMISSION_CACHE);
        int permission = readOnly ? SecurityPermission.PERMISSION_READ : SecurityPermission.PERMISSION_WRITE;
        Filter filter = new AndFilter(new EqualsFilter("getRole", firstPrincipalName),new EqualsFilter("getPermission", permission));
        Set<Map.Entry> set = permissionCache.entrySet(filter);
        for(Map.Entry entry : set) {
            SecurityPermission perm = (SecurityPermission) entry.getValue();
            if(perm.checkPermission(cacheName, permission, firstPrincipalName)) {
                return true;
            }
        }
        return false;

    }
}
