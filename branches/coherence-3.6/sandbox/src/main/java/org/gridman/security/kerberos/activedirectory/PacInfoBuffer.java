package org.gridman.security.kerberos.activedirectory;

import org.gridman.encoding.ndr.NDRSerializable;
import org.gridman.encoding.ndr.NDRStream;
import org.gridman.encoding.ndr.NDRWriter;

import java.io.IOException;

/**
 * @author Jonathan Knight
 */
public class PacInfoBuffer implements Comparable<PacInfoBuffer>, NDRSerializable {
    private int type;

    private int bufferSize;

    private long offset;

    private NDRStream dataStream;

    public PacInfoBuffer() {
    }

    public PacInfoBuffer(int type, int bufferSize, long offset) {
        this.type = type;
        this.bufferSize = bufferSize;
        this.offset = offset;
    }

    public int getType() {
        return type;
    }

    public int getBufferSize() {
        return bufferSize;
    }

    public long getOffset() {
        return offset;
    }

    public NDRStream getDataStream() {
        return dataStream;
    }

    /**
     * Returns the byte array that backs this buffer.
     * Modifications to this buffer's content will cause the returned array's content to be modified, and vice versa.
     *
     * @return The array that backs this buffer
     */
    public byte[] underlyingData() {
        return (dataStream != null) ? dataStream.underlyingByteArray() : null;
    }
    
    public void setDataStream(NDRStream dataStream) {
        int length = dataStream.length();
        if (length > this.bufferSize) {
            throw new IllegalArgumentException("data length[" + length + "] must not be greater than bufferSize[" + bufferSize + "]");
        }
        this.dataStream = dataStream;
    }

    public void deserialize(NDRStream stream) throws IOException {
        type = stream.readInt();
        bufferSize = stream.readInt();
        offset = stream.readLong();
    }

    public void serialize(NDRWriter writer) throws IOException {
        writer.writeInt(type);
        writer.writeInt(bufferSize);
        writer.writeLong(offset);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        PacInfoBuffer that = (PacInfoBuffer) o;

        if (bufferSize != that.bufferSize) {
            return false;
        }
        if (offset != that.offset) {
            return false;
        }
        if (type != that.type) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = type;
        result = 31 * result + bufferSize;
        result = 31 * result + (int) (offset ^ (offset >>> 32));
        return result;
    }

    @Override
    public int compareTo(PacInfoBuffer other) {
        return Long.valueOf(this.offset).compareTo(other.offset);
    }
}
