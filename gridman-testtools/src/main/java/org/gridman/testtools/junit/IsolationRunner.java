package org.gridman.testtools.junit;

import org.gridman.testtools.classloader.IsolatedActionException;
import org.gridman.testtools.classloader.IsolatedExceptionAction;
import org.gridman.testtools.classloader.PropertyIsolation;
import org.gridman.testtools.classloader.SystemPropertyLoader;
import org.gridman.testtools.kerberos.RunIsolated;
import org.junit.runners.BlockJUnit4ClassRunner;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.InitializationError;
import org.junit.runners.model.Statement;

import java.util.Properties;

/**
 * @author Jonathan Knight
 */
public class IsolationRunner extends BlockJUnit4ClassRunner {

    public IsolationRunner(Class<?> klass) throws InitializationError {
        super(klass);
    }


    @Override
    protected Statement methodInvoker(FrameworkMethod method, Object test) {
        Statement statement = super.methodInvoker(method, test);

        RunIsolated isolated = method.getAnnotation(RunIsolated.class);
        if (isolated != null) {
            Properties localProperties = SystemPropertyLoader.loadProperties(isolated.properties());
            statement = new IsolatedStatement(statement, localProperties);
        }

        return statement;
    }

    private static class IsolatedStatement extends Statement {
        private Statement statement;
        private Properties localProperties;

        private IsolatedStatement(Statement statement, Properties localProperties) {
            this.statement = statement;
            this.localProperties = localProperties;
        }

        @Override
        public void evaluate() throws Throwable {
            try {
                PropertyIsolation.runIsolated(localProperties, new IsolatedExceptionAction<Object>() {
                    @Override
                    public Object run() throws Throwable {
                        statement.evaluate();
                        return null;
                    }
                });
            } catch (IsolatedActionException e) {
                throw e.getCause();
            }
        }
    }

}
