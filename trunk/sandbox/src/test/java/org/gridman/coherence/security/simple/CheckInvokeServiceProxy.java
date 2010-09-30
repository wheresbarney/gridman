package org.gridman.coherence.security.simple;

import com.tangosol.net.*;

import org.apache.log4j.Logger;

import javax.security.auth.Subject;

/**
 * For checking restricting access to Invoke Service.
 * @author Andrew Wilson
 */
public class CheckInvokeServiceProxy implements InvocationSecurityProvider  {
    private static final Logger logger = Logger.getLogger(CheckInvokeServiceProxy.class);

    public CheckInvokeServiceProxy() {
        logger.debug("CheckInvokeServiceProxy()");
    }

    // This checks whether they are allowed.
    @Override public boolean checkInvocation(Subject subject, Invocable invocable) {
        if(CoherenceSecurityUtils.checkFirstPrincipalName(SecurityTest.DISALLOWED_INVOKE)) {
            throw new SecurityException("Not allowed");
        }
        return true;
    }
}  
