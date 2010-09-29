package org.gridman.kerberos.junit;

import org.apache.directory.server.core.DefaultDirectoryService;
import org.apache.directory.server.core.DirectoryService;
import org.apache.directory.server.core.integ.IntegrationUtils;
import org.apache.directory.server.core.interceptor.Interceptor;
import org.apache.directory.server.core.kerberos.KeyDerivationInterceptor;
import org.apache.directory.server.kerberos.kdc.KdcServer;
import org.apache.directory.server.ldap.LdapServer;
import org.apache.directory.server.ldap.handlers.bind.MechanismHandler;
import org.apache.directory.server.ldap.handlers.bind.SimpleMechanismHandler;
import org.apache.directory.server.ldap.handlers.bind.cramMD5.CramMd5MechanismHandler;
import org.apache.directory.server.ldap.handlers.bind.digestMD5.DigestMd5MechanismHandler;
import org.apache.directory.server.ldap.handlers.bind.gssapi.GssapiMechanismHandler;
import org.apache.directory.server.ldap.handlers.bind.ntlm.NtlmMechanismHandler;
import org.apache.directory.server.ldap.handlers.extended.StartTlsHandler;
import org.apache.directory.server.ldap.handlers.extended.StoredProcedureExtendedOperationHandler;
import org.apache.directory.server.protocol.shared.transport.TcpTransport;
import org.apache.directory.server.protocol.shared.transport.UdpTransport;
import org.apache.directory.shared.ldap.constants.SupportedSaslMechanisms;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Jonathan Knight
 */
public interface ServerFactory {

    DirectoryService newDirectoryService() throws Exception;

    LdapServer newLdapServer(DirectoryService directoryService) throws Exception;

    KdcServer newKdcServer(DirectoryService directoryService, int port) throws Exception;

    ServerFactory DEFAULT = new ServerFactory() {

        public DirectoryService newDirectoryService() throws Exception {
            DirectoryService service = new DefaultDirectoryService();
            service.setWorkingDirectory(new File("target/server-work"));
            IntegrationUtils.doDelete(service.getWorkingDirectory());
            service.getChangeLog().setEnabled(true);
            service.setShutdownHookEnabled(false);

            List<Interceptor> interceptors = service.getInterceptors();
            interceptors.add(8, new KeyDerivationInterceptor());
            service.setInterceptors(interceptors);

            return service;
        }

        public LdapServer newLdapServer(DirectoryService directoryService) throws Exception {
            LdapServer ldapServer = new LdapServer();
            ldapServer.setDirectoryService(directoryService);
            int port = 1024; //AvailablePortFinder.getNextAvailable( 1024 );
            ldapServer.setTransports(new TcpTransport(port, 3));
            ldapServer.addExtendedOperationHandler(new StartTlsHandler());
            ldapServer.addExtendedOperationHandler(new StoredProcedureExtendedOperationHandler());

            // Setup SASL Mechanisms

            Map<String, MechanismHandler> mechanismHandlerMap = new HashMap<String, MechanismHandler>();
            mechanismHandlerMap.put(SupportedSaslMechanisms.PLAIN, new SimpleMechanismHandler());

            CramMd5MechanismHandler cramMd5MechanismHandler = new CramMd5MechanismHandler();
            mechanismHandlerMap.put(SupportedSaslMechanisms.CRAM_MD5, cramMd5MechanismHandler);

            DigestMd5MechanismHandler digestMd5MechanismHandler = new DigestMd5MechanismHandler();
            mechanismHandlerMap.put(SupportedSaslMechanisms.DIGEST_MD5, digestMd5MechanismHandler);

            GssapiMechanismHandler gssapiMechanismHandler = new GssapiMechanismHandler();
            mechanismHandlerMap.put(SupportedSaslMechanisms.GSSAPI, gssapiMechanismHandler);

            NtlmMechanismHandler ntlmMechanismHandler = new NtlmMechanismHandler();
            mechanismHandlerMap.put(SupportedSaslMechanisms.NTLM, ntlmMechanismHandler);
            mechanismHandlerMap.put(SupportedSaslMechanisms.GSS_SPNEGO, ntlmMechanismHandler);

            ldapServer.setSaslMechanismHandlers(mechanismHandlerMap);

            return ldapServer;
        }

        public KdcServer newKdcServer(DirectoryService directoryService, int port) throws Exception {
            KdcServer kdcServer = new KdcServer();
            kdcServer.setTransports(new TcpTransport(port), new UdpTransport(port));
            kdcServer.setDirectoryService(directoryService);
            return kdcServer;
        }
    };
}
