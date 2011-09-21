package org.gridman.classloader;

/**
 * Interface implemented by actions that can be run
 * with isolated sets of System properties.
 *
 * @param <T> the return type of this actions run method
 *
 * @author Jonathan Knight
 */
public interface IsolatedAction<T> {

    /**
     * Perform an action.
     * This method is called by {@link org.gridman.classloader.PropertyIsolation)} runIsolated method after
     * setting the isolated System properties.
     * The return value from this method will be returned to the caller of
     * the {@link org.gridman.classloader.PropertyIsolation} runIsolated method
     *
     * @return a class-dependent value that may represent the results of the
     *	       action.  Each class that implements
     *	       <code>IsolatedAction</code> should document what
     *         (if anything) this value represents.
     */
    T run();
}
