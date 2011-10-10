package org.gridman.encoding.ndr;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.ByteOrder;

/**
 * This class is used to write NDR formatted
 * values to a byte toByteArray so that NDR decoders can be tested.
 *
 * @author Jonathan Knight
 */
public class NDRWriter {

    private ByteArrayOutputStream data;
    private DataOutputStream stream;
    private ByteOrder order;

    public NDRWriter() {
        data = new ByteArrayOutputStream(1000);
        stream = new DataOutputStream(data);
        order = ByteOrder.LITTLE_ENDIAN;
    }

    public void setByteOrder(ByteOrder byteOrder) {
        this.order = byteOrder;
    }

    public ByteOrder getOrder() {
        return order;
    }

    public byte[] toByteArray() {
        return data.toByteArray();
    }

    public void writeByte(byte value) throws IOException {
        stream.writeByte(value);
    }

    public void writeInt(int value) throws IOException {
        if (order == ByteOrder.LITTLE_ENDIAN) {
            value = Integer.reverseBytes(value);
        }
        stream.writeInt(value);
    }

    public void writeLong(long value) throws IOException {
        if (order == ByteOrder.LITTLE_ENDIAN) {
            value = Long.reverseBytes(value);
        }
        stream.writeLong(value);
    }

    public void writeShort(short value) throws IOException {
        if (order == ByteOrder.LITTLE_ENDIAN) {
            value = Short.reverseBytes(value);
        }
        stream.writeShort(value);
    }

    public void write(byte[] bytes) throws IOException {
        stream.write(bytes);
    }

    public void padTo(int position) throws IOException {
        int currentPosition = data.size();
        if (currentPosition > position) {
            throw new IOException("Cannot pad to " + position + " as current stream position is already " + currentPosition);
        }

        for (int i=currentPosition; i<position; i++) {
            stream.writeByte(0x00);
        }
    }

    public void writeUnsignedInt(long value) throws IOException {
        if (order == ByteOrder.LITTLE_ENDIAN) {
            value = Long.reverseBytes(value);
            byte[] writeBuffer = new byte[4];
            writeBuffer[0] = (byte)(value >>> 56);
            writeBuffer[1] = (byte)(value >>> 48);
            writeBuffer[2] = (byte)(value >>> 40);
            writeBuffer[3] = (byte)(value >>> 32);
            write(writeBuffer);
        } else {
            byte[] writeBuffer = new byte[4];
            writeBuffer[0] = (byte)(value >>> 24);
            writeBuffer[1] = (byte)(value >>> 16);
            writeBuffer[2] = (byte)(value >>> 8);
            writeBuffer[3] = (byte)value;
            write(writeBuffer);
        }
    }

    public void writeConformantVaryingNonTerminatedUnicodeString(String string) throws IOException {
        writeConformantVaryingUnicodeStringInternal(string, 0, false);
    }

    public void writeConformantVaryingNonTerminatedUnicodeString(String string, int offset) throws IOException {
        writeConformantVaryingUnicodeStringInternal(string, offset, false);
    }

    public void writeConformantVaryingUnicodeString(String string) throws IOException {
        writeConformantVaryingUnicodeStringInternal(string, 0, true);
    }

    public void writeConformantVaryingUnicodeString(String string, int offset) throws IOException {
        writeConformantVaryingUnicodeStringInternal(string, offset, true);
    }

    private void writeConformantVaryingUnicodeStringInternal(String string, int offset, boolean terminated) throws IOException {
        int maximum = string.length() + offset;
        if (terminated) {
            maximum += 1;
        }
        writeInt(maximum);

        writeInt(offset);

        int length = string.length();
        if (terminated) {
            length += 1;
        }

        writeInt(length);

        for (int i=0; i<offset; i++) {
            writeShort((short) 0);
        }

        for (char c : string.toCharArray()) {
            writeShort((short) c);
        }

        if (terminated) {
            writeShort((short) 0x00);
        }
    }
}
