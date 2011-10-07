package org.gridman.security.kerberos.activedirectory;

import org.gridman.encoding.ndr.NDRStream;
import org.gridman.encoding.ndr.NDRWriter;
import org.junit.Test;

import java.util.Arrays;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;

/**
 * @author Jonathan Knight
 */
public class PacSidTest {

    @Test
    public void shouldDefaultToCorrectRevision() throws Exception {
        PacSid sid = new PacSid();
        assertEquals(1, sid.getRevision());
    }

    @Test
    public void shouldDefaultToCorrectRevisionWhenConstructedFromSubAuthorities() throws Exception {
        PacSid sid = new PacSid(1,2,3);
        assertEquals(1, sid.getRevision());
    }

    @Test
    public void shouldDefaultToCorrectAuthority() throws Exception {
        PacSid sid = new PacSid();
        assertEquals(5, sid.getAuthority());
    }

    @Test
    public void shouldDefaultToCorrectAuthorityWhenConstructedFromSubAuthorities() throws Exception {
        PacSid sid = new PacSid(1,2,3);
        assertEquals(5, sid.getAuthority());
    }

    @Test
    public void shouldHaveCorrectToStringWithNoSubAuthorities() throws Exception {
        PacSid sid = new PacSid();
        assertEquals("S-1-5", sid.toString());
    }

    @Test
    public void shouldHaveCorrectToStringWithAuthorityLessThan2to32() throws Exception {
        long value = 1966L;
        byte[] authority = new byte[6];
        authority[0] = 0x00;
        authority[1] = 0x00;
        authority[2] = (byte)(value >> 24);
        authority[3] = (byte)(value >> 16);
        authority[4] = (byte)(value >> 8);
        authority[5] = (byte)value;

        PacSid sid = new PacSid((byte)1, authority);
        assertEquals("S-1-1966", sid.toString());
    }

    @Test
    public void shouldHaveCorrectToStringWithAuthorityGreaterThan2to32() throws Exception {
        long value = 0x002006197634L;
        byte[] authority = new byte[6];
        authority[0] = (byte)(value >> 40);
        authority[1] = (byte)(value >> 32);
        authority[2] = (byte)(value >> 24);
        authority[3] = (byte)(value >> 16);
        authority[4] = (byte)(value >> 8);
        authority[5] = (byte)value;

        PacSid sid = new PacSid((byte)1, authority);
        assertEquals("S-1-0x002006197634", sid.toString());
    }

    @Test
    public void shouldHaveCorrectToStringWithSingleSubAuthority() throws Exception {
        PacSid sid = new PacSid(19);
        assertEquals("S-1-5-19", sid.toString());
    }

    @Test
    public void shouldHaveCorrectToStringWithMultipleSubAuthorities() throws Exception {
        PacSid sid = new PacSid(10,9,8);
        assertEquals("S-1-5-10-9-8", sid.toString());
    }

    @Test
    public void shouldSerializeAndDeserializeSidWithNoSubAuthorities() throws Exception {
        PacSid sid = new PacSid((byte)19, new byte[]{0x00, 0x00, 0x00, 0x01, 0x02, 0x03});

        NDRWriter writer = new NDRWriter();
        sid.serialize(writer);

        NDRStream stream = new NDRStream(writer.toByteArray());
        PacSid result = new PacSid();
        result.deserialize(stream);

        assertEquals(sid, result);
    }

    @Test
    public void shouldSerializeAndDeserializeSidWithSomeSubAuthorities() throws Exception {
        PacSid sid = new PacSid((byte)19, new byte[]{0x00, 0x00, 0x00, 0x01, 0x02, 0x03}, 10L, 11L);

        NDRWriter writer = new NDRWriter();
        sid.serialize(writer);

        NDRStream stream = new NDRStream(writer.toByteArray());
        PacSid result = new PacSid();
        result.deserialize(stream);

        assertEquals(sid, result);
    }

    @Test
    public void shouldAddSubAuthoritiesToSidWithNoSubAuthorities() throws Exception {
        PacSid sid = new PacSid((byte)19, new byte[]{0x00, 0x00, 0x00, 0x01, 0x02, 0x03});
        sid.addSubAuthorities(1,2,3);

        assertTrue(Arrays.equals(new long[]{1,2,3}, sid.getSubAuthorities()));
    }

    @Test
    public void shouldAddSubAuthoritiesToSidWithSomeSubAuthorities() throws Exception {
        PacSid sid = new PacSid((byte)19, new byte[]{0x00, 0x00, 0x00, 0x01, 0x02, 0x03}, 1, 2, 3);
        sid.addSubAuthorities(4,5,6);

        assertTrue(Arrays.equals(new long[]{1,2,3,4,5,6}, sid.getSubAuthorities()));
    }

    @Test
    public void shouldLeaveSubAuthoritiesUnchangedWhenAddingNoAuthorities() throws Exception {
        PacSid sid = new PacSid((byte)19, new byte[]{0x00, 0x00, 0x00, 0x01, 0x02, 0x03}, 1, 2, 3);
        sid.addSubAuthorities();

        assertTrue(Arrays.equals(new long[]{1,2,3}, sid.getSubAuthorities()));
    }

    @Test
    public void shouldParseSidWithNoSubAuthorities() throws Exception {
        PacSid sid = new PacSid((byte)1, new byte[]{0,0,0,0,0,5});
        PacSid parsedSid = PacSid.parse("S-1-5");
        assertEquals(sid, parsedSid);
    }

    @Test
    public void shouldParseSidWithHexAuthorities() throws Exception {
        PacSid sid = new PacSid((byte)1, new byte[]{0x0A,0x0B,0x0C,0x0D,0x0E,0x0F});
        PacSid parsedSid = PacSid.parse("S-1-0x0A0B0C0D0E0F");
        assertEquals(sid, parsedSid);
    }

    @Test
    public void shouldParseSidWithSingleSubAuthority() throws Exception {
        PacSid sid = new PacSid((byte)1, new byte[]{0,0,0,0,0,5}, 1234);
        PacSid parsedSid = PacSid.parse("S-1-5-1234");
        assertEquals(sid, parsedSid);
    }

    @Test
    public void shouldParseSidWithMultipleSubAuthorities() throws Exception {
        PacSid sid = new PacSid((byte)1, new byte[]{0,0,0,0,0,5}, 1234, 5678, 9876);
        PacSid parsedSid = PacSid.parse("S-1-5-1234-5678-9876");
        assertEquals(sid, parsedSid);
    }
}
