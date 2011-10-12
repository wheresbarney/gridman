package org.gridman.testtools.coherence.queries;

import java.util.Set;

/**
 * @author Jonathan Knight
 */
public class ClusterQueries {

    public static ClusterQuery<Integer> clusterSize() {
        return new ClusterSize();
    }

    public static ClusterQuery<Set<String>> memberSet() {
        return new MemberSet();
    }

    public static ClusterQuery<String> localMember() {
        return new LocalMember();
    }
    
}
