package org.gridman.coherence.security.demo;

import com.tangosol.net.Invocable;
import org.apache.log4j.Logger;
import org.gridman.coherence.security.simple.CoherenceUtils;
import org.gridman.coherence.security.simple.InvocationSecurityProvider;

import javax.security.auth.Subject;

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
        return DemoServer.checkPermission(CoherenceUtils.getFirstPrincipalName(subject),invocable.getClass().getName(),false,true);
    }

}
