package org.gridman.coherence.backup;

import com.tangosol.io.pof.PofReader;
import com.tangosol.io.pof.PofWriter;
import com.tangosol.io.pof.PortableObject;
import com.tangosol.net.AbstractInvocable;

import java.io.IOException;

/**
 * @author Jonathan Knight
 */
public class SystemExitInvocable extends AbstractInvocable implements PortableObject {

    @Override
    public void run() {
        System.exit(0);
    }

    @Override
    public void readExternal(PofReader pofReader) throws IOException {
    }

    @Override
    public void writeExternal(PofWriter pofWriter) throws IOException {
    }
}
