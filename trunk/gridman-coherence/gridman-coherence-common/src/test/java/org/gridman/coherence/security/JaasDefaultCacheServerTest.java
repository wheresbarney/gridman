package org.gridman.coherence.security;

import com.sun.security.auth.module.Krb5LoginModule;
import com.tangosol.net.CacheFactory;
import com.tangosol.net.Cluster;
import org.apache.directory.server.core.integ.Level;
import org.apache.directory.server.core.integ.annotations.ApplyLdifFiles;
import org.apache.directory.server.core.integ.annotations.CleanupLevel;
import org.gridman.security.GridManCallbackHandler;
import org.gridman.security.JaasHelper;
import org.gridman.testtools.classloader.IsolatedAction;
import org.gridman.testtools.classloader.PropertyIsolation;
import org.gridman.testtools.classloader.SystemPropertyLoader;
import org.gridman.testtools.coherence.classloader.ClusterInfo;
import org.gridman.testtools.coherence.classloader.ClusterProperties;
import org.gridman.testtools.coherence.classloader.ClusterStarter;
import org.gridman.testtools.coherence.classloader.JaasClusterClassloaderLifecycle;
import org.gridman.testtools.kerberos.*;
import org.gridman.testtools.kerberos.apacheds.ApacheDsServerContext;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Properties;
import java.util.Set;

import static org.junit.Assert.assertEquals;

/**
 * @author Jonathan Knight
 */
@RunWith(KiRunner.class)
@KdcType(type = ApacheDsServerContext.class)
@CleanupLevel(Level.CLASS)
@DirectoryPartition(suffixes = "dc=example,dc=com")
@ApplyLdifFiles( value = {"/gridman.ldif"})
@Krb5( credentials = {
        "knightj@EXAMPLE.COM,secret",
        "awilson@EXAMPLE.COM,secret",
        "cacheserver@EXAMPLE.COM,secret",
        "thomas@EXAMPLE.COM,secret"
       }
     , keytabFile = "jk-test.keytab"
     , krb5Conf = "jk-krb5.conf"
     , kdcPort = 60088
     , realm = "EXAMPLE.COM"
)
@JAAS( fileName = "jk-test-jaas.conf"
     , moduleName = "Coherence"
     , loginModuleClass = Krb5LoginModule.class
     , settings = {
        "required",
        "debug=true",
        "doNotPrompt=false",
        "storeKey=true",
        "realm=\"EXAMPLE.COM\""
     }
)
public class JaasDefaultCacheServerTest {

    private final ClusterStarter clusterStarter = ClusterStarter.getInstance();

    private Cluster cluster;

    @Before
    public void startIsolatedClusterMember() throws Exception {

        Properties localProperties = SystemPropertyLoader.loadProperties(
                "/coherence/default.properties",
                "/coherence/security/kerberos/common-server.properties",
                "/coherence/security/kerberos/storage-node.properties");
        localProperties.setProperty(JaasHelper.PROP_JAAS_MODULE, "Coherence");
        localProperties.setProperty(GridManCallbackHandler.PROP_USERNAME, "knightj");
        localProperties.setProperty(GridManCallbackHandler.PROP_PASSWORD, "secret");

        cluster = PropertyIsolation.runIsolated(localProperties, new IsolatedAction<Cluster>() {
            public Cluster run() {
                Cluster cluster = CacheFactory.ensureCluster();
                CacheFactory.getCacheFactoryBuilder().getConfigurableCacheFactory(null);
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

        Properties defaultProperties = SystemPropertyLoader.loadProperties("/coherence/default.properties");

        cluster.addNodeWithServers(2, JaasClusterClassloaderLifecycle.class)
                //.useDefaultServerProperties()
                .withProperties(defaultProperties)
                .withProperties(SystemPropertyLoader.loadProperties("/coherence/security/kerberos/common-server.properties"))
                .withProperties(SystemPropertyLoader.loadProperties("/coherence/security/kerberos/storage-node.properties"))
                .withProperty(JaasHelper.PROP_JAAS_MODULE, "Coherence")
                .withProperty(GridManCallbackHandler.PROP_USERNAME, "knightj")
                .withProperty(GridManCallbackHandler.PROP_PASSWORD, "secret")
                .withCoherenceOverride("/coherence/security/kerberos/secure-extend-override.xml")
                .withCacheConfig("/coherence/security/kerberos/secured-storage-node-config.xml");

        cluster.addNodeWithServers(1, JaasClusterClassloaderLifecycle.class)
                //.useDefaultServerProperties()
                .withProperties(defaultProperties)
                .withProperties(SystemPropertyLoader.loadProperties("/coherence/security/kerberos/common-server.properties"))
                .withProperties(SystemPropertyLoader.loadProperties("/coherence/security/kerberos/extend-proxy.properties"))
                .withProperty(JaasHelper.PROP_JAAS_MODULE, "Coherence")
                .withProperty(GridManCallbackHandler.PROP_USERNAME, "knightj")
                .withProperty(GridManCallbackHandler.PROP_PASSWORD, "secret")
                .withCoherenceOverride("/coherence/security/kerberos/secure-extend-override.xml")
                .withCacheConfig("/coherence/security/kerberos/secured-extend-proxy-config.xml")
                .withDistributedLocalStorage(false)
                .withProxyEnabled(true);

        String identifier = "test.cluster";
        ClusterInfo clusterInfo = new ClusterInfo(identifier, cluster.asProperties());
        clusterStarter.ensureCluster(clusterInfo);
        int membersStarted = getClusterSize();

        clusterStarter.shutdown(clusterInfo);
        int finalSize = getClusterSize();

        assertEquals("Expected to start 3 members", 3, membersStarted);
        assertEquals("Expected to shutdown all members", 0, finalSize);
    }

    @Test
    public void shouldStartAndStopClusterGroup() {
        String clusterFile = "/coherence/security/kerberos/secure-cluster.properties";

        ClusterInfo clusterInfo = new ClusterInfo(clusterFile);
        clusterInfo.overrideProperty(JaasHelper.PROP_JAAS_MODULE, "Coherence")
                   .overrideProperty(GridManCallbackHandler.PROP_USERNAME, "knightj")
                   .overrideProperty(GridManCallbackHandler.PROP_PASSWORD, "secret");

        clusterStarter.ensureAllServersInClusterGroup(clusterInfo.getGroup(0));

        int membersStarted = getClusterSize();

        clusterStarter.shutdown(clusterInfo.getGroup(0));
        int finalSize = getClusterSize();

        assertEquals("Expected to start 2 members", 2, membersStarted);
        assertEquals("Expected to shutdown all members", 0, finalSize);
    }
}
