package org.gridman.coherence.net;

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
public class ClusterNodeFailureSimulationTest {

    private final ClusterStarter clusterStarter = ClusterStarter.getInstance();
    private ClusterInfo clusterInfo;
    private ClusterNodeGroup storageGroup;
    private ClusterNode nodeZero;

    @Before
    public void setup() throws Exception {
        clusterInfo = new ClusterInfo(COMMON_CLUSTER_FILE);
        storageGroup = clusterInfo.getGroup(COMMON_STORAGE_GROUP);
        clusterInfo.addNodeToGroup(storageGroup);

        nodeZero = clusterInfo.getNode(storageGroup, 0);

        clusterStarter.ensureCluster(clusterInfo);
    }

    @After
    public void stopSecureCluster() {
        clusterStarter.shutdown(clusterInfo);
    }

    @Test
    @RunIsolated(properties = {COMMON_CLIENT_PROPERTIES})
    @SuppressWarnings({"unchecked"})
    public void shouldCauseMemberDeparture() throws Exception {
        ClusterNode nodeToKill = clusterInfo.getNode(storageGroup, 1);

        Integer before = clusterStarter.invoke(nodeZero, clusterSize());
        String memberToKill = clusterStarter.invoke(nodeToKill, localMember());

        clusterStarter.killNode(nodeToKill);

        Integer after = clusterStarter.invoke(nodeZero, clusterSize());
        Set memberSetAfter = clusterStarter.invoke(nodeZero, memberSet());

        assertThat(before - after, is(1));
        assertThat(memberSetAfter.contains(memberToKill), isFalse());
    }

    @Test
    @RunIsolated(properties = {COMMON_CLIENT_PROPERTIES})
    @SuppressWarnings({"unchecked"})
    public void shouldCauseTwoMemberDepartures() throws Exception {
        ClusterNode nodeToKill_1 = clusterInfo.getNode(storageGroup, 1);
        ClusterNode nodeToKill_2 = clusterInfo.getNode(storageGroup, 2);

        Integer before = clusterStarter.invoke(nodeZero, clusterSize());
        String memberToKill_1 = clusterStarter.invoke(nodeToKill_1, localMember());
        String memberToKill_2 = clusterStarter.invoke(nodeToKill_2, localMember());

        clusterStarter.killNode(nodeToKill_1, nodeToKill_2);

        Integer after = clusterStarter.invoke(nodeZero, clusterSize());
        Set memberSetAfter = clusterStarter.invoke(nodeZero, memberSet());

        assertThat(before - after, is(2));
        assertThat(memberSetAfter.contains(memberToKill_1), isFalse());
        assertThat(memberSetAfter.contains(memberToKill_2), isFalse());
    }

    //@Test
    @RunIsolated(properties = {COMMON_CLIENT_PROPERTIES})
    @SuppressWarnings({"unchecked"})
    public void shouldCauseMemberDepartureAndAllowRejoin() throws Exception {
        ClusterNode nodeToKill = clusterInfo.getNode(storageGroup, 1);

        Integer before = clusterStarter.invoke(nodeZero, clusterSize());

        clusterStarter.killNode(nodeToKill);
        clusterStarter.ensureServerInstance(nodeToKill);

        Integer after = clusterStarter.invoke(nodeZero, clusterSize());

        assertThat(before, is(after));
    }
}
