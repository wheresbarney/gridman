package org.gridman.coherence.security.demo.security;

import com.tangosol.net.CacheFactory;
import junit.framework.TestCase;
import org.gridman.classloader.CoherenceClusterStarter;

/**
 * @todo Get this working with FIT?
 */
public class SecurityTest extends TestCase {
    public void testSecurity() {
        // Set up the cluster.
        CoherenceClusterStarter.getInstance().ensureCluster("/coherence/security/securityCluster.properties");

        // Set up the permissions.

        System.out.println("About to calll....");


        // Check the permissions.
        CacheFactory.getCache("test").put(1,"A");


    }
}
