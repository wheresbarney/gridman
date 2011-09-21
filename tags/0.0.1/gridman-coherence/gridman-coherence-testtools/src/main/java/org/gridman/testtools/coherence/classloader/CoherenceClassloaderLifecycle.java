package org.gridman.testtools.coherence.classloader;

import com.tangosol.net.CacheFactory;
import com.tangosol.net.Cluster;
import com.tangosol.net.DefaultConfigurableCacheFactory;
import com.tangosol.net.Member;
import com.tangosol.run.xml.XmlElement;
import com.tangosol.util.Base;
import com.tangosol.util.Service;
import org.gridman.testtools.classloader.ClassloaderLifecycle;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.List;

/**
 * Start up a Coherence Server for use in a Classloader.
 *
 * @author Andrew Wilson
 * @author <a href="jk@thegridman.com">Jonathan Knight</a>
 */
public class CoherenceClassloaderLifecycle implements ClassloaderLifecycle {
    private ClassLoader classLoader;
    private Cluster cluster;

    private String classname = "com.tangosol.net.DefaultCacheServer";
    private String startMethod = "startDaemon";
    private String stopMethod = "shutdown";

    public CoherenceClassloaderLifecycle() {
    } // default constructor required

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
        CacheFactory.log("Starting Server", CacheFactory.LOG_INFO);
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
        CacheFactory.log("Started Server", CacheFactory.LOG_INFO);
    }

    public boolean isStarted() {
        boolean running;
        ClassLoader saved = Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader(classLoader);
        running = cluster != null && cluster.isRunning() && areAutoStartServicesRunning(classLoader);
        Thread.currentThread().setContextClassLoader(saved);
        return running;
    }

    @SuppressWarnings({"unchecked"})
    public boolean areAutoStartServicesRunning(ClassLoader classLoader) {
        DefaultConfigurableCacheFactory factory = (DefaultConfigurableCacheFactory) CacheFactory.getCacheFactoryBuilder().getConfigurableCacheFactory(classLoader);
        XmlElement xmlConfig = factory.getConfig();

        Member member = cluster.getLocalMember();
        boolean running = true;
        for (XmlElement xmlScheme : (List<XmlElement>)xmlConfig.getSafeElement("caching-schemes").getElementList()) {
            if (xmlScheme.getSafeElement("autostart").getBoolean()) {
                String serviceName = xmlScheme.getSafeElement("service-name").getString();
                if (serviceName != null && serviceName.length() > 0) {
                    Service service = cluster.getService(serviceName);
                    running = service != null && service.isRunning();
                    if (!running) {
                        CacheFactory.log("Auto-Start Service still starting - service=" + serviceName + " member=" + member, CacheFactory.LOG_INFO);
                        break;
                    }
                }
            }
        }

        return running;
    }

    public Object invoke(String className, String methodName, Class[] paramTypes, Object[] params) {
        ClassLoader saved = Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader(classLoader);
        try {

            Class cls = Class.forName(className);
            Method method = cls.getDeclaredMethod(methodName, paramTypes);
            Object result;
            if (Modifier.isStatic(method.getModifiers())) {
                result = method.invoke(null, params);
            } else {
                result = method.invoke(cls.newInstance(), params);
            }
            return result;
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            Thread.currentThread().setContextClassLoader(saved);
        }
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
        CacheFactory.log("Shutting down Server", CacheFactory.LOG_INFO);
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
            CacheFactory.log("Waiting for cluster to stop " + cluster, CacheFactory.LOG_DEBUG);
        }

        cluster = null;
        Thread.currentThread().setContextClassLoader(saved);
//        CacheFactory.log("Shut down Server", CacheFactory.LOG_INFO);
    }
}
