package org.gridman.testtools.coherence.queries;

import com.tangosol.net.CacheFactory;
import com.tangosol.net.Cluster;

/**
 * @author Jonathan Knight
 */
public class LocalMember extends BaseQuery<String> {

    @Override
    public String run() {
        String localMember = "null";
        Cluster cluster = CacheFactory.getCluster();
        if (cluster != null) {
            localMember = String.valueOf(cluster.getLocalMember());
        }
        return localMember;
    }

}
