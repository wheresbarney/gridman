package org.gridman.coherence.security.simple;

import com.tangosol.net.CacheFactory;
import com.tangosol.net.NamedCache;
import com.tangosol.util.Filter;
import com.tangosol.util.filter.AndFilter;
import com.tangosol.util.filter.EqualsFilter;
import org.apache.log4j.Logger;
import org.gridman.coherence.security.demo.DemoServer;

import java.util.Set;

/**
 * Basically a wrapper to the the Permission Cache.
 */
public class SimpleSecurityProvider {
    private static final Logger logger = Logger.getLogger(DemoServer.class);

    private String permissionCacheName;

    private static SimpleSecurityProvider sInstance = new SimpleSecurityProvider();

    private NamedCache permissionCache;
    private boolean defaultResponse;

    public SimpleSecurityProvider() {
        permissionCacheName = System.getProperty("org.gridman.coherence.security.simple.PermissionCache","PermissionCache");
        permissionCache = CacheFactory.getCache(permissionCacheName);
        defaultResponse = Boolean.getBoolean("org.gridman.coherence.security.simple.defaultResponse");
    }

    public static SimpleSecurityProvider getInstance() { return sInstance; }

    public String getPermissionCacheName() { return permissionCacheName; }

    public boolean getDefaultResponse() { return defaultResponse; }

    /**
     * check the permission in the cache, used by both cache + invocation.  This is not the best place for this method but will do.
     */
    public boolean checkPermission(String checkRole, String checkResourceName, boolean isCacheRatherThanInvoke, boolean checkReadOnly) {

        Filter filter = new AndFilter(  new EqualsFilter("getRole", checkRole), new EqualsFilter("isCacheRatherThanInvoke",isCacheRatherThanInvoke));
        Set<SimpleSecurityPermission> set = permissionCache.keySet(filter);
        logger.debug("Got " + set.size());
        for(SimpleSecurityPermission perm : set) {
            if(perm.matchPermission(checkRole,checkResourceName,isCacheRatherThanInvoke,checkReadOnly)) {
                return true;
            }
        }
        logger.warn("Failed permission for checkRole : '" + checkRole + "' checkResourceName : '" + checkResourceName +
                     "' isCacheRatherThanInvoke : " + isCacheRatherThanInvoke + " checkReadOnly : " + checkReadOnly );
        return defaultResponse;
    }

    /**
     * Is the user in the permission cache?
     */
    public boolean containsUser(String user) {
        return !permissionCache.keySet(new EqualsFilter("getRole",user)).isEmpty();
    }
}
