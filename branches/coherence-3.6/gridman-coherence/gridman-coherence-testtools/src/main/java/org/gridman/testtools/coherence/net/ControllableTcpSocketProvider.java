package org.gridman.testtools.coherence.net;

import com.tangosol.net.TcpSocketProvider;

import java.io.IOException;
import java.net.DatagramSocket;
import java.nio.channels.SocketChannel;

public class ControllableTcpSocketProvider extends TcpSocketProvider {

    public ControllableTcpSocketProvider() {
    }

    @Override
    public DatagramSocket openDatagramSocket() throws IOException {
        return configure((this.m_fBlocking) ? new ControllableTcpDatagramSocket(this) : new ControllableNonBlockingTcpDatagramSocket(this));
    }

    @Override
    public SocketChannel openSocketChannel() throws IOException {
        SocketChannel channel = super.openSocketChannel();
        return new ControllableSocketChannel(channel, channel.provider());
    }
}
