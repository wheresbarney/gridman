package org.gridman.coherence.backup;

import com.tangosol.net.CacheFactory;
import com.tangosol.net.Cluster;

/**
 * @author Jonathan Knight
 */
public class TestInvocable {

    public Object test() {
        Cluster cluster = CacheFactory.getCluster();
        if (cluster != null) {
            return String.valueOf(cluster.getLocalMember());
        }
        return "No Cluster";
    }

    public Object test(String msg) {
        return msg;
    }
}
