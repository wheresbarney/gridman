package org.gridman.coherence.security.demo;

import com.tangosol.net.CacheFactory;
import com.tangosol.net.NamedCache;
import com.tangosol.util.Filter;
import com.tangosol.util.filter.AndFilter;
import com.tangosol.util.filter.EqualsFilter;
import org.apache.log4j.Logger;

import java.util.Set;

/**
 * Basically a wrapper to the the Permission Cache.
 */
public class DemoSecurityProvider {
    static final Logger logger = Logger.getLogger(DemoServer.class);
    static final String PERMISSION_CACHE = "PermissionCache";
    static final String CLIENT_INVOKE_SERVICE = "ClientInvokeService";

    /**
     * check the permission in the cache, used by both cache + invocation.  This is not the best place for this method but will do.
     */
    public static boolean checkPermission(String checkRole, String checkResourceName, boolean isCacheRatherThanInvoke, boolean checkReadOnly) {
        NamedCache permissionCache = CacheFactory.getCache(PERMISSION_CACHE);
        Filter filter = new AndFilter(  new EqualsFilter("getRole", checkRole), new EqualsFilter("isCacheRatherThanInvoke",isCacheRatherThanInvoke));
        Set<DemoSecurityPermission> set = permissionCache.keySet(filter);
        logger.debug("Got " + set.size());
        for(DemoSecurityPermission perm : set) {
            if(perm.matchPermission(checkRole,checkResourceName,isCacheRatherThanInvoke,checkReadOnly)) {
                return true;
            }
        }
        return false;
    }
}
