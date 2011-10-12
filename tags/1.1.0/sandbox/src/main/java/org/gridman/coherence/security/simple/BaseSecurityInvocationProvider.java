package org.gridman.coherence.security.simple;

import com.tangosol.net.Invocable;

import javax.security.auth.Subject;

/**
 * This is the interface for providing security to Invocation
 */
public interface BaseSecurityInvocationProvider {
    /**     
     * @param subject - the subject that was requested.
     * @param invocable - the invocable
     * @return result - are they allowed or not.
     */
    public boolean checkInvocation(Subject subject, Invocable invocable);

}
