package org.gridman.security.kerberos.activedirectory;

import org.gridman.encoding.ndr.NDRStream;
import org.gridman.encoding.ndr.NDRWriter;
import org.junit.Test;

import java.nio.ByteOrder;

import static junit.framework.Assert.assertEquals;

/**
 * @author Jonathan Knight
 */
public class FileTimeTest {

    @Test
    public void shouldSerializeAndDeserialize() throws Exception {
        FileTime time = new FileTime();

        NDRWriter writer = new NDRWriter();
        writer.setByteOrder(ByteOrder.LITTLE_ENDIAN);
        time.serialize(writer);

        NDRStream reader = new NDRStream(writer.toByteArray(), ByteOrder.LITTLE_ENDIAN);
        FileTime result = new FileTime();
        result.deserialize(reader);

        assertEquals(time, result);
    }

}
