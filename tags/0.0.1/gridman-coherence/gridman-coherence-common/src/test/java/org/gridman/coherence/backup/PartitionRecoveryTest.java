package org.gridman.coherence.backup;

import org.gridman.testtools.coherence.CoherenceTestConstants;
import org.gridman.testtools.coherence.classloader.ClusterInfo;
import org.gridman.testtools.coherence.classloader.ClusterNode;
import org.gridman.testtools.coherence.classloader.ClusterNodeGroup;
import org.gridman.testtools.coherence.classloader.ClusterStarter;
import org.gridman.testtools.junit.IsolationRunner;
import org.gridman.testtools.kerberos.RunIsolated;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Set;

import static org.gridman.testtools.Matchers.isFalse;
import static org.gridman.testtools.coherence.CoherenceTestConstants.*;
import static org.gridman.testtools.coherence.queries.ClusterQueries.*;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * @author Jonathan Knight
 */
@RunWith(IsolationRunner.class)
public class PartitionRecoveryTest {

    private final ClusterStarter clusterStarter = ClusterStarter.getInstance();
    private ClusterInfo clusterInfo;
    private ClusterNodeGroup storageGroup;
    private ClusterNodeGroup extendProxyGroup;
    private ClusterNode nodeZero;

    @Before
    public void setup() throws Exception {
        clusterInfo = new ClusterInfo(COMMON_CLUSTER_FILE);
        storageGroup = clusterInfo.getGroup(COMMON_STORAGE_GROUP);
        extendProxyGroup = clusterInfo.getGroup(COMMON_EXTENDPROXY_GROUP);
        nodeZero = clusterInfo.getNode(storageGroup, 0);

        storageGroup.overrideProperty("tangosol.coherence.cacheconfig", "coherence/backup/storage-node-config.xml")
                    .overrideProperty("backup.bdb.directory", "target/bdb");
        
        extendProxyGroup.overrideProperty("tangosol.coherence.cacheconfig", "coherence/backup/extend-proxy-config.xml");

        clusterStarter.ensureCluster(clusterInfo);
    }

    @After
    public void stopSecureCluster() {
        clusterStarter.shutdown(clusterInfo);
    }

    @Test
    @RunIsolated(properties = {CoherenceTestConstants.COMMON_CLIENT_PROPERTIES})
    @SuppressWarnings({"unchecked"})
    public void shouldWork() throws Exception {
        ClusterNode nodeToKill = clusterInfo.getNode(storageGroup, 1);
                                                      
        Integer before = clusterStarter.invoke(nodeZero, clusterSize());
        String memberToKill = clusterStarter.invoke(nodeToKill, localMember());

        clusterStarter.killNode(nodeToKill);

        Integer after = clusterStarter.invoke(nodeZero, clusterSize());
        Set memberSetAfter = clusterStarter.invoke(nodeZero, memberSet());

        assertThat(before - after, is(1));
        assertThat(memberSetAfter.contains(memberToKill), isFalse());
    }

}
