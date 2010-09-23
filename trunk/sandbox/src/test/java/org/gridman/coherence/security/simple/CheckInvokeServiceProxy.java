package org.gridman.coherence.security.simple;

import com.tangosol.net.*;

import org.apache.log4j.Logger;

/**
 * For checking restricting access to Invoke Service.
 * @author Andrew Wilson
 */
public class CheckInvokeServiceProxy extends SimpleInvokeServiceProxy  {
    private static final Logger logger = Logger.getLogger(CheckInvokeServiceProxy.class);

    public CheckInvokeServiceProxy(InvocationService invocationService) {
        super(invocationService);
        logger.debug("CheckInvokeServiceProxy()");
    }

    // This checks whether they are allowed.
    private void check() {
        if(CoherenceUtils.checkFirstPrincipalName(SecurityTest.DISALLOWED_INVOKE)) {
            throw new SecurityException("Not allowed");
        }
    }
}  
