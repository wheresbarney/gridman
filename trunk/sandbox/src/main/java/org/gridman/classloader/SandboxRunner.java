package org.gridman.classloader;

import org.apache.log4j.Logger;

import java.net.URL;
import java.util.List;
import java.util.ArrayList;
import java.util.Properties;

/**
 * The Sandbox Runner will start the given class in a SandboxClassLoader.
 * This uses the ClientFirstClassLoader to make sure that any classes not beginning with java are loaded in a sandbox.
 */
public class SandboxRunner {
    public static final Logger logger = Logger.getLogger(SandboxRunner.class);
    private static Throwable ALL_OK = new Throwable("ALL_OK");
    
    private final String className;
    private volatile Throwable throwable;
    private Properties args;
    private Object instance;
    private Class aClass;

    public SandboxRunner(String className, Properties args) throws Throwable {
        logger.debug("In SandboxRunner");
        this.className = className;
        this.args = args;
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
        return (Boolean)aClass.getMethod("isStarted",null).invoke(instance,null);
    }

    public void shutdown() throws Exception {
        logger.info("Calling shutdown");
        aClass.getMethod("shutdown",null).invoke(instance,null);
        logger.info("Called shutdown");        
    }

    private class MyRunner implements Runnable {

        private MyRunner() {
            new Thread(this).start();
        }

        public void run() {
            try {
                logger.debug("ClassPath is" + System.getProperty("java.class.path"));
                String[] vals = System.getProperty("java.class.path").split(System.getProperty("path.separator"));
                List<URL> urls = new ArrayList();
                for (int i = 0; i < vals.length; i++) {
                    logger.debug("Adding classpath : " + vals[i]);
                    String ending = vals[i].endsWith(".jar") ? "" : "/";
                    urls.add(new URL("file:///" + vals[i] + ending));
                }
                ClassLoader loader = new ChildFirstClassLoader(urls.toArray(new URL[0]), this.getClass().getClassLoader());
                Thread.currentThread().setContextClassLoader(loader);
                aClass = loader.loadClass(className);
                instance = aClass.newInstance();
                aClass.getMethod("start",Properties.class).invoke(instance,args);
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
        sb.append("SandboxRunner");
        sb.append("{className='").append(className).append('\'');
        sb.append(", args=").append(args);
        sb.append('}');
        return sb.toString();
    }
}
