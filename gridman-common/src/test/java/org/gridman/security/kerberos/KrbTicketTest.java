package org.gridman.security.kerberos;

import com.sun.security.auth.module.Krb5LoginModule;
import org.apache.directory.server.core.integ.Level;
import org.apache.directory.server.core.integ.annotations.ApplyLdifFiles;
import org.apache.directory.server.core.integ.annotations.CleanupLevel;
import org.gridman.security.JaasHelper;
import org.gridman.testtools.kerberos.*;
import org.gridman.testtools.kerberos.apacheds.ApacheDsServerContext;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.security.auth.Subject;

import static org.junit.Assert.assertEquals;

/**
 * @author Jonathan Knight
 */
@RunWith(KiRunner.class)
@KdcType(type = ApacheDsServerContext.class)
@CleanupLevel(Level.CLASS)
@DirectoryPartition(suffixes = "dc=example,dc=com")
@ApplyLdifFiles( value = {"/gridman.ldif"})
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
     , moduleName = "GridMan"
     , loginModuleClass = Krb5LoginModule.class
     , settings = {
        "required",
        "debug=true",
        "doNotPrompt=false",
        "storeKey=true",
        "realm=\"EXAMPLE.COM\""
     }
)
public class KrbTicketTest {

    private Subject subjectKnightJ;
    private Subject subjectCacheServer;
    private String servicePrincipalName = "cacheserver@EXAMPLE.COM";
    private byte[] serviceTicket;

    @Before
    public void initialiseSubjects() throws Exception {
        subjectKnightJ = JaasHelper.logon("GridMan", "knightj@EXAMPLE.COM", "secret");
        subjectCacheServer = JaasHelper.logon("GridMan", "cacheserver@EXAMPLE.COM", "secret");
        serviceTicket = KrbHelper.getServiceTicket(subjectKnightJ, servicePrincipalName);
    }

    @Test
    public void testTicketHasCorrectName()  {
        KrbTicket ticket = KrbTicket.newInstance(serviceTicket, subjectCacheServer);
        assertEquals("knightj", ticket.getName());
    }

    @Test
    public void testTicketHasCorrectClientPrincipalName()  {
        KrbTicket ticket = KrbTicket.newInstance(serviceTicket, subjectCacheServer);
        assertEquals("knightj", ticket.getClientPrincipalName());
    }
}
