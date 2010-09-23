package org.gridman.coherence.security.demo;

import com.tangosol.net.CacheFactory;
import com.tangosol.net.Invocable;
import com.tangosol.net.InvocationService;
import com.tangosol.net.NamedCache;
import org.gridman.coherence.security.simple.CoherenceUtils;
import org.gridman.coherence.security.simple.SimpleInvokeServiceProxy;

/**
 * The Demo Invoke Service Proxy.
 */
public class DemoInvokeServiceProxy extends SimpleInvokeServiceProxy {
    private NamedCache permissionCache;

    public DemoInvokeServiceProxy(InvocationService invocationService) throws Throwable {
        super(invocationService);
        try {
        permissionCache = CacheFactory.getCache(SecurityPermission.PERMISSION_CACHE);
        } catch(Throwable t) {
            t.printStackTrace();
            throw t;
        }
    }

    @Override protected void check(Invocable invocable) {
        String principal = CoherenceUtils.getFirstPrincipalName(CoherenceUtils.getCurrentSubject());
        boolean result = permissionCache.containsKey(new SecurityPermission(principal, SecurityPermission.INVOKE_SERVICE, SecurityPermission.PERMISSION_INVOKE));
        if(!result) {
            throw new SecurityException("Failed Invoke : " + principal);
        }
    }
}
