package org.gridman.security.kerberos;

import com.sun.security.auth.module.Krb5LoginModule;
import org.apache.directory.server.core.integ.Level;
import org.apache.directory.server.core.integ.annotations.ApplyLdifFiles;
import org.apache.directory.server.core.integ.annotations.CleanupLevel;
import org.gridman.coherence.security.demo.DemoServer;
import org.gridman.security.JaasHelper;
import org.gridman.security.kerberos.junit.*;
import org.gridman.security.kerberos.junit.apacheds.ApacheDsServerContext;
import org.junit.runner.RunWith;

import javax.security.auth.Subject;
import java.security.PrivilegedExceptionAction;

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
        "awilson@EXAMPLE.COM,secret",
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
public class DemoKrbCluster {

    //@Test
    public void runServer() throws InterruptedException {
        Subject subject = JaasHelper.logon("Coherence", "cacheserver@EXAMPLE.COM", "secret");
        JaasHelper.doAs(subject, new PrivilegedExceptionAction<Object>(){
            @Override
            public Object run() throws Exception {
                DemoServer.main(new String[0]);
                return null;
            }
        });
    }
}
