package org.gridman.classloader;

import com.tangosol.net.CacheFactory;
import com.tangosol.net.Cluster;
import com.tangosol.net.DefaultCacheServer;
import org.apache.log4j.Logger;

/**
 * Start up a Coherence Server for use in a Classloader.
 *
 * @author Andrew Wilson
 * @author <a href="jk@thegridman.com">Jonathan Knight</a>
 */
public class SandboxCoherenceServer implements SandboxServer {
    private static final Logger logger = Logger.getLogger(SandboxCoherenceServer.class);

    private ClassLoader classLoader;
    private Cluster cluster;

    public SandboxCoherenceServer() {} // default constructor required

    @Override public void start() {
        logger.info("Starting Server");
        classLoader = Thread.currentThread().getContextClassLoader();
        DefaultCacheServer.startDaemon();
        cluster = CacheFactory.getCluster();
        while (cluster == null) {
            cluster = CacheFactory.getCluster();
        }
        logger.info("Started Server");
    }

    @Override public boolean isStarted() {
        boolean running;
        ClassLoader saved = Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader(classLoader);
        running = cluster != null && cluster.isRunning();
        Thread.currentThread().setContextClassLoader(saved);
        return running;
    }

    @Override public void shutdown() {
        logger.info("Shutting down Server");
        ClassLoader saved = Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader(classLoader);
        DefaultCacheServer.shutdown();
        while (cluster.isRunning()) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                // ignored
            }
            logger.debug("Waiting for cluster to stop " + cluster);
        }
        cluster = null;
        Thread.currentThread().setContextClassLoader(saved);
        logger.info("Shut down Server");
    }
}
