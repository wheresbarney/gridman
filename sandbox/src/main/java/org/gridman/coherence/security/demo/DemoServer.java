package org.gridman.coherence.security.demo;

import com.tangosol.net.CacheFactory;
import com.tangosol.net.DefaultCacheServer;
import com.tangosol.net.NamedCache;
import org.gridman.classloader.SystemPropertyLoader;
import org.gridman.coherence.security.simple.SimpleSecurityPermission;
import org.gridman.coherence.security.simple.SimpleSecurityProvider;

/**
 * A simple Demo Server
 */
public class DemoServer {

    public static void main(String[] args) throws InterruptedException {
        SystemPropertyLoader.loadSystemProperties("/coherence/security/demo/securityDemoDefault.properties");
        DefaultCacheServer.start();
        String permissionCacheName = SimpleSecurityProvider.getInstance().getPermissionCacheName();
        NamedCache cache = CacheFactory.getCache(permissionCacheName);
        SimpleSecurityPermission perm = new SimpleSecurityPermission("admin", permissionCacheName, true, false);
        cache.put(perm,perm);
        synchronized(DemoServer.class) { DemoServer.class.wait(); }
    }
}
