package org.gridman.security;

import com.sun.security.auth.module.Krb5LoginModule;
import org.apache.directory.server.core.integ.Level;
import org.apache.directory.server.core.integ.annotations.ApplyLdifFiles;
import org.apache.directory.server.core.integ.annotations.CleanupLevel;
import org.gridman.testtools.kerberos.*;
import org.gridman.testtools.kerberos.apacheds.ApacheDsServerContext;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.security.auth.Subject;
import java.security.PrivilegedAction;
import java.security.PrivilegedExceptionAction;

import static org.gridman.security.JaasHelper.asSubject;
import static org.junit.Assert.*;

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
public class JaasHelperTest {

    @Test
    @SuppressWarnings({"ThrowableInstanceNeverThrown", "ThrowableResultOfMethodCallIgnored"})
    public void shouldConvertExceptionToSecurityExceptionWithNoMessage() {
        Exception cause = new Exception("testing...");

        SecurityException result = JaasHelper.ensureSecurityException(cause);
        assertSame(result.getCause(), cause);
        assertNull(result.getMessage());
    }

    @Test
    @SuppressWarnings({"ThrowableInstanceNeverThrown", "ThrowableResultOfMethodCallIgnored"})
    public void shouldConvertExceptionToSecurityExceptionWithMessage() {
        String message = "An error message";
        Exception cause = new Exception("testing...");

        SecurityException result = JaasHelper.ensureSecurityException(cause, message);
        assertSame(result.getCause(), cause);
        assertEquals(result.getMessage(), message);
    }

    @Test
    @SuppressWarnings({"ThrowableInstanceNeverThrown", "ThrowableResultOfMethodCallIgnored"})
    public void shouldNotConvertSecurityExceptionWithNoMessage() {
        Exception cause = new SecurityException("testing...");

        SecurityException result = JaasHelper.ensureSecurityException(cause);
        assertSame(result, cause);
    }

    @Test
    @SuppressWarnings({"ThrowableInstanceNeverThrown", "ThrowableResultOfMethodCallIgnored"})
    public void shouldConvertSecurityExceptionToSecurityExceptionWithMessage() {
        String message = "An error message";
        Exception cause = new SecurityException("testing...");

        SecurityException result = JaasHelper.ensureSecurityException(cause, message);
        assertSame(result.getCause(), cause);
        assertEquals(result.getMessage(), message);
    }

    @Test
    public void shouldReturnNullCurrentSubjectIfNoSubjectSet() {
        Subject result = JaasHelper.getCurrentSubject();
        assertNull(result);
    }

    @Test
    public void shouldReturnCorrectCurrentSubject() {
        Subject subject = asSubject(new PrincipalStub("knightj"));

        Subject result = Subject.doAs(subject, new PrivilegedAction<Subject>() {
            @Override
            public Subject run() {
                return JaasHelper.getCurrentSubject();
            }
        });

        assertSame(result, subject);
    }

    @Test
    public void shouldDoAsSpecifiedSubject() {
        Subject subject = asSubject(new PrincipalStub("knightj"));

        Subject result = JaasHelper.doAs(subject, new PrivilegedExceptionAction<Subject>() {
            @Override
            public Subject run() {
                return JaasHelper.getCurrentSubject();
            }
        });

        assertSame(result, subject);
    }

    @Test
    public void shouldDoAsEnclosingSubject() {
        Subject subject = asSubject(new PrincipalStub("knightj"));

        Subject result = Subject.doAs(subject, new PrivilegedAction<Subject>() {
            @Override
            public Subject run() {
                return JaasHelper.doAs(new PrivilegedExceptionAction<Subject>() {
                    @Override
                    public Subject run() {
                        return JaasHelper.getCurrentSubject();
                    }
                });
            }
        });

        assertSame(result, subject);
    }

}
