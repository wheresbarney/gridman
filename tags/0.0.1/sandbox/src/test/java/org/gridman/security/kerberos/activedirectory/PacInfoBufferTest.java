package org.gridman.security.kerberos.activedirectory;

import org.gridman.encoding.ndr.NDRStream;
import org.gridman.encoding.ndr.NDRWriter;
import org.junit.Before;
import org.junit.Test;

import java.nio.ByteOrder;
import java.util.Arrays;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;

/**
 * @author Jonathan Knight
 */
public class PacInfoBufferTest {

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowExceptionIfTryingToSetTooMuchData() throws Exception {
        NDRStream streamTooBig = new NDRStream(new byte[6]);

        PacInfoBuffer info = new PacInfoBuffer(1, 5, 10);
        info.setDataStream(streamTooBig);
    }

    @Test
    public void shouldSetData() throws Exception {
        byte[] data = {0x01,0x02,0x03,0x04};
        NDRStream stream = new NDRStream(data);

        PacInfoBuffer info = new PacInfoBuffer(1, data.length, 10);
        info.setDataStream(stream);

        assertTrue(Arrays.equals(data, info.underlyingData()));
    }

    @Test
    public void shouldSetTypeFromStream() throws Exception {
        ndrWriter.writeUnsignedInt(PacBufferType.PAC_LOGON_INFO.getTypeId());
        ndrWriter.writeUnsignedInt(1234);
        ndrWriter.writeLong(5678);

        PacInfoBuffer buffer = new PacInfoBuffer();
        buffer.deserialize(createNDRStream());

        assertEquals(PacBufferType.PAC_LOGON_INFO.getTypeId(), buffer.getType());
    }

    @Test
    public void shouldSetBufferSizeFromStream() throws Exception {
        ndrWriter.writeUnsignedInt(PacBufferType.PAC_LOGON_INFO.getTypeId());
        ndrWriter.writeUnsignedInt(1234);
        ndrWriter.writeLong(5678);

        PacInfoBuffer buffer = new PacInfoBuffer();
        buffer.deserialize(createNDRStream());

        assertEquals(1234, buffer.getBufferSize());
    }

    @Test
    public void shouldSetOffsetFromStream() throws Exception {
        ndrWriter.writeUnsignedInt(PacBufferType.PAC_LOGON_INFO.getTypeId());
        ndrWriter.writeUnsignedInt(1234);
        ndrWriter.writeLong(5678);

        PacInfoBuffer buffer = new PacInfoBuffer();
        buffer.deserialize(createNDRStream());

        assertEquals(5678, buffer.getOffset());
    }

    @Test
    public void shouldSerializeAndDeserialize() throws Exception {
        PacInfoBuffer buffer = new PacInfoBuffer(PacBufferType.PAC_LOGON_INFO.getTypeId(), 100, 1000);

        NDRWriter writer = new NDRWriter();
        buffer.serialize(writer);

        NDRStream stream = new NDRStream(writer.toByteArray(), ByteOrder.LITTLE_ENDIAN);
        PacInfoBuffer result = new PacInfoBuffer();
        result.deserialize(stream);

        assertEquals(result, buffer);
    }

    @Before
    public void setup() {
        ndrWriter = new NDRWriter();
        ndrWriter.setByteOrder(ByteOrder.LITTLE_ENDIAN);

    }

    NDRStream createNDRStream() {
        byte[] bytes = ndrWriter.toByteArray();
        return new NDRStream(bytes, ByteOrder.LITTLE_ENDIAN);
    }

    private NDRWriter ndrWriter;
}
