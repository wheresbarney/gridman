package org.gridman.testtools.kerberos;

import org.apache.directory.server.core.DirectoryService;
import org.apache.directory.server.core.partition.Partition;
import org.apache.directory.server.kerberos.kdc.KdcServer;
import org.apache.directory.server.ldap.LdapServer;
import org.gridman.testtools.classloader.IsolatedExceptionAction;
import org.gridman.testtools.classloader.PropertyIsolation;
import org.gridman.testtools.classloader.SystemPropertyLoader;
import org.gridman.testtools.junit.RunIsolated;
import org.junit.runner.Description;
import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.model.Statement;
import org.junit.runners.model.TestClass;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.naming.NamingException;
import javax.security.auth.Subject;
import java.io.IOException;
import java.lang.reflect.Field;
import java.security.PrivilegedAction;
import java.util.List;
import java.util.Properties;

/**
 * @author Jonathan Knight
 */
public abstract class KdcServerContext {
    /**
     * The logger
     */
    private static final Logger LOG = LoggerFactory.getLogger(KdcServerContext.class);

    /**
     * The ThreadLocal containing the contexts
     */
    private static final ThreadLocal<KdcServerContext> CONTEXTS = new ThreadLocal<KdcServerContext>();

    /**
     * current service state with respect to the testing life cycle
     */
    private KdcServerState state;

    /**
     * the DirectoryService managed by this context
     */
    private DirectoryService directoryService;

    /**
     * the ldap server managed by this context
     */
    private LdapServer ldapServer;

    /**
     * the KDC server managed by this context
     */
    private KdcServer kdcServer;

    private List<Partition> partitions;

    /**
     * A private constructor, the class contains only static methods,
     * no need to construct an instance.
     */
    public KdcServerContext() {
        initialise();
        setState(getInitialState());
    }

    protected void initialise() {
    }

    public abstract KdcServerState getInitialState();

    /**
     * Gets the TestServerContext associated with the current thread of
     * execution.  If one does not yet exist it will be created.
     *
     * @return the context associated with the calling thread
     */
    public static KdcServerContext getServerContext() {
        return CONTEXTS.get();
    }

    /**
     * Sets the TestServerContext for this current thread
     *
     * @param context the context associated with the calling thread
     */
    public static void set(KdcServerContext context) {
        CONTEXTS.set(context);
    }

    /**
     * Sets the TestServerContext for this current thread if not already set
     *
     * @param contextClass the context associated with the calling thread
     */
    public static void set(Class<? extends KdcServerContext> contextClass) {
        if (getServerContext() == null) {
            try {
                set(contextClass.newInstance());
            } catch (Exception e) {
                throw new RuntimeException("Error creating KdcServerContext", e);
            }
        }
    }

    /**
     * IsolatedAction where an attempt is made to create the service.  Service
     * creation in this system is the combined instantiation and
     * configuration which takes place when the factory is used to get
     * a new instance of the service.
     *
     * @param settings the settings for this test
     * @throws javax.naming.NamingException if we can't create the service
     */
    public static void create(InheritableKdcServerSettings settings) throws NamingException {
        KdcServerState state = getServerContext().getState();
        state.create(settings);
    }


    /**
     * IsolatedAction where an attempt is made to destroy the service.  This
     * entails nulling out reference to it and triggering garbage
     * collection.
     */
    public static void destroy() {
        KdcServerState state = getServerContext().getState();
        state.destroy();
    }


    /**
     * IsolatedAction where an attempt is made to erase the contents of the
     * working directory used by the service for various files including
     * partition database files.
     *
     * @throws java.io.IOException on errors while deleting the working directory
     */
    public static void cleanup(InheritableKdcServerSettings settings) throws IOException {
        KdcServerState state = getServerContext().getState();
        state.cleanup(settings);
    }


    /**
     * IsolatedAction where an attempt is made to start up the service.
     *
     * @throws Exception on failures to start the core directory service
     */
    public static void startup(InheritableKdcServerSettings settings) throws Exception {
        KdcServerState state = getServerContext().getState();
        state.startup(settings);
    }


    /**
     * IsolatedAction where an attempt is made to shutdown the service.
     *
     * @throws Exception on failures to stop the core directory service
     */
    public static void shutdown(InheritableKdcServerSettings settings) throws Exception {
        KdcServerState state = getServerContext().getState();
        state.shutdown(settings);
    }


    /**
     * IsolatedAction where an attempt is made to run a test against the service.
     *
     * @param testClass the class whose test method is to be run
     * @param statement the test method which is to be run
     * @param notifier  a notifier to report failures to
     * @param settings  the inherited settings and annotations associated with
     *                  the test method
     */
    public static void test(TestClass testClass, Statement statement, RunNotifier notifier,
                            InheritableKdcServerSettings settings) {
        LOG.debug("calling test(): {}", settings.getDescription().getDisplayName());
        KdcServerState state = getServerContext().getState();
        state.test(testClass, statement, notifier, settings);
    }


    /**
     * IsolatedAction where an attempt is made to revert the service to it's
     * initial start up state by using a previous snapshot.
     *
     * @throws Exception on failures to revert the state of the core
     *                   directory service
     */
    public static void revert() throws Exception {
        KdcServerState state = getServerContext().getState();
        state.revert();
    }


    public static void invokeTest(TestClass testClass, Statement statement, RunNotifier notifier, Description description) {
        Throwable thrown = null;
        try {
            Field field;
            try {
                field = testClass.getJavaClass().getDeclaredField("service");
                field.set(testClass.getJavaClass(), getServerContext().getDirectoryService());
            } catch (NoSuchFieldException _ignored) {
                // ignored exception
            }

            try {
                field = testClass.getJavaClass().getDeclaredField("ldapServer");
                field.set(testClass.getJavaClass(), getServerContext().getLdapServer());
            } catch (NoSuchFieldException _ignored) {
                // ignored exception
            }

            try {
                field = testClass.getJavaClass().getDeclaredField("kdcServer");
                field.set(testClass.getJavaClass(), getServerContext().getKdcServer());
            } catch (NoSuchFieldException _ignored) {
                // ignored exception
            }

            PrivilegedTest privilegedTest = null;
            Subject subject = null;
            RunPrivileged privileged = description.getAnnotation(RunPrivileged.class);
            if (privileged != null) {
                field = testClass.getJavaClass().getDeclaredField(privileged.subject());
                subject = (Subject) field.get(testClass.getJavaClass());
                privilegedTest = new PrivilegedTest(statement);
            }

            Properties localProperties = null;
            IsolatedExceptionAction<Throwable> isolatedAction = null;
            RunIsolated isolated = description.getAnnotation(RunIsolated.class);
            if (isolated != null) {
                localProperties = SystemPropertyLoader.loadProperties(isolated.properties());
                if (subject == null) {
                    isolatedAction = new IsolatedTest(statement);
                } else {
                    isolatedAction = new IsolatedPrivilegedTest(subject, privilegedTest);
                }
            }
            // Backward compatibility for deprecated kerberos.RunIsolated
            if (isolated == null) {
                org.gridman.testtools.kerberos.RunIsolated oldIsolated = description.getAnnotation(org.gridman.testtools.kerberos.RunIsolated.class);
                if (oldIsolated != null) {
                    localProperties = SystemPropertyLoader.loadProperties(oldIsolated.properties());
                    if (subject == null) {
                        isolatedAction = new IsolatedTest(statement);
                    } else {
                        isolatedAction = new IsolatedPrivilegedTest(subject, privilegedTest);
                    }
                }
            }

            notifier.fireTestStarted(description);
            if (isolatedAction != null) {
                thrown = PropertyIsolation.runIsolated(localProperties, isolatedAction);
            } else if (privilegedTest != null) {
                thrown = Subject.doAs(subject, privilegedTest);
            } else {
                statement.evaluate();
            }

            if (thrown == null) {
                notifier.fireTestFinished(description);
            }
        }
        catch (IllegalAccessException iae) {
            LOG.error("Failed to invoke test method: " + description.getDisplayName(), iae);
            testAborted(notifier, description, iae);
        }
        catch (Throwable t) {
            thrown = t;
        }

        if (thrown != null) {
            LOG.error("Failed to invoke test method: " + description.getDisplayName(), thrown);
            testAborted(notifier, description, thrown);
        }
    }

    private static class IsolatedTest implements IsolatedExceptionAction<Throwable> {
        private Statement statement;

        private IsolatedTest(Statement statement) {
            this.statement = statement;
        }

        @Override
        public Throwable run() throws Exception {
            Throwable thrown = null;
            try {
                statement.evaluate();
            } catch (Throwable throwable) {
                thrown = throwable;
            }
            return thrown;
        }
    }

    private static class IsolatedPrivilegedTest implements IsolatedExceptionAction<Throwable> {
        private PrivilegedTest test;
        private Subject subject;

        private IsolatedPrivilegedTest(Subject subject, PrivilegedTest test) {
            this.subject = subject;
            this.test = test;
        }

        @Override
        public Throwable run() throws Exception {
            return Subject.doAs(subject, test);
        }
    }

    private static class PrivilegedTest implements PrivilegedAction<Throwable> {
        private Statement statement;

        private PrivilegedTest(Statement statement) {
            this.statement = statement;
        }

        @Override
        public Throwable run() {
            Throwable thrown = null;
            try {
                statement.evaluate();
            } catch (Throwable throwable) {
                thrown = throwable;
            }
            return thrown;
        }
    }

    // -----------------------------------------------------------------------
    // Package Friendly Instance Methods
    // -----------------------------------------------------------------------


    public void setState(KdcServerState state) {
        this.state = state;
    }


    public KdcServerState getState() {
        return state;
    }


//    KdcServerState getNonExistentState() {
//        return nonExistentState;
//    }
//
//
//    KdcServerState getStartedPristineState() {
//        return startedPristineState;
//    }
//
//
//    KdcServerState getStartedNormalState() {
//        return startedNormalState;
//    }


    public LdapServer getLdapServer() {
        return ldapServer;
    }


    public void setLdapServer(LdapServer ldapServer) {
        this.ldapServer = ldapServer;
    }

    public KdcServer getKdcServer() {
        return kdcServer;
    }

    public void setKdcServer(KdcServer kdcServer) {
        this.kdcServer = kdcServer;
    }

    public DirectoryService getDirectoryService() {
        return directoryService;
    }

    public void setDirectoryService(DirectoryService directoryService) {
        this.directoryService = directoryService;
    }

    public List<Partition> getPartitions() {
        return partitions;
    }

    public void setPartitions(List<Partition> partitions) {
        this.partitions = partitions;
    }

    private static void testAborted(RunNotifier notifier, Description description, Throwable cause) {
        notifier.fireTestStarted(description);
        notifier.fireTestFailure(new Failure(description, cause));
        notifier.fireTestFinished(description);
    }
}
