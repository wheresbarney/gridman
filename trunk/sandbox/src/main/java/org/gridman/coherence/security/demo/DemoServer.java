package org.gridman.coherence.security.demo;

import com.tangosol.net.CacheFactory;
import com.tangosol.net.DefaultCacheServer;
import com.tangosol.net.NamedCache;
import org.gridman.classloader.SystemPropertyLoader;

/**
 * A simple Demo Server
 */
public class DemoServer {
    static final String PERMISSION_CACHE = "PermissionCache";
    static final String CLIENT_INVOKE_SERVICE = "ClientInvokeService";
    static final String SERVER_INVOKE_SERVICE = "ServerInvokeService";


    public static void main(String[] args) throws InterruptedException {
        SystemPropertyLoader.loadSystemProperties("/coherence/security/demo/securityDemoDefault.properties");
        DefaultCacheServer.start();
        NamedCache cache = CacheFactory.getCache(PERMISSION_CACHE);
        DemoSecurityPermission perm = new DemoSecurityPermission("admin", PERMISSION_CACHE, true, false);
        cache.put(perm,perm);
        synchronized(DemoServer.class) { DemoServer.class.wait(); }
    }
}
