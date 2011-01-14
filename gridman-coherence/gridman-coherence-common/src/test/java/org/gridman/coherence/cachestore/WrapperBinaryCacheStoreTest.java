package org.gridman.coherence.cachestore;

import com.tangosol.net.CacheFactory;
import com.tangosol.net.NamedCache;
import org.gridman.testtools.classloader.SystemPropertyLoader;
import org.gridman.testtools.coherence.classloader.ClusterStarter;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class WrapperBinaryCacheStoreTest {

    private final ClusterStarter clusterStarter = ClusterStarter.getInstance();
    private String clusterFile;

    @Before
    public void startSecureCluster() throws Exception {
        clusterFile = "/coherence/common/common-cluster.properties";
        clusterStarter.ensureCluster(clusterFile);
    }

    @After
    public void stopSecureCluster() {
        clusterStarter.shutdown(clusterFile);
    }

    @Test
    public void shouldWork() throws Exception {
//        SystemPropertyLoader.loadSystemProperties("/coherence/common/common-client.properties");
//
//        NamedCache cache = CacheFactory.getCache("dist-test");
//
//        for (int i=0; i<10; i++) {
//            if (i%2 == 0) {
//                cache.put("key-" + i, "value-" +i);
//            }
//        }
//
//        System.err.println("*************");
//        for (int i=0; i<10; i++) {
//            Object value = cache.get("key-" + i);
//            System.err.println("key-" + i + " value=" + value);
//        }
//        for (int i=0; i<10; i++) {
//            Object value = cache.get("key-" + i);
//            System.err.println("key-" + i + " value=" + value);
//        }
//        System.err.println("*************");
    }
}
