package org.gridman.testtools.coherence.net;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.MulticastSocket;
import java.net.SocketAddress;

public class ControllableMulticastSocket extends MulticastSocket {

    public ControllableMulticastSocket() throws IOException {
    }

    public ControllableMulticastSocket(SocketAddress bindaddr) throws IOException {
        super(bindaddr);
    }

    public ControllableMulticastSocket(int port) throws IOException {
        super(port);
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
