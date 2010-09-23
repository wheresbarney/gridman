package org.gridman.coherence.security.demo;

import com.tangosol.net.CacheFactory;
import com.tangosol.net.NamedCache;
import com.tangosol.util.Filter;
import com.tangosol.util.filter.AndFilter;
import com.tangosol.util.filter.EqualsFilter;
import org.apache.log4j.Logger;
import org.gridman.coherence.security.simple.CoherenceUtils;
import org.gridman.coherence.security.simple.SecurityProvider;

import javax.security.auth.Subject;
import java.util.Set;

/**
 * Our Security Provider, it checks the permission in the permissionCache.
 */
public class DemoSecurityProvider implements SecurityProvider {
    public static final Logger logger = Logger.getLogger(DemoSecurityProvider.class);

    private NamedCache permissionCache;

    public DemoSecurityProvider() {
        logger.debug(DemoSecurityProvider.class.getName());

    }
    
    @Override public boolean checkAccess(Subject subject, boolean readOnly, String cacheName) {
        logger.debug("checkAccess " + cacheName + " : " + readOnly + " : " + subject);
        permissionCache = CacheFactory.getCache(SecurityPermission.PERMISSION_CACHE);        
        String firstPrincipalName = CoherenceUtils.getFirstPrincipalName(subject);
        int permission = readOnly ? SecurityPermission.PERMISSION_READ : SecurityPermission.PERMISSION_WRITE;
        Filter filter = new AndFilter(new EqualsFilter("getRole", firstPrincipalName),new EqualsFilter("getPermission", permission));
        Set<SecurityPermission> set = permissionCache.entrySet(filter);
        for(SecurityPermission perm : set) {
            if(perm.checkPermission(cacheName)) { return true; }    
        }
        return false;

    }
}
