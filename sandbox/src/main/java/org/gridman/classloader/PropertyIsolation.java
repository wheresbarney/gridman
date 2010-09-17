package org.gridman.classloader;

import java.util.Properties;

/**
 * Utility class to run code with an isolated set of System properties
 *
 * @author <a href="jk@thegridman.com">Jonathan Knight</a>
 */
public class PropertyIsolation {

    /**
     * Run the specified {@link org.gridman.classloader.PropertyIsolation.Action} with its own
     * set of System properties. These properties and any changes to them are only visible to the
     * specific {@link org.gridman.classloader.PropertyIsolation.Action}.
     *
     * @param localProperties - The System properties to assign to the action
     * @param action the action to perform
     * @param <T> The return type of the action's run method
     *
     * @return any return value from executing the action's run method
     */
    @SuppressWarnings({"unchecked"})
    public static <T> T runIsolated(final Properties localProperties, final Action<T> action) {
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

                result[0] = action.run();
                done[0] = true;
                synchronized (this) {
                    this.notifyAll();
                }
            }
        };

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

        return (T)result[0];
    }

    /**
     * Interface implemented by actions that can be run
     * with isolated sets of System properties.
     *
     * @param <T> the return type of this actions run method
     */
    public static interface Action<T> {

        /**
         * Perform an action.
         * This method is called by {@link org.gridman.classloader.PropertyIsolation)} runIsolated method after
         * setting the isolated System properties.
         * The return value from this method will be returned to the caller of
         * the {@link org.gridman.classloader.PropertyIsolation} runIsolated method 
         *
         * @return the results of running the action.
         */
        T run();
    }
}
