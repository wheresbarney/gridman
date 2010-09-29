package org.gridman.kerberos.junit;

import org.apache.directory.server.core.integ.Level;
import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.Suite;
import org.junit.runners.model.InitializationError;
import org.junit.runners.model.RunnerBuilder;

import static org.gridman.kerberos.junit.KdcServerContext.*;


/**
 * A replacement for standard JUnit 4 suites. Note that this test suite
 * will not startup an DirectoryService instance but will clean it up if
 * one remains.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 * @version $Rev$, $Date$
 */
public class KiSuite extends Suite {
    private InheritableKdcServerSettings settings = new InheritableKdcServerSettings(getDescription());


    public KiSuite(Class<?> clazz, RunnerBuilder builder) throws InitializationError {
        super(clazz, builder);
        settings = new InheritableKdcServerSettings(getDescription());
    }


    @Override
    public void run(final RunNotifier notifier) {
        super.run(notifier);

        /*
         * For any service scope other than test system scope, we must have to
         * shutdown the sevice and cleanup the working directory.  Failures to
         * do this without exception shows that something is wrong with the
         * server and so the entire test should be marked as failed.  So we
         * presume that tests have failed in the suite if the fixture is in an
         * inconsistent state.  Who knows if this inconsistent state of the
         * service could have made it so false results were acquired while
         * running tests.
         */

        if (settings.getCleanupLevel() != Level.SYSTEM) {
            try {
                shutdown(getSettings());
                cleanup(getSettings());
                destroy();
            }
            catch (Exception e) {
                notifier.fireTestFailure(new Failure(getDescription(), e));
            }
        }
    }


    public InheritableKdcServerSettings getSettings() {
        return settings;
    }
}