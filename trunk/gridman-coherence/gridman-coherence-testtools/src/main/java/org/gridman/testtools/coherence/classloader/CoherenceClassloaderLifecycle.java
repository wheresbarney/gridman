package org.gridman.testtools.coherence.classloader;

import com.tangosol.net.CacheFactory;
import com.tangosol.net.Cluster;
import com.tangosol.util.Base;
import org.apache.log4j.Logger;
import org.gridman.testtools.classloader.ClassloaderLifecycle;

import java.lang.reflect.Method;

/**
 * Start up a Coherence Server for use in a Classloader.
 *
 * @author Andrew Wilson
 * @author <a href="jk@thegridman.com">Jonathan Knight</a>
 */
public class CoherenceClassloaderLifecycle implements ClassloaderLifecycle {

    private static final Logger logger = Logger.getLogger(CoherenceClassloaderLifecycle.class);

    private ClassLoader classLoader;
    private Cluster cluster;

    private String classname = "com.tangosol.net.DefaultCacheServer";
    private String startMethod = "startDaemon";
    private String stopMethod = "shutdown";

    public CoherenceClassloaderLifecycle() {} // default constructor required

    public CoherenceClassloaderLifecycle(String classname, String startMethod, String stopMethod) {
        this.classname = classname;
        this.startMethod = startMethod;
        this.stopMethod = stopMethod;
    }

    private Method getStartMethod() {
        try {
            Class clazz = Class.forName(classname);
            return clazz.getMethod(startMethod);
        } catch (Exception e) {
            throw Base.ensureRuntimeException(e);
        }
    }

    private Method getStopMethod() {
        try {
            Class clazz = Class.forName(classname);
            return clazz.getMethod(stopMethod);
        } catch (Exception e) {
            throw Base.ensureRuntimeException(e);
        }
    }

    public void start() {
        logger.info("Starting Server");
        classLoader = Thread.currentThread().getContextClassLoader();
        try {
            getStartMethod().invoke(null);
        } catch (Exception e) {
            throw Base.ensureRuntimeException(e);
        }
        cluster = CacheFactory.getCluster();
        while (cluster == null) {
            cluster = CacheFactory.getCluster();
        }
        logger.info("Started Server");
    }

    public boolean isStarted() {
        boolean running;
        ClassLoader saved = Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader(classLoader);
        running = cluster != null && cluster.isRunning();
        Thread.currentThread().setContextClassLoader(saved);
        return running;
    }

    public void suspendNetwork() {
        ClassLoader saved = Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader(classLoader);
        callSuspend(true);
        Thread.currentThread().setContextClassLoader(saved);
    }

    public void unsuspendNetwork() {
        ClassLoader saved = Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader(classLoader);
        callSuspend(false);
        Thread.currentThread().setContextClassLoader(saved);
    }

    private void callSuspend(boolean suspend) {
        try {
            Class clz = Class.forName("org.gridman.testtools.coherence.net.PacketController");
            Method suspendMethod = clz.getDeclaredMethod("suspend", boolean.class);
            suspendMethod.invoke(null, suspend);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void shutdown() {
        logger.info("Shutting down Server");
        ClassLoader saved = Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader(classLoader);

        try {
            Class cls = classLoader.loadClass("com.tangosol.net.CacheFactory");
            Method shutdown = cls.getDeclaredMethod("shutdown");
            shutdown.invoke(null);
            cls = classLoader.loadClass("com.tangosol.net.DefaultCacheServer");
            shutdown = cls.getDeclaredMethod("shutdown");
            shutdown.invoke(null);
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            getStopMethod().invoke(null);
        } catch (Exception e) {
            throw Base.ensureRuntimeException(e);
        }
        while (cluster.isRunning()) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException ignored) {
            }
            logger.debug("Waiting for cluster to stop " + cluster);
        }

        cluster = null;
        Thread.currentThread().setContextClassLoader(saved);
        logger.info("Shut down Server");
    }
}
