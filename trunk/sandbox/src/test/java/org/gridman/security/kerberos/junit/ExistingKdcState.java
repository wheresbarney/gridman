package org.gridman.security.kerberos.junit;

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
public class ExistingKdcState extends AbstractKerberosState {
    
    private static final Logger LOG = LoggerFactory.getLogger(ExistingKdcState.class);

    /**
     * Creates a new instance of AbstractState.
     *
     * @param context The associated context
     */
    protected ExistingKdcState(KdcServerContext context) {
        super(context);
    }

    @Override
    public void shutdown(InheritableKdcServerSettings settings) throws Exception {
    }

    @Override
    public void destroy() {
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

        ExistingKdcContext context = (ExistingKdcContext) getContext();

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

        KdcServerContext.invokeTest(testClass, statement, notifier, settings.getDescription());
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
                .append('\n').append("        default_tkt_enctypes = des-cbc-md5 rc4-hmac")
                .append('\n').append("        default_tgs_enctypes = des-cbc-md5 rc4-hmac")
                .append('\n').append("        dns_lookup_kdc = flase")
                .append('\n').append("        dns_lookup_realm = false")
                .append('\n').append("[realms]")
                .append('\n').append("        " + realm + " = {")
                .append('\n').append("                kdc = " + kdcAddress)
                .append('\n').append("                master_kdc = " + kdcAddress)
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
}
