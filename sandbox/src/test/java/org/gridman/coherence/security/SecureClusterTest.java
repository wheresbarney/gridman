package org.gridman.coherence.security;

import com.sun.security.auth.module.Krb5LoginModule;
import com.tangosol.io.pof.PortableException;
import com.tangosol.net.CacheFactory;
import com.tangosol.net.InvocationObserver;
import com.tangosol.net.InvocationService;
import com.tangosol.net.Member;
import com.tangosol.net.messaging.ConnectionException;
import org.apache.directory.server.core.integ.Level;
import org.apache.directory.server.core.integ.annotations.ApplyLdifFiles;
import org.apache.directory.server.core.integ.annotations.CleanupLevel;
import org.gridman.classloader.ClusterStarter;
import org.gridman.coherence.util.NullInvokable;
import org.gridman.security.GridManCallbackHandler;
import org.gridman.security.JaasHelper;
import org.gridman.security.kerberos.junit.*;
import org.gridman.security.kerberos.junit.apacheds.ApacheDsServerContext;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.security.auth.Subject;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

/**
 * @author Jonathan Knight
 */
@RunWith(KiRunner.class)
@KdcType(type = ApacheDsServerContext.class)
@CleanupLevel(Level.CLASS)
@DirectoryPartition(suffixes = "dc=example,dc=com")
@ApplyLdifFiles( value = {
        "/coherence/security/kerberos/coherence-security.ldif"
})
@Krb5( credentials = {
        "knightj@EXAMPLE.COM,secret",
        "thomas@EXAMPLE.COM,secret",
        "fatcontroller@EXAMPLE.COM,secret",
        "cacheserver@EXAMPLE.COM,secret"
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
public class SecureClusterTest {

    private final ClusterStarter clusterStarter = ClusterStarter.getInstance();
    private String clusterFile;

    public static Subject subjectNULL;
    public static Subject subjectKnightj;
    public static Subject subjectThomas;

    @Before
    public void startSecureCluster() throws Exception {
        clusterFile = "/coherence/security/kerberos/secure-cluster.properties";
        clusterStarter
                .setProperty(JaasHelper.PROP_JAAS_MODULE, "Coherence")
                .setProperty(GridManCallbackHandler.PROP_USERNAME, "cacheserver")
                .setProperty(GridManCallbackHandler.PROP_PASSWORD, "secret")
                .ensureCluster(clusterFile);

        subjectNULL = null;
        subjectKnightj = JaasHelper.logon("Coherence", "knightj", "secret");
        subjectThomas = JaasHelper.logon("Coherence", "thomas", "secret");
    }

    @After
    public void stopSecureCluster() {
        clusterStarter.shutdown(clusterFile);
    }

    @Test
    @RunIsolated(properties = {
            "/coherence/security/kerberos/common-client.properties",
            "/coherence/security/kerberos/secure-client.properties"
    })
    @RunPrivileged(subject = "subjectNULL")
    public void shouldNotAllowCacheAccessWithNullSubject() throws Exception {
        try {
            CacheFactory.getCache("one-Cache");
            fail("Expected to catch SecurityException");
        } catch (SecurityException e) {
            assertThat(e.getMessage(), is("Missing Credentials - Subject not present"));
        } finally {
            CacheFactory.shutdown();
        }
    }

    @Test
    @RunIsolated(properties = {
            "/coherence/security/kerberos/common-client.properties",
            "/coherence/security/kerberos/secure-client.properties"
    })
    @RunPrivileged(subject = "subjectThomas")
    public void shouldNotAllowCacheAccessWithUnauthorisedSubject() throws Exception {
        try {
            CacheFactory.getCache("one-Cache");
        } catch (PortableException e) {
            assertThat(e.getMessage(), is("Not authorised for permission (org.gridman.coherence.security.CachePermission one-Cache ensure)"));
        } finally {
            CacheFactory.shutdown();
        }
    }

    @Test
    @RunIsolated(properties = {
            "/coherence/security/kerberos/common-client.properties",
            "/coherence/security/kerberos/secure-client.properties"
    })
    @RunPrivileged(subject = "subjectKnightj")
    public void shouldAllowCacheAccessWithValidSubject() throws Exception {
        try {
            CacheFactory.getCache("one-Cache");
        } catch (Throwable t) {
            fail("Expected no exception but caught " + t);
        } finally {
            CacheFactory.shutdown();
        }
    }

    @Test
    @RunIsolated(properties = {
            "/coherence/security/kerberos/common-client.properties",
            "/coherence/security/kerberos/secure-client.properties"
    })
    @RunPrivileged(subject = "subjectNULL")
    public void shouldNotAllowInvocationServiceQueryWithNullSubject() throws Exception {
        try {
            InvocationService service = (InvocationService) CacheFactory.getService("ClientInvokeService");
            service.query(new NullInvokable(), null);
            fail("Expected to catch SecurityException");
        } catch (ConnectionException e) {
            assertThat(e.getMessage(), is("Missing Credentials - Subject not present"));
        } finally {
            CacheFactory.shutdown();
        }
    }

    @Test
    @RunIsolated(properties = {
            "/coherence/security/kerberos/common-client.properties",
            "/coherence/security/kerberos/secure-client.properties"
    })
    @RunPrivileged(subject = "subjectThomas")
    public void shouldNotAllowInvocationServiceQueryWithUnauthorisedSubject() throws Exception {
        try {
            InvocationService service = (InvocationService) CacheFactory.getService("ClientInvokeService");
            service.query(new NullInvokable(), null);
            fail("Expected to catch PortableException");
        } catch (PortableException e) {
            assertThat(e.getMessage(), is("Not authorised for permission (org.gridman.coherence.security.InvocablePermission org.gridman.coherence.util.NullInvokable query)"));
        } finally {
            CacheFactory.shutdown();
        }
    }

    @Test
    @RunIsolated(properties = {
            "/coherence/security/kerberos/common-client.properties",
            "/coherence/security/kerberos/secure-client.properties"
    })
    @RunPrivileged(subject = "subjectKnightj")
    public void shouldAllowInvocationServiceQueryWithValidSubject() throws Exception {
        try {
            InvocationService service = (InvocationService) CacheFactory.getService("ClientInvokeService");
            service.query(new NullInvokable(), null);
        } catch (Throwable t) {
            fail("Expected no exception but caught " + t);
        } finally {
            CacheFactory.shutdown();
        }
    }

//    @Test
//    @RunIsolated(properties = {
//            "/coherence/security/kerberos/common-client.properties",
//            "/coherence/security/kerberos/secure-client.properties"
//    })
//    @RunPrivileged(subject = "subjectNULL")
//    public void shouldNotAllowInvocationServiceExecuteWithNullSubject() throws Exception {
//        try {
//            InvocationService service = (InvocationService) CacheFactory.getService("ClientInvokeService");
//            service.execute(new NullInvokable(), null, new InvocationObserverStub());
//            fail("Expected to catch SecurityException");
//        } catch (SecurityException e) {
//            assertThat(e.getMessage(), is("Missing Credentials - Subject not present"));
//        } finally {
//            CacheFactory.shutdown();
//        }
//    }
//
//    @Test
//    @RunIsolated(properties = {
//            "/coherence/security/kerberos/common-client.properties",
//            "/coherence/security/kerberos/secure-client.properties"
//    })
//    @RunPrivileged(subject = "subjectThomas")
//    public void shouldNotAllowInvocationServiceExecuteWithUnauthorisedSubject() throws Exception {
//        try {
//            InvocationService service = (InvocationService) CacheFactory.getService("ClientInvokeService");
//            service.execute(new NullInvokable(), null, new InvocationObserverStub());
//            fail("Expected to catch PortableException");
//        } catch (PortableException e) {
//            assertThat(e.getMessage(), is("Not authorised for permission (org.gridman.coherence.security.InvocablePermission org.gridman.coherence.util.NullInvokable execute)"));
//        } finally {
//            CacheFactory.shutdown();
//        }
//    }
//
//    @Test
//    @RunIsolated(properties = {
//            "/coherence/security/kerberos/common-client.properties",
//            "/coherence/security/kerberos/secure-client.properties"
//    })
//    @RunPrivileged(subject = "subjectKnightj")
//    public void shouldAllowInvocationServiceExecuteWithValidSubject() throws Exception {
//        try {
//            InvocationService service = (InvocationService) CacheFactory.getService("ClientInvokeService");
//            service.execute(new NullInvokable(), null, new InvocationObserverStub());
//        } catch (Throwable t) {
//            fail("Expected no exception but caught " + t);
//        } finally {
//            CacheFactory.shutdown();
//        }
//    }

    
    class InvocationObserverStub implements InvocationObserver {
        @Override
        public void memberCompleted(Member member, Object o) {
        }

        @Override
        public void memberFailed(Member member, Throwable throwable) {
        }

        @Override
        public void memberLeft(Member member) {
        }

        @Override
        public void invocationCompleted() {
        }
    }
}