package org.gridman.coherence.security.kerberos;

import com.tangosol.io.pof.PofReader;
import com.tangosol.io.pof.PofSerializer;
import com.tangosol.io.pof.PofWriter;
import org.gridman.security.kerberos.activedirectory.PacSid;

import java.io.IOException;

/**
 * @author Jonathan Knight
 */
public class PacSidPofSerializer implements PofSerializer {

    @Override
    public void serialize(PofWriter pofWriter, Object o) throws IOException {
        PacSid sid = (PacSid)o;
        pofWriter.writeByte(100, sid.getRevision());
        pofWriter.writeByteArray(101, sid.getAuthorityBytes());
        long[] subAuthorities = sid.getSubAuthorities();
        pofWriter.writeLongArray(102, subAuthorities);
        pofWriter.writeRemainder(null);
    }

    @Override
    public Object deserialize(PofReader pofReader) throws IOException {
        byte revision = pofReader.readByte(100);
        byte[] authority = pofReader.readByteArray(101);
        long[] subAuthorities = pofReader.readLongArray(102);
        pofReader.readRemainder();
        return new PacSid(revision, authority, subAuthorities);
    }
}
