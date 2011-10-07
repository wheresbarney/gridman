package org.gridman.coherence.backup;

import com.tangosol.net.CacheFactory;
import com.tangosol.net.NamedCache;
import org.gridman.testtools.classloader.SystemPropertyLoader;

/**
 * @author Jonathan Knight
 */
public class Main {

    @SuppressWarnings({"unchecked"})
    public static void main(String[] args) throws Exception {
        SystemPropertyLoader.loadSystemProperties(
                "/coherence/backup/common-client.properties",
                "/coherence/backup/client.properties");

        System.err.println("Loading cache...");
        NamedCache cache = CacheFactory.getCache("dist-test");
        for (int i=0; i<1000; i++) {
            cache.put("key-" + i, "value-" + i);
        }

//        System.err.println("Sleeping...");
//        Thread.sleep(10000);
//
//        System.err.println("Stopping nodes...");
//        InvocationService service = (InvocationService) CacheFactory.getService("ClientInvokeService");
//        service.query(new NodeExitInvocable("storage-node", 2), null);
//
//        System.err.println("Sleeping...");
//        Thread.sleep(10000);
//
//        System.err.println("Getting data...");
//        for (int i=0; i<1000; i++) {
//            System.err.println("Get: " + i + " = " + cache.get("key-" + i));
//        }
    }
}
