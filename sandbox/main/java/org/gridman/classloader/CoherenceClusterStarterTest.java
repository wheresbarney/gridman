package org.gridman.classloader;

import junit.framework.TestCase;
import com.tangosol.net.CacheFactory;

import java.util.Date;

/**
 *
 * 
 */
public class CoherenceClusterStarterTest extends TestCase {
    public void testClusterPass() throws Throwable {
        CoherenceClusterStarter cluster = CoherenceClusterStarter.getInstance();
        System.out.println("*** 1 : " + new Date());
        cluster.setCluster("/coherence/classloader/cluster.properties");
        System.out.println("*** 2 : " + new Date());
        cluster.shutdown();
        System.out.println("*** 3 : " + new Date());
        cluster.setCluster("/coherence/classloader/cluster.properties");
        System.out.println("*** 4 : " + new Date());
        CacheFactory.getCache("test").put(1,"A");
    }

    public void testClusterFail() {}
}
