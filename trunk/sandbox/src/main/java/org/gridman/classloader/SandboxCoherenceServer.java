package org.gridman.classloader;

import com.tangosol.net.CacheFactory;
import com.tangosol.net.DefaultCacheServer;
import org.apache.log4j.Logger;

/**
 * Start up a Coherence Server for use in a Classloader.
 */
public class SandboxCoherenceServer implements SandboxServer {
    private static final Logger logger = Logger.getLogger(SandboxCoherenceServer.class);

    private ClassLoader classLoader;

    public SandboxCoherenceServer() {} // default constructor required

    @Override public void start() {
        logger.info("Starting Server");
        classLoader = Thread.currentThread().getContextClassLoader();
        DefaultCacheServer.startDaemon();
        logger.info("Started Server");
    }

    @Override public boolean isStarted() {
        boolean running;
        ClassLoader saved = Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader(classLoader);
        running = CacheFactory.getCluster().isRunning();
        Thread.currentThread().setContextClassLoader(saved);
        return running;
    }

    @Override public void shutdown() {
        logger.info("Shutting down Server");
        ClassLoader saved = Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader(classLoader);
        DefaultCacheServer.shutdown();
        Thread.currentThread().setContextClassLoader(saved);
        logger.info("Shut down Server");
    }
}
