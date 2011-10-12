package org.gridman.coherence.security.simple;

import com.tangosol.net.Invocable;
import org.apache.log4j.Logger;

import javax.security.auth.Subject;

/**
 * The Demo Invoke Service Proxy.
 * This is how we secure Invocation.
 * I choose to do it by classname, but you could extend as much as you want.
 * eg. by parameters in the invocation service etc.
 */
public class SimpleSecurityInvokeProvider implements BaseSecurityInvocationProvider {
    public static final Logger logger = Logger.getLogger(SimpleSecurityCacheProvider.class);

    public SimpleSecurityInvokeProvider() throws Throwable {
        logger.debug(SimpleSecurityInvokeProvider.class.getName());
    }

    @Override public boolean checkInvocation(Subject subject, Invocable invocable) {
        return SimpleSecurityProvider.getInstance().checkPermission(CoherenceSecurityUtils.getFirstPrincipalName(subject), invocable.getClass().getName(), false, true);
    }

}
