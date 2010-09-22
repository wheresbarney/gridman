package org.gridman.coherence.security.simple;

import com.tangosol.net.*;

import java.util.Set;
import java.util.Map;

import org.apache.log4j.Logger;

/**
 * For checking restricting access to Invoke Service.
 * @author Andrew Wilson
 */
public class CheckInvokeServiceProxy extends WrapperInvocationService  {
    private static final Logger logger = Logger.getLogger(CheckInvokeServiceProxy.class);

    public CheckInvokeServiceProxy(InvocationService invocationService) {
        super(invocationService);
        logger.debug("CheckInvokeServiceProxy()");
    }

    @Override public void execute(Invocable invocable, Set set, InvocationObserver invocationObserver) {      
        logger.debug("execute invocable : " + invocable + " set : " + set + " invocationObserver : " + invocationObserver);
        check();
        super.execute(invocable,set,invocationObserver);
    }

    @Override public Map query(Invocable invocable, Set set) {
        logger.debug("query : invocable : " + invocable + " set : " + set);
        check();
        return super.query(invocable,set);
    }

    // This checks whether they are allowed.
    private void check() {
        if(CoherenceUtils.checkFirstPrincipalName(SecurityTest.DISALLOWED_INVOKE)) {
            throw new SecurityException("Not allowed");
        }
    }
}  
