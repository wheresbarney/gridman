package org.gridman.testtools.coherence.classloader;

/**
 * @author Jonathan Knight
 */
public class JaasClusterClassloaderLifecycle extends CoherenceClassloaderLifecycle {

    public JaasClusterClassloaderLifecycle() {
        super("org.gridman.coherence.security.JaasDefaultCacheServer", "startDaemon", "shutdown");
    }
}
