package org.gridman.classloader;

import org.apache.log4j.Logger;
import com.tangosol.net.DefaultCacheServer;
import com.tangosol.net.CacheFactory;

import java.util.Map;
import java.util.Properties;

/**
 * Start up a Coherence Server for use in a Classloader.
 */
public class SandboxCoherenceServer implements SandboxServer {
    private static final Logger logger = Logger.getLogger(SandboxCoherenceServer.class);
    
    public SandboxCoherenceServer() {} // default constructor required

    @Override public void start(Properties properties) {
        logger.info("Starting Server");
        for (Map.Entry<Object, Object> entry : properties.entrySet()) {
            logger.debug("Setting : " + entry.getKey() + " : " + entry.getValue());
            System.setProperty((String)entry.getKey(), (String)entry.getValue());
        }
        DefaultCacheServer.startDaemon();
        logger.info("Started Server");
    }

    @Override public boolean isStarted() {
        return CacheFactory.getCluster().isRunning();
    }

    @Override public void shutdown() {
        logger.info("Shutting down Server");
        DefaultCacheServer.shutdown();
        logger.info("Shut down Server");
    }
}