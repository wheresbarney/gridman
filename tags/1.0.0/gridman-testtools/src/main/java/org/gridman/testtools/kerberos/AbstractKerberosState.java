package org.gridman.testtools.kerberos;

import org.junit.runner.Description;
import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.model.Statement;
import org.junit.runners.model.TestClass;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.naming.NamingException;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Map;

/**
 * @author Jonathan Knight
 */
public abstract class AbstractKerberosState implements KdcServerState {
    /**
     * The class logger
     */
    private static final Logger LOG = LoggerFactory.getLogger(AbstractKerberosState.class);

    /**
     * Error message when we can't destroy the service
     */
    public static final String DESTROY_ERR = "Cannot destroy when service is in NonExistant state";
    public static final String CLEANUP_ERROR = "Cannot cleanup when service is in NonExistant state";
    public static final String STARTUP_ERR = "Cannot startup when service is in NonExistant state";
    public static final String SHUTDOWN_ERR = "Cannot shutdown service in NonExistant state.";
    public static final String REVERT_ERROR = "Cannot revert when service is in NonExistant state";

    /**
     * The context for this test
     */
    private KdcServerContext context;

    public AbstractKerberosState(KdcServerContext context) {
        this.context = context;
    }

    public KdcServerContext getContext() {
        return context;
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
    }

    /**
     * IsolatedAction where an attempt is made to start up the service.
     *
     * @throws Exception on failures to start the core directory service
     */
    public void startup(InheritableKdcServerSettings settings) throws Exception {
        LOG.debug("calling start()");
        writeKrb5Configuration(settings);
        writeJaasConfiguration(settings);
        createKeytabs(settings);
    }

    /**
     * IsolatedAction where an attempt is made to erase the contents of the
     * working directory used by the service for various files including
     * partition database files.
     *
     * @throws java.io.IOException on errors while deleting the working directory
     */
    public void cleanup(InheritableKdcServerSettings settings) throws IOException {
        LOG.debug("calling cleanup()");

        System.gc();

        String filename = settings.getKeyTabFile();
        File file = new File(filename);
        if(file.exists()) {
            LOG.debug("Deleting keytab " + filename);
            file.delete();
        }

        Map<Krb5.FIELDS,Object> krb5Settings = settings.getKrb5Settings();
        file = new File((String)krb5Settings.get(Krb5.FIELDS.KRB5CONF));
        if(file.exists()) {
            LOG.debug("Deleting krb5 configuration " + filename);
            file.delete();
        }

        Map<JAAS.FIELDS,Object> jaasSettings = settings.getJaasSettings();
        file = new File((String)jaasSettings.get(JAAS.FIELDS.FILENAME));
        if(file.exists()) {
            LOG.debug("Deleting JAAS configuration " + filename);
            file.delete();
        }
    }
    
    /**
     * IsolatedAction where an attempt is made to destroy the service. This
     * entails nulling out reference to it and triggering garbage
     * collection.
     */
    public void destroy() {
        LOG.error(DESTROY_ERR);
        throw new IllegalStateException(DESTROY_ERR);
    }

    /**
     * IsolatedAction where an attempt is made to shutdown the service.
     *
     * @throws Exception on failures to stop the core directory service
     */
    public void shutdown(InheritableKdcServerSettings settings) throws Exception {
        LOG.error(SHUTDOWN_ERR);
        throw new IllegalStateException(SHUTDOWN_ERR);
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
    }


    /**
     * IsolatedAction where an attempt is made to revert the service to it's
     * initial start up state by using a previous snapshot.
     *
     * @throws Exception on failures to revert the state of the core
     *                   directory service
     */
    public void revert() throws Exception {
        LOG.error(REVERT_ERROR);
        throw new IllegalStateException(REVERT_ERROR);
    }

    public void writeKrb5Configuration(InheritableKdcServerSettings settings) throws Exception {
        Map<Krb5.FIELDS,Object> krb5Settings = settings.getKrb5Settings();
        writeKrb5Configuration(krb5Settings);
    }

    public void writeKrb5Configuration(Map<Krb5.FIELDS,Object> krb5Settings) throws Exception {
        if(krb5Settings.size() > 0) {
            String realm = (String) krb5Settings.get(Krb5.FIELDS.REALM);
            String realmLower = realm.toLowerCase();

            String host = (String)krb5Settings.get(Krb5.FIELDS.KDCHOST);
            int port = (Integer)krb5Settings.get(Krb5.FIELDS.KDCPORT);
            String kdcAddress;
            if (port > 0) {
                kdcAddress = host + ":" + port;
            } else {
                kdcAddress = host;
            }

            StringBuilder data = new StringBuilder();
            data.append("[libdefaults]")
                .append('\n').append("        default_realm = ").append(realm)
                .append('\n').append("[realms]")
                .append('\n').append("        " + realm + " = {")
                .append('\n').append("                kdc = " + kdcAddress)
                .append('\n').append("                default_domain = " + realmLower)
                .append('\n').append("        }")
                .append('\n').append("[domain_realm]")
                .append('\n').append("        ." + realmLower + " = " + realm)
                .append('\n').append("        " + realmLower + " = " + realm)
                .append('\n').append("[login]")
                .append('\n').append("        krb4_convert = true")
                .append('\n').append("        krb4_get_tickets = false");

            File krb5Conf = new File((String)krb5Settings.get(Krb5.FIELDS.KRB5CONF));
            LOG.info("creating krb5 configuration " + krb5Conf + "\n" + data);

            PrintWriter out = new PrintWriter(krb5Conf);
            out.print(data);
            out.flush();
            out.close();
//            System.setProperty("java.security.krb5.kdc", "localhost:" + port);
//            System.setProperty("java.security.krb5.realm", (String)krb5Settings.get(Krb5.FIELDS.REALM));
            System.setProperty("java.security.krb5.conf", krb5Conf.getCanonicalPath());
        }
    }

    public void writeJaasConfiguration(InheritableKdcServerSettings settings) throws Exception {
        Map<JAAS.FIELDS,Object> jaasSettings = settings.getJaasSettings();
        writeJaasConfiguration(jaasSettings);
    }

    public void writeJaasConfiguration(Map<JAAS.FIELDS,Object> jaasSettings) throws Exception {
        if (jaasSettings.size() > 0) {
            StringBuilder data = new StringBuilder();
            data.append((String)jaasSettings.get(JAAS.FIELDS.MODULENAME))
                .append("\n{\n    ")
                .append(((Class)jaasSettings.get(JAAS.FIELDS.MODULECLASS)).getCanonicalName());

            String[] settings = (String[]) jaasSettings.get(JAAS.FIELDS.SETTINGS);
            for (String setting : settings) {
                data.append("\n    ")
                    .append(setting);
            }
            data.append(";\n};");

            File jaasConf = new File((String)jaasSettings.get(JAAS.FIELDS.FILENAME));
            LOG.info("creating JAAS configuration " + jaasConf + "\n" + data);

            PrintWriter out = new PrintWriter(jaasConf);
            out.print(data);
            out.flush();
            out.close();

            System.setProperty("java.security.auth.login.config", jaasConf.toURI().toURL().toString());
        }
    }

    public void createKeytabs(InheritableKdcServerSettings settings) {
        String[][] ktabArgs = settings.getKtabArgs();
        if (ktabArgs != null) {
            for(String[] args : ktabArgs) {
                Ktab.main(args);
            }
        }
    }

    protected void testAborted(RunNotifier notifier, Description description, Throwable cause) {
        notifier.fireTestStarted(description);
        notifier.fireTestFailure(new Failure(description, cause));
        notifier.fireTestFinished(description);
    }    

}
