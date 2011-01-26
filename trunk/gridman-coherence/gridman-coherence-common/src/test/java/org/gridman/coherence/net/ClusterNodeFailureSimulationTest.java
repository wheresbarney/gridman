package org.gridman.coherence.net;

import com.tangosol.net.CacheFactory;
import com.tangosol.net.Cluster;
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

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

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
    public void shouldCauseNodeDeparture() throws Exception {
        Object key = new DomainKey("Key-1");
        Object value = new DomainValue("JK");

        NamedCache cache = CacheFactory.getCache("dist-test");
        cache.put(key, value);

        System.err.println("Putting...");
        for (int i=0; i<1000; i++) {
            cache.put("key-" + i, "value-" + i);
        }

        Object before = clusterStarter.invoke(clusterFile, 0, 0, ClusterSize.class.getName(), "run");
        clusterStarter.suspendNetwork(clusterFile, 0, 1);
        Object after = clusterStarter.invoke(clusterFile, 0, 0, ClusterSize.class.getName(), "run");
        clusterStarter.shutdown(clusterFile, 0, 1);

        CacheFactory.shutdown();

        assertThat(String.valueOf(before), is("3"));
        assertThat(String.valueOf(after), is("2"));
    }

    public static class ClusterSize {
        public int run() {
            int size = 0;
            Cluster cluster = CacheFactory.getCluster();
            if (cluster != null) {
                size = cluster.getMemberSet().size();
            }
            return size;
        }
    }

}
