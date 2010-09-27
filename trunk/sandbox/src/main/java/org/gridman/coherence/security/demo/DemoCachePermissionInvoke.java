package org.gridman.coherence.security.demo;

import com.tangosol.net.CacheFactory;
import com.tangosol.net.Invocable;
import com.tangosol.net.InvocationService;
import com.tangosol.net.NamedCache;
import com.tangosol.util.Filter;
import com.tangosol.util.filter.AndFilter;
import com.tangosol.util.filter.EqualsFilter;
import org.apache.log4j.Logger;

import java.io.Serializable;
import java.util.Set;

/**
 * An Invocable to wrapper the Permission cache, otherwise we get loopback.
 * @todo Add POF 
 */
public class DemoCachePermissionInvoke implements Invocable, Serializable {
    public static final Logger logger = Logger.getLogger(DemoCachePermissionInvoke.class);

    private String checkRole;
    private String checkCacheName;
    private boolean checkReadOnly;
    private boolean result;
    private boolean isCacheRatherThanInvoke;

    DemoCachePermissionInvoke(String checkRole, String checkResourceName, boolean isCacheRatherThanInvoke, boolean checkReadOnly) {
        this.checkRole = checkRole;
        this.checkCacheName = checkResourceName;
        this.checkReadOnly = checkReadOnly;
        this.isCacheRatherThanInvoke = isCacheRatherThanInvoke;
    }

    @Override public void init(InvocationService invocationService) {}

    @Override public void run() {
        NamedCache permissionCache = CacheFactory.getCache(DemoServer.PERMISSION_CACHE);
        Filter filter = new AndFilter(  new EqualsFilter("getRole", checkRole), new EqualsFilter("isCacheRatherThanInvoke",isCacheRatherThanInvoke));
        Set<DemoSecurityPermission> set = permissionCache.keySet(filter);
        System.out.println("Got " + set.size());
        for(DemoSecurityPermission perm : set) {
            if(perm.matchPermission(checkRole,checkCacheName,isCacheRatherThanInvoke,checkReadOnly)) {
                result = true;
                return;
            }
        }
    }

    @Override public Object getResult() { return result; }
}
