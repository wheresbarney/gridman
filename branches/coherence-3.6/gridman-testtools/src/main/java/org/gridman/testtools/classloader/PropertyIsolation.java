package org.gridman.testtools.classloader;

import java.util.Properties;

/**
 * Utility class to run code with an isolated set of System properties
 *
 * @author <a href="jk@thegridman.com">Jonathan Knight</a>
 */
public class PropertyIsolation {

    /**
     * Run the specified {@link IsolatedAction} with its own
     * set of System properties. These properties and any changes to them are only visible to the
     * specific {@link IsolatedAction}.
     *
     * @param localProperties - The System properties to assign to the action
     * @param isolatedAction the action to perform
     * @param <T> The return type of the action's run method
     *
     * @return any return value from executing the action's run method
     * @throws IsolatedActionException if the IsolatedExceptionAction throws a checked Exception
     */
    @SuppressWarnings({"unchecked"})
    public static <T> T runIsolated(final Properties localProperties, final IsolatedExceptionAction<T> isolatedAction) throws IsolatedActionException {
        final Object[] result = new Object[1];
        final boolean[] done = new boolean[1];
        final Throwable[] exception = new Throwable[1];

        final Thread thread = new Thread() {
            @Override
            public void run() {
                ClassLoaderProperties.use();
                PropertyIsolatingClassLoader isolatingClassLoader = new PropertyIsolatingClassLoader();
                isolatingClassLoader.setProperties(System.getProperties());
                isolatingClassLoader.setProperties(localProperties);
                this.setContextClassLoader(isolatingClassLoader);

                try {
                    result[0] = isolatedAction.run();
                } catch (Throwable e) {
                    exception[0] = e;
                }
                done[0] = true;
                synchronized (this) {
                    this.notifyAll();
                }
            }
        };

        waitForThread(thread, done);
        if (exception[0] != null) {
            throw new IsolatedActionException(exception[0]);
        }
        return (T)result[0];
    }

    /**
     * Run the specified {@link org.gridman.classloader.IsolatedAction} with its own
     * set of System properties. These properties and any changes to them are only visible to the
     * specific {@link org.gridman.classloader.IsolatedAction}.
     *
     * @param localProperties - The System properties to assign to the isolatedAction
     * @param isolatedAction the isolatedAction to perform
     * @param <T> The return type of the isolatedAction's run method
     *
     * @return any return value from executing the isolatedAction's run method
     */
    @SuppressWarnings({"unchecked"})
    public static <T> T runIsolated(final Properties localProperties, final IsolatedAction<T> isolatedAction) {
        final Object[] result = new Object[1];
        final boolean[] done = new boolean[1];

        final Thread thread = new Thread() {
            @Override
            public void run() {
                ClassLoaderProperties.use();
                PropertyIsolatingClassLoader isolatingClassLoader = new PropertyIsolatingClassLoader();
                isolatingClassLoader.setProperties(System.getProperties());
                isolatingClassLoader.setProperties(localProperties);
                this.setContextClassLoader(isolatingClassLoader);

                result[0] = isolatedAction.run();
                done[0] = true;
                synchronized (this) {
                    this.notifyAll();
                }
            }
        };

        waitForThread(thread, done);
        return (T)result[0];
    }

    private static void waitForThread(final Thread thread, boolean[] done) {
        done[0] = false;
        thread.start();
        while (!done[0]) {
            try {
                synchronized (thread) {
                    thread.wait(1000);
                }
            } catch (InterruptedException _ignored) {
                // ignored
            }
        }
    }
}
