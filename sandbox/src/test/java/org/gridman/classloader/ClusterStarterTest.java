package org.gridman.classloader;

import com.tangosol.net.CacheFactory;
import com.tangosol.net.Cluster;
import org.gridman.classloader.coherence.CoherenceClassloaderLifecycle;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.Properties;
import java.util.Set;

import static org.junit.Assert.assertEquals;

/**
 *
 *  @author Jonathan Knight
 */
public class ClusterStarterTest {

	private final ClusterStarter clusterStarter = ClusterStarter.getInstance();

    private Cluster cluster;

	@Before
    public void startIsolatedClusterMember() throws Exception {

        Properties localProperties = SystemPropertyLoader.loadProperties(
                          "/coherence/default.properties",
                          "/coherence/simple/simpleCommon.properties",
                          "/coherence/simple/simpleServer.properties");

        // Start an isolated Cluster node with the above properties
        // We can then use this to check member counts etc in our tests
        cluster = PropertyIsolation.runIsolated(localProperties, new IsolatedAction<Cluster>() {
            public Cluster run() {
                Cluster cluster = CacheFactory.ensureCluster();
                while (!cluster.isRunning()) {
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException _ignored) { /* ignored */ }
                }
                return cluster;
            }
        });
    }

    @After
    public void stopIsolatedClusterMember() {
        cluster.shutdown();
        while (cluster.isRunning()) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException _ignored) { /* ignored */ }
        }
        cluster = null;
    }

    public int waitForSize(int expected, long timeout) {
        expected += 1;
        long start = System.currentTimeMillis();
        Set members = cluster.getMemberSet();
        while (members.size() != expected) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException _) {
                // ignored
            }
            long now = System.currentTimeMillis();
            if (timeout < (now - start)) {
                break;
            }
            members = cluster.getMemberSet();
        }

        return members.size();
    }

    public int getClusterSize() {
        // subtract 1 from the size to account for the node we started in
        // the startCluster() method.
        // We only want to assert the correct number started/stopped by
        // the ClusterStarter.
        Set members = cluster.getMemberSet();
        return members.size() - 1;
    }

	@Test
	public void shouldStartAndStopClusterUsingAPropertiesInstanceBuiltProgrammatically() {
		ClusterProperties cluster = new ClusterProperties();

		cluster.addNodeWithServers(2, CoherenceClassloaderLifecycle.class)
				.useDefaultServerProperties()
				.withCacheConfig("coherence/simple/simpleServerConfig.xml");

		cluster.addNodeWithServers(1, CoherenceClassloaderLifecycle.class)
				.useDefaultServerProperties()
				.withCacheConfig("coherence/simple/simpleServerConfig.xml")
				.withDistributedLocalStorage(false)
				.withProxyEnabled(true);

		String identifier = "test.cluster";
		clusterStarter.ensureCluster(identifier, cluster.asProperties());
        waitForSize(3, 30000);
		int membersStarted = getClusterSize();

		clusterStarter.shutdown(identifier);
        waitForSize(0, 30000);
		int finalSize = getClusterSize();

		assertEquals("Expected to start 3 members", 3, membersStarted);
		assertEquals("Expected to shutdown all members", 0, finalSize);
	}

    @Test
    public void shouldStartAndStopClusterUsingAClusterFile() {
        String clusterFile = "/coherence/classloader/cluster.properties";

        clusterStarter.ensureCluster(clusterFile);
        waitForSize(3, 30000);
        int membersStarted = getClusterSize();

        clusterStarter.shutdown(clusterFile);
        waitForSize(0, 30000);
        int finalSize = getClusterSize();

		assertEquals("Expected to start 3 members", 3, membersStarted);
        assertEquals("Expected to shutdown all members", 0, finalSize);
    }

    @Test
    public void shouldStartAndStopClusterGroup() {
        String clusterFile = "/coherence/classloader/cluster.properties";
        Properties properties = SystemPropertyLoader.getSystemProperties(clusterFile);

        assertEquals(0, getClusterSize());
        
        clusterStarter.ensureAllServersInClusterGroup(clusterFile, properties, 0);
        waitForSize(2, 30000);
        int membersStarted = getClusterSize();

        clusterStarter.shutdown(clusterFile, 0);
        waitForSize(0, 30000);
        int finalSize = getClusterSize();

        assertEquals("Expected to start 2 members", 2, membersStarted);
        assertEquals("Expected to shutdown all members", 0, finalSize);
    }

    @Test
    public void shouldStartAndStopSingleInstance() {
        String clusterFile = "/coherence/classloader/cluster.properties";
        Properties properties = SystemPropertyLoader.getSystemProperties(clusterFile);

        clusterStarter.ensureServerInstance(clusterFile, properties, 0, 0);
        waitForSize(1, 30000);
        int membersStarted = getClusterSize();
        clusterStarter.shutdown(clusterFile, 0, 0);
        waitForSize(0, 30000);
        int finalSize = getClusterSize();
        assertEquals("Expected to start single member", 1, membersStarted);
        assertEquals("Expected to shutdown all members", 0, finalSize);
    }

    @Test
    public void shouldStartWholeClusterAndStopGroup() {
        String clusterFile = "/coherence/classloader/cluster.properties";

        clusterStarter.ensureCluster(clusterFile);
        waitForSize(3, 30000);
        int membersStarted = getClusterSize();

        clusterStarter.shutdown(clusterFile, 0);
        waitForSize(1, 30000);
        int afterGroupShutdownSize = getClusterSize();

        clusterStarter.shutdown(clusterFile);
        waitForSize(0, 30000);
        int finalSize = getClusterSize();

		assertEquals("Expected to start 3 members", 3, membersStarted);
        assertEquals("Expected 1 member after shutdown of group 0", 1, afterGroupShutdownSize);
        assertEquals("Expected to shutdown all members", 0, finalSize);
    }

    @Test
    public void shouldStartWholeClusterAndStopSingleNode() {
        String clusterFile = "/coherence/classloader/cluster.properties";

        clusterStarter.ensureCluster(clusterFile);
        waitForSize(3, 30000);
        int membersStarted = getClusterSize();

        clusterStarter.shutdown(clusterFile, 0, 1);
        waitForSize(2, 30000);
        int afterNodeShutdownSize = getClusterSize();

        clusterStarter.shutdown(clusterFile);
        waitForSize(0, 30000);
        int finalSize = getClusterSize();

		assertEquals("Expected to start 3 members", 3, membersStarted);
        assertEquals("Expected 2 members after shutdown of node", 2, afterNodeShutdownSize);
        assertEquals("Expected to shutdown all members", 0, finalSize);
    }
}
