package org.gridman.testtools.coherence.queries;

import com.tangosol.net.CacheFactory;
import com.tangosol.net.Cluster;

/**
 * @author Jonathan Knight
 */
public class ClusterSize extends BaseQuery<Integer> {

    public Integer run() {
        int size = 0;
        Cluster cluster = CacheFactory.getCluster();
        if (cluster != null) {
            size = cluster.getMemberSet().size();
        }
        return size;
    }
}
