package org.gridman.encoding.ndr;

import org.junit.Test;

import java.io.IOException;
import java.util.Arrays;

import static junit.framework.Assert.assertTrue;

/**
 * @author Jonathan Knight
 */
public class NDRWriterTest {

    @Test
    public void shouldPadByCorrectNumberOfBytes() throws Exception {
        NDRWriter writer = new NDRWriter();
        writer.writeByte((byte)0x01);
        writer.padTo(10);
        writer.writeByte((byte)0x02);
        assertTrue(Arrays.equals(new byte[]{0x01,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x02}, writer.toByteArray()));
    }

    @Test
    public void shouldAddNothingIfPadoCurrentPosition() throws Exception {
        byte[] data = new byte[]{0x01,0x02,0x03,0x04};
        NDRWriter writer = new NDRWriter();
        writer.write(data);
        writer.padTo(4);
        assertTrue(Arrays.equals(data, writer.toByteArray()));
    }

    @Test(expected = IOException.class)
    public void shouldThrowExceptionIfTryingToPadToEarlierPositionInStream() throws Exception {
        NDRWriter writer = new NDRWriter();
        writer.write(new byte[]{0x01,0x02,0x03,0x04});
        writer.padTo(3);
    }


}
