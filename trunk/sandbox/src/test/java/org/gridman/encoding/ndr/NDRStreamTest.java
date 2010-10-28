package org.gridman.encoding.ndr;

import org.junit.Before;
import org.junit.Test;

import java.nio.ByteOrder;
import java.util.Arrays;
import java.util.UUID;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;

/**
 * @author Jonathan Knight
 */
public class NDRStreamTest {
    
    @Test
    public void shouldReadByte() throws Exception {
        ndrWriter.write(new byte[]{3,2,1});
        NDRStream reader = createLittleEndianNDRStream();
        assertEquals(3, reader.readByte());
    }

    @Test
    public void shouldReadLittleEndianLong() throws Exception {
        long expected = 1234L;
        ndrWriter.setByteOrder(ByteOrder.LITTLE_ENDIAN);
        ndrWriter.writeLong(expected);

        NDRStream reader = createLittleEndianNDRStream();
        assertEquals(expected, reader.readLong());
    }

    @Test
    public void shouldReadBigEndianLong() throws Exception {
        long expected = 1234L;
        ndrWriter.setByteOrder(ByteOrder.BIG_ENDIAN);
        ndrWriter.writeLong(expected);

        NDRStream reader = createNDRStream(ByteOrder.BIG_ENDIAN);
        assertEquals(expected, reader.readLong());
    }

    @Test
    public void shouldReadLittleEndianInt() throws Exception {
        int expected = 19;
        ndrWriter.setByteOrder(ByteOrder.LITTLE_ENDIAN);
        ndrWriter.writeInt(expected);

        NDRStream reader = createLittleEndianNDRStream();
        assertEquals(expected, reader.readInt());
    }

    @Test
    public void shouldReadBigEndianInt() throws Exception {
        int expected = 19;
        ndrWriter.setByteOrder(ByteOrder.BIG_ENDIAN);
        ndrWriter.writeInt(expected);

        NDRStream reader = createBigEndianNDRStream();
        assertEquals(expected, reader.readInt());
    }

    @Test
    public void shouldReadLittleEndianUnsignedInt() throws Exception {
        long expected = (long)Integer.MAX_VALUE + 1;
        ndrWriter.setByteOrder(ByteOrder.LITTLE_ENDIAN);
        ndrWriter.writeUnsignedInt(expected);

        NDRStream reader = createLittleEndianNDRStream();
        assertEquals(expected, reader.readUnsignedInt());
    }

    @Test
    public void shouldReadBigEndianUnsignedInt() throws Exception {
        long expected = (long)Integer.MAX_VALUE + 1;
        ndrWriter.setByteOrder(ByteOrder.BIG_ENDIAN);
        ndrWriter.writeUnsignedInt(expected);

        NDRStream reader = createBigEndianNDRStream();
        assertEquals(expected, reader.readUnsignedInt());
    }

    @Test
    public void shouldReadLittleEndianShort() throws Exception {
        short expected = 2;
        ndrWriter.setByteOrder(ByteOrder.LITTLE_ENDIAN);
        ndrWriter.writeShort(expected);

        NDRStream reader = createLittleEndianNDRStream();
        assertEquals(expected, reader.readShort());
    }

    @Test
    public void shouldReadBigEndianShort() throws Exception {
        short expected = 2;
        ndrWriter.setByteOrder(ByteOrder.BIG_ENDIAN);
        ndrWriter.writeShort(expected);

        NDRStream reader = createBigEndianNDRStream();
        assertEquals(expected, reader.readShort());
    }

    @Test
    public void shouldReadFully() throws Exception {
        byte[] expected = {1,2,3,4,5,6,7,8};
        ndrWriter.write(expected);

        NDRStream reader = createLittleEndianNDRStream();
        byte result = reader.readByte();

        assertEquals(1, result);
    }

    @Test
    public void shouldSkipBytes() throws Exception {
        byte[] expected = {1,2,3,4,5,6,7,8};
        byte[] padding = {0,0,0,0,0,0,0};
        ndrWriter.write(padding);
        ndrWriter.write(expected);

        byte[] result = new byte[expected.length];

        NDRStream reader = createLittleEndianNDRStream();
        reader.skipBytes(padding.length);
        reader.readFully(result);

        assertTrue(Arrays.equals(expected, result));
    }

    @Test
    public void shouldReadStringWithNoUnusedChars() throws Exception {
        String expected = UUID.randomUUID().toString();
        ndrWriter.setByteOrder(ByteOrder.LITTLE_ENDIAN);
        ndrWriter.writeConformantVaryingUnicodeString(expected);

        NDRStream reader = createLittleEndianNDRStream();
        assertEquals(expected, reader.readConformantVaryingUnicodeString());
    }

    @Test
    public void shouldReadStringWithSomeUnusedChars() throws Exception {
        String expected = UUID.randomUUID().toString();
        ndrWriter.writeConformantVaryingUnicodeString(expected, 10);
        
        NDRStream reader = createLittleEndianNDRStream();
        assertEquals(expected, reader.readConformantVaryingUnicodeString());
    }

    @Test
    public void shouldEmptyReadStringWithNoUnusedChars() throws Exception {
        String expected = "";
        ndrWriter.setByteOrder(ByteOrder.LITTLE_ENDIAN);
        ndrWriter.writeConformantVaryingUnicodeString(expected);

        NDRStream reader = createLittleEndianNDRStream();
        assertEquals(expected, reader.readConformantVaryingUnicodeString());
    }

    @Test
    public void shouldReadEmptyStringWithSomeUnusedChars() throws Exception {
        String expected = "";
        ndrWriter.writeConformantVaryingUnicodeString(expected, 10);

        NDRStream reader = createLittleEndianNDRStream();
        assertEquals(expected, reader.readConformantVaryingUnicodeString());
    }

    @Test
    public void shouldCreateSubStreamOfCorrectLength() throws Exception {
        byte[] data = {1,2,3,4,5,6,7,8,9,10,11,12};
        ndrWriter.write(data);

        NDRStream reader = createLittleEndianNDRStream();
        NDRStream subStream = reader.getSubStream(3, 5);
        assertEquals(5, subStream.length());
    }

    @Test
    public void shouldCreateSubStreamWithCorrectByteOrder() throws Exception {
        ByteOrder order = ByteOrder.LITTLE_ENDIAN;
        byte[] data = {1,2,3,4,5,6,7,8,9,10,11,12};
        ndrWriter.write(data);

        NDRStream reader = createNDRStream(order);
        NDRStream subStream = reader.getSubStream(3, 5);
        assertEquals(order, subStream.order());
    }

    @Test
    public void shouldCreateSubStreamWithCorrectData() throws Exception {
        byte[] data = {1,2,3,4,5,6,7,8,9,10,11,12};
        ndrWriter.write(data);

        NDRStream reader = createLittleEndianNDRStream();
        NDRStream subStream = reader.getSubStream(3, 5);
        assertTrue(Arrays.equals(new byte[]{4,5,6,7,8}, subStream.toByteArray()));
    }

    @Before
    public void setup() {
        ndrWriter = new NDRWriter();
        ndrWriter.setByteOrder(ByteOrder.LITTLE_ENDIAN);
        
    }
    
    NDRStream createLittleEndianNDRStream() {
        return createNDRStream(ByteOrder.LITTLE_ENDIAN);
    }

    NDRStream createBigEndianNDRStream() {
        return createNDRStream(ByteOrder.BIG_ENDIAN);
    }
    
    NDRStream createNDRStream(ByteOrder byteOrder) {
        byte[] bytes = ndrWriter.toByteArray();
        return new NDRStream(bytes, byteOrder);
    }

    private NDRWriter ndrWriter;
}
