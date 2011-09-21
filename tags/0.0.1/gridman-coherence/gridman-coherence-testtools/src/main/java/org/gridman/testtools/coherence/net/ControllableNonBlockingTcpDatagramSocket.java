package org.gridman.testtools.coherence.net;

import com.tangosol.net.SocketProvider;
import com.tangosol.net.TcpDatagramSocket;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.SocketAddress;
import java.net.SocketException;

public class ControllableNonBlockingTcpDatagramSocket extends TcpDatagramSocket {

    public ControllableNonBlockingTcpDatagramSocket() throws SocketException {
    }

    public ControllableNonBlockingTcpDatagramSocket(SocketAddress addr) throws SocketException {
        super(addr);
    }

    public ControllableNonBlockingTcpDatagramSocket(int nPort) throws SocketException {
        super(nPort);
    }

    public ControllableNonBlockingTcpDatagramSocket(int nPort, InetAddress addr) throws SocketException {
        super(nPort, addr);
    }

    public ControllableNonBlockingTcpDatagramSocket(SocketProvider provider) throws IOException {
        super(provider);
    }

    @Override
    public void send(DatagramPacket p) throws IOException {
        if (PacketController.suspended) {
            return;
        }
        super.send(p);
    }

    @Override
    public void receive(DatagramPacket p) throws IOException {
        while(PacketController.suspended) {
            super.receive(p);
        }
        super.receive(p);
    }
}
