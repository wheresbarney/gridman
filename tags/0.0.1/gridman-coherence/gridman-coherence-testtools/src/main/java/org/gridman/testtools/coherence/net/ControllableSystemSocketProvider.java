package org.gridman.testtools.coherence.net;

import com.tangosol.net.SystemSocketProvider;
import com.tangosol.net.WrapperSocketProvider;

import java.io.IOException;
import java.net.DatagramSocket;
import java.net.MulticastSocket;
import java.nio.channels.SocketChannel;

public class ControllableSystemSocketProvider extends WrapperSocketProvider {

    public ControllableSystemSocketProvider() {
        super(new SystemSocketProvider());
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
        return new ControllableSocketChannel(ensureDelegate().openSocketChannel(), this);
//        SocketChannel channel = super.openSocketChannel();
//        return new ControllableSocketChannel(channel, channel.provider());
    }
}
