package org.gridman.security.kerberos.junit;

import org.apache.directory.server.core.integ.Level;
import org.junit.Ignore;
import org.junit.runner.Description;
import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.BlockJUnit4ClassRunner;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.InitializationError;
import org.junit.runners.model.Statement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.gridman.security.kerberos.junit.KdcServerContext.*;

/**
 * @author Jonathan Knight
 */
public class KiRunner extends BlockJUnit4ClassRunner {

    private static final Logger LOG = LoggerFactory.getLogger(KiRunner.class);
    private KiSuite suite;
    private InheritableKdcServerSettings settings;


    public KiRunner(Class<?> clazz) throws InitializationError {
        super(clazz);
    }


    protected InheritableKdcServerSettings getSettings() {
        if (settings != null) {
            return settings;
        }

        if (suite == null) {
            settings = new InheritableKdcServerSettings(getDescription(), null);
        }

        return settings;
    }


    @Override
    public void run(final RunNotifier notifier) {
        super.run(notifier);
        Level cleanupLevel = getSettings().getCleanupLevel();

        if (cleanupLevel == Level.CLASS) {
            try {
                shutdown(getSettings());
                cleanup(getSettings());
                destroy();
            }
            catch (Exception e) {
                LOG.error("Encountered exception while trying to cleanup after test class: "
                        + this.getDescription().getDisplayName(), e);
                notifier.fireTestFailure(new Failure(getDescription(), e));
            }
        }
    }


    @Override
    protected void runChild(FrameworkMethod method, RunNotifier notifier) {
        LOG.debug("About to invoke test method {}", method.getName());

        Description description = describeChild(method);
        if (method.getAnnotation(Ignore.class) != null) {
            notifier.fireTestIgnored(description);
            return;
        }

        Statement statement = methodBlock(method);
        InheritableKdcServerSettings testSettings = new InheritableKdcServerSettings(description, getSettings());
        set(testSettings.getKdcContextClass());
        test(getTestClass(), statement, notifier, testSettings);

        Level cleanupLevel = getSettings().getCleanupLevel();

        if (cleanupLevel == Level.METHOD) {
            try {
                shutdown(getSettings());
                cleanup(getSettings());
                destroy();
            }
            catch (Exception e) {
                LOG.error("Encountered exception while trying to cleanup after test class: "
                        + this.getDescription().getDisplayName(), e);
                notifier.fireTestFailure(new Failure(getDescription(), e));
            }
        }
    }


    public void setSuite(KiSuite suite) {
        this.suite = suite;
        this.settings = new InheritableKdcServerSettings(getDescription(), suite.getSettings());
    }


    public KiSuite getSuite() {
        return suite;
    }
}
