package org.gridman.security.kerberos.junit.apacheds;


import org.apache.directory.server.core.DirectoryService;
import org.apache.directory.server.core.partition.Partition;
import org.gridman.security.kerberos.junit.InheritableKdcServerSettings;
import org.gridman.security.kerberos.junit.KdcServerContext;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.model.Statement;
import org.junit.runners.model.TestClass;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.List;

import static org.apache.directory.server.core.integ.IntegrationUtils.doDelete;


/**
 * A test service state where the server is running and has not been used for
 * any integration test since it was created.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 * @version $Rev$, $Date$
 */
public class StartedPristineState extends AbstractApacheDsState {
    private static final Logger LOG = LoggerFactory.getLogger(StartedPristineState.class);


    /**
     * Creates a new instance of StartedPristineState.
     *
     * @param context the test's context
     */
    public StartedPristineState(KdcServerContext context) {
        super(context);
    }


    /**
     * IsolatedAction where an attempt is made to stop the server.
     *
     * @throws Exception on failures to stop the ldap server
     */
    public void shutdown(InheritableKdcServerSettings settings) throws Exception {
        LOG.debug("calling stop()");
        ApacheDsServerContext context = (ApacheDsServerContext) getContext();
        context.getKdcServer().stop();
        context.getLdapServer().stop();
        DirectoryService service = context.getDirectoryService();
        List<Partition> partitions = context.getPartitions();
        for (Partition partition : partitions) {
            service.removePartition(partition);
        }
        service.shutdown();
    }


    /**
     * IsolatedAction where an attempt is made to destroy the service. This
     * entails nulling out reference to it and triggering garbage
     * collection.
     */
    public void destroy() {
        ApacheDsServerContext context = (ApacheDsServerContext) getContext();
        File dir = context.getLdapServer().getDirectoryService().getWorkingDirectory();
        LOG.debug("calling destroy()");
        context.getKdcServer().setDirectoryService(null);
        context.setKdcServer(null);
        context.getLdapServer().setDirectoryService(null);
        context.setLdapServer(null);
        context.setDirectoryService(null);
        context.setState(((ApacheDsServerContext)context).getNonExistentState());
        System.gc();
        try {
            doDelete(dir);
        } catch (IOException e) {
            System.err.println("Could not delete working directory " + e.getMessage());
        }
    }


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
    public void test(TestClass testClass, Statement statement, RunNotifier notifier, InheritableKdcServerSettings settings) {
        LOG.debug("calling test(): {}, mode {}", settings.getDescription().getDisplayName(), settings.getMode());

        ApacheDsServerContext context = (ApacheDsServerContext) getContext();
        switch (settings.getMode()) {
            case PRISTINE:
                // Inject the LDIFs, if any
                try {
                    injectLdifs(context.getDirectoryService(), settings);
                }
                catch (Exception e) {
                    // @TODO - we might want to check the revision of the service before
                    // we presume that it has been soiled.  Some tests may simply perform
                    // some read operations or checks on the service and may not alter it
                    testAborted(notifier, settings.getDescription(), e);
                    return;
                }

                KdcServerContext.invokeTest(testClass, statement, notifier, settings.getDescription());

                try {
                    shutdown(settings);
                }
                catch (Exception e) {
                    // @TODO - we might want to check the revision of the service before
                    // we presume that it has been soiled.  Some tests may simply perform
                    // some read operations or checks on the service and may not alter it
                    testAborted(notifier, settings.getDescription(), e);
                    return;
                }

                try {
                    cleanup(settings);
                }
                catch (IOException ioe) {
                    LOG.error("Failed to cleanup new server instance: " + ioe);
                    testAborted(notifier, settings.getDescription(), ioe);
                    return;
                }

                destroy();
                context.setState(((ApacheDsServerContext)context).getNonExistentState());
                return;

            case ROLLBACK:
                try {
                    context.getLdapServer().getDirectoryService().getChangeLog().tag();

                    // Inject the LDIFs, if any
                    injectLdifs(context.getLdapServer().getDirectoryService(), settings);
                }
                catch (Exception e) {
                    // @TODO - we might want to check the revision of the service before
                    // we presume that it has been soiled.  Some tests may simply perform
                    // some read operations or checks on the service and may not alter it
                    testAborted(notifier, settings.getDescription(), e);
                    return;
                }

                KdcServerContext.invokeTest(testClass, statement, notifier, settings.getDescription());
                context.setState(((ApacheDsServerContext)context).getStartedNormalState());

                try {
                    context.getState().revert();
                }
                catch (Exception e) {
                    // @TODO - we might want to check the revision of the service before
                    // we presume that it has been soiled.  Some tests may simply perform
                    // some read operations or checks on the service and may not alter it
                    testAborted(notifier, settings.getDescription(), e);
                    return;
                }
                return;

            default:
                return;
        }
    }
}