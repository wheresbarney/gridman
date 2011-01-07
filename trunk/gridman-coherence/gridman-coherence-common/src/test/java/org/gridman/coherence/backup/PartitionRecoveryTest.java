package org.gridman.coherence.backup;

import com.tangosol.net.CacheFactory;
import com.tangosol.net.NamedCache;
import org.gridman.testtools.coherence.classloader.ClusterStarter;
import org.gridman.testtools.junit.IsolationRunner;
import org.gridman.testtools.kerberos.RunIsolated;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

/**
 * @author Jonathan Knight
 */
@RunWith(IsolationRunner.class)
public class PartitionRecoveryTest {

    private final ClusterStarter clusterStarter = ClusterStarter.getInstance();
    private String clusterFile;

    @Before
    public void startSecureCluster() throws Exception {
        clusterFile = "/coherence/backup/test-cluster.properties";
        clusterStarter
                .setProperty("backup.bdb.directory", "target/bdb")
                .ensureCluster(clusterFile);
    }

    @After
    public void stopSecureCluster() {
        clusterStarter.shutdown(clusterFile);
    }

    @Test
    @RunIsolated(properties = {
            "/coherence/backup/common-client.properties",
            "/coherence/backup/client.properties"
    })
    @SuppressWarnings({"unchecked"})
    public void shouldWork() throws Exception {
        Object key = "Key-jk";
        Object value = "JK";

        NamedCache cache = CacheFactory.getCache("dist-test");
        cache.put(key, value);
        for (int i=0; i<1000; i++) {
            cache.put("key-" + i, "value-" + i);
        }

        Object retrievedValue = cache.get(key);
        CacheFactory.shutdown();

        assertThat(retrievedValue, is(value));

        clusterStarter.shutdownNoWait(clusterFile, 0, 1);
        clusterStarter.shutdownNoWait(clusterFile, 0, 2);

        Thread.sleep(10000);

        for (int i=0; i<1000; i++) {
            System.err.println("Get: " + i + " = " + cache.get("key-" + i));
        }

        //Thread.sleep(60000);
    }

}
