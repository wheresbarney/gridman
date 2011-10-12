package org.gridman.testtools.coherence.net;

import com.tangosol.net.SocketProvider;
import com.tangosol.net.TcpDatagramSocket;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.SocketAddress;
import java.net.SocketException;

public class ControllableTcpDatagramSocket extends TcpDatagramSocket {

    public ControllableTcpDatagramSocket() throws SocketException {
    }

    public ControllableTcpDatagramSocket(SocketAddress addr) throws SocketException {
        super(addr);
    }

    public ControllableTcpDatagramSocket(int nPort) throws SocketException {
        super(nPort);
    }

    public ControllableTcpDatagramSocket(int nPort, InetAddress addr) throws SocketException {
        super(nPort, addr);
    }

    public ControllableTcpDatagramSocket(SocketProvider provider) throws IOException {
        super(provider);
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
