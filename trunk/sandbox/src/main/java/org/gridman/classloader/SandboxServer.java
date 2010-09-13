package org.gridman.classloader;

import java.util.Properties;

/**
 * 
 */
public interface SandboxServer {

    // Start the server
    public void start();

    // Has started?
    public boolean isStarted();

    // Shut it down
    public void shutdown();
}
