package org.gridman.security.kerberos.activedirectory;

import org.gridman.encoding.ndr.NDRStream;
import org.gridman.encoding.ndr.NDRWriter;
import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static junit.framework.Assert.assertEquals;

/**
 * @author Jonathan Knight
 */
public class PacTest {

    private Pac pac;
    private Map<Integer, PacInfoBuffer> buffers;
    private PacInfoBuffer logonInfoBuffer;

    private byte[] pacData;

    @Before
    public void initialiseTestPac() throws Exception {
        pac = new Pac();
        pac.setVersion(0);

        logonInfoBuffer = new PacInfoBuffer(PacBufferType.PAC_LOGON_INFO.getTypeId(), 100, 100);
        byte[] logonInfoBufferData = new byte[100];
        logonInfoBuffer.setDataStream(new NDRStream(logonInfoBufferData));
        pac.addBufferInfo(logonInfoBuffer);
        
        buffers = new HashMap<Integer, PacInfoBuffer>();
        buffers.put(logonInfoBuffer.getType(), logonInfoBuffer);

        NDRWriter writer = new NDRWriter();
        pac.serialize(writer);
        pacData = writer.toByteArray();
    }

    @Test
    public void shouldSerializeAndDeserialize() throws Exception {
        NDRWriter writer = new NDRWriter();
        pac.serialize(writer);
        byte[] data = writer.toByteArray();

        NDRStream stream = new NDRStream(data);
        Pac result = new Pac();
        result.deserialize(stream);

        assertEquals(pac, result);
    }

//    @Test
//    public void shouldReadFullPacFromBytes() throws Exception {
//        InputStream base64EncodedStream = getClass().getResourceAsStream("/ad-base64-pac.txt");
//        Base64InputStream decodedStream = new Base64InputStream(new InputStreamReader(base64EncodedStream));
//
//        byte[] ndrEncodedPacData = new byte[base64EncodedStream.available()];
//        decodedStream.read(ndrEncodedPacData);
//        decodedStream.close();
//
//        NDRStream ndrEncodedStream = new NDRStream(ndrEncodedPacData);
//
//        Pac pac = new Pac();
//        pac.deserialize(ndrEncodedStream);
//
//        System.out.println("PAC: ");
//    }

}
