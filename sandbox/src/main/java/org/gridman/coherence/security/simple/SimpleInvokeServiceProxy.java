package org.gridman.coherence.security.simple;

import com.tangosol.net.Invocable;
import com.tangosol.net.InvocationObserver;
import com.tangosol.net.InvocationService;
import com.tangosol.net.WrapperInvocationService;
import org.apache.log4j.Logger;

import java.util.Map;
import java.util.Set;

/**
 * 
 */
public class SimpleInvokeServiceProxy extends WrapperInvocationService {
    private static final Logger logger = Logger.getLogger(SimpleInvokeServiceProxy.class);

    private InvocationSecurityProvider invocationSecurityProvider;

    public SimpleInvokeServiceProxy(InvocationService invocationService, InvocationSecurityProvider invocationSecurityProvider) {
        super(invocationService);
        this.invocationSecurityProvider = invocationSecurityProvider;
        logger.debug("CheckInvokeServiceProxy()");
    }

    public SimpleInvokeServiceProxy(InvocationService invocationService, String invocationSecurityProviderClass) throws Exception {
        this(invocationService, (InvocationSecurityProvider)Class.forName(invocationSecurityProviderClass).newInstance());
    }


    @Override public void execute(Invocable invocable, Set set, InvocationObserver invocationObserver) {
        logger.debug("execute invocable : " + invocable + " set : " + set + " invocationObserver : " + invocationObserver);
        checkInternal(invocable);
        super.execute(invocable,set,invocationObserver);
    }

    @Override public Map query(Invocable invocable, Set set) {
        logger.debug("query : invocable : " + invocable + " set : " + set);
        checkInternal(invocable);
        return super.query(invocable,set);
    }

    private void checkInternal(Invocable invocable) {
        if(!invocationSecurityProvider.checkInvocation(CoherenceSecurityUtils.getCurrentSubject(),invocable)) {
            throw new SecurityException("Failed Invoke : " + CoherenceSecurityUtils.getCurrentFirstPrincipalName() + " invocable : " + invocable);
        }
    }
}
