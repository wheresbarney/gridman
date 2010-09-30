package org.gridman.coherence.security.demo;

import com.tangosol.net.CacheFactory;
import com.tangosol.net.DefaultCacheServer;
import com.tangosol.net.NamedCache;
import org.gridman.classloader.SystemPropertyLoader;

/**
 * A simple Demo Server
 */
public class DemoServer {

    public static void main(String[] args) throws InterruptedException {
        SystemPropertyLoader.loadSystemProperties("/coherence/security/demo/securityDemoDefault.properties");
        DefaultCacheServer.start();
        NamedCache cache = CacheFactory.getCache(DemoSecurityProvider.PERMISSION_CACHE);
        DemoSecurityPermission perm = new DemoSecurityPermission("admin", DemoSecurityProvider.PERMISSION_CACHE, true, false);
        cache.put(perm,perm);
        synchronized(DemoServer.class) { DemoServer.class.wait(); }
    }
}
