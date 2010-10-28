package org.gridman.security.kerberos.junit;


import org.junit.runner.notification.RunNotifier;
import org.junit.runners.model.Statement;
import org.junit.runners.model.TestClass;

import javax.naming.NamingException;
import java.io.IOException;


/**
 * The interface representing a state in the lifecycle of a service
 * during integration testing.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 * @version $Rev$, $Date$
 */
public interface KdcServerState {
    /**
     * IsolatedAction where an attempt is made to create the service.  Service
     * creation in this system is the combined instantiation and
     * configuration which takes place when the factory is used to get
     * a new instance of the service.
     *
     * @param settings The inherited settings
     * @throws javax.naming.NamingException if we can't create the service
     */
    void create(InheritableKdcServerSettings settings) throws NamingException;


    /**
     * IsolatedAction where an attempt is made to destroy the service. This
     * entails nulling out reference to it and triggering garbage
     * collection.
     */
    void destroy();


    /**
     * IsolatedAction where an attempt is made to erase the contents of the
     * working directory used by the service for various files including
     * partition database files.
     *
     * @throws java.io.IOException on errors while deleting the working directory
     */
    void cleanup(InheritableKdcServerSettings settings) throws IOException;


    /**
     * IsolatedAction where an attempt is made to start up the service.
     *
     * @throws Exception on failures to start the core directory service
     */
    void startup(InheritableKdcServerSettings settings) throws Exception;


    /**
     * IsolatedAction where an attempt is made to shutdown the service.
     *
     * @throws Exception on failures to stop the core directory service
     */
    void shutdown(InheritableKdcServerSettings settings) throws Exception;


    /**
     * IsolatedAction where an attempt is made to run a test against the service.
     * <p/>
     * All annotations should have already been processed for
     * InheritableServerSettings yet they and others can be processed since we have
     * access to the method annotations below
     *
     * @param testClass the class whose test method is to be run
     * @param statement the test method which is to be run
     * @param notifier  a notifier to report failures to
     * @param settings  the inherited settings and annotations associated with
     *                  the test method
     */
    void test(TestClass testClass, Statement statement, RunNotifier notifier, InheritableKdcServerSettings settings);


    /**
     * IsolatedAction where an attempt is made to revert the service to it's
     * initial start up state by using a previous snapshot.
     *
     * @throws Exception on failures to revert the state of the core
     *                   directory service
     */
    void revert() throws Exception;
}