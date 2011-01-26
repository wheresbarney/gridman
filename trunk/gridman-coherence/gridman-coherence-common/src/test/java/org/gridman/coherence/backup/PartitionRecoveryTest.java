package org.gridman.coherence.backup;

import org.gridman.testtools.coherence.ClusterQueries;
import org.gridman.testtools.coherence.classloader.ClusterStarter;
import org.gridman.testtools.junit.IsolationRunner;
import org.gridman.testtools.kerberos.RunIsolated;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Set;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;


/**
 * @author Jonathan Knight
 */
@RunWith(IsolationRunner.class)
public class PartitionRecoveryTest {

    private final ClusterStarter clusterStarter = ClusterStarter.getInstance();
    private String clusterFile;
    private final int storageNodesGroupId = 0;
    private final int extendNodesGroupId = 1;
    private final int memberZeroId = 0;

    @Before
    public void startSecureCluster() throws Exception {
        clusterFile = "/coherence/common/common-cluster.properties";
        clusterStarter
                .overrideClusterProperty(clusterFile, storageNodesGroupId, "tangosol.coherence.cacheconfig", "coherence/backup/storage-node-config.xml")
                .overrideClusterProperty(clusterFile, storageNodesGroupId, "backup.bdb.directory", "target/bdb")
                .overrideClusterProperty(clusterFile, extendNodesGroupId, "tangosol.coherence.cacheconfig", "coherence/backup/extend-proxy-config.xml")
                .ensureCluster(clusterFile);
    }

    @After
    public void stopSecureCluster() {
        clusterStarter.shutdown(clusterFile);
    }

    @Test
    @RunIsolated(properties = {"/coherence/common/common-client.properties"})
    @SuppressWarnings({"unchecked"})
    public void shouldWork() throws Exception {
        final int memberToKillId = 1;

        Integer before = clusterStarter.invoke(clusterFile, storageNodesGroupId, memberZeroId, ClusterQueries.NAME, ClusterQueries.CLUSTER_SIZE);
        String memberToKill = clusterStarter.invoke(clusterFile, storageNodesGroupId, memberToKillId, ClusterQueries.NAME, ClusterQueries.LOCAL_MEMBER);

        clusterStarter.suspendNetwork(clusterFile, storageNodesGroupId, memberToKillId);
        clusterStarter.shutdown(clusterFile, storageNodesGroupId, memberToKillId);

        Integer after = clusterStarter.invoke(clusterFile, storageNodesGroupId, memberZeroId, ClusterQueries.NAME, ClusterQueries.CLUSTER_SIZE);
        Set memberSetAfter = clusterStarter.invoke(clusterFile, storageNodesGroupId, memberZeroId, ClusterQueries.NAME, ClusterQueries.MEMBER_SET);

        assertThat(before - after, is(1));
        assertThat(memberSetAfter.contains(memberToKill), is(false));
    }

}
