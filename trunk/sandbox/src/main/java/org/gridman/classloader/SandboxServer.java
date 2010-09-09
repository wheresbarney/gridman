package org.gridman.classloader;

import java.util.Properties;

/**
 * Created by IntelliJ IDEA.
 * User: wilsane
 * Date: 07-Sep-2010
 * Time: 11:34:56
 * To change this template use File | Settings | File Templates.
 */
public interface SandboxServer {

    // Start the server
    public void start(Properties properties);

    // Has started?
    public boolean isStarted();

    // Shut it down
    public void shutdown();
}
