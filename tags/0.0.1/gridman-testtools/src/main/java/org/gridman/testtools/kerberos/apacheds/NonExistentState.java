package org.gridman.testtools.kerberos.apacheds;

import org.apache.directory.server.core.DirectoryService;
import org.gridman.testtools.kerberos.InheritableKdcServerSettings;
import org.gridman.testtools.kerberos.KdcServerContext;
import org.gridman.testtools.kerberos.ServerFactory;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.model.Statement;
import org.junit.runners.model.TestClass;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.naming.NamingException;
import java.io.IOException;


/**
 * The state of a test service when it has not yet been created.
 *
 */
public class NonExistentState extends AbstractApacheDsState {
    private static final Logger LOG = LoggerFactory.getLogger(NonExistentState.class);

    /**
     * Creates a new instance of NonExistentState.
     *
     * @param context the test context
     */
    public NonExistentState(KdcServerContext context) {
        super(context);
    }


    /**
     * IsolatedAction where an attempt is made to create the service.  Service
     * creation in this system is the combined instantiation and
     * configuration which takes place when the factory is used to get
     * a new instance of the service.
     *
     * @param settings The inherited settings
     * @throws javax.naming.NamingException if we can't create the service
     */
    public void create(InheritableKdcServerSettings settings) throws NamingException {
        LOG.debug("calling create()");

        try {
            ServerFactory factory = settings.getFactory();
            DirectoryService directoryService = factory.newDirectoryService();
            ApacheDsServerContext context = (ApacheDsServerContext) getContext();
            context.setDirectoryService(directoryService);
            context.setLdapServer(factory.newLdapServer(directoryService));
            context.setKdcServer(factory.newKdcServer(directoryService, settings.getKdcPort()));
        }
        catch (InstantiationException ie) {
            throw new NamingException(ie.getMessage());
        }
        catch (IllegalAccessException iae) {
            throw new NamingException(iae.getMessage());
        }
        catch (Exception e) {
            throw new NamingException(e.getMessage());
        }
    }

    /**
     * This method is a bit different.  Consider this method to hold the logic
     * which is needed to shift the context state from the present state to a
     * started state so we can call test on the current state of the context.
     * <p/>
     * Basically if the service is not needed or the test is ignored, then we
     * just invoke the test: if ignored the test is not dealt with by the
     * MethodRoadie run method.
     * <p/>
     * In tests not ignored requiring setup modes RESTART and CUMULATIVE we
     * simply create the service and start it up without a cleanup.  In the
     * PRISTINE and ROLLBACK modes we do the same but cleanup() before a
     * restart.
     */
    public void test(TestClass testClass, Statement statement, RunNotifier notifier, InheritableKdcServerSettings settings) {
        LOG.debug("calling test(): {}, mode {}", settings.getDescription().getDisplayName(), settings.getMode());

        ApacheDsServerContext context = (ApacheDsServerContext) getContext();

        switch (settings.getMode()) {
            case CUMULATIVE:
            case RESTART:
                try {
                    create(settings);
                }
                catch (NamingException ne) {
                    LOG.error("Failed to create and start new server instance: " + ne);
                    testAborted(notifier, settings.getDescription(), ne);
                    return;
                }

                try {
                    startup(settings);
                }
                catch (Exception e) {
                    LOG.error("Failed to create and start new server instance: " + e);
                    testAborted(notifier, settings.getDescription(), e);
                    return;
                }

                context.setState(context.getStartedNormalState());
                context.getState().test(testClass, statement, notifier, settings);
                return;


            case PRISTINE:
            case ROLLBACK:
                try {
                    create(settings);
                }
                catch (NamingException ne) {
                    LOG.error("Failed to create and start new server instance: " + ne);
                    testAborted(notifier, settings.getDescription(), ne);
                    return;
                }

                try {
                    cleanup(settings);
                }
                catch (IOException ioe) {
                    LOG.error("Failed to create and start new server instance: " + ioe);
                    testAborted(notifier, settings.getDescription(), ioe);
                    return;
                }

                try {
                    startup(settings);
                }
                catch (Exception e) {
                    LOG.error("Failed to create and start new server instance: " + e);
                    testAborted(notifier, settings.getDescription(), e);
                    return;
                }

                context.setState(context.getStartedPristineState());
                context.getState().test(testClass, statement, notifier, settings);
                return;

            default:
                return;
        }
    }
}