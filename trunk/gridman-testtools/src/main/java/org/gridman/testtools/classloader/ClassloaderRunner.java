package org.gridman.testtools.classloader;

import org.apache.log4j.Logger;

import java.lang.reflect.Method;
import java.util.Properties;

/**
 * The ClassloaderRunner will start the given class in an isolated ClassLoader.
 * This uses the ClientFirstClassLoader to make sure that any classes not beginning with java are loaded in an isolated classloader.
 *
 * @author Andrew Wilson
 * @author <a href="jk@thegridman.com">Jonathan Knight</a>
 */
public class ClassloaderRunner {
    public static final Logger logger = Logger.getLogger(ClassloaderRunner.class);
    private static Throwable ALL_OK = new Throwable("ALL_OK");
    
    private final String className;
    private volatile Throwable throwable;
    private Properties localSystemProperties;
    private Object instance;
    private Class<?> aClass;

    public ClassloaderRunner(String className, Properties localSystemProperties) throws Throwable {
        logger.debug("In ClassloaderRunner");
        this.className = className;
        this.localSystemProperties = localSystemProperties;
        MyRunner myRunner = new MyRunner();
        if(throwable == null) {
            synchronized(myRunner) { myRunner.wait(); }
        }
        if(throwable != ALL_OK) {
            throw throwable;
        }
    }

    public boolean isStarted() throws Exception {
        logger.info("Calling isStarted");
        return (Boolean)aClass.getMethod("isStarted").invoke(instance);
    }

    public void shutdown() throws Exception {
        logger.info("Calling shutdown");
        aClass.getMethod("shutdown").invoke(instance);
        logger.info("Called shutdown");        
    }

    public void suspendNetwork() throws Exception {
        logger.info("Calling suspendNetwork");
        aClass.getMethod("suspendNetwork").invoke(instance);
        logger.info("Called suspendNetwork");
    }

    public void unsuspendNetwork() throws Exception {
        logger.info("Calling unsuspendNetwork");
        aClass.getMethod("unsuspendNetwork").invoke(instance);
        logger.info("Called unsuspendNetwork");
    }

    @SuppressWarnings({"unchecked"})
    public Object invoke(String className, String methodName, Class[] paramTypes, Object[] params) throws Exception {
        Method method = aClass.getMethod("invoke", String.class, String.class, Class[].class, Object[].class);
        return method.invoke(instance, className, methodName, paramTypes, params);
    }

    private class MyRunner implements Runnable {

        private MyRunner() {
            new Thread(this).start();
        }

        public void run() {
            try {
                ClassLoader loader = ChildFirstClassLoader.newInstance(localSystemProperties);
                Thread.currentThread().setContextClassLoader(loader);
                aClass = loader.loadClass(className);
                instance = aClass.newInstance();
                aClass.getMethod("start").invoke(instance);
                throwable = ALL_OK;
                logger.info("Succeeded in starting " + className);
            } catch(Throwable t) {
                logger.error("Failed in starting " + className + " : " + t);
                throwable = t;
            }
            synchronized(this) { this.notifyAll(); }
        }
    }

    @Override public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append("ClassloaderRunner");
        sb.append("{className='").append(className).append('\'');
        sb.append(", args=").append(localSystemProperties);
        sb.append('}');
        return sb.toString();
    }
}
