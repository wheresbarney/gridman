package org.gridman.security.kerberos.activedirectory;

import org.gridman.encoding.ndr.NDRStream;
import org.gridman.encoding.ndr.NDRWriter;
import org.junit.Test;

import java.util.UUID;

import static junit.framework.Assert.assertEquals;

/**
 * @author Jonathan Knight
 */
public class RpcUnicodeStringTest {

    @Test
    public void shouldReduceMaximumLengthByOneIfOddNumber() throws Exception {
        RpcUnicodeString rpcUnicodeString = new RpcUnicodeString((short)100, (short)1001, 1);
        assertEquals(1000, rpcUnicodeString.getMaximumLength());
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowExceptionIfLengthGreaterThanMaximumLength() throws Exception {
        new RpcUnicodeString((short)100, (short)10, 1);
    }

    @Test
    public void shouldSetLengthFromConstructor() throws Exception {
        RpcUnicodeString rpcUnicodeString = new RpcUnicodeString((short)100, (short)1000, 1);
        assertEquals((short)100, rpcUnicodeString.getLength());
    }

    @Test
    public void shouldSetMaximumLengthFromConstructor() throws Exception {
        RpcUnicodeString rpcUnicodeString = new RpcUnicodeString((short)100, (short)1000, 1);
        assertEquals((short)1000, rpcUnicodeString.getMaximumLength());        
    }

    @Test
    public void shouldSetPointerFromConstructor() throws Exception {
        RpcUnicodeString rpcUnicodeString = new RpcUnicodeString((short)100, (short)1000, 1);
        assertEquals(1, rpcUnicodeString.getPointer());
    }

    @Test
    public void shouldSetLengthFromString() throws Exception {
        String value = UUID.randomUUID().toString();
        short expected = (short)(value.length() * 2);

        RpcUnicodeString rpcUnicodeString = new RpcUnicodeString(value, 1234);
        assertEquals(expected, rpcUnicodeString.getLength());
    }

    @Test
    public void shouldSetMaximumLengthFromString() throws Exception {
        String value = UUID.randomUUID().toString();
        short expected = (short)(value.length() * 2);

        RpcUnicodeString rpcUnicodeString = new RpcUnicodeString(value, 1234);
        assertEquals(expected, rpcUnicodeString.getMaximumLength());
    }

    @Test
    public void shouldSetPointerWhenUsingStringConstructor() throws Exception {
        String value = UUID.randomUUID().toString();

        RpcUnicodeString rpcUnicodeString = new RpcUnicodeString(value, 1234);
        assertEquals(1234, rpcUnicodeString.getPointer());
    }

    @Test
    public void shouldSerializeAndDeserialize() throws Exception {
        RpcUnicodeString rpcUnicodeString = new RpcUnicodeString((short)100, (short)1000, 1);

        NDRWriter writer = new NDRWriter();
        rpcUnicodeString.serialize(writer);

        NDRStream reader = new NDRStream(writer.toByteArray());
        RpcUnicodeString result = new RpcUnicodeString();
        result.deserialize(reader);

        assertEquals(rpcUnicodeString, result);
    }

    @Test
    public void shouldDeserializeStringValue() throws Exception {
        String value = UUID.randomUUID().toString();
        short length = (short)(value.length() * 2);

        NDRWriter writer = new NDRWriter();
        writer.writeConformantVaryingNonTerminatedUnicodeString(value, 10);

        NDRStream reader = new NDRStream(writer.toByteArray());

        RpcUnicodeString rpcUnicodeString = new RpcUnicodeString(length, (short)1000, 1);
        rpcUnicodeString.deserializeString(reader);
        assertEquals(value, rpcUnicodeString.getStringValue());
    }

    @Test
    public void shouldSerializeAndDeserializeStringValue() throws Exception {
        String value = UUID.randomUUID().toString();
        short length = (short)(value.length() * 2);

        NDRWriter writer = new NDRWriter();

        RpcUnicodeString rpcUnicodeString = new RpcUnicodeString(value, 1);
        rpcUnicodeString.serializeString(writer);

        NDRStream reader = new NDRStream(writer.toByteArray());

        rpcUnicodeString = new RpcUnicodeString(length, (short)1000, 1);
        rpcUnicodeString.deserializeString(reader);
        assertEquals(value, rpcUnicodeString.getStringValue());
    }
}
