package org.gridman.coherence.security.kerberos;

import com.tangosol.io.pof.ConfigurablePofContext;
import com.tangosol.util.Binary;
import com.tangosol.util.ExternalizableHelper;
import org.gridman.security.kerberos.activedirectory.PacSid;
import org.junit.Test;

import static junit.framework.Assert.assertEquals;

/**
 * @author Jonathan Knight
 */
public class PacSidPofSerializerTest {

    @Test
    public void shouldSerializeAndDeserializePacSidWithNoSubAuthorities() throws Exception {
        ConfigurablePofContext serializer = new ConfigurablePofContext("coherence/security/security-pof-config.xml");

        PacSid sid = new PacSid((byte)1, new byte[]{1,2,3,4,5});
        Binary binary = ExternalizableHelper.toBinary(sid, serializer);
        PacSid result = (PacSid) ExternalizableHelper.fromBinary(binary, serializer);
        assertEquals(sid, result);
    }

    @Test
    public void shouldSerializeAndDeserializePacSidWithSubAuthorities() throws Exception {
        ConfigurablePofContext serializer = new ConfigurablePofContext("coherence/security/security-pof-config.xml");

        PacSid sid = new PacSid((byte)1, new byte[]{1,2,3,4,5}, 10, 11, 12);
        Binary binary = ExternalizableHelper.toBinary(sid, serializer);
        PacSid result = (PacSid) ExternalizableHelper.fromBinary(binary, serializer);
        assertEquals(sid, result);
    }
}
