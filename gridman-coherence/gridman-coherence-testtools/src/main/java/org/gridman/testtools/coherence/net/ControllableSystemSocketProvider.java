package org.gridman.testtools.coherence.net;

import com.tangosol.net.SystemSocketProvider;

import java.io.IOException;
import java.net.DatagramSocket;
import java.net.MulticastSocket;
import java.nio.channels.SocketChannel;

public class ControllableSystemSocketProvider extends SystemSocketProvider {

    public ControllableSystemSocketProvider() {
    }

    @Override
    public DatagramSocket openDatagramSocket() throws IOException {
        return new ControllableDatagramSocket(null);
    }

    @Override
    public MulticastSocket openMulticastSocket() throws IOException {
        return new ControllableMulticastSocket(null);
    }
    
    @Override
    public SocketChannel openSocketChannel() throws IOException {
        SocketChannel channel = super.openSocketChannel();
        return new ControllableSocketChannel(channel, channel.provider());
    }
}
