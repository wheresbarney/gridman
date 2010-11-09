package org.gridman.encoding.ndr;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * A class that can read values from a byte toByteArray that has been encoded in NDR format.
 * NDR format is decribed here: <a href="http://www.opengroup.org/onlinepubs/9629399/chap14.htm">NDR Syntax.</a>
 *
 * This class is not a complete NDR implementation, it contains enough required to read
 * Active Directory PACOld data but it would be simple enough to expand to read other types
 * described in the link above.
 *
 * @author Jonathan Knight
 */
public class NDRStream {
    /** An enum representing stream alignment */
    public enum Align {
        SHORT(2),
        INT(4),
        LONG(8);

        private int bytes;

        private Align(int bytes) {
            this.bytes = bytes;
        }
    }

    /** The NIO buffer wrapping the bytes being read */
    private ByteBuffer stream;
    /** The original size of the byte toByteArray containing the data */
    private int originalSize;

    /**
     * Create an NDR stream wrapping the secified byte toByteArray.
     * The byte ordering for numeric values will be ByteOrder.LITTLE_ENDIAN
     * will be used.
     *
     * @param data the NDR encoded byte toByteArray to read.
     */
    public NDRStream(byte[] data) {
        this(data, ByteOrder.LITTLE_ENDIAN);
    }

    /**
     * Create an NDR stream wrapping the secified byte toByteArray.
     * Numeric values will be read using the specified byte ordering.
     *
     * @param data the NDR encoded byte toByteArray to read
     * @param byteOrder the byte order to use when reading numeric values
     */
    public NDRStream(byte[] data, ByteOrder byteOrder) {
        this.stream = (data != null && data.length > 0) ? ByteBuffer.wrap(data) : ByteBuffer.allocate(1000);
        this.stream.order(byteOrder);
        this.originalSize = (data != null && data.length > 0) ? data.length : 0;
    }

    private NDRStream(ByteBuffer stream) {
        this.stream = stream;
        this.originalSize = stream.remaining();
    }

    /**
     * Return the length of this NDRStream.
     *
     * @return the length of this NDRStream.
     */
    public int length() {
        return stream.limit();
    }

    /**
     * Return the byte ordering of this NDRStream.
     * This will be one of the values of {@link java.nio.ByteOrder}
     * @return the byte ordering of this NDRStream
     */
    public ByteOrder order() {
        return stream.order();
    }

    /**
     * Returns a copy of the byte toByteArray that backs this stream.
     * @return a copy of the byte toByteArray that backs this stream.
     */
    public byte[] toByteArray() {
        int position = stream.position();
        stream.position(0);
        byte[] bytes = new byte[stream.remaining()];
        stream.get(bytes);
        stream.position(position);
        return bytes;
    }

    /**
     * Returns the byte array that backs this buffer.
     * Modifications to this buffer's content will cause the returned array's content to be modified, and vice versa.
     * 
     * @return The array that backs this buffer
     */
    public byte[] underlyingByteArray() {
        return stream.array();
    }

    /**
     * Read a single octet (byte) from the NDR stream.
     *
     * @return the octet (byte) read from the stream
     * @throws java.io.IOException if an IO error occurs
     */
    public byte readByte() throws IOException {
        return stream.get();
    }

    /**
     * An NDR Boolean is a logical quantity that assumes one of two values: TRUE or FALSE.
     * NDR represents a Boolean as one octet. It represents a value of FALSE as a zero octet,
     * an octet in which every bit is reset. It represents a value of TRUE as a non-zero octet,
     * an octet in which one or more bits are set.
     *
     * @return true if the byte read is non-zero false if the byte is 0x00
     * @throws java.io.IOException if an IO error occurs
     */
    public boolean readBoolean() throws IOException {
        return readByte() != 0x00;
    }

    /**
     * An NDR short is a 16bit value corrsponding to a
     * Java short value.
     *
     * @return a short value read from the stream
     * @throws java.io.IOException if an IO error occurs
     */
    public short readShort() throws IOException {
        ensureStreamAlignment(Align.SHORT);
        return stream.getShort();
    }

    /**
     * Reads a 32 bit signed number corresponding to Java int from the stream.
     * In NDR a 32 bit number is referred to as a long although this method
     * is called readInt to retain the Java terminology.
     *
     * @return a Java int value read from the stream
     * @throws java.io.IOException if an IO error occurs
     */
    public int readInt() throws IOException {
        ensureStreamAlignment(Align.INT);
        return stream.getInt();
    }

    /**
     * Reads a 32 bit unsigned number corresponding to Java int from the stream.
     * Java does not support unsigned int values so the unsigned value read
     * from the stream is converted to a Java long to support the required
     * range of values.
     *
     * @return an unsigned int represented by a Java long value read from the stream
     * @throws java.io.IOException if an IO error occurs
     */
    public long readUnsignedInt() throws IOException {
        return ((long)readInt()) & 0xffffffffL;
    }

    /**
     * Reads a 64 bit signed number corresponding to Java long from the stream.
     * In NDR a 64 bit number is referred to as a hyper although this method
     * is called readLong to retain the Java terminology.
     *
     * @return a Java long value read from the stream
     * @throws java.io.IOException if an IO error occurs
     */
    public long readLong() throws IOException {
        ensureStreamAlignment(Align.LONG);
        return stream.getLong();
    }

    /**
     * An NDR conformant and varying string is a string in which the maximum number of elements
     * is not known beforehand and therefore is included in the representation of the string.
     *
     * NDR represents a conformant and varying string as an ordered sequence of representations
     * of the string elements, preceded by three unsigned long integers.
     * The first integer gives the maximum number of elements in the string, including the terminator.
     * The second integer gives the offset from the first index of the string to the first index of
     * the actual subset being passed.
     * The third integer gives the actual number of elements being passed, including the terminator.
     *
     * A conformant and varying string can contain at most 2^(32-1-o) elements, where o is the offset,
     * and must contain at least one element, the terminator.
     *
     * In this case a Unicode string is being read so each string element is 2 bytes.
     *
     * @return A String read from the data stream.
     * @throws java.io.IOException if an IO error occurs or if the string is malformed. A malformed string is
     *         where the offset is bigger than the maximum number of elements or the actualCount is
     *         bigger than the maximum count minus the offset.
     */
    public String readConformantVaryingUnicodeString() throws IOException {
        return readString(true);
    }

    public String readConformantVaryingNonTerminatedUnicodeString() throws IOException {
        return readString(false);
    }

    private String readString(boolean nullTerminated) throws IOException {
        int maximumCount = readInt();
        int offset = readInt();
        int actualCount = readInt();

        if(offset > maximumCount || actualCount > (maximumCount - offset)) {
            throw new IOException("Malformed String maximumCount must be greater than offset and actualCount must be greater than (maximumCount - actualCount)");
        }

        // skip the to the start of the string data
        if (offset > 0) {
            skipBytes(offset * 2);
        }

        // read the string data
        int charCount = nullTerminated ? actualCount - 1 : actualCount;
        char[] chars = new char[charCount];
        for(int l = 0; l < chars.length; l++) {
            chars[l] = (char)readShort();
        }

        if (nullTerminated) {
            // skip the terminator
            readShort();
        }

        return new String(chars);
    }

    /**
     * Skip the specified number of bytes in the stream.
     *
     * @param count the number of bytes to skip
     * @throws java.io.IOException if in IO error occurs
     */
    public void skipBytes(int count) throws IOException {
        stream.position(stream.position() + count);
    }

    /**
     * Sets this buffer's position.
     *
     * @param newPosition - The new position value; must be non-negative and no larger than the current limit
     * @throws IllegalArgumentException - If the preconditions on newPosition do not hold
     */
    public void position(int newPosition) {
        stream.position(newPosition);
    }

    /**
     * Ensures that the stream is aligned to the specified byte
     * boundary.
     *
     * @param alignment the required byte boundary
     * @throws java.io.IOException if an IO error occurs
     */
    public void ensureStreamAlignment(Align alignment) throws IOException {
        int position = originalSize - stream.remaining();
        int shift = position & alignment.bytes - 1;
        if(alignment.bytes != 0 && shift != 0) {
            skipBytes(alignment.bytes - shift);
        }
    }

    /**
     * Read bytes from the stream to fill the specified byte toByteArray.
     *
     * @param b the byte toByteArray to be filled from the stream.
     * @throws java.io.IOException - BufferUnderflowException - If there are fewer than the toByteArray length bytes
     *         remaining in this buffer
     */
    public void readFully(byte[] b) throws IOException {
        stream.get(b);
    }

    /**
     * Return a new instance of an NDRStream that points to a portion
     * of the bytes toByteArray that this stream references.
     *
     * @param offset the starting point of the byte toByteArray to reference
     * @param length the length of the byte toByteArray
     * @return a new instance of an NDRStream that points to a portion
     * of the bytes toByteArray that this stream references.
     */
    public NDRStream getSubStream(int offset, int length) {
        int position = stream.position();
        stream.position(offset);
        ByteBuffer buffer = stream.slice();
        buffer.limit(length);
        buffer.order(stream.order());
        stream.position(position);
        return new NDRStream(buffer);
    }
}
