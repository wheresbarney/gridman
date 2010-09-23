package org.gridman.coherence.security.demo;

import com.tangosol.net.DefaultCacheServer;
import org.gridman.classloader.SystemPropertyLoader;

/**
 * A simple Demo Server
 */
public class DemoServer {
    public static void main(String[] args) {
        SystemPropertyLoader.loadSystemProperties("/coherence/security/demo/securityDemoDefault.properties");
        DefaultCacheServer.main(args);
    }
}
