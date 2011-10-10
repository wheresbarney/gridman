package org.gridman.classloader.coherence;

/**
 * @author Jonathan Knight
 */
public class JaasClusterClassloaderLifecycle extends CoherenceClassloaderLifecycle {

    public JaasClusterClassloaderLifecycle() {
        super("org.gridman.coherence.security.JaasDefaultCacheServer", "startDaemon", "shutdown");
    }
}
