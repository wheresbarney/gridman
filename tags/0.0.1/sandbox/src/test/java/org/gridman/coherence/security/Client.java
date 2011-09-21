package org.gridman.coherence.security;

import com.tangosol.net.CacheFactory;
import com.tangosol.net.NamedCache;
import org.gridman.security.JaasHelper;

import javax.security.auth.Subject;
import java.security.PrivilegedExceptionAction;

/**
 * @author Jonathan Knight
 */
public class Client {
    public static void main(String[] args) {

        PrivilegedExceptionAction<Object> action = new PrivilegedExceptionAction<Object>() {
            @Override
            public Object run() throws Exception {
                NamedCache cache1 = CacheFactory.getCache("one-test");

                NamedCache cache2 = CacheFactory.getCache("two-test");
                return null;
            }
        };

        Subject subject = JaasHelper.logon("Coherence", "knightj", "Secret1");
        JaasHelper.doAs(subject, action);
        System.err.println("Done");
    }
}
