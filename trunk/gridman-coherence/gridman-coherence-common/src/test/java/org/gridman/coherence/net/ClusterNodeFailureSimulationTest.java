package org.gridman.coherence.net;

import com.tangosol.net.CacheFactory;
import com.tangosol.net.NamedCache;
import org.gridman.coherence.pof.DomainKey;
import org.gridman.coherence.pof.DomainValue;
import org.gridman.testtools.coherence.classloader.ClusterStarter;
import org.gridman.testtools.junit.IsolationRunner;
import org.gridman.testtools.kerberos.RunIsolated;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * @author Jonathan Knight
 */
@RunWith(IsolationRunner.class)
public class ClusterNodeFailureSimulationTest {

    private final ClusterStarter clusterStarter = ClusterStarter.getInstance();
    private String clusterFile;

    @Before
    public void startSecureCluster() throws Exception {
        clusterFile = "/coherence/pof/pof-test-cluster.properties";
        clusterStarter.ensureCluster(clusterFile);
    }

    @After
    public void stopSecureCluster() {
        clusterStarter.shutdown(clusterFile);
    }

    @Test
    @RunIsolated(properties = {
            "/coherence/pof/common-client.properties",
            "/coherence/pof/client.properties"
    })
    @SuppressWarnings({"unchecked"})
    public void shouldWork() throws Exception {
        Object key = new DomainKey("Key-1");
        Object value = new DomainValue("JK");

        NamedCache cache = CacheFactory.getCache("dist-test");
        cache.put(key, value);
        Object retrievedValue = cache.get(key);

        System.err.println("Putting...");
        for (int i=0; i<1000; i++) {
            cache.put("key-" + i, "value-" + i);
        }

        System.err.println("Suspending...");
        clusterStarter.suspendNetwork(clusterFile, 0, 1);
        clusterStarter.shutdown(clusterFile, 0, 1);

        System.err.println("Sleeping...");
        Thread.sleep(5000);

        clusterStarter.unsuspendNetwork(clusterFile, 0, 1);

        System.err.println("Putting...");
        for (int i=0; i<1000; i++) {
            cache.put("key-" + i, "value-" + i);
        }

        System.err.println("sleeping...");
        Thread.sleep(5000);

        CacheFactory.shutdown();

//        assertThat(retrievedValue, is(value));
    }
}
