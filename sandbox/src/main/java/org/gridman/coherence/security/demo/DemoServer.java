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
        NamedCache cache = CacheFactory.getCache(SecurityPermission.PERMISSION_CACHE);
        SecurityPermission perm = new SecurityPermission("admin", SecurityPermission.PERMISSION_CACHE, SecurityPermission.PERMISSION_WRITE);
        cache.put(perm,perm);
        perm = new SecurityPermission("admin", SecurityPermission.PERMISSION_CACHE, SecurityPermission.PERMISSION_READ);        
        cache.put(perm,perm);
        synchronized(DemoServer.class) { DemoServer.class.wait(); }
    }
}
