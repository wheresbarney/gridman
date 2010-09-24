package org.gridman.coherence.security.demo;

import com.tangosol.net.CacheFactory;
import com.tangosol.net.Invocable;
import com.tangosol.net.InvocationService;
import com.tangosol.net.NamedCache;
import org.gridman.coherence.security.simple.CoherenceUtils;
import org.gridman.coherence.security.simple.SimpleInvokeServiceProxy;

/**
 * The Demo Invoke Service Proxy.
 * This is how we secure Invocation.
 * I choose to do it by classname, but you could extend as much as you want.
 * eg. by parameters in the invocation service etc.
 */
public class DemoInvokeServiceProxy extends SimpleInvokeServiceProxy {
    private NamedCache permissionCache;

    public DemoInvokeServiceProxy(InvocationService invocationService) throws Throwable {
        super(invocationService);
        permissionCache = CacheFactory.getCache(SecurityPermission.PERMISSION_CACHE);
    }

    @Override protected void check(Invocable invocable) {
        String principal = CoherenceUtils.getFirstPrincipalName(CoherenceUtils.getCurrentSubject());
        boolean result = permissionCache.containsKey(new SecurityPermission(principal, invocable.getClass().getName(), SecurityPermission.PERMISSION_INVOKE));
        if(!result) {
            throw new SecurityException("Failed Invoke : " + principal);
        }
    }
}
