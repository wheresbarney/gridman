package org.gridman.coherence.util;

import com.tangosol.io.pof.PofReader;
import com.tangosol.io.pof.PofWriter;
import com.tangosol.io.pof.PortableObject;
import com.tangosol.net.Invocable;
import com.tangosol.net.InvocationService;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.util.Date;

/**
 * @todo Why is there no NullImplementation.getInvocable();
 */
public class NullInvokable implements Invocable, PortableObject {
    private static final Logger logger = Logger.getLogger(NullInvokable.class);

    public NullInvokable() {
        logger.debug(NullInvokable.class.getName());
    }

    public void init(InvocationService invocationService) {
        logger.debug("Calling init : " + invocationService);
    }

    public void run() { logger.debug("Calling run"); }

    public Object getResult() {
        logger.debug("Calling getResult");
        return new Date();
    }

    @Override public void readExternal(PofReader pofReader) throws IOException {}

    @Override public void writeExternal(PofWriter pofWriter) throws IOException {}
}
