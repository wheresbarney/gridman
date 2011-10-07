package org.gridman.testtools.classloader;

/**
 * 
 * @author Andrew Wilson
 */
public interface ClassloaderLifecycle {

    // Start the server
    public void start();

    // Has started?
    public boolean isStarted();

    // Shut it down
    public void shutdown();

    void suspendNetwork();

    void unsuspendNetwork();

    Object invoke(String className, String methodName, Class[] paramTypes, Object[] params);
}
