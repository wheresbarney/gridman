package org.gridman.coherence.cachestore;

import org.gridman.testtools.coherence.classloader.ClusterInfo;
import org.gridman.testtools.coherence.classloader.ClusterStarter;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class WrapperBinaryCacheStoreTest {

    private ClusterStarter clusterStarter = ClusterStarter.getInstance();
    private ClusterInfo clusterInfo;

    @Before
    public void startSecureCluster() throws Exception {
        String clusterFile = "/coherence/common/common-cluster.properties";
        clusterInfo = new ClusterInfo(clusterFile);
        clusterStarter.ensureCluster(clusterInfo);
    }

    @After
    public void stopSecureCluster() {
        clusterStarter.shutdown(clusterInfo);
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
