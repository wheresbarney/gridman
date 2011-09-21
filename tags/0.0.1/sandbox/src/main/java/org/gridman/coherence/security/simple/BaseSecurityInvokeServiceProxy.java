package org.gridman.coherence.security.simple;

import com.tangosol.net.Invocable;
import com.tangosol.net.InvocationObserver;
import com.tangosol.net.InvocationService;
import com.tangosol.net.WrapperInvocationService;
import com.tangosol.run.xml.XmlElement;
import org.apache.log4j.Logger;

import java.util.Map;
import java.util.Set;

/**
 * A simple Security Proxy for the Invocation Service. 
 */
public class BaseSecurityInvokeServiceProxy extends WrapperInvocationService {
    private static final Logger logger = Logger.getLogger(BaseSecurityInvokeServiceProxy.class);

    private BaseSecurityInvocationProvider invocationSecurityProvider;

    public BaseSecurityInvokeServiceProxy(InvocationService invocationService, BaseSecurityInvocationProvider invocationSecurityProvider) {
        super(invocationService);
        this.invocationSecurityProvider = invocationSecurityProvider;
        logger.debug("CheckInvokeServiceProxy()");
    }

    public BaseSecurityInvokeServiceProxy(InvocationService invocationService, String invocationSecurityProviderClass) throws Exception {
        this(invocationService, (BaseSecurityInvocationProvider)Class.forName(invocationSecurityProviderClass).newInstance());
    }

    public BaseSecurityInvokeServiceProxy(InvocationService invocationService, String invocationSecurityProviderClass, XmlElement xml) throws Exception {
        this(invocationService, (BaseSecurityInvocationProvider)Class.forName(invocationSecurityProviderClass).newInstance());
    }

    /**
     * This is not supported in the current version of Coherence over Extends, but might be in the future...
     */
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
