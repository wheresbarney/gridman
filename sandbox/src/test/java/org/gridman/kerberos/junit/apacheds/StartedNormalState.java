package org.gridman.kerberos.junit.apacheds;

import org.apache.directory.server.core.DirectoryService;
import org.apache.directory.server.core.partition.Partition;
import org.gridman.kerberos.junit.InheritableKdcServerSettings;
import org.gridman.kerberos.junit.KdcServerContext;
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
 * The state of a running test service which has been used for running
 * integration tests and has been reverted to contain the same content as it
 * did when created and started.  It is not really pristine however for all
 * practical purposes of integration testing it appears to be the same as
 * when first started.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 * @version $Rev$, $Date$
 */
public class StartedNormalState extends AbstractApacheDsState {
    private static final Logger LOG = LoggerFactory.getLogger(StartedNormalState.class);


    /**
     * Creates a new instance of StartedNormalState.
     *
     * @param context the test's context
     */
    public StartedNormalState(KdcServerContext context) {
        super(context);
    }


    /**
     * Action where an attempt is made to destroy the service. This
     * entails nulling out reference to it and triggering garbage
     * collection.
     */
    public void destroy() {
        LOG.debug("calling destroy()");
        ApacheDsServerContext context = (ApacheDsServerContext) getContext();
        File dir = context.getLdapServer().getDirectoryService().getWorkingDirectory();
        context.setDirectoryService(null);
        context.getLdapServer().setDirectoryService(null);
        context.setLdapServer(null);
        context.getKdcServer().setDirectoryService(null);
        context.setKdcServer(null);
        context.setState(((ApacheDsServerContext)context).getNonExistentState());
        System.gc();
        try {
            doDelete(dir);
        } catch (IOException e) {
            System.err.println("Could not delete working directory " + e.getMessage());
        }
    }


    /**
     * Action where an attempt is made to shutdown the service.
     *
     * @throws Exception on failures to stop the core directory service
     */
    public void shutdown(InheritableKdcServerSettings settings) throws Exception {
        LOG.debug("calling shutdown()");
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
     * Action where an attempt is made to revert the service to it's
     * initial start up state by using a previous snapshot.
     *
     * @throws Exception on failures to revert the state of the core
     *                   directory service
     */
    public void revert() throws Exception {
        LOG.debug("calling revert()");
        ApacheDsServerContext context = (ApacheDsServerContext) getContext();
        context.getDirectoryService().revert();
    }


    /**
     * Action where an attempt is made to run a test against the service.
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
            case ROLLBACK:
                try {
                    DirectoryService directoryService = context.getDirectoryService();
                    directoryService.getChangeLog().tag();

                    // Inject the LDIFs, if any
                    injectLdifs(directoryService, settings);
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
                    revert();
                }
                catch (Exception e) {
                    // @TODO - we might want to check the revision of the service before
                    // we presume that it has been soiled.  Some tests may simply perform
                    // some read operations or checks on the service and may not alter it
                    testAborted(notifier, settings.getDescription(), e);
                    return;
                }

                return;

            case RESTART:
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
                    startup(settings);
                }
                catch (Exception e) {
                    LOG.error("Failed to create and start new server instance: " + e);
                    testAborted(notifier, settings.getDescription(), e);
                    return;
                }

                return;

            default:
                return;
        }
    }
}