package org.gridman.coherence.security;

import com.tangosol.net.DefaultCacheServer;
import org.gridman.security.JaasHelper;

import javax.security.auth.Subject;
import java.security.PrivilegedExceptionAction;

/**
 * This class is a wrapper around {@link com.tangosol.net.DefaultCacheServer} and will
 * run a normal Coherence Cache Server wrapped in a {@link java.security.PrivilegedExceptionAction}
 * and hence withing the scope of a {@link javax.security.auth.Subject}.
 *
 * @author Jonathan Knight
 */
public class JaasDefaultCacheServer {

    public static void main(final String[] args) throws Exception {
        JaasDefaultCacheServer.startMain(args);
    }

    public static void start() throws Exception {
        run(new PrivilegedExceptionAction() {
            public Object run() throws Exception {
                DefaultCacheServer.start();
                return null;
            }
        });
    }

    public static void shutdown() {
        DefaultCacheServer.shutdown();
    }
    
    public static void startDaemon() throws Exception {
        Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
            @Override
            public void uncaughtException(Thread t, Throwable e) {
                System.err.println("Uncaught exception from Thread " + t.getName());
                e.printStackTrace();
                System.exit(1);
            }
        });

        run(new PrivilegedExceptionAction() {
            public Object run() throws Exception {
                DefaultCacheServer.startDaemon();
                return null;
            }
        });
    }

    public static void startMain(final String[] args) throws Exception {
        run(new PrivilegedExceptionAction() {
            public Object run() throws Exception {
                DefaultCacheServer.main(args);
                return null;
            }
        });
    }

    private static void run(PrivilegedExceptionAction<?> action) throws Exception {
        Subject subject = JaasHelper.logon();
        Subject.doAs(subject, action);
    }

}
