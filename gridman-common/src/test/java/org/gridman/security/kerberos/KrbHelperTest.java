package org.gridman.security.kerberos;

import com.sun.security.auth.module.Krb5LoginModule;
import org.apache.directory.server.core.integ.Level;
import org.apache.directory.server.core.integ.annotations.ApplyLdifFiles;
import org.apache.directory.server.core.integ.annotations.CleanupLevel;
import org.gridman.security.JaasHelper;
import org.gridman.testtools.kerberos.*;
import org.gridman.testtools.kerberos.apacheds.ApacheDsServerContext;
import org.ietf.jgss.GSSContext;
import org.ietf.jgss.GSSCredential;
import org.ietf.jgss.GSSException;
import org.ietf.jgss.GSSManager;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.security.auth.Subject;
import java.security.PrivilegedExceptionAction;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

/**
 * @author Jonathan Knight
 */
@RunWith(KiRunner.class)

@KdcType(type = ApacheDsServerContext.class)
@CleanupLevel(Level.CLASS)
@DirectoryPartition(suffixes = "dc=example,dc=com")
@ApplyLdifFiles(value = {"/gridman.ldif"})
@Krb5( credentials = {
        "knightj@EXAMPLE.COM,secret",
        "awilson@EXAMPLE.COM,secret",
        "cacheserver@EXAMPLE.COM,secret",
        "thomas@EXAMPLE.COM,secret"
       }
     , keytabFile = "jk-test.keytab"
     , krb5Conf = "jk-krb5.conf"
     , kdcPort = 60088
     , realm = "EXAMPLE.COM"
)
@JAAS( fileName = "jk-test-jaas.conf"
     , moduleName = "Coherence"
     , loginModuleClass = Krb5LoginModule.class
     , settings = {
        "required",
        "debug=true",
        "doNotPrompt=false",
        "storeKey=true",
        "realm=\"EXAMPLE.COM\""
     }
)
public class KrbHelperTest {

    private String servicePrincipalName = "cacheserver@EXAMPLE.COM";
    private Subject subjectKnightJ;
    private Subject subjectCacheServer;

    @Before
    public void initialiseSubjects() throws Exception {
        subjectKnightJ = JaasHelper.logon("Coherence", "knightj@EXAMPLE.COM", "secret");
        subjectCacheServer = JaasHelper.logon("Coherence", "cacheserver@EXAMPLE.COM", "secret");
    }

    @Test
    public void shouldObtainServiceTicket() throws Exception {
        final byte[] ticket = KrbHelper.getServiceTicket(subjectKnightJ, servicePrincipalName);

        String userName = Subject.doAs(subjectCacheServer, new PrivilegedExceptionAction<String>() {
            @Override
            public String run() throws Exception {
                GSSManager manager = GSSManager.getInstance();
                GSSContext context = manager.createContext((GSSCredential) null);
                context.acceptSecContext(ticket, 0, ticket.length);
                return context.getSrcName().toString();
            }
        });

        assertEquals("knightj@EXAMPLE.COM", userName);
    }

    @Test
    public void shouldValidateServiceTicket() throws Exception {
        byte[] serviceTicket = KrbHelper.getServiceTicket(subjectKnightJ, servicePrincipalName);
        KrbTicket krbTicket = KrbHelper.validate(serviceTicket, false, subjectCacheServer);

        assertEquals("knightj", krbTicket.getName());
    }

    @Test
    public void shouldDetectReplayWhenValidatingDuplicateTicket() throws Exception {
        byte[] serviceTicket = KrbHelper.getServiceTicket(subjectKnightJ, servicePrincipalName);
        KrbHelper.validate(serviceTicket, false, subjectCacheServer);

        Throwable exception = null;

        try {
            KrbHelper.validate(serviceTicket, false, subjectCacheServer);
        } catch (SecurityException e) {
            exception = e;
        }
        
        if (exception != null && exception.getCause() instanceof GSSException) {
            GSSException gssException = (GSSException) exception.getCause();
            assertEquals("Request is a replay (34)", gssException.getMinorString());
        } else {
            fail("Expected SecurityException wrapping GSSException Replay Error");
        }

    }

    @Test
    public void shouldIgnoreReplayWhenValidatingDuplicateTicket() throws Exception {
        byte[] serviceTicket = KrbHelper.getServiceTicket(subjectKnightJ, servicePrincipalName);
        KrbHelper.validate(serviceTicket, false, subjectCacheServer);
        KrbHelper.validate(serviceTicket, true, subjectCacheServer);
    }
}
