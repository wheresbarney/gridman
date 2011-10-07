package org.gridman.security.kerberos.activedirectory;

import org.gridman.encoding.ndr.NDRSerializable;
import org.gridman.encoding.ndr.NDRStream;
import org.gridman.encoding.ndr.NDRWriter;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * @author Jonathan Knight
 */
public class Pac implements NDRSerializable {

    int version;

    Map<Integer, PacInfoBuffer> bufferInfoMap;
    Map<Integer, NDRSerializable> buffers;

    public Pac() {
        this(null);
    }

    public Pac(byte[] data) {
        try {
            bufferInfoMap = new HashMap<Integer, PacInfoBuffer>();
            buffers = new HashMap<Integer, NDRSerializable>();
            NDRStream stream = new NDRStream(data);
            this.deserialize(stream);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public int getBufferCount() {
        return bufferInfoMap.size();
    }

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    public PacInfoBuffer getBufferInfo(PacBufferType type) {
        return bufferInfoMap.get(type.getTypeId());
    }

    public void addBufferInfo(PacInfoBuffer buffer) {
        bufferInfoMap.put(buffer.getType(), buffer);
    }

    @SuppressWarnings({"unchecked"})
    public <T> T getBuffer(PacBufferType type) {
        if (!buffers.containsKey(type.getTypeId())) {
            try {
                PacInfoBuffer info = bufferInfoMap.get(type.getTypeId());
                if (info != null) {
                    Class<? extends NDRSerializable> implementationClass = type.getImplementation();
                    NDRSerializable impl = implementationClass.newInstance();
                    impl.deserialize(info.getDataStream());
                    buffers.put(type.getTypeId(), impl);
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        return (T) buffers.get(type.getTypeId());
    }

    public void deserialize(NDRStream stream) throws IOException {
        int bufferCount = stream.readInt();
        version = stream.readInt();

        SortedSet<PacInfoBuffer> sortedBuffer = new TreeSet<PacInfoBuffer>(bufferInfoMap.values());
        for (int i=0; i<bufferCount; i++) {
            PacInfoBuffer pacInfoBuffer = new PacInfoBuffer();
            pacInfoBuffer.deserialize(stream);
            bufferInfoMap.put(pacInfoBuffer.getType(), pacInfoBuffer);
            sortedBuffer.add(pacInfoBuffer);
        }

        for (PacInfoBuffer pacInfoBuffer : sortedBuffer) {
            pacInfoBuffer.setDataStream(stream.getSubStream((int)pacInfoBuffer.getOffset(), pacInfoBuffer.getBufferSize()));
        }

    }

    public void serialize(NDRWriter writer) throws IOException {
        writer.writeInt(getBufferCount());
        writer.writeInt(version);

        SortedSet<PacInfoBuffer> sortedBuffer = new TreeSet<PacInfoBuffer>(bufferInfoMap.values());
        for(PacInfoBuffer buffer : sortedBuffer) {
            buffer.serialize(writer);
        }

        for(PacInfoBuffer buffer : sortedBuffer) {
            byte[] data = buffer.underlyingData();
            if (data != null) {
                writer.padTo((int)buffer.getOffset());
                NDRSerializable pacBuffer = buffers.get(buffer.getType());
                if (pacBuffer == null) {
                    writer.write(data);
                } else {
                    pacBuffer.serialize(writer);
                }
            }
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        Pac pac = (Pac) o;

        if (version != pac.version) {
            return false;
        }
        if (bufferInfoMap != null ? !bufferInfoMap.equals(pac.bufferInfoMap) : pac.bufferInfoMap != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = version;
        result = 31 * result + (bufferInfoMap != null ? bufferInfoMap.hashCode() : 0);
        return result;
    }
}
