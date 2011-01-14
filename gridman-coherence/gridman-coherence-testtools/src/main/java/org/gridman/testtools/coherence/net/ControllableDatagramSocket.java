package org.gridman.testtools.coherence.net;

import com.tangosol.net.CacheFactory;
import com.tangosol.net.Cluster;
import com.tangosol.net.Member;

import java.io.IOException;
import java.net.*;

public class ControllableDatagramSocket extends DatagramSocket {

    public ControllableDatagramSocket() throws SocketException {
    }

    public ControllableDatagramSocket(SocketAddress bindaddr) throws SocketException {
        super(bindaddr);
    }

    public ControllableDatagramSocket(int port) throws SocketException {
        super(port);
    }

    public ControllableDatagramSocket(int port, InetAddress laddr) throws SocketException {
        super(port, laddr);
    }

    @Override
    public void send(DatagramPacket p) throws IOException {
        PacketController.check();
        super.send(p);
    }

    @Override
    public void receive(DatagramPacket p) throws IOException {
        PacketController.check();
        super.receive(p);
    }

}
