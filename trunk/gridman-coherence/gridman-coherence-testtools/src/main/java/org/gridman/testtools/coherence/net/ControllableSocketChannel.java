package org.gridman.testtools.coherence.net;

import com.tangosol.net.WrapperSocketChannel;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.nio.channels.spi.SelectorProvider;

public class ControllableSocketChannel extends WrapperSocketChannel {

    public ControllableSocketChannel(SocketChannel channel, SelectorProvider provider) {
        super(channel, provider);
    }

    @Override
    public int write(ByteBuffer src) throws IOException {
        if (PacketController.suspended) {
            throw new IOException("Network suspended");
        }
        return super.write(src);
    }

    @Override
    public long write(ByteBuffer[] srcs, int offset, int length) throws IOException {
        if (PacketController.suspended) {
            throw new IOException("Network suspended");
        }
        return super.write(srcs, offset, length);
    }

    @Override
    public int read(ByteBuffer dst) throws IOException {
        if (PacketController.suspended) {
            throw new IOException("Network suspended");
        }
        return super.read(dst);
    }

    @Override
    public long read(ByteBuffer[] dsts, int offset, int length) throws IOException {
        if (PacketController.suspended) {
            throw new IOException("Network suspended");
        }
        return super.read(dsts, offset, length);
    }
}
