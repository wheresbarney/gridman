package org.gridman.testtools.coherence.net;

import com.tangosol.net.CacheFactory;
import com.tangosol.net.Cluster;
import com.tangosol.net.Member;

import java.io.IOException;
import java.net.PortUnreachableException;

public class PacketController {

    public static boolean suspended = false;

    public static void suspend(boolean suspended) {
        PacketController.suspended = suspended;
        Member member = null;
        Cluster cluster = CacheFactory.getCluster();
        if (cluster != null) {
            member = cluster.getLocalMember();
        }
        System.err.println("DatagramSockets suspended=" + suspended + "member=" + member);
    }

    public static boolean isSuspended() {
        return suspended;
    }

    public static void check() throws IOException {
        if (suspended) {
            throw new PortUnreachableException("Network Suspended");
        }
    }
}
