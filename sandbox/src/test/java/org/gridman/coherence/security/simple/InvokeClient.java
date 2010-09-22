package org.gridman.coherence.security.simple;

import com.tangosol.io.pof.PofReader;
import com.tangosol.io.pof.PofWriter;
import com.tangosol.io.pof.PortableObject;
import com.tangosol.net.CacheFactory;
import com.tangosol.net.Invocable;
import com.tangosol.net.InvocationService;
import org.gridman.classloader.SystemPropertyLoader;

import java.io.IOException;
import java.util.Date;

/**
 * @todo Why is there no NullImplementation.getInvocable();
 */
public class InvokeClient implements Invocable, PortableObject {
    public InvokeClient() {
        System.out.println("Calling InvokeClient");
    }
    public static void main(String[] args) throws Throwable {
        SystemPropertyLoader.loadEnvironment("client");
        InvocationService service = (InvocationService) CacheFactory.getService("BatchService");
        System.out.println("Result : " + service.query(new InvokeClient(), null));
    }

    public void init(InvocationService invocationService) {
        System.out.println("Calling init : " + invocationService);
    }

    public void run() {
        System.out.println("Calling run");
    }

    public Object getResult() {
        System.out.println("Calling getResult");
        return new Date();
    }

    @Override
    public void readExternal(PofReader pofReader) throws IOException {

    }

    @Override
    public void writeExternal(PofWriter pofWriter) throws IOException {

    }
}
