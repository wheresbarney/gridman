package org.gridman.coherence.security.demo;

import com.tangosol.net.CacheFactory;
import com.tangosol.net.Invocable;
import com.tangosol.net.InvocationService;
import com.tangosol.net.Member;
import org.apache.log4j.Logger;
import org.gridman.coherence.security.simple.CoherenceUtils;
import org.gridman.coherence.security.simple.InvocationSecurityProvider;

import javax.security.auth.Subject;
import java.util.Collections;
import java.util.Map;
import java.util.Set;

/**
 * The Demo Invoke Service Proxy.
 * This is how we secure Invocation.
 * I choose to do it by classname, but you could extend as much as you want.
 * eg. by parameters in the invocation service etc.
 */
public class DemoInvokeServiceProvider implements InvocationSecurityProvider {
    public static final Logger logger = Logger.getLogger(DemoCacheSecurityProvider.class);

    public DemoInvokeServiceProvider() throws Throwable {
        logger.debug(DemoInvokeServiceProvider.class.getName());
    }

    @Override public boolean checkInvocation(Subject subject, Invocable invocable) {
        String checkRole = CoherenceUtils.getFirstPrincipalName(CoherenceUtils.getCurrentSubject());
        InvocationService service = (InvocationService) CacheFactory.getService(DemoServer.SERVER_INVOKE_SERVICE);
        Set<Member> localMemberSet = Collections.singleton(CacheFactory.getCluster().getLocalMember());
        Map map = service.query(new DemoCachePermissionInvoke(checkRole, invocable.getClass().getName(), false, true), localMemberSet);
        return (Boolean)map.values().iterator().next();
    }

}
