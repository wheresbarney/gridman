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

    public SimpleInvokeServiceProxy(InvocationService invocationService) {
        super(invocationService);
        logger.debug("CheckInvokeServiceProxy()");
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
        if(!check(invocable)) {
            throw new SecurityException("Failed Invoke : " + CoherenceUtils.getCurrentFirstPrincipalName() + " invocable : " + invocable);
        }
    }

    // This checks whether they are allowed, override accordingly.
    protected boolean check(Invocable invocable) { throw new UnsupportedOperationException("Implement"); }
}
