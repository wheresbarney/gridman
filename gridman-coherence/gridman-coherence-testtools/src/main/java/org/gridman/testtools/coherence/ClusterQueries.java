package org.gridman.testtools.coherence;

import com.tangosol.net.CacheFactory;
import com.tangosol.net.Cluster;
import com.tangosol.net.Member;

import java.util.HashSet;
import java.util.Set;

/**
 * @author Jonathan Knight
 */
public class ClusterQueries {
    public static final String NAME = ClusterQueries.class.getName();

    public static final String CLUSTER_SIZE = "clusterSize";
    public static final String MEMBER_SET = "memberSet";
    public static final String LOCAL_MEMBER = "localMember";

    public int clusterSize() {
        int size = 0;
        Cluster cluster = CacheFactory.getCluster();
        if (cluster != null) {
            size = cluster.getMemberSet().size();
        }
        return size;
    }

    @SuppressWarnings({"unchecked"})
    public Set<String> memberSet() {
        Set<String> members = new HashSet<String>();

        Cluster cluster = CacheFactory.getCluster();
        if (cluster != null) {
            for (Member member : (Set<Member>)cluster.getMemberSet()) {
                members.add(member.toString());
            }
        }

        return members;
    }

    public String localMember() {
        String localMember = "null";
        Cluster cluster = CacheFactory.getCluster();
        if (cluster != null) {
            localMember = String.valueOf(cluster.getLocalMember());
        }
        return localMember;
    }

}
