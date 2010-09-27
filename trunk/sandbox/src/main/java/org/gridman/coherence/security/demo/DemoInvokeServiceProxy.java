package org.gridman.coherence.security.demo;

import com.tangosol.net.CacheFactory;
import com.tangosol.net.Invocable;
import com.tangosol.net.InvocationService;
import com.tangosol.net.Member;
import org.gridman.coherence.security.simple.CoherenceUtils;
import org.gridman.coherence.security.simple.SimpleInvokeServiceProxy;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

/**
 * The Demo Invoke Service Proxy.
 * This is how we secure Invocation.
 * I choose to do it by classname, but you could extend as much as you want.
 * eg. by parameters in the invocation service etc.
 */
public class DemoInvokeServiceProxy extends SimpleInvokeServiceProxy {

    public DemoInvokeServiceProxy(InvocationService invocationService) throws Throwable {
        super(invocationService);
    }

    @Override protected boolean check(Invocable invocable) {
        String checkRole = CoherenceUtils.getFirstPrincipalName(CoherenceUtils.getCurrentSubject());
        InvocationService service = (InvocationService) CacheFactory.getService(DemoServer.SERVER_INVOKE_SERVICE);
        Set<Member> localMemberSet = Collections.singleton(CacheFactory.getCluster().getLocalMember());
        Map map = service.query(new DemoCachePermissionInvoke(checkRole, invocable.getClass().getName(), false, false), localMemberSet);
        return (Boolean)map.values().iterator().next();
    }
}
