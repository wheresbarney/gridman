package org.gridman.kerberos;

import com.sun.security.auth.module.Krb5LoginModule;
import org.apache.directory.server.core.integ.Level;
import org.apache.directory.server.core.integ.annotations.ApplyLdifFiles;
import org.apache.directory.server.core.integ.annotations.CleanupLevel;
import org.gridman.kerberos.junit.JAAS;
import org.gridman.kerberos.junit.KdcType;
import org.gridman.kerberos.junit.KiRunner;
import org.gridman.kerberos.junit.Krb5;
import org.gridman.kerberos.junit.apacheds.ApacheDsServerContext;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.security.auth.Subject;
import javax.security.auth.login.LoginContext;
import java.security.Principal;
import java.security.PrivilegedAction;
import java.util.HashSet;

import static org.junit.Assert.*;

/**
 * @author Jonathan Knight
 */
@RunWith(KiRunner.class)
@KdcType(type = ApacheDsServerContext.class)
@CleanupLevel(Level.CLASS)
@ApplyLdifFiles({
        "/coherence/security/kerberos/coherence-security.ldif"
})
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
        "useKeyTab=true",
        "principal=\"knightj\"",
        "keyTab=\"jk-test.keytab\"",
        "debug=true",
        "storeKey=true",
        "realm=\"EXAMPLE.COM\"",
        "doNotPrompt=true",
        "useTicketCache=true",
        "renewTGT=true",
        "serviceprincipalname=\"cacheserver@EXAMPLE.COM\""
     }
)
public class KrbHelperTest {

    /**
     * Test we can log on to the Apache DS server.
     *
     * @throws Exception if there is a problem
     */
    @Test
    public void testLogon() throws Exception {
        LoginContext lc = new LoginContext("Coherence");
        lc.login();
        Subject subject = lc.getSubject();

        assertNotNull(subject);
    }

    @Test
    @SuppressWarnings({"ThrowableInstanceNeverThrown", "ThrowableResultOfMethodCallIgnored"})
    public void shouldConvertExceptionToSecurityExceptionWithNoMessage() {
        Exception cause = new Exception("testing...");

        SecurityException result = KrbHelper.ensureSecurityException(cause);
        assertSame(result.getCause(), cause);
        assertNull(result.getMessage());
    }
    
    @Test
    @SuppressWarnings({"ThrowableInstanceNeverThrown", "ThrowableResultOfMethodCallIgnored"})
    public void shouldConvertExceptionToSecurityExceptionWithMessage() {
        String message = "An error message";
        Exception cause = new Exception("testing...");

        SecurityException result = KrbHelper.ensureSecurityException(cause, message);
        assertSame(result.getCause(), cause);
        assertEquals(result.getMessage(), message);
    }

    @Test
    @SuppressWarnings({"ThrowableInstanceNeverThrown", "ThrowableResultOfMethodCallIgnored"})
    public void shouldNotConvertSecurityExceptionWithNoMessage() {
        Exception cause = new SecurityException("testing...");

        SecurityException result = KrbHelper.ensureSecurityException(cause);
        assertSame(result, cause);
    }

    @Test
    @SuppressWarnings({"ThrowableInstanceNeverThrown", "ThrowableResultOfMethodCallIgnored"})
    public void shouldConvertSecurityExceptionToSecurityExceptionWithMessage() {
        String message = "An error message";
        Exception cause = new SecurityException("testing...");

        SecurityException result = KrbHelper.ensureSecurityException(cause, message);
        assertSame(result.getCause(), cause);
        assertEquals(result.getMessage(), message);
    }

    @Test
    public void shouldReturnNullCurrentSubjectIfNoSubjectSet() {
        Subject result = KrbHelper.getCurrentSubject();
        assertNull(result);
    }

    @Test
    public void shouldReturnCorrectCurrentSubject() {
        Subject subject = new Subject(true, new HashSet<Principal>(), new HashSet(), new HashSet());

        Subject result = Subject.doAs(subject, new PrivilegedAction<Subject>() {
            @Override
            public Subject run() {
                return KrbHelper.getCurrentSubject();
            }
        });

        assertSame(result, subject);
    }
}
