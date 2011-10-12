package org.gridman.coherence.pof;

import com.tangosol.net.CacheFactory;
import com.tangosol.net.NamedCache;
import org.gridman.testtools.coherence.classloader.ClusterInfo;
import org.gridman.testtools.coherence.classloader.ClusterStarter;
import org.gridman.testtools.junit.IsolationRunner;
import org.gridman.testtools.junit.RunIsolated;
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
public class PofClusterTest {

    private final ClusterStarter clusterStarter = ClusterStarter.getInstance();
    private String clusterFile;
    private ClusterInfo clusterInfo;
    @Before
    public void startSecureCluster() throws Exception {
        clusterFile = "/coherence/pof/pof-test-cluster.properties";
        clusterInfo = new ClusterInfo(clusterFile);
        clusterStarter.ensureCluster(clusterInfo);
        CacheFactory.shutdown();
    }

    @After
    public void stopSecureCluster() {
        clusterStarter.shutdown(clusterInfo);
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

        CacheFactory.shutdown();
        
        assertThat(retrievedValue, is(value));
    }

}
